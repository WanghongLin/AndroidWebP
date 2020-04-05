/*
 * Copyright 2019 wanghonglin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wanghong.webpnative

import android.graphics.Bitmap
import java.nio.ByteBuffer

/**
 * WebP Java native interface bridge
 *
 * Created by wanghonglin on 2019/2/15 10:22 AM.
 */
class WebPNative {

    init {
        System.loadLibrary("webpnative")
    }

    /**
     * Check webp native library version
     */
    external fun checkWebPVersion()

    /**
     * Initialize webp library with byte data, the first step to play a animated webp
     * @param byteArray file byte data
     * @param webPInfo output webp information
     * @return [Unit]
     */
    external fun initialize(byteArray: ByteArray, webPInfo: WebPInfo)

    /**
     * Check if the current animated webp has next frame to play
     * @return true if has next frame, otherwise false
     */
    external fun hasNextFrame(): Boolean

    /**
     * obtain next frame bitmap data to [byteBuffer]
     * @param byteBuffer a byte buffer to hold the result data
     * @param webPInfo webp information of current frame
     */
    external fun nextFrame(byteBuffer: ByteBuffer, webPInfo: WebPInfo)

    /**
     * Release libwebp decoder for animated webp
     */
    external fun release()

    /**
     * encode a bitmap to webp, it's another version of the system api [Bitmap.compress] with
     * [android.graphics.Bitmap.CompressFormat.WEBP] format parameter
     * @param bitmap the source bitmap
     * @param outPath output string path
     * @param qualityFactor compress quality
     */
    external fun encodeRGBA(bitmap: Bitmap, outPath: String, qualityFactor: Float = 90.0f)
}
