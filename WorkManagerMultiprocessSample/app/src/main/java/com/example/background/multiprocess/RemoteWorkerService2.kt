/*
 * Copyright 2021 The Android Open Source Project
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

package com.example.background.multiprocess

import androidx.work.multiprocess.RemoteCoroutineWorker
import androidx.work.multiprocess.RemoteListenableWorker
import androidx.work.multiprocess.RemoteListenableWorker.ARGUMENT_CLASS_NAME
import androidx.work.multiprocess.RemoteListenableWorker.ARGUMENT_PACKAGE_NAME
import androidx.work.multiprocess.RemoteWorkerService

/**
 * This class is to demonstrate tagging a worker with a different service in order to bind separate
 * workers to different Services.
 *
 * See [RemoteCoroutineWorker] and [RemoteListenableWorker] for more
 * information about how the arguments [ARGUMENT_PACKAGE_NAME] and [ARGUMENT_CLASS_NAME] are used
 * to determine the service that a Worker can bind to.
 */
class RemoteWorkerService2 : RemoteWorkerService()