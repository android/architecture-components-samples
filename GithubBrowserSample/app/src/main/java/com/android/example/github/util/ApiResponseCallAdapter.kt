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

package com.android.example.github.util

import com.android.example.github.api.ApiResponse
import okhttp3.Request
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

internal class ApiResponseCall<T>(
    private val delegate: Call<T>
) : Call<ApiResponse<T>> {
    override fun enqueue(realCallback: Callback<ApiResponse<T>>) {
        delegate.enqueue(object : Callback<T> {
            override fun onFailure(call: Call<T>, error: Throwable) {
                // we always succeed
                realCallback.onResponse(
                    this@ApiResponseCall,
                    Response.success(ApiResponse.create(error))
                )
            }

            override fun onResponse(call: Call<T>, response: Response<T>) {
                realCallback.onResponse(
                    this@ApiResponseCall, Response.success(
                        ApiResponse.create(response)
                    )
                )
            }

        })
    }

    override fun isExecuted() = delegate.isExecuted

    override fun clone(): Call<ApiResponse<T>> = ApiResponseCall(delegate)

    override fun isCanceled() = delegate.isCanceled

    override fun cancel() = delegate.cancel()

    override fun execute(): Response<ApiResponse<T>> {
        return Response.success(ApiResponse.create(delegate.execute()))
    }

    override fun request(): Request = delegate.request()
}


class ApiResponseCallAdapter<R>(
    private val bodyType: Type,
    private val delegate: CallAdapter<R, Call<R>>
) : CallAdapter<R, Call<ApiResponse<R>>> {
    override fun adapt(original: Call<R>): Call<ApiResponse<R>> {
        return ApiResponseCall(delegate.adapt(original))
    }

    override fun responseType(): Type = bodyType
}

class ApiResponseCallAdapterFactory : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        // avoid recursion here
        if (returnType is OneArgParameterizedType) {
            return null
        }
        val parameterizedReturn = returnType as? ParameterizedType ?: return null
        if (parameterizedReturn.rawType != Call::class.java) {
            return null
        }
        val parameterizedApiResponse =
            parameterizedReturn.actualTypeArguments.firstOrNull() as? ParameterizedType
                ?: return null
        val bodyType = parameterizedApiResponse.actualTypeArguments.firstOrNull() ?: return null
        val callBody = OneArgParameterizedType(Call::class.java, arrayOf(bodyType))
        val delegate = retrofit.callAdapter(callBody, annotations) ?: return null
        @Suppress("UNCHECKED_CAST")
        return ApiResponseCallAdapter(bodyType, delegate as CallAdapter<Any, Call<Any>>)
    }
}

open class OneArgParameterizedType(
    private val myRawType: Type,
    private val myTypeArgs: Array<Type>
) : ParameterizedType {
    override fun getRawType() = myRawType

    override fun getOwnerType() = null

    override fun getActualTypeArguments() = myTypeArgs
}
