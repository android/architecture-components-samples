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

package com.example.android.observability;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import com.example.android.observability.persistence.User;
import com.example.android.observability.ui.UserViewModel;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import io.reactivex.Flowable;

/**
 * Unit test for {@link UserViewModel}
 */
public class UserViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private UserDataSource mDataSource;

    @Captor
    private ArgumentCaptor<User> mUserArgumentCaptor;

    private UserViewModel mViewModel;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        mViewModel = new UserViewModel(mDataSource);
    }

    @Test
    public void getUserName_whenNoUserSaved() throws InterruptedException {
        // Given that the UserDataSource returns an empty list of users
        when(mDataSource.getUser()).thenReturn(Flowable.<User>empty());

        //When getting the user name
        mViewModel.getUserName()
                .test()
                // The user name is empty
                .assertNoValues();
    }

    @Test
    public void getUserName_whenUserSaved() throws InterruptedException {
        // Given that the UserDataSource returns a user
        User user = new User("user name");
        when(mDataSource.getUser()).thenReturn(Flowable.just(user));

        //When getting the user name
        mViewModel.getUserName()
                .test()
                // The correct user name is emitted
                .assertValue("user name");
    }

    @Test
    public void updateUserName_updatesNameInDataSource() {
        // When updating the user name
        mViewModel.updateUserName("new user name")
                .test()
                .assertComplete();

        // The user name is updated in the data source
        verify(mDataSource).insertOrUpdateUser(mUserArgumentCaptor.capture());
        assertThat(mUserArgumentCaptor.getValue().getUserName(), Matchers.is("new user name"));
    }

}