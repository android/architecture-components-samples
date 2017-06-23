/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.example.github.ui.search;

import com.android.example.github.R;
import com.android.example.github.binding.FragmentDataBindingComponent;
import com.android.example.github.databinding.SearchFragmentBinding;
import com.android.example.github.di.Injectable;
import com.android.example.github.ui.common.NavigationController;
import com.android.example.github.ui.common.RepoListAdapter;
import com.ivianuu.dusty.Dusty;
import com.ivianuu.dusty.annotations.Clear;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import javax.inject.Inject;

public class SearchFragment extends LifecycleFragment implements Injectable {

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Inject
    NavigationController navigationController;

    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    @Clear
    SearchFragmentBinding binding;

    @Clear
    RepoListAdapter adapter;

    private SearchViewModel searchViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dusty.register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil
                .inflate(inflater, R.layout.search_fragment, container, false,
                        dataBindingComponent);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        searchViewModel = ViewModelProviders.of(this, viewModelFactory).get(SearchViewModel.class);
        initRecyclerView();
        adapter = new RepoListAdapter(dataBindingComponent, true,
                repo -> navigationController.navigateToRepo(repo.owner.login, repo.name));
        binding.repoList.setAdapter(adapter);

        initSearchInputListener();

        binding.setCallback(() -> searchViewModel.refresh());
    }

    private void initSearchInputListener() {
        binding.input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch(v);
                return true;
            }
            return false;
        });
        binding.input.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN)
                    && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                doSearch(v);
                return true;
            }
            return false;
        });
    }

    private void doSearch(View v) {
        String query = binding.input.getText().toString();
        // Dismiss keyboard
        dismissKeyboard(v.getWindowToken());
        binding.setQuery(query);
        searchViewModel.setQuery(query);
    }

    private void initRecyclerView() {

        binding.repoList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager)
                        recyclerView.getLayoutManager();
                int lastPosition = layoutManager
                        .findLastVisibleItemPosition();
                if (lastPosition == adapter.getItemCount() - 1) {
                    searchViewModel.loadNextPage();
                }
            }
        });
        searchViewModel.getResults().observe(this, result -> {
            binding.setSearchResource(result);
            binding.setResultCount((result == null || result.data == null)
                    ? 0 : result.data.size());
            adapter.replace(result == null ? null : result.data);
            binding.executePendingBindings();
        });

        searchViewModel.getLoadMoreStatus().observe(this, loadingMore -> {
            if (loadingMore == null) {
                binding.setLoadingMore(false);
            } else {
                binding.setLoadingMore(loadingMore.isRunning());
                String error = loadingMore.getErrorMessageIfNotHandled();
                if (error != null) {
                    Snackbar.make(binding.loadMoreBar, error, Snackbar.LENGTH_LONG).show();
                }
            }
            binding.executePendingBindings();
        });
    }

    private void dismissKeyboard(IBinder windowToken) {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(windowToken, 0);
        }
    }
}
