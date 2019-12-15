package com.android.example.flow.twitter.data.models


/**
 * Created by Santanu 😁 on 2019-11-19.
 * @property exception  is typically null when there is data
 * @property data       is the data thrown by the API
 */
class ResponseWrapper<T, E : Exception?>(var data: T, var exception: E)