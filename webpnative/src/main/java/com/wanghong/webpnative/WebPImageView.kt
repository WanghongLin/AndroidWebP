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

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import java.io.FileInputStream

/**
 * Created by wanghonglin on 2019/2/19 7:29 PM.
 */
class WebPImageView(context: Context) : ImageView(context) {

    companion object {
        val TAG = WebPImageView::class.java.simpleName
    }

    private var fileUri: String? = null

    private val webPDrawable = WebPDrawable()

    init {
        initView(context, null, 0)
    }

    constructor(context: Context, attributeSet: AttributeSet) : this(context) {
        initView(context, attributeSet, 0)
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : this(context, attributeSet)

    private fun initView(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) {
        context.theme.obtainStyledAttributes(attributeSet, R.styleable.WebPImageView, 0, 0)
            .apply {
                try {
                    fileUri = getString(R.styleable.WebPImageView_fileUri)
                } finally {
                    recycle()
                }
            }
    }

    private fun fileByteFromFileUri(): ByteArray? {
        val fileUri: String? = this.fileUri
        return if (fileUri != null) when {
            fileUri.startsWith("file://") -> {
                FileInputStream(fileUri.removePrefix("file://")).use {
                    it.readBytes()
                }
            }
            fileUri.startsWith("assets://") -> {
                context.assets.open(fileUri.removePrefix("assets://")).use {
                    it.readBytes()
                }
            }
            fileUri.startsWith("res://") -> {
                val resId = context.resources.getIdentifier(fileUri.removePrefix("res://"), null, null)
                if (resId == 0) null else context.resources.openRawResource(resId).use {
                    it.readBytes()
                }
            }
            else -> {
                null
            }
        } else null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        showWebPImage()
    }

    private fun showWebPImage() {
        with(webPDrawable) {
            webPData = fileByteFromFileUri()
            start()
            setImageDrawable(this)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        webPDrawable.stop()
    }
}