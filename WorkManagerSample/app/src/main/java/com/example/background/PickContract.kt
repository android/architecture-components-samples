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