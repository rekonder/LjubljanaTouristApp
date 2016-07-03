# LjubljanaTouristApp

###Description
Tourist walk around Ljubljana city with Andorid mobile phone and installed application. If turn to specific build application try to recognize it. If app recognize building, has user chance to click on button. When button is cliked user get details about recognized building.

### Instalation (Ubuntu 14.04 and android phone)
1.) Install Java JDK, ant builder, Android NDK and SDK

2.) Install in Android Manager API 14

3.) Download OpenCV Android SDK for OpenCV 3.1.

4.) Clone repository https://github.com/rekonder/LjubljanaTouristApp

5.) In directory android create folder build

6.) In directory build write command cmake -DANDROID_NDK="path to NDK" -DANDROID_ABI="armeabi-v7a" -DANDROID_SDK="path to SDK" -DANDROID_OPENCV="path to OpenCV" -DCMAKE_TOOLCHAIN_FILE="../../cmake/AndroidToolchain.cmake" ..

7.) run command make

8.) run command adb install -r {path to project folder}/LjubljanaTouristApp/android/build/apk/bin/NativeCamera-debug.apk

###Buildings
1.) University of Ljuljana

2.) Kongresni trg square

3.) Vodnik monument

4.) Ljubljana Castle

5.) Dragon bridge

6.) National and University lirary

7.) Prešernov monument

8.) Franciscan church

9.) Town Hall

10.) Robba fountaion


###Video example 
https://www.youtube.com/watch?v=t4M5Ej4t6n0


