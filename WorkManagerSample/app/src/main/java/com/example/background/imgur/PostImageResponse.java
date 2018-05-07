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

import com.google.gson.annotations.SerializedName;

/**
 * The Imgur API post image response.
 */
public class PostImageResponse {

    @SerializedName("data")
    private UploadedImage mData;

    @SerializedName("success")
    private boolean mSuccess;

    @SerializedName("status")
    private int mStatus;

    public UploadedImage getData() {
        return mData;
    }

    public boolean isSuccess() {
        return mSuccess;
    }

    public int getStatus() {
        return mStatus;
    }

    public static class UploadedImage {
        @SerializedName("id")
        private String mId;

        @SerializedName("link")
        private String mLink;

        public String getId() {
            return mId;
        }

        public String getLink() {
            return mLink;
        }
    }

}
