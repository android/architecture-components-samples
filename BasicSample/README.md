Android Architecture Components Basic Sample
===================================

This sample showcases the following Architecture Components:

* [Room](https://developer.android.com/topic/libraries/architecture/room.html)
* [ViewModels](https://developer.android.com/reference/android/arch/lifecycle/ViewModel.html)
* [LiveData](https://developer.android.com/reference/android/arch/lifecycle/LiveData.html)

## Features

This sample contains two screens: a list of products and a detail view, that shows product reviews.

### Presentation layer

The presentation layer consists of the following components:
 * A main activity that handles navigation.
* A fragment to display the list of products.
* A fragment to display a product review.

The app uses a Model-View-ViewModel (MVVM) architecture for the presentation layer. Each of the fragments corresponds to a MVVM View. The View and ViewModel communicate  using LiveData and the following design principles:

* ViewModel objects don't have references to activities, fragments, or Android views. That would cause leaks on configuration changes, such as a screen rotation, because the system retains a ViewModel across the entire lifecycle of the corresponding view.



![ViewModel Diagram](docs/images/VM_diagram.png?raw=true "ViewModel Diagram")


* ViewModel objects expose data using `LiveData` objects. `LiveData` allows you to observe changes to data across multiple components of your app without creating explicit and rigid dependency paths between them.

* Views, including the fragments used in this sample, subscribe to corresponding `LiveData` objects. Because `LiveData` is lifecycle-aware, it doesn’t push changes to the underlying data if the observer is not in an active state, and this helps to avoid many common bugs. This is an example of a subscription:

```java
        // Update the list of products when the underlying data changes.
        viewModel.getProducts().observe(this, new Observer<List<ProductEntity>>() {
            @Override
            public void onChanged(@Nullable List<ProductEntity> myProducts) {
                if (myProducts != null) {
                    mBinding.setIsLoading(false);
                    mProductAdapter.setProductList(myProducts);
                } else {
                    mBinding.setIsLoading(true);
                }
            }
        });
```

### Data layer

The database is created using Room and it has two entities: a `ProductEntity` and a `CommentEntity` that generate corresponding SQLite tables at runtime.

Room populates the database asynchronously on first use. The `DatabaseCreator` class is responsible for creating the database and tables, and populating them with sample product and review data. This is done on the first use of the database, with the help of an `AsyncTask`. To simulate low-performance, an artificial delay is added. To let other components know when the data has finished populating, the `DatabaseCreator` exposes a `LiveData` object..

To access the data and execute queries, you use a [Data Access Object](https://developer.android.com/topic/libraries/architecture/room.html#daos) (DAO). For example, a product is loaded with the following query:

```
    @Query("select * from products where id = :productId")
    LiveData<ProductEntity> loadProduct(int productId);
```

Queries that return a `LiveData` object can be observed, so when  a change in one of the affected tables is detected, `LiveData` delivers a notification of that change to the registered observers.

### Transformations

Fragments don't observe the database directly, they only interact with ViewModel objects. A ViewModel observes database queries as well as the `DatabaseCreator`, which exposes whether the database is created or not.

For the purpose of the sample, the database is deleted and re-populated each time the app is started, so the app needs to wait until this process is finished. This is solved with a **Transformation**:

```java
        mObservableProducts = Transformations.switchMap(databaseCreated,
                new Function<Boolean, LiveData<List<ProductEntity>>>() {
            @Override
            public LiveData<List<ProductEntity>> apply(Boolean isDbCreated) {
                if (!isDbCreated) {
                    return ABSENT;
                } else {
                    return databaseCreator.getDatabase().productDao().loadAllProducts();
                }
            }
        });
```

Whenever `databaseCreated` changes, `mObservableProducts` will get a new value, either an `ABSENT` `LiveData` or the list of products. The database will be observed with the same scope as `mObservableProducts`.

Note that the first time a LiveData object is observed, the current value is emitted and `onChanged` is called.

The following diagram shows the general structure of the sample:


![ViewModel Subscriptions diagram](docs/images/VM_subscriptions.png?raw=true "ViewModel Subscriptions diagram")

Exercise for the reader: try to apply a transformation to the list of products in the ViewModel
before they are delivered to the fragment. (hint: `Transformations.Map`).

## License
-------

Copyright 2015 The Android Open Source Project, Inc.

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



