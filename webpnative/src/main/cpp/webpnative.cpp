
#include "webp/mux.h"
#include "webp/demux.h"
#include "webpnative.h"
#include <android/log.h>

const char* TAG = "WebPNative";

static void WebPCheckVersion_()
{
    int mux = WebPGetMuxVersion();
    int demux = WebPGetDemuxVersion();
    int decoder = WebPGetDecoderVersion();

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "mux %d, demux %d, decoder %d\n", mux, demux, decoder);
}

void Java_com_wanghong_webpnative_WebPNative_checkWebPVersion(JNIEnv * env, jobject obj) {
    WebPCheckVersion_();
}

void Java_com_wanghong_webpnative_WebPNative_initialize(JNIEnv *, jobject, jbyteArray) {

}

jboolean Java_com_wanghong_webpnative_WebPNative_hasNextFrame(JNIEnv *, jobject) {
    return 0;
}

void Java_com_wanghong_webpnative_WebPNative_nextFrame(JNIEnv *, jobject, jobject) {

}
