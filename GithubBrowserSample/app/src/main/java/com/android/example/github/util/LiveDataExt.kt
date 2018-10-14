package com.android.example.github.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations

inline fun <T> LiveData<T>.observe(
    owner: LifecycleOwner,
    crossinline observer: (T?) -> Unit
) {
    observe(owner, Observer<T> { v -> observer(v) })
}

inline fun <X, Y> LiveData<X>.switchMap(crossinline transformer: (X) -> LiveData<Y>): LiveData<Y> =
    Transformations.switchMap(this) { transformer(it) }