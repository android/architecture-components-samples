/*
 *
 *  * Copyright (C) 2018 The Android Open Source Project
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.example.background.imgur

import android.net.Uri
import com.example.background.Constants
import okhttp3.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException

/**
 * The Imgur API client, which uses the [ImgurService] Retrofit APIs.
 */
class ImgurApi private constructor() {
    private val mImgurService: ImgurService

    init {
        val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor())
                .build()
        val retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        mImgurService = retrofit.create(ImgurService::class.java)
    }

    fun uploadImage(imageUri: Uri): Call<PostImageResponse> {
        val imageFile = File(imageUri.path!!)
        val requestFile = RequestBody.create(MEDIA_TYPE_PNG, imageFile)
        val body = MultipartBody.Part.createFormData("image", "image.png", requestFile)
        return mImgurService.postImage(body)
    }

    private class AuthInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val headers = request.headers().newBuilder()
                    .add("Authorization", "Client-ID " + Constants.IMGUR_CLIENT_ID)
                    .build()
            val authenticatedRequest = request.newBuilder().headers(headers).build()
            return chain.proceed(authenticatedRequest)
        }
    }

    companion object {
        private val MEDIA_TYPE_PNG = MediaType.parse("image/png")

        val instance: Lazy<ImgurApi> = lazy { ImgurApi() }
    }
}
