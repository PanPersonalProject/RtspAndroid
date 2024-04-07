# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("stream_pusher")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
#
# In this top level CMakeLists.txt, ${CMAKE_PROJECT_NAME} is used to define
# the target library name; in the sub-module's CMakeLists.txt, ${PROJECT_NAME}
# is preferred for the same purpose.
#
# In order to load a library into your app from Java/Kotlin, you must call
# System.loadLibrary() and pass the name of the library defined here;
# for GameActivity/NativeActivity derived applications, the same library name must be
# used in the AndroidManifest.xml file.

#定义了NO_OPENSSL这个宏。TLSState.hh不会包含任何和OpenSSL相关的成员函数和成员变量
add_definitions(-DNO_OPENSSL)

include_directories(
        ${CMAKE_CURRENT_SOURCE_DIR}/live555/include/BasicUsageEnvironment
        ${CMAKE_CURRENT_SOURCE_DIR}/live555/include/groupsock
        ${CMAKE_CURRENT_SOURCE_DIR}/live555/include/liveMedia
        ${CMAKE_CURRENT_SOURCE_DIR}/live555/include/UsageEnvironment
        ${CMAKE_CURRENT_SOURCE_DIR}/live555/src/include
        ${CMAKE_CURRENT_SOURCE_DIR}/Base/include
)

add_library(
        BasicUsageEnvironment
        STATIC
        IMPORTED
)
set_target_properties(
        BasicUsageEnvironment
        PROPERTIES
        IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/live555/lib/${ANDROID_ABI}/libBasicUsageEnvironment.a
)

add_library(
        groupsock
        STATIC
        IMPORTED
)
set_target_properties(
        groupsock
        PROPERTIES
        IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/live555/lib/${ANDROID_ABI}/libgroupsock.a
)

add_library(
        liveMedia
        STATIC
        IMPORTED
)
set_target_properties(
        liveMedia
        PROPERTIES
        IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/live555/lib/${ANDROID_ABI}/libliveMedia.a
)

add_library(
        UsageEnvironment
        STATIC
        IMPORTED
)
set_target_properties(
        UsageEnvironment
        PROPERTIES
        IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/live555/lib/${ANDROID_ABI}/libUsageEnvironment.a
)

add_library(
        ${CMAKE_PROJECT_NAME}
        SHARED
        stream_pusher.cpp
        Base/src/AndroidLog.cpp
        live555/src/testH264VideoStreamer.cpp
        live555/src/announceURL.cpp
        live555/src/WW_H264VideoServerMediaSubsession.cpp
        live555/src/WW_H264VideoSource.cpp
        live555/src/RTSPStream.cpp
        live555/src/live555MediaServer.cpp
        live555/src/CameraSource.cpp
)

target_link_libraries(
        ${CMAKE_PROJECT_NAME}
        android
        log
        BasicUsageEnvironment
        groupsock
        liveMedia
        UsageEnvironment
)