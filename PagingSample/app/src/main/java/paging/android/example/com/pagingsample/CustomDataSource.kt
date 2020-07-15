package paging.android.example.com.pagingsample

import androidx.paging.DataSource

class Factory : DataSource.Factory<Int, String>() {
    override fun create(): DataSource<Int, String> {
        TODO("This is where we would call the repository")
    }
}