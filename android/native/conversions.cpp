
#include <android/bitmap.h>
#include <opencv2/imgproc.hpp>

#include "conversions.h"

using namespace std;
using namespace cv;

bool convertBufferToMat(JNIEnv *jenv, Mat& image, jbyteArray jbuffer, jint jbuffer_width, jint jbuffer_height, jint jbuffer_type, int flags) {

    switch ( jbuffer_type) {
    case JAVA_BUFFER_TYPE_3BYTE_BGR: {
        void* a = jenv->GetPrimitiveArrayCritical(jbuffer,0);
        Mat bgr(jbuffer_height, jbuffer_width, CV_8UC3, (uchar*)a);
        if (flags != 0)
            bgr.copyTo(image);
        else
            cvtColor(bgr, image, COLOR_BGR2GRAY);
        jenv->ReleasePrimitiveArrayCritical(jbuffer, a, 0);
        return true;
    }
    case JAVA_BUFFER_TYPE_INT_ARGB: {
        void* a = jenv->GetPrimitiveArrayCritical(jbuffer,0);
        Mat rgba(jbuffer_height, jbuffer_width, CV_8UC4, (uchar*)a);
        if (flags != 0)
            cvtColor(rgba, image, COLOR_RGBA2BGR);
        else
            cvtColor(rgba, image, COLOR_RGBA2GRAY);
        jenv->ReleasePrimitiveArrayCritical(jbuffer, a, 0);
        return true;
    }
    case JAVA_BUFFER_TYPE_BYTE_GRAY: {
        void* a = jenv->GetPrimitiveArrayCritical(jbuffer,0);
        Mat gray(jbuffer_height, jbuffer_width, CV_8UC1, (uchar*)a);
        if (flags != 0)
            cvtColor(gray, image, COLOR_GRAY2BGR);
        else
            gray.copyTo(image);
        jenv->ReleasePrimitiveArrayCritical(jbuffer, a, 0);
        return true;
    }
    case JAVA_BUFFER_TYPE_NV21: {
        void* a = jenv->GetPrimitiveArrayCritical(jbuffer,0);
        Mat yuv(jbuffer_height + jbuffer_height/2, jbuffer_width, CV_8UC1, (uchar*)a);
        int cvt = (flags == 0) ? COLOR_YUV2GRAY_NV21 : COLOR_YUV2BGR_NV21;
        cvtColor(yuv, image, cvt);
        jenv->ReleasePrimitiveArrayCritical(jbuffer, a, 0);
        return true;
    }
    case JAVA_BUFFER_TYPE_JPEG: {
        jsize l = jenv->GetArrayLength(jbuffer);
        uchar* a = (uchar*) jenv->GetPrimitiveArrayCritical(jbuffer, 0);
        Mat buffer(1, l, CV_8UC1, a);
        image = imdecode(buffer, flags, &image);
        jenv->ReleasePrimitiveArrayCritical(jbuffer, a, 0);
        return true;
    }
    }

    jclass clazz = jenv->FindClass("java/lang/Exception");
    jenv->ThrowNew(clazz, "Unable to convert image");
    return false;

}
