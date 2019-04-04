/*
 * Copyright 2019 The Android Open Source Project
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

package com.android.example.github.repository

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.android.example.github.api.ApiEmptyResponse
import com.android.example.github.api.ApiErrorResponse
import com.android.example.github.api.ApiResponse
import com.android.example.github.api.ApiSuccessResponse
import com.android.example.github.vo.Resource
import com.android.example.github.vo.Status
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import java.util.concurrent.CancellationException


fun <ResultType, RequestType> networkBoundResource(
    saveCallResult: suspend (RequestType) -> Unit,
    shouldFetch: (ResultType) -> Boolean = { true },
    loadFromDb: () -> LiveData<ResultType>,
    fetch: suspend () -> ApiResponse<RequestType>,
    processResponse: (suspend (ApiSuccessResponse<RequestType>) -> RequestType) = { it.body },
    onFetchFailed: ((ApiErrorResponse<RequestType>) -> Unit)? = null
): LiveData<Resource<ResultType>> {
    return CoroutineNetworkBoundResource(
        saveCallResult = saveCallResult,
        shouldFetch = shouldFetch,
        loadFromDb = loadFromDb,
        fetch = fetch,
        processResponse = processResponse,
        onFetchFailed = onFetchFailed
    ).asLiveData().distinctUntilChanged() // not super happy about this as the data might be BIG
}

/**
 * A [NetworkBoundResource] implementation in corotuines
 */
private class CoroutineNetworkBoundResource<ResultType, RequestType>
@MainThread constructor(
    private val saveCallResult: suspend (RequestType) -> Unit,
    private val shouldFetch: (ResultType) -> Boolean = { true },
    private val loadFromDb: () -> LiveData<ResultType>,
    private val fetch: suspend () -> ApiResponse<RequestType>,
    private val processResponse: (suspend (ApiSuccessResponse<RequestType>) -> RequestType),
    private val onFetchFailed: ((ApiErrorResponse<RequestType>) -> Unit)?
) {
    @ExperimentalCoroutinesApi
    private val result = liveData<Resource<ResultType>> {
        if (initialValue?.status != Status.SUCCESS) {
            yield(Resource.loading(initialValue?.data))
        }
        val dbSource = loadFromDb()
        val initialValue = dbSource.await()
        val willFetch = initialValue == null || shouldFetch(initialValue)
        if (!willFetch) {
            // if we won't fetch, just yield existing db values as success
            yieldSource(dbSource.map {
                Resource.success(it)
            })
        } else {
            doFetch(dbSource, this)
        }
    }

    private suspend fun doFetch(
        dbSource: LiveData<ResultType>,
        liveDataScope: LiveDataScope<Resource<ResultType>>
    ) {
        // yield existing values as loading while we fetch
        liveDataScope.yieldSource(dbSource.map {
            Resource.loading(it)
        })
        val response = fetchCatching()
        when (response) {
            is ApiSuccessResponse, is ApiEmptyResponse -> {
                if (response is ApiSuccessResponse) {
                    val processed = processResponse(response)
                    liveDataScope.clearSource()
                    // before saving it, disconnect it so that new values comes w/ success
                    saveCallResult(processed)
                }
                liveDataScope.yieldSource(loadFromDb().map {
                    Resource.success(it)
                })
            }
            is ApiErrorResponse -> {
                onFetchFailed?.invoke(response)
                liveDataScope.yieldSource(dbSource.map {
                    Resource.error(response.errorMessage, it)
                })
            }
        }
    }

    // temporary here during migration
    fun asLiveData() = result as LiveData<Resource<ResultType>>

    private suspend fun fetchCatching(): ApiResponse<RequestType> {
        return try {
            fetch()
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Throwable) {
            ApiResponse.create(ex)
        }
    }

    /**
     * temporary workaround until we have cancellable yieldSource
     */
    private suspend fun <T> LiveDataScope<T>.clearSource() {
        yieldSource(MutableLiveData<T>())
    }

    private suspend fun <T> LiveData<T>.await() = withContext(Dispatchers.Main) {
        val receivedValue = CompletableDeferred<T?>()
        val observer = Observer<T> {
            if (receivedValue.isActive){
                receivedValue.complete(it)
            }
        }
        try {
            observeForever(observer)
            return@withContext receivedValue.await()
        } finally {
            removeObserver(observer)
        }
    }
}