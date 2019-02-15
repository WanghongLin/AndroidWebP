
# libwebp module
WEBP_LOCAL_PATH := $(call my-dir)

LOCAL_PATH := $(WEBP_LOCAL_PATH)
include $(LOCAL_PATH)/../libwebp/Android.mk

# webp native module
LOCAL_PATH := $(WEBP_LOCAL_PATH)
include $(CLEAR_VARS)

LOCAL_MODULE := webpnative
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../libwebp/src

LOCAL_SRC_FILES := src/main/cpp/webpnative.cpp

LOCAL_LDLIBS := -llog

LOCAL_STATIC_LIBRARIES := webpdemux webpmux

include $(BUILD_SHARED_LIBRARY)

