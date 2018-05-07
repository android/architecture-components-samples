/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.android.example.github.util

import android.os.Bundle
import androidx.navigation.NavDirections
import org.mockito.Mockito

private fun Bundle?.eq(other : Bundle?) : Boolean {
    if (this == null) {
        return other == null
    }
    if (other == null) {
        return false
    }
    if (keySet().size != other.keySet().size) {
        return false
    }
    return keySet().all {
        get(it) == other.get(it)
    }
}

/**
 * A convenience method to create a Mockito argument matcher for navigation directions.
 */
fun NavDirections.matcher() = Mockito.argThat<NavDirections> {
    it.actionId == this.actionId && arguments.eq(it.arguments)
}