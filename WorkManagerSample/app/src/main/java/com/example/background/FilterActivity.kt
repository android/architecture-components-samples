/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.background

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Checkable
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.background.databinding.ActivityProcessingBinding

/** The [android.app.Activity] where the user picks filters to be applied on an image. */
class FilterActivity : AppCompatActivity() {

    private val viewModel: FilterViewModel by viewModels()
    private var imageUri: Uri? = null
    private var outputImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityProcessingBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        // Don't enable upload to Imgur, unless the developer specifies their own clientId.
        val enableUpload = Constants.IMGUR_CLIENT_ID.isNotEmpty()
        binding.upload.isEnabled = enableUpload

        val imageUriExtra = intent.getStringExtra(Constants.KEY_IMAGE_URI)
        if (!imageUriExtra.isNullOrEmpty()) {
            imageUri = Uri.parse(imageUriExtra)
            val imageView = findViewById<ImageView>(R.id.imageView)
            Glide.with(this).load(imageUri).into(imageView)
        }

        binding.go.setOnClickListener {
            val applyWaterColor = isChecked(R.id.filter_watercolor)
            val applyGrayScale = isChecked(R.id.filter_grayscale)
            val applyBlur = isChecked(R.id.filter_blur)
            val save = isChecked(R.id.save)
            val upload = isChecked(R.id.upload)

            val imageOperations = ImageOperations(
                applicationContext, imageUri!!,
                applyWaterColor, applyGrayScale, applyBlur,
                save, upload
            )

            viewModel.apply(imageOperations)
        }

        binding.output.setOnClickListener {
            if (outputImageUri != null) {
                val actionView = Intent(Intent.ACTION_VIEW, outputImageUri)
                if (actionView.resolveActivity(packageManager) != null) {
                    startActivity(actionView)
                }
            }
        }

        binding.cancel.setOnClickListener { viewModel.cancel() }

        // Check to see if we have output.
        viewModel.outputStatus.observe(this, Observer { listOfInfos ->
            if (listOfInfos == null || listOfInfos.isEmpty()) {
                return@Observer
            }

            // We only care about the one output status.
            // Every continuation has only one worker tagged TAG_OUTPUT
            val info = listOfInfos[0]
            val finished = info.state.isFinished
            if (!finished) {
                with(binding) {
                    progressBar.visibility = View.VISIBLE
                    cancel.visibility = View.VISIBLE
                    go.visibility = View.GONE
                    output.visibility = View.GONE
                }
            } else {
                with(binding) {
                    progressBar.visibility = View.GONE
                    cancel.visibility = View.GONE
                    go.visibility = View.VISIBLE
                }

                val outputData = info.outputData
                val outputImageUri = outputData.getString(Constants.KEY_IMAGE_URI)

                if (!outputImageUri.isNullOrEmpty()) {
                    this.outputImageUri = Uri.parse(outputImageUri)
                    binding.output.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun isChecked(@IdRes resourceId: Int): Boolean {
        val view = findViewById<View>(resourceId)
        return view is Checkable && (view as Checkable).isChecked
    }

    companion object {

        /**
         * Creates a new intent which can be used to start [FilterActivity].
         *
         * @param context the application [Context].
         * @param imageUri the input image [Uri].
         * @return the instance of [Intent].
         */
        internal fun newIntent(context: Context, imageUri: Uri) =
            Intent(context, FilterActivity::class.java).putExtra(
                Constants.KEY_IMAGE_URI, imageUri.toString()
            )
    }
}
