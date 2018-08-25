/*
 *
 *  * Copyright (C) 2018 The Android Open Source Project
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package com.example.pagingsample;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;

import java.util.concurrent.Executors;

public class BeerViewModel extends AndroidViewModel {

    private static final int BEER_LIST_PAGE_SIZE = 20;

    private final BeerDao mBeerDao;
    private final LiveData<PagedList<Beer>> mObservableBeers;

    public BeerViewModel(Application application) {
        super(application);
        mBeerDao = BeerDb.getInstance(application, Executors.newSingleThreadExecutor()).beerDao();
        mObservableBeers = new LivePagedListBuilder<>(mBeerDao.loadAll(), BEER_LIST_PAGE_SIZE).build();
    }

    public LiveData<PagedList<Beer>> getObservableBeers() {
        return mObservableBeers;
    }

}
