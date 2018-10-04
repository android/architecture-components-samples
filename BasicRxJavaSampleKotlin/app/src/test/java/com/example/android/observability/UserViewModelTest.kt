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

package com.example.android.observability

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.android.observability.persistence.User
import com.example.android.observability.persistence.UserDao
import com.example.android.observability.ui.UserViewModel
import io.reactivex.Flowable
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

/**
 * Unit test for [UserViewModel]
 */
class UserViewModelTest {

    @get:Rule var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock private lateinit var dataSource: UserDao

    @Captor private lateinit var userArgumentCaptor: ArgumentCaptor<User>

    private lateinit var viewModel: UserViewModel

    @Before fun setUp() {
        MockitoAnnotations.initMocks(this)

        viewModel = UserViewModel(dataSource)
    }

    @Test fun getUserName_whenNoUserSaved() {
        // Given that the UserDataSource returns an empty list of users
        `when`(dataSource.getUserById(UserViewModel.USER_ID)).thenReturn(Flowable.empty<User>())

        //When getting the user name
        viewModel.userName()
                .test()
                // The user name is empty
                .assertNoValues()
    }

    @Test fun getUserName_whenUserSaved() {
        // Given that the UserDataSource returns a user
        val user = User(userName = "user name")
        `when`(dataSource.getUserById(UserViewModel.USER_ID)).thenReturn(Flowable.just(user))

        //When getting the user name
        viewModel.userName()
                .test()
                // The correct user name is emitted
                .assertValue("user name")
    }

    @Test fun updateUserName_updatesNameInDataSource() {
        // When updating the user name
        viewModel.updateUserName("new user name")
                .test()
                .assertComplete()

        // The user name is updated in the data source
        // using ?: User("someUser") because otherwise, we get
        // "IllegalStateException: userArgumentCaptor.capture() must not be null"
        verify<UserDao>(dataSource).insertUser(capture(userArgumentCaptor))
        assertThat(userArgumentCaptor.value.userName, Matchers.`is`("new user name"))
    }

}
