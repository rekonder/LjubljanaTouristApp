#ifndef _ANDROID_OPERATIONS_H
#define _ANDROID_OPERATIONS_H

#include <jni.h>
#include <vector>
#include <algorithm>

#include <opencv2/core.hpp>
#include <opencv2/imgcodecs.hpp>


using namespace std;
using namespace cv;

struct picturePoint {
    Mat image;
    double lat;
    double lon;
    int pos;
};

struct distancePoint {
    double dist;
    int pos;
};


bool cmd(const distancePoint & p1, const distancePoint & p2);
int idConversion(int position, int type);
Mat initializePrediction(Mat &featureMean, Mat &eigenvectors, Mat &feature);
int endPrediction();
int predictBasic(Mat &feature, Mat &labels, Mat &featureMean, Mat &eigenvectors, Mat &features);
int predictBuild(Mat &feature, Mat &labels, Mat &featureMean, Mat &eigenvectors, Mat &features, int type, vector<picturePoint> buildingsAvailable);

#endif
