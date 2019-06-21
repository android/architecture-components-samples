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

package com.example.android.persistence.migrations;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Main screen of the app. Displays a user name and allows the option to update the user name.
 */
public class UserActivity extends AppCompatActivity implements UserView {

    private TextView mUserName;

    private EditText mUserNameInput;

    private Button mUpdateButton;

    private UserPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mUserName = findViewById(R.id.user_name);
        mUserNameInput = findViewById(R.id.user_name_input);
        mUpdateButton = findViewById(R.id.update_user);

        mUpdateButton.setOnClickListener(v -> {
            String userName = mUserNameInput.getText().toString();
            mPresenter.updateUserName(userName);
        });

        // Creating the repository here for simplicity.
        // In an real app, this would be a singleton injected
        UserRepository userRepository = new UserRepository(new AppExecutors(),
                LocalUserDataSource.getInstance(getApplicationContext()));

        mPresenter = new UserPresenter(userRepository, this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPresenter.stop();
    }

    @Override
    public void showUserName(String userName) {
        mUserName.setVisibility(View.VISIBLE);
        mUserName.setText(userName);
    }

    @Override
    public void hideUserName() {
        mUserName.setVisibility(View.INVISIBLE);
    }
}
