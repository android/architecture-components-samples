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
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
        val fetchDecision = CompletableDeferred<Boolean>()
        yieldSource(dbSource.map {
            if (fetchDecision.isActive) {
                val shouldFetch = it == null || shouldFetch(it)
                fetchDecision.complete(shouldFetch)
                if (shouldFetch) {
                    // dispatch loading
                    Resource.loading(it)
                } else {
                    // no reason to fetch
                    Resource.success(it)
                }
            } else {
                if (fetchDecision.getCompleted()) {
                    Resource.loading(it)
                } else {
                    Resource.success(it)
                }
            }
        })

        // now wait until database sends us some value
        val shouldFetch = fetchDecision.await()
        if (shouldFetch) {
            doFetch(dbSource, this)
        }
    }

    private suspend fun doFetch(
        dbSource: LiveData<ResultType>,
        liveDataScope: LiveDataScope<Resource<ResultType>>
    ) {
        val response = fetchCatching()
        when (response) {
            is ApiSuccessResponse, is ApiEmptyResponse -> {
                if (response is ApiSuccessResponse) {
                    val processed = processResponse(response)
                    liveDataScope.clearSource()
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
}