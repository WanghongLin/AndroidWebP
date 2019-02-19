//
// Created by Wanghong Lin on 2019/2/18.
//

#ifndef ANDROIDWEBP_WEBPINTERNAL_H
#define ANDROIDWEBP_WEBPINTERNAL_H

#include <jni.h>
#include <android/log.h>
#include <cstdint>
#include <map>
#include "webp/mux.h"
#include "webp/demux.h"

class WebPInternal {

public:

    WebPInternal(uint8_t *buffer, size_t size);

    virtual ~WebPInternal();

    jboolean hasNextFrame();
    void nextFrame(void* outBuffer, size_t outSize);
    void fillWebPInfo(JNIEnv* env, jobject webpInfo);

    static WebPInternal* get(jint id);
    static void put(jint id, WebPInternal* internal);
private:
    uint8_t* buffer_;
    size_t size_;

    WebPData* webPData;
    WebPDemuxer* demuxer;

    WebPIterator* iterator;
    int frameNumber;

    uint32_t canvasWidth;
    uint32_t canvasHeight;
    uint32_t flags;
    uint32_t frameCount;
    uint32_t loopCount;
    uint32_t backgroundColor;

    int width;
    int height;
    int timeStamp;

    static const char* TAG;
    static std::map<int, WebPInternal*> internalMaps_;
};


#endif //ANDROIDWEBP_WEBPINTERNAL_H
