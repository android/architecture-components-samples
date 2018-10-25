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

package com.example.background;

/**
 * Defines a list of constants used for {@link androidx.work.Worker} names, inputs & outputs.
 */
public final class Constants {

    // The name of the image manipulation work
    public static final String IMAGE_MANIPULATION_WORK_NAME = "image_manipulation_work";

    // Other keys
    public static final String OUTPUT_PATH = "demo_filter_outputs";
    public static final String BASE_URL = "https://api.imgur.com/3/";
    public static final String KEY_IMAGE_URI = "KEY_IMAGE_URI";

    public static final String TAG_OUTPUT = "OUTPUT";

    // Provide your own clientId to test Imgur uploads.
    public static final String IMGUR_CLIENT_ID = "";

    private Constants() {

    }
}
