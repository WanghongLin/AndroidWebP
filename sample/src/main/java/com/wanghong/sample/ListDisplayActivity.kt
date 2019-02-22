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

package com.wanghong.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.wanghong.webpnative.WebPImageView
import kotlinx.android.synthetic.main.activity_list_display.*

class ListDisplayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_display)

        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.adapter = ImageViewAdapter()
    }
}

class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    init {

    }
}

class ImageViewAdapter : RecyclerView.Adapter<ImageViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ImageViewHolder {
        return ImageViewHolder(WebPImageView(p0.context))
    }

    override fun getItemCount() = 10

    override fun onBindViewHolder(p0: ImageViewHolder, p1: Int) {
        val webPImageView = p0.itemView as WebPImageView
        webPImageView.fileUri = "file:///mnt/sdcard/1.webp"
    }
}