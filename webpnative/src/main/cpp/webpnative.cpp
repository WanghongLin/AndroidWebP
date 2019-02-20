
#include "webp/mux.h"
#include "webp/demux.h"
#include "webpnative.h"
#include "WebPInternal.h"
#include <android/log.h>

const char* TAG = "WebPNative";

static void WebPCheckVersion_()
{
    int mux = WebPGetMuxVersion();
    int demux = WebPGetDemuxVersion();
    int decoder = WebPGetDecoderVersion();

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "mux %d, demux %d, decoder %d\n", mux, demux, decoder);
}

static jint GetJavaObjectId(JNIEnv* env, jobject obj)
{
    jmethodID mid = env->GetMethodID(env->GetObjectClass(obj), "hashCode", "()I");
    return env->CallIntMethod(obj, mid);
}

void Java_com_wanghong_webpnative_WebPNative_checkWebPVersion(JNIEnv * env, jobject obj) {
    WebPCheckVersion_();
}

void Java_com_wanghong_webpnative_WebPNative_initialize(JNIEnv * env, jobject obj, jbyteArray bytes_, jobject webpInfo) {
    size_t len = static_cast<size_t>(env->GetArrayLength(bytes_));
    uint8_t* buf = new uint8_t[len];
    env->GetByteArrayRegion(bytes_, 0, len, reinterpret_cast<jbyte *>(buf));
    WebPInternal* webPInternal = new WebPInternal(buf, len);
    webPInternal->fillWebPInfo(env, webpInfo);
    WebPInternal::put(GetJavaObjectId(env, obj), webPInternal);
}

jboolean Java_com_wanghong_webpnative_WebPNative_hasNextFrame(JNIEnv * env, jobject obj) {
    WebPInternal* internal = WebPInternal::get(GetJavaObjectId(env, obj));
    if (internal) {
        return internal->hasNextFrame();
    }
    return 0;
}

void Java_com_wanghong_webpnative_WebPNative_nextFrame(JNIEnv * env, jobject obj, jobject buf, jobject webpInfo) {
    WebPInternal* internal = WebPInternal::get(GetJavaObjectId(env, obj));
    if (internal) {
        void *buffer = env->GetDirectBufferAddress(buf);
        jlong capacity = env->GetDirectBufferCapacity(buf);
        internal->nextFrame(buffer, static_cast<size_t>(capacity));
        internal->fillWebPInfo(env, webpInfo);
    }
}

void Java_com_wanghong_webpnative_WebPNative_release(JNIEnv * env, jobject obj) {
    WebPInternal* internal = WebPInternal::get(GetJavaObjectId(env, obj));
    if (internal) {
        delete internal;
    }
}
