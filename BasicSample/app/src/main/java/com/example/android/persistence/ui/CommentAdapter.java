/*
 * Copyright 2017, The Android Open Source Project
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

package com.example.android.persistence.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.android.persistence.databinding.CommentItemBinding;
import com.example.android.persistence.db.entity.CommentEntity;
import com.example.android.persistence.R;

public class CommentAdapter extends ListAdapter<CommentEntity, CommentAdapter.CommentViewHolder> {

    @Nullable
    private final CommentClickCallback mCommentClickCallback;

    CommentAdapter(@Nullable CommentClickCallback commentClickCallback) {
        super(new AsyncDifferConfig.Builder<>(new DiffUtil.ItemCallback<CommentEntity>() {
            @Override
            public boolean areItemsTheSame(@NonNull CommentEntity old,
                    @NonNull CommentEntity comment) {
                return old.getId() == comment.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull CommentEntity old,
                    @NonNull CommentEntity comment) {
                return old.getId() == comment.getId()
                        && old.getPostedAt().equals(comment.getPostedAt())
                        && old.getProductId() == comment.getProductId()
                        && TextUtils.equals(old.getText(), comment.getText());
            }
        }).build());
        mCommentClickCallback = commentClickCallback;
    }

    @Override
    @NonNull
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CommentItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.comment_item,
                        parent, false);
        binding.setCallback(mCommentClickCallback);
        return new CommentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.binding.setComment(getItem(position));
        holder.binding.executePendingBindings();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {

        final CommentItemBinding binding;

        CommentViewHolder(CommentItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
