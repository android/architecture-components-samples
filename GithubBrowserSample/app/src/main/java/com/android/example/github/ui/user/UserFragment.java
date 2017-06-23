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

package com.android.example.github.ui.user;

import com.android.example.github.R;
import com.android.example.github.binding.FragmentDataBindingComponent;
import com.android.example.github.databinding.UserFragmentBinding;
import com.android.example.github.di.Injectable;
import com.android.example.github.ui.common.NavigationController;
import com.android.example.github.ui.common.RepoListAdapter;
import com.ivianuu.dusty.Dusty;
import com.ivianuu.dusty.annotations.Clear;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

public class UserFragment extends LifecycleFragment implements Injectable {
    private static final String LOGIN_KEY = "login";
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    NavigationController navigationController;

    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    private UserViewModel userViewModel;
    @Clear
    UserFragmentBinding binding;
    @Clear
    RepoListAdapter adapter;

    public static UserFragment create(String login) {
        UserFragment userFragment = new UserFragment();
        Bundle bundle = new Bundle();
        bundle.putString(LOGIN_KEY, login);
        userFragment.setArguments(bundle);
        return userFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dusty.register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.user_fragment,
                container, false, dataBindingComponent);
        binding.setRetryCallback(() -> userViewModel.retry());
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        userViewModel = ViewModelProviders.of(this, viewModelFactory).get(UserViewModel.class);
        userViewModel.setLogin(getArguments().getString(LOGIN_KEY));
        userViewModel.getUser().observe(this, userResource -> {
            binding.setUser(userResource == null ? null : userResource.data);
            binding.setUserResource(userResource);
            // this is only necessary because espresso cannot read data binding callbacks.
            binding.executePendingBindings();
        });
        adapter = new RepoListAdapter(dataBindingComponent, false,
                repo -> navigationController.navigateToRepo(repo.owner.login, repo.name));
        binding.repoList.setAdapter(adapter);
        initRepoList();
    }

    private void initRepoList() {
        userViewModel.getRepositories().observe(this, repos -> {
            // no null checks for adapter since LiveData guarantees that we'll not receive
            // the event if fragment is now show.
            if (repos == null) {
                adapter.replace(null);
            } else {
                adapter.replace(repos.data);
            }
        });
    }
}
