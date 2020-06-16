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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.result.contract.ActivityResultContract

class PickContract : ActivityResultContract<Uri, Uri?>() {

    override fun createIntent(context: Context, uri: Uri): Intent {
        return Intent(Intent.ACTION_PICK, uri)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (resultCode == Activity.RESULT_OK) {
            return if (Build.VERSION.SDK_INT >= 16 && intent?.clipData != null) {
                intent.clipData?.getItemAt(0)?.uri
            }else{
                intent?.data
            }
        } else {
            null
        }
    }

}