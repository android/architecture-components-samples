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

package com.example.background

import android.net.Uri
import java.util.*

/**
 * Helps produce a random stock image [Uri].
 */
internal object StockImages {
    private val sRandom = Random()
    private val sAssetUris = arrayOf(
            Uri.parse("file:///android_asset/images/lit_pier.jpg"),
            Uri.parse("file:///android_asset/images/parting_ways.jpg"),
            Uri.parse("file:///android_asset/images/wrong_way.jpg"),
            Uri.parse("file:///android_asset/images/jack_beach.jpg"),
            Uri.parse("file:///android_asset/images/jack_blur.jpg"),
            Uri.parse("file:///android_asset/images/jack_road.jpg"))

    /**
     * This method produces a random image [Uri]. This is so you can see
     * the effects of applying filters on different kinds of stock images.
     *
     * @return a random stock image [Uri].
     */
    fun randomStockImage(): Uri {
        val index = sRandom.nextInt(sAssetUris.size)
        return sAssetUris[index]
    }
}
