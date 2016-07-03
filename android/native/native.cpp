#include "opencv2/imgproc.hpp"

#include <android/bitmap.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#include "conversions.h"
#include "operations.h"

static const double DEG_TO_RAD = 0.017453292519943295769236907684886;

static const double EARTH_RADIUS = 6367;

static Mat labelsSB, labelsBu, labelsSc, labelsNeg; // labels of learned models

static Mat featureMeanSB, featureMeanBu, featureMeanSc, featureMeanNeg; // means of learned models

static Mat eigenvectorsSB, eigenvectorsBu, eigenvectorsSc, eigenvectorsNeg; // eigenvectors of learned models

static Mat featuresSB, featuresBu, featuresSc, featuresNeg; // features of learned models

struct picturePoint point;

vector<picturePoint> buildings;

vector<picturePoint> buildingsAvailable;

extern "C" {

    /*
        Function which initialize different matrix which we will use it for different classifications
    */
    JNIEXPORT void JNICALL Java_si_rekonder_android_touristApp_CameraActivity_initializeNative(JNIEnv* jenv, jobject, jobject assetManager) {
        buildings.clear();
        AAssetManager* mgr = AAssetManager_fromJava(jenv, assetManager);

        AAsset* asset = AAssetManager_open(mgr, "posNegOrb.yaml", AASSET_MODE_UNKNOWN);
        if (asset) {
            long sizeNeg = AAsset_getLength(asset);
            char* bufferNeg = new char[sizeNeg];
            AAsset_read(asset, bufferNeg, sizeNeg);

            FileStorage fsNeg(string(bufferNeg, sizeNeg), FileStorage::READ | FileStorage::MEMORY);
            fsNeg["mean"] >> featureMeanNeg;
            fsNeg["eigenvectors"] >> eigenvectorsNeg;
            fsNeg["features"] >> featuresNeg;
            fsNeg["labels"] >> labelsNeg;
            AAsset_close(asset);
            delete[] bufferNeg;
        }

        asset = AAssetManager_open(mgr, "kipBuildOrb.yaml", AASSET_MODE_UNKNOWN);
        if (asset) {
            long sizeSB = AAsset_getLength(asset);
            char* bufferSB = new char[sizeSB];
            AAsset_read(asset, bufferSB, sizeSB);

            FileStorage fsSB(string(bufferSB, sizeSB), FileStorage::READ | FileStorage::MEMORY);
            fsSB["mean"] >> featureMeanSB;
            fsSB["eigenvectors"] >> eigenvectorsSB;
            fsSB["features"] >> featuresSB;
            fsSB["labels"] >> labelsSB;
            AAsset_close(asset);
            delete[] bufferSB;
        }
        asset = AAssetManager_open(mgr, "buildOrb.yaml", AASSET_MODE_UNKNOWN);
        if (asset) {
            long sizeBu = AAsset_getLength(asset);
            char* bufferBu = new char[sizeBu];
            AAsset_read(asset, bufferBu, sizeBu);

            FileStorage fsBu(string(bufferBu, sizeBu), FileStorage::READ | FileStorage::MEMORY);
            fsBu["mean"] >> featureMeanBu;
            fsBu["eigenvectors"] >> eigenvectorsBu;
            fsBu["features"] >> featuresBu;
            fsBu["labels"] >> labelsBu;
            AAsset_close(asset);
            delete[] bufferBu;
        }
        asset = AAssetManager_open(mgr, "kipOrb.yaml", AASSET_MODE_UNKNOWN);
        if (asset) {
            long sizeSc = AAsset_getLength(asset);
            char* bufferSc = new char[sizeSc];
            AAsset_read(asset, bufferSc, sizeSc);

            FileStorage fsSc(string(bufferSc, sizeSc), FileStorage::READ | FileStorage::MEMORY);
            fsSc["mean"] >> featureMeanSc;
            fsSc["eigenvectors"] >> eigenvectorsSc;
            fsSc["features"] >> featuresSc;
            fsSc["labels"] >> labelsSc;
            AAsset_close(asset);
            delete[] bufferSc;
        }

    }

    /*
        Function which add positions of our learned buildings into our vector buildings.
    */
    JNIEXPORT void JNICALL Java_si_rekonder_android_touristApp_CameraActivity_processImage(JNIEnv* jenv, jobject, jbyteArray jbuffer, jint jbuffer_width, jint jbuffer_height, jdouble lat, jdouble lon) {
        if (!convertBufferToMat(jenv, point.image, jbuffer, jbuffer_width, jbuffer_height, JAVA_BUFFER_TYPE_JPEG, IMREAD_GRAYSCALE))
            return;
        point.lon = lon;
        point.lat = lat;
        point.pos = buildings.size()+1;
        buildings.push_back(point);
    }

    /*
        Function which calculate haversine distance in km between predefined points and our gps location
        at moment. If distance is lower than wanted, add predefined building into vector of potential
        buildings for recognizing
    */
    JNIEXPORT void JNICALL Java_si_rekonder_android_touristApp_CameraActivity_fillVector(JNIEnv* jenv, jobject, jdouble lat, jdouble lon) {
        buildingsAvailable.clear();
        int count = 1;
        for (std::vector<picturePoint>::iterator it = buildings.begin() ; it != buildings.end(); ++it) {
            double latitudeArc  = ((*it).lat - lat) * DEG_TO_RAD;
            double longitudeArc = ((*it).lon - lon) * DEG_TO_RAD;

            double latitudeH = pow(sin(latitudeArc * 0.5), 2.0);
            double lontitudeH = pow(sin(longitudeArc * 0.5), 2.0);
            double tmp = cos((*it).lat*DEG_TO_RAD) * cos(lat*DEG_TO_RAD);
            tmp = latitudeH + tmp* lontitudeH;
            tmp = 2 * atan2(sqrt(tmp), sqrt(1-tmp));
            double distance = EARTH_RADIUS * tmp;

            if(distance <= 0.1 || (count == 4 && distance <= 1))
                buildingsAvailable.push_back(*it);
            count +=1;
        }

    }

    /*
        Function who accept new image, and return id of recognized building or -1.
        Function first check new image if is not false positive. If is not then check type of our building.
        And on end it classifiy image as specific build.
    */
    JNIEXPORT jint JNICALL Java_si_rekonder_android_touristApp_CameraActivity_recognizeObject(JNIEnv* jenv, jobject, jbyteArray jbuffer, jint jbuffer_width, jint jbuffer_height, jint jbuffer_type, jint result) {
        int min_pos = -1;
        Mat image;
        if(!buildingsAvailable.empty()) {
            if (!convertBufferToMat(jenv, image, jbuffer, jbuffer_width, jbuffer_height, jbuffer_type, IMREAD_GRAYSCALE))
                return -1;
            if (image.empty()) {
                return 0;
            }
            resize(image, image, Size(10000, 1));
            if(predictBasic(image, labelsNeg, featureMeanNeg, eigenvectorsNeg, featuresNeg) == 2) {
                int predictBuildingType = predictBasic(image, labelsSB, featureMeanSB, eigenvectorsSB, featuresSB);
                if( predictBuildingType == 3) {
                    min_pos = 2;
                } else if( predictBuildingType == 1) {
                    min_pos = predictBuild(image, labelsBu, featureMeanBu, eigenvectorsBu, featuresBu, 1, buildingsAvailable);
                } else if(predictBuildingType == 2) {
                    min_pos = predictBuild(image, labelsSc, featureMeanSc, eigenvectorsSc, featuresSc, 2, buildingsAvailable);
                }
            }
        }
        return min_pos;

    }

}
