Kotlin Coroutines with Architecture Components (LiveData, ViewModel, Lifecycle)
=============================================

This sample showcases the following Architecture Components:

* [LiveData](https://developer.android.com/reference/android/arch/lifecycle/LiveData.html)
* [ViewModels](https://developer.android.com/reference/android/arch/lifecycle/ViewModel.html)
* [Data Binding](https://developer.android.com/topic/libraries/data-binding)


And the following artifacts:

* [androidx.lifecycle.lifecycle-livedata-ktx](https://developer.android.com/jetpack/androidx/releases/lifecycle) -
currently using its alpha version.

This project shows how to integrate them with Kotlin's coroutines using the `liveData` builder. 

Introduction
-------------

LiveData is a data holder class that can be observed within a given lifecycle. Usually, you use LiveData to communicate a ViewModel with a View. In this project you'll find different patterns showcasing the `liveData` builder that lets you control a LiveData from a coroutine block.

## Patterns

### LiveData builder that emits values while it's observed

In `DataSource.kt` you'll find the following snippet:

```

/**
 * LiveData builder generating a value that will be transformed.
 */
fun getCurrentTime(): LiveData<Long> =
    liveData {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1000)
        }
    }
```

When observed, it generates a new value every second. This is executed in a coroutine and consumed directly by the UI.

### Transformation in a suspend function or background

To make one LiveData depend on the value of another, you use Transformations or the generic MediatorLiveData class.
Transformations, like `map` and `switchMap`, are computed in the main thread. However, using a combination of `switchMap` and the `liveData` builder, you can easily call suspend functions or move the transformation to a different thread.

In `LiveDataViewModel.kt`:

```
val currentTimeTransformed = currentTime.switchMap {
    liveData(defaultDispatcher) { emit(timeStampToTime(it)) }
}
```

### Using emit and emitSource

The `liveData` builder also lets you emit the updates from another LiveData using `emitSource`:

In `LiveDataViewModel.kt`:

```
// Exposed liveData that emits and single value and subsequent values from another source.
val currentWeather: LiveData<String> = liveData {
    emit("Loading...")
    emitSource(dataSource.fetchWeather())
}
```

The first value will be replaced when the source LiveData receives its first value and it will continue emitting all updates while `currentWeather` has observers.

### Cache value and fetch new data using coroutines

A very common pattern is to have the UI observe a LiveData source (for example, using Room as the source of truth) and have another process to update it like fetching new data from network and writing it to the database.

This is simplified in the following piece of code in `DataSource.kt`:

```
// Cache of a data point that is exposed to VM
private val _cachedData = MutableLiveData("This is old data")
override val cachedData: LiveData<String> = _cachedData

// Called when the cache needs to be refreshed. Must be called from coroutine.
override suspend fun fetchNewData() {
    // Force Main thread
    withContext(Dispatchers.Main) {
        _cachedData.value = "Fetching new data..."
        _cachedData.value = simulateNetworkDataFetch()
    }
}
```

The exposed `cachedData` LiveData replaces Room observing a query in the example above. `fetchNewData` is a suspend function so it must be called from a coroutine as `simulateNetworkDataFetch` is a potentially expensive or long operation.

In `DataSource.kt`:

```
// Using ioDispatcher because the function simulates a long and expensive operation.
private suspend fun simulateNetworkDataFetch(): String = withContext(ioDispatcher) {
    delay(3000)
    counter++
    "New data from request #$counter"
}
```

It switches the coroutine context to an injected `ioDispatcher` to show how to use a background thread.

In `LiveDataVieWModel.kt` you can see that the `viewModelScope` is used to call this function:

```
// Called when the user clicks on the "FETCH NEW DATA" button. Updates value in data source.
fun onRefresh() {
    // Launch a coroutine that reads from a remote data source and updates cache
    viewModelScope.launch {
        dataSource.fetchNewData()
    }
}
```

Unit-testing LiveData
-------------
`LiveDataViewModelTest.kt` shows different techniques to unit-test a class that exposes LiveData and uses coroutines. It uses two important functions in `LiveDataTestUtil`:
 - `getOrAwaitValue`:  Gets the value of a LiveData or waits for it to have one, with a timeout
 - `observeForTesting`: Observes a LiveData until the `block` is done executing.


License
--------

Copyright 2019 The Android Open Source Project, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.



