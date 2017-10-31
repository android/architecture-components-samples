Room Migration Sample
======================

This is an API sample to showcase how to deal with database migrations using Room. From
[docs](https://developer.android.com/topic/libraries/architecture/room.html#db-migration):

> As you add and change features in your app, you need to modify your entity classes to reflect these changes. When a
user updates to the latest version of your app, you don't want them to lose all of their existing data, especially if
you can't recover the data from a remote server.

Room allows you to write [Migration](https://developer.android.com/reference/android/arch/persistence/room/migration/Migration.html)
classes to preserve user data in this manner. Each [Migration](https://developer.android.com/reference/android/arch/persistence/room/migration/Migration.html)
class specifies a startVersion and endVersion. At runtime, Room runs each [Migration](https://developer.android.com/reference/android/arch/persistence/room/migration/Migration.html)
class's [migrate()](https://developer.android.com/reference/android/arch/persistence/room/migration/Migration.html#migrate(android.arch.persistence.db.SupportSQLiteDatabase)
method, using the correct order to migrate the database to a later version.

Introduction
-------------

## Functionality
The sample app shows an editable user name, stored in the database.

## Implementation

The UI layer uses the Model-View-Presenter design pattern and works with a `UserRepository` class. The `UserRepository`  has a reference to the local repository to get and save the data. It ensures that all of these operations are done off the UI thread. The UI layer classes are common for all flavors.

## Usage
To showcase different implementations of the data layer product, flavors are used:

* `sqlite` - Uses SQLiteOpenHelper and traditional SQLite interfaces. Database version is 1
* `room` - Replaces implementation with Room and provides migrations. Database version is 2.
* `room2` - Adds a new column to the table and provides migration Database version is 3.
* `room3` - Changes the type of the table's primary key from `int` to `String` and provides migration. Database version is 4.

## Building

Use the Build Variants window in Android Studio to choose which version of the app you want to install, or alternatively choose one of the following tasks from the command line:

```
$ ./gradlew installSqliteDebug
$ ./gradlew installRoomDebug
$ ./gradlew installRoom2Debug
$ ./gradlew installRoom3Debug
```

## Testing

The project uses both instrumentation tests that run on the device and local unit tests that run on your computer.

### Device Tests

#### Database Tests

For the `sqlite` flavor the project is using the application database to test the functionality of `LocalUserDataSource` class.
An in-memory database is used for `room` flavors `UserDao` and `LocalUserDataSource` tests, but still they are run on the device.
An on-device database is used for the migration tests in all `room` flavors.

### Local Unit Tests

#### Presenter Tests

The `UserPresenter` is tested using local unit tests with mocked Repository implementation.

#### Repository Tests

The `UserRepository` is tested using local unit tests with mocked `UserDataSource` and instant execution.

License
--------

Copyright 2017 The Android Open Source Project, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
