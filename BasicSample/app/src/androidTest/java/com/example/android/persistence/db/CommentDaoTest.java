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

package com.example.android.persistence.db;

import static com.example.android.persistence.db.TestData.COMMENTS;
import static com.example.android.persistence.db.TestData.COMMENT_ENTITY;
import static com.example.android.persistence.db.TestData.PRODUCTS;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.persistence.room.Room;
import android.database.sqlite.SQLiteConstraintException;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.example.android.persistence.LiveDataTestUtil;
import com.example.android.persistence.db.dao.CommentDao;
import com.example.android.persistence.db.dao.ProductDao;
import com.example.android.persistence.db.entity.CommentEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Test the implementation of {@link CommentDao}
 */
@RunWith(AndroidJUnit4.class)
public class CommentDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AppDatabase mDatabase;

    private CommentDao mCommentDao;

    private ProductDao mProductDao;

    @Before
    public void initDb() throws Exception {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                AppDatabase.class)
                // allowing main thread queries, just for testing
                .allowMainThreadQueries()
                .build();

        mCommentDao = mDatabase.commentDao();
        mProductDao = mDatabase.productDao();
    }

    @After
    public void closeDb() throws Exception {
        mDatabase.close();
    }

    @Test
    public void getCommentsWhenNoCommentInserted() throws InterruptedException {
        List<CommentEntity> comments = LiveDataTestUtil.getValue(mCommentDao.loadComments
                (COMMENT_ENTITY.getProductId()));

        assertTrue(comments.isEmpty());
    }

    @Test
    public void cantInsertCommentWithoutProduct() throws InterruptedException {
        try {
            mCommentDao.insertAll(COMMENTS);
            fail("SQLiteConstraintException expected");
        } catch (SQLiteConstraintException ignored) {

        }
    }

    @Test
    public void getCommentsAfterInserted() throws InterruptedException {
        mProductDao.insertAll(PRODUCTS);
        mCommentDao.insertAll(COMMENTS);

        List<CommentEntity> comments = LiveDataTestUtil.getValue(mCommentDao.loadComments
                (COMMENT_ENTITY.getProductId()));

        assertThat(comments.size(), is(1));
    }

    @Test
    public void getCommentByProductId() throws InterruptedException {
        mProductDao.insertAll(PRODUCTS);
        mCommentDao.insertAll(COMMENTS);

        List<CommentEntity> comments = LiveDataTestUtil.getValue(mCommentDao.loadComments(
                (COMMENT_ENTITY.getProductId())));

        assertThat(comments.size(), is(1));
    }

}
