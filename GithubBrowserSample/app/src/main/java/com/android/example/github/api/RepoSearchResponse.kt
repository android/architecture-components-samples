/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.example.github.api


import com.android.example.github.vo.Repo
import com.google.gson.annotations.SerializedName

/**
 * Simple object to hold repo search responses. This is different from the Entity in the database
 * because we are keeping a search result in 1 row and denormalizing list of results into a single
 * column.
 */
data class RepoSearchResponse(
    @SerializedName("total_count")
    val total: Int = 0,
    @SerializedName("items")
    val items: List<Repo>
) {
    var nextPage: Int? = null
}
