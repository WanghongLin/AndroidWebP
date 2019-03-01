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

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.wanghong.webpnative.WebPNative
import kotlinx.android.synthetic.main.activity_sample.*
import java.io.File
import kotlin.concurrent.thread

class SampleActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_GET_CONTENT = 123
        const val REQUEST_CODE_GET_RW_EXTERNAL_PERMISSION = 124
    }

    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            with(Manifest.permission.READ_EXTERNAL_STORAGE) {
                if (ContextCompat.checkSelfPermission(this@SampleActivity, this) == PermissionChecker.PERMISSION_GRANTED) {
                    displayImage()
                } else {
                    ActivityCompat.requestPermissions(this@SampleActivity, arrayOf(this), 0)
                }
            }
        }

        imageListButton.setOnClickListener {
            startActivity(Intent(this@SampleActivity, ListDisplayActivity::class.java))
        }

        encodeRGBA.setOnClickListener {
            pickImage()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
            displayImage()
        }
        if (requestCode == REQUEST_CODE_GET_RW_EXTERNAL_PERMISSION && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
            performEncodeImage(bitmap)
        }
    }

    private fun displayImage() {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_GET_CONTENT &&
            resultCode == Activity.RESULT_OK) {
            data?.data?.let {
                contentResolver.openInputStream(it)?.readBytes()?.let { array ->
                    BitmapFactory.decodeByteArray(array, 0, array.size)?.let { bitmap ->
                        encodeImage(bitmap)
                    }
                }
            }
        }
    }

    private fun pickImage() {
        Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }.let {
            startActivityForResult(it, REQUEST_CODE_GET_CONTENT)
        }
    }

    private fun performEncodeImage(bitmap: Bitmap) {
        thread {
            val output = Environment.getExternalStorageDirectory().absolutePath + File.separator + "ic_launcher.webp"
            WebPNative().encodeRGBA(bitmap, output)
            runOnUiThread {
                Toast.makeText(this@SampleActivity, "output to $output", Toast.LENGTH_SHORT)
                    .show()
            }
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }

    private fun encodeImage(bitmap: Bitmap) {
        with(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
            if (ContextCompat.checkSelfPermission(this@SampleActivity, this) ==
                    PermissionChecker.PERMISSION_GRANTED) {
                performEncodeImage(bitmap)
            } else {
                this@SampleActivity.bitmap = bitmap
                ActivityCompat.requestPermissions(this@SampleActivity, arrayOf(this), REQUEST_CODE_GET_RW_EXTERNAL_PERMISSION)
            }
        }
    }
}
