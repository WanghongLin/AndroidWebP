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
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import java.nio.ByteBuffer

/**
 * WebP drawable support animation
 *
 * Created by wanghonglin on 2019/2/18 11:37 AM.
 */
class WebPDrawable(var webPData: ByteArray? = null) : Drawable() {

    private val webPNative = WebPNative()
    private val webPInfo = WebPInfo()
    private lateinit var byteBuffer: ByteBuffer
    private lateinit var bitmap: Bitmap
    private lateinit var handler: Handler
    private lateinit var handlerThread: HandlerThread
    private lateinit var runnable: Runnable
    private var previousTimestamp: Int = 0

    companion object {
        val TAG: String = WebPDrawable::class.java.simpleName
        const val VERBOSE = false
        const val DEFAULT_ANIMATION_DELAY_MILLIS = 40
    }

    fun start() {
        initialize()
    }

    fun stop() {
        webPNative.release()
        if (::handler.isInitialized) {
            handler.removeCallbacksAndMessages(null)
        }
        if (::handlerThread.isInitialized) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                handlerThread.quitSafely()
            } else {
                handlerThread.quit()
            }
        }
    }

    private fun initialize() {
        if (webPData == null) {
            return
        }

        webPNative.initialize(webPData!!, webPInfo)

        handler = Handler(HandlerThread("webp").apply {
            handlerThread = this
            start()
        }.looper)

        byteBuffer = ByteBuffer.allocateDirect(webPInfo.canvasSize())
        bitmap = Bitmap.createBitmap(webPInfo.canvasWidth, webPInfo.canvasHeight, Bitmap.Config.ARGB_8888)
        setBounds(0, 0, webPInfo.canvasWidth, webPInfo.canvasHeight)

        runnable = object : Runnable {
            override fun run() {
                val webPInfo = WebPInfo()
                if (webPNative.hasNextFrame()) {
                    webPNative.nextFrame(byteBuffer, webPInfo)
                    if (VERBOSE) Log.d(TAG, "$webPInfo")
                    invalidateSelf()

                    var delay = webPInfo.timeStamp - previousTimestamp
                    if (delay <= 0) {
                        delay = DEFAULT_ANIMATION_DELAY_MILLIS
                    }

                    handler.postDelayed(this, delay.toLong())
                    previousTimestamp = webPInfo.timeStamp
                }
            }
        }

        handler.post(runnable)
        if (VERBOSE) Log.d(TAG, "$webPInfo")
    }

    override fun draw(canvas: Canvas) {
        if (::bitmap.isInitialized and ::byteBuffer.isInitialized) {
            with(bitmap) {
                byteBuffer.position(0)
                byteBuffer.limit(webPInfo.canvasSize())
                this.copyPixelsFromBuffer(byteBuffer)
                canvas.drawBitmap(this, 0f, 0f, null)
            }
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