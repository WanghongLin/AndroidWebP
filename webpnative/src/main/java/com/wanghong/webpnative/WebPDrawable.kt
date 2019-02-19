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

import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.HandlerThread
import java.io.FileInputStream
import java.nio.ByteBuffer

/**
 * Created by wanghonglin on 2019/2/18 11:37 AM.
 */
class WebPDrawable(path: String) : Drawable() {

    private val webPNative = WebPNative()
    private val webPInfo = WebPInfo()
    private var byteBuffer: ByteBuffer
    private var bitmap: Bitmap
    private var handler: Handler
    private var runnable: Runnable

    companion object {
        val TAG = WebPDrawable::class.java.simpleName
    }

    init {
        FileInputStream(path).use {
            webPNative.initialize(it.readBytes(), webPInfo)
        }

        handler = Handler(HandlerThread("webp").apply { start() }.looper)

        byteBuffer = ByteBuffer.allocateDirect(webPInfo.canvasSize())
        bitmap = Bitmap.createBitmap(webPInfo.canvasWidth, webPInfo.canvasHeight, Bitmap.Config.ARGB_8888)
        setBounds(0, 0, webPInfo.canvasWidth, webPInfo.canvasHeight)

        runnable = object : Runnable {
            override fun run() {
                val webPInfo = WebPInfo()
                if (webPNative.hasNextFrame()) {
                    webPNative.nextFrame(byteBuffer, webPInfo)
                    byteBuffer.position(0)
                    byteBuffer.limit(webPInfo.imageSize())
                    invalidateSelf()
                    handler.post(this)
                }
            }
        }

        handler.post(runnable)
        println("WebPDrawable.$webPInfo")
    }

    override fun draw(canvas: Canvas) {
        println("WebPDrawable.draw")
        with(bitmap) {
            this.copyPixelsFromBuffer(byteBuffer)
            canvas.drawBitmap(this, 0f, 0f, null)
        }
    }

    override fun getIntrinsicWidth(): Int {
        return if (webPInfo.canvasWidth != 0) webPInfo.canvasWidth else super.getIntrinsicWidth()
    }

    override fun getIntrinsicHeight(): Int {
        return if (webPInfo.canvasHeight != 0) webPInfo.canvasHeight else super.getIntrinsicHeight()
    }

    override fun setAlpha(alpha: Int) {
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }
}