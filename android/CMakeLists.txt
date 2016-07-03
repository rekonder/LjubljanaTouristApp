cmake_minimum_required(VERSION 3.1)
PROJECT(NativeCamera)

IF(NOT ANDROID_NDK)
  MESSAGE(FATAL_ERROR "ANDROID_NDK variable not set")
ENDIF()

IF(NOT ANDROID_ABI)
  MESSAGE(FATAL_ERROR "ANDROID_ABI variable not set")
ENDIF()

IF(NOT ANDROID_OPENCV)
  MESSAGE(FATAL_ERROR "ANDROID_OPENCV variable not set")
ENDIF()

SET(CMAKE_MODULE_PATH ${CMAKE_CURRENT_SOURCE_DIR}/../cmake) # Adding path to CMake scripts
INCLUDE(Android REQUIRED)

# Setting paths to libraries and header files
LINK_DIRECTORIES(
  ${ANDROID_NDK}/platforms/android-${ANDROID_NATIVE_API_LEVEL}/arch-${ANDROID_ARCH_NAME}/usr/lib
  ${ANDROID_OPENCV}/sdk/native/3rdparty/libs/${ANDROID_ABI}
  ${ANDROID_OPENCV}/sdk/native/libs/${ANDROID_ABI}
)
INCLUDE_DIRECTORIES(AFTER 
  ${CMAKE_CURRENT_BINARY_DIR}
  ${ANDROID_NDK}/platforms/android-${ANDROID_NATIVE_API_LEVEL}/arch-${ANDROID_ARCH_NAME}/usr/include
  ${ANDROID_OPENCV}/sdk/native/jni/include
)

SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++0x -Wall")
 
ADD_LIBRARY(NativeCamera SHARED native/native.cpp native/conversions.cpp native/operations.cpp)

TARGET_LINK_LIBRARIES(NativeCamera opencv_calib3d opencv_features2d opencv_flann opencv_objdetect opencv_videoio opencv_imgcodecs opencv_imgproc opencv_core) # Add other OpenCV modules if needed
TARGET_LINK_LIBRARIES(NativeCamera IlmImf libjasper libjpeg libpng libtiff libwebp tbb z log m log android jnigraphics)

ANDROID_CREATE_APK(NativeCamera "si.rekonder" "${CMAKE_BINARY_DIR}/apk")

