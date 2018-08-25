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

import android.arch.paging.PagedListAdapter;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public class BeerAdapter extends PagedListAdapter<Beer, BeerViewHolder> {

    BeerAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public BeerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BeerViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.list_item_beer, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BeerViewHolder holder, int position) {
        Beer beer = getItem(position);
        if (beer != null) {
            holder.bindTo(beer);
        } else {
            holder.clear();
        }
    }

    private static DiffUtil.ItemCallback<Beer> DIFF_CALLBACK = new DiffUtil.ItemCallback<Beer>() {
        @Override
        public boolean areItemsTheSame(Beer oldBeer, Beer newBeer) {
            return oldBeer.getId() == newBeer.getId();
        }

        @Override
        public boolean areContentsTheSame(Beer oldBeer, Beer newBeer) {
            return oldBeer.equals(newBeer);
        }
    };


}
