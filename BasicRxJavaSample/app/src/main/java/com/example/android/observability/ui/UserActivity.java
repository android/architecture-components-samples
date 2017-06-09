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

package com.example.android.observability.ui;

import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.observability.Injection;
import com.example.android.persistence.R;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


/**
 * Main screen of the app. Displays a user name and gives the option to update the user name.
 */
public class UserActivity extends LifecycleActivity {

    private static final String TAG = UserActivity.class.getSimpleName();

    private TextView mUserName;

    private EditText mUserNameInput;

    private Button mUpdateButton;

    private ViewModelFactory mViewModelFactory;

    private UserViewModel mViewModel;

    private final CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mUserName = (TextView) findViewById(R.id.user_name);
        mUserNameInput = (EditText) findViewById(R.id.user_name_input);
        mUpdateButton = (Button) findViewById(R.id.update_user);

        mViewModelFactory = Injection.provideViewModelFactory(this);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(UserViewModel.class);
        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserName();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Subscribe to the emissions of the user name from the view model.
        // Update the user name text view, at every onNext emission.
        // In case of error, log the exception.
        mDisposable.add(mViewModel.getUserName()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String userName) throws Exception {
                        mUserName.setText(userName);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "Unable to update username", throwable);
                    }
                }));
    }

    @Override
    protected void onStop() {
        super.onStop();

        // clear all the subscription
        mDisposable.clear();
    }

    private void updateUserName() {
        String userName = mUserNameInput.getText().toString();
        // Disable the update button until the user name update has been done
        mUpdateButton.setEnabled(false);
        // Subscribe to updating the user name.
        // Enable back the button once the user name has been updated
        mDisposable.add(mViewModel.updateUserName(userName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        mUpdateButton.setEnabled(true);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "Unable to update username", throwable);
                    }
                }));
    }
}
