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

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class BeerViewHolder extends RecyclerView.ViewHolder {

    private View mView;
    private TextView mNameTextView;
    private TextView mCountryOfOriginTextView;

    BeerViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mNameTextView = mView.findViewById(R.id.name_text_view);
        mCountryOfOriginTextView = mView.findViewById(R.id.country_of_origin_text_view);
    }

    public void bindTo(Beer beer) {
        mNameTextView.setText(beer.getName());
        mCountryOfOriginTextView.setText(beer.getCountryOfOrigin());
    }

    public void clear() {
        mView.setVisibility(View.GONE);
    }
}
