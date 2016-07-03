#ifndef _ANDROID_CONVERSIONS_H
#define _ANDROID_CONVERSIONS_H

#include <jni.h>
#include <opencv2/core.hpp>
#include <opencv2/imgcodecs.hpp>

#define JAVA_BUFFER_TYPE_CUSTOM 0
#define JAVA_BUFFER_TYPE_INT_RGB 1
#define JAVA_BUFFER_TYPE_INT_ARGB 2
#define JAVA_BUFFER_TYPE_INT_ARGB_PRE 3
#define JAVA_BUFFER_TYPE_INT_BGR 4
#define JAVA_BUFFER_TYPE_3BYTE_BGR 5
#define JAVA_BUFFER_TYPE_4BYTE_ABGR 6
#define JAVA_BUFFER_TYPE_4BYTE_ABGR_PRE 7
#define JAVA_BUFFER_TYPE_USHORT_565_RGB 8
#define JAVA_BUFFER_TYPE_USHORT_555_RGB 9
#define JAVA_BUFFER_TYPE_BYTE_GRAY 10
#define JAVA_BUFFER_TYPE_USHORT_GRAY 11
#define JAVA_BUFFER_TYPE_BYTE_BINARY 12
#define JAVA_BUFFER_TYPE_JPEG 256
#define JAVA_BUFFER_TYPE_NV21 17
#define JAVA_BUFFER_TYPE_RGB656 1004
#define JAVA_BUFFER_TYPE_YUY2 1020

bool convertBufferToMat(JNIEnv *jenv, cv::Mat& image, jbyteArray jbuffer, jint jbuffer_width, jint jbuffer_height, jint jbuffer_type, int flags = cv::IMREAD_UNCHANGED);

#endif
