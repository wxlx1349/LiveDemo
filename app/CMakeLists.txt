
cmake_minimum_required(VERSION 3.4.1)

#变量
set(distribution_DIR ../../../../libs)

add_library( # Sets the name of the library.
             native-lib
             SHARED
             src/main/cpp/queue.c
             src/main/cpp/native-lib.cpp )

add_library( x264
             STATIC
             IMPORTED)
set_target_properties( x264
                       PROPERTIES IMPORTED_LOCATION
                       ${distribution_DIR}/x264/libx264.a)

add_library( rtmp
             STATIC
             IMPORTED)
set_target_properties( rtmp
                       PROPERTIES IMPORTED_LOCATION
                       ${distribution_DIR}/rtmpdump/librtmp.a)

add_library( faac
             STATIC
             IMPORTED)
set_target_properties( faac
                       PROPERTIES IMPORTED_LOCATION
                       ${distribution_DIR}/faac/libfaac.a)

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

find_library( android-lib
              android)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

include_directories(src/main/cpp/include)
include_directories(libs/x264/include)
include_directories(libs/faac/include)
include_directories(libs/rtmpdump/include)


target_link_libraries( # Specifies the target library.
                       native-lib
                       faac
                       x264
                       rtmp
                       ${android-lib}
                       ${log-lib} )