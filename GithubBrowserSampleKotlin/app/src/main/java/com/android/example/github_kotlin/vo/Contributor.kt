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

package com.android.example.github_kotlin.vo

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import com.google.gson.annotations.SerializedName

@Entity(primaryKeys = arrayOf("repoName", "repoOwner", "login"),
        foreignKeys = arrayOf(ForeignKey(entity = Repo::class,
                                         parentColumns = arrayOf("name", "owner_login"),
                                         childColumns = arrayOf("repoName", "repoOwner"),
                                         onUpdate = ForeignKey.CASCADE,
                                         deferred = true)))
class Contributor(
        login: String,
        contributions: Int,
        avatarUrl: String?) {
    @field:SerializedName("login")
    val login = login
    @field:SerializedName("contributions")
    val contributions = contributions
    @field:SerializedName("avatar_url")
    val avatarUrl = avatarUrl
    var repoName = ""
    var repoOwner = ""
}
