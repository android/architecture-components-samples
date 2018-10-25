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

package com.example.background.imgur;

import android.net.Uri;
import androidx.annotation.NonNull;
import com.example.background.Constants;

import java.io.File;
import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * The Imgur API client, which uses the {@link ImgurService} Retrofit APIs.
 */
public class ImgurApi {

    private static ImgurApi sInstance;
    private ImgurService mImgurService;

    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    private static final Object sLock = new Object();

    /**
     * @return an instance of the {@link ImgurApi} client.
     */
    public static ImgurApi getInstance() {
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new ImgurApi();
            }
        }
        return sInstance;
    }

    private ImgurApi() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor())
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mImgurService = retrofit.create(ImgurService.class);
    }

    public Call<PostImageResponse> uploadImage(Uri imageUri) {
        File imageFile = new File(imageUri.getPath());
        RequestBody requestFile = RequestBody.create(MEDIA_TYPE_PNG, imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", "image.png", requestFile);
        return mImgurService.postImage(body);
    }

    private static class AuthInterceptor implements Interceptor {
        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();
            Headers headers = request.headers().newBuilder()
                    .add("Authorization", "Client-ID " + Constants.IMGUR_CLIENT_ID)
                    .build();
            Request authenticatedRequest = request.newBuilder().headers(headers).build();
            return chain.proceed(authenticatedRequest);
        }
    }
}
