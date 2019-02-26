//
// Created by Wanghong Lin on 2019/2/18.
//

#include "WebPInternal.h"

const char* WebPInternal::TAG = "WebPInternal";

std::map<int, WebPInternal*> WebPInternal::internalMaps_;

WebPInternal::WebPInternal(uint8_t *buffer, size_t size) : buffer_(buffer), size_(size) {
    webPData = new WebPData;
    WebPDataInit(webPData);

    webPData->size = size;
    webPData->bytes = buffer;

    demuxer = WebPDemux(webPData);
    if (demuxer != nullptr) {
        canvasWidth = WebPDemuxGetI(demuxer, WEBP_FF_CANVAS_WIDTH);
        canvasHeight = WebPDemuxGetI(demuxer, WEBP_FF_CANVAS_HEIGHT);
        flags = WebPDemuxGetI(demuxer, WEBP_FF_FORMAT_FLAGS);
        frameCount = WebPDemuxGetI(demuxer, WEBP_FF_FRAME_COUNT);
        loopCount = WebPDemuxGetI(demuxer, WEBP_FF_LOOP_COUNT);
        backgroundColor = WebPDemuxGetI(demuxer, WEBP_FF_BACKGROUND_COLOR);

        if (hasFlag(ANIMATION_FLAG)) {
            Logger::debug().tag(TAG) << "animation webp\n";
            animDecoder = WebPAnimDecoderNew(webPData, nullptr);
            WebPAnimDecoderGetInfo(animDecoder, &animInfo);
        } else {
            Logger::debug().tag(TAG) << "normal webp width*height " << canvasWidth << "*" << canvasHeight;
        }

        // prepare for reading first frame
        frameNumber = 1;
        iterator = new WebPIterator;
        currentFrameCount = 0;
        currentLoopCount = 0;
    }
}

WebPInternal::~WebPInternal() {
    WebPDemuxReleaseIterator(iterator);
    WebPDemuxDelete(demuxer);
    WebPDataClear(webPData);
    if (animDecoder) {
        WebPAnimDecoderDelete(animDecoder);
    }
}

WebPInternal* WebPInternal::get(jint id) {
    return internalMaps_[id];
}

void WebPInternal::put(jint id, WebPInternal *internal) {
    internalMaps_.insert(std::make_pair(id, internal));
}

jboolean WebPInternal::hasNextFrame() {
    if (hasFlag(ANIMATION_FLAG)) {
        if (currentFrameCount < animInfo.frame_count) {
            return JNI_TRUE;
        } else {
            return JNI_FALSE;
        }
        // return static_cast<jboolean>(WebPAnimDecoderHasMoreFrames(animDecoder));
    }
    if (frameNumber == 1 || WebPDemuxNextFrame(iterator) != 0) {
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

void WebPInternal::nextFrame(void *outBuffer, size_t outSize) {
    if (hasFlag(ANIMATION_FLAG)) {
        uint8_t* buf;
        WebPAnimDecoderGetNext(animDecoder, &buf, &timeStamp);
        width = animInfo.canvas_width;
        height = animInfo.canvas_height;
        std::memcpy(outBuffer, buf, outSize);

        currentFrameCount++;
        if (currentFrameCount == animInfo.frame_count) {
            if (animInfo.loop_count == 0 || ++currentLoopCount < animInfo.loop_count) {
                // start from beginning
                currentFrameCount = 0;
                WebPAnimDecoderReset(animDecoder);
            } else {
                Logger::warn().tag(TAG) << "animation stop loop " << animInfo.loop_count
                                        << " frame count " << animInfo.frame_count;
            }
        }
        return;
    }

    if (demuxer && iterator) {
        WebPDemuxGetFrame(demuxer, frameNumber++, iterator);
        if (outBuffer && outSize > 0) {
            uint8_t* result = WebPDecodeRGBA(iterator->fragment.bytes, iterator->fragment.size, &width, &height);
            std::memcpy(outBuffer, result, static_cast<size_t>(width * height * 4));
            WebPFree(result);
        }
    }
}

void WebPInternal::fillWebPInfo(JNIEnv *env, jobject webpInfo) {
    jclass clz = env->GetObjectClass(webpInfo);
    env->SetIntField(webpInfo, env->GetFieldID(clz, "canvasWidth", "I"), canvasWidth);
    env->SetIntField(webpInfo, env->GetFieldID(clz, "canvasHeight", "I"), canvasHeight);
    env->SetIntField(webpInfo, env->GetFieldID(clz, "width", "I"), width);
    env->SetIntField(webpInfo, env->GetFieldID(clz, "height", "I"), height);
    env->SetIntField(webpInfo, env->GetFieldID(clz, "backgroundColor", "I"), backgroundColor);
    env->SetIntField(webpInfo, env->GetFieldID(clz, "loopCount", "I"), loopCount);
    env->SetIntField(webpInfo, env->GetFieldID(clz, "frameCount", "I"), frameCount);
    env->SetIntField(webpInfo, env->GetFieldID(clz, "timeStamp", "I"), timeStamp);
}

bool WebPInternal::hasFlag(int flag) {
    return (flags & flag) == flag;
}

void WebPInternal::remove(jint id) {
    internalMaps_.erase(id);
}

