
#include "webp/mux.h"
#include "webp/demux.h"
#include "webpnative.h"
#include "WebPInternal.h"
#include <android/log.h>
#include <fstream>
#include "Logger.h"

const char* TAG = "WebPNative";

static void WebPCheckVersion_()
{
    int mux = WebPGetMuxVersion();
    int demux = WebPGetDemuxVersion();
    int decoder = WebPGetDecoderVersion();

    Logger::debug().tag(TAG) << "mux " << mux << ", demux " << demux << ", decoder " << decoder;
}

static jint GetJavaObjectId(JNIEnv* env, jobject obj)
{
    jmethodID mid = env->GetMethodID(env->GetObjectClass(obj), "hashCode", "()I");
    return env->CallIntMethod(obj, mid);
}

extern "C" void Java_com_wanghong_webpnative_WebPNative_checkWebPVersion(JNIEnv * env, jobject obj) {
    WebPCheckVersion_();
}

extern "C" void Java_com_wanghong_webpnative_WebPNative_initialize(JNIEnv * env, jobject obj, jbyteArray bytes_, jobject webpInfo) {
    size_t len = static_cast<size_t>(env->GetArrayLength(bytes_));
    uint8_t* buf = new uint8_t[len];
    env->GetByteArrayRegion(bytes_, 0, len, reinterpret_cast<jbyte *>(buf));
    WebPInternal* webPInternal = new WebPInternal(buf, len);
    webPInternal->fillWebPInfo(env, webpInfo);
    WebPInternal::put(GetJavaObjectId(env, obj), webPInternal);
}

extern "C" jboolean Java_com_wanghong_webpnative_WebPNative_hasNextFrame(JNIEnv * env, jobject obj) {
    WebPInternal* internal = WebPInternal::get(GetJavaObjectId(env, obj));
    if (internal) {
        return internal->hasNextFrame();
    }
    return 0;
}

extern "C" void Java_com_wanghong_webpnative_WebPNative_nextFrame(JNIEnv * env, jobject obj, jobject buf, jobject webpInfo) {
    WebPInternal* internal = WebPInternal::get(GetJavaObjectId(env, obj));
    if (internal) {
        void *buffer = env->GetDirectBufferAddress(buf);
        jlong capacity = env->GetDirectBufferCapacity(buf);
        internal->nextFrame(buffer, static_cast<size_t>(capacity));
        internal->fillWebPInfo(env, webpInfo);
    }
}

extern "C" void Java_com_wanghong_webpnative_WebPNative_release(JNIEnv * env, jobject obj) {
    const jint id = GetJavaObjectId(env, obj);
    WebPInternal* internal = WebPInternal::get(id);
    if (internal) {
        WebPInternal::remove(id);
        delete internal;
    }
}

extern "C" void Java_com_wanghong_webpnative_WebPNative_encodeRGBA(JNIEnv * env, jobject obj,
                                                                   jobject bitmap, jstring output, jfloat quality) {
    jboolean copy = 0;
    const char* path = env->GetStringUTFChars(output, &copy);

    AndroidBitmapInfo bitmapInfo;
    AndroidBitmap_getInfo(env, bitmap, &bitmapInfo);

    void* buffer = nullptr;
    AndroidBitmap_lockPixels(env, bitmap, &buffer);

    uint8_t* out_buffer = nullptr;
    if (bitmapInfo.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
        size_t bytes = 0;
        if ((bytes = WebPEncodeRGBA(static_cast<uint8_t *>(buffer), bitmapInfo.width, bitmapInfo.height,
                                    bitmapInfo.stride, quality, &out_buffer)) > 0) {
            std::ofstream of(path);
            if (of.is_open()) {
                of.write(reinterpret_cast<const char *>(out_buffer), bytes);
            }
            Logger::debug().tag(TAG) << "write file " << path << bitmapInfo.width << 'x' << bitmapInfo.height << '\n';
            of.flush();
            of.close();
        } else {
            Logger::error().tag(TAG) << "encode error\n";
        }
    } else {
        Logger::warn().tag(TAG) << "the given bitmap format not supported\n";
    }

    AndroidBitmap_unlockPixels(env, bitmap);
    env->ReleaseStringUTFChars(output, path);
}
