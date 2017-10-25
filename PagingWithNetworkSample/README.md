# Paging With Network Sample

This sample demonstrates how to use the Paging library with a backend API (in this
case [Reddit API][8]).

There are 3 variations of the demo, which you can select in the `MainActivity` class.

After selecting an option, it starts the `RedditActivity` which is the activity that
shows the list of posts in a given subreddit.

## Paging With Database And Network
This sample, implemented in the [DbRedditPostRepository][1] class, demonstrates how to set up
a Repository that will use the local database to page in data for the UI and also back-fill
the database from the network as the user reaches to the end of the data in the database.

It uses `Room` to create the `DataSource.Factory` ([dao][3]) and the Paging Boundary Callback
API to get notified when the Paging library consumes the available local data ([boundary callback
implementation][4])

This usually provides the best user experience as the cached content is always available
on the device and the user will still have a good experience even if the network is slow /
unavailable.

## Paging Using Item Keys
This sample, implemented in the [InMemoryByItemRepository][2] class, demonstrates how to
set up a Repository that will directly page in from the network and will use the `key` from
the previous item to find the request parameters for the next page.

[ItemKeyedSubredditDataSource][5]: The data source that uses the `key` in items
(`name` in Reddit API) to find the next page. It extends from the `ItemKeyedDataSource` class
in the Paging Library.

## Paging Using Next Tokens From The Previous Query
This sample, implemented in the [InMemoryByPageKeyRepository][6] class, demonstrates how to
utilize the `before` and `after` keys in the response to discover the next page. (This is
the intended use of the Reddit API but this sample still provides
[ItemKeyedSubredditDataSource][5] to serve as an example if the backend does not provide
before/after links)

[PageKeyedSubredditDataSource][7]: The data source that uses the `after` and `before` fields
in the API request response. It extends from the `PageKeyedDataSource` class
in the Paging Library.


### Libraries
* [Android Support Library][support-lib]
* [Android Architecture Components][arch]
* [Retrofit][retrofit] for REST api communication
* [Glide][glide] for image loading
* [espresso][espresso] for UI tests
* [mockito][mockito] for mocking in tests
* [Retrofit Mock][retrofit-mock] for creating a fake API implementation for tests

[1]: app/src/main/java/com/android/example/paging/pagingwithnetwork/reddit/repository/inDb/DbRedditPostRepository.kt
[2]: app/src/main/java/com/android/example/paging/pagingwithnetwork/reddit/repository/inMemory/byItem/InMemoryByItemRepository.kt
[3]: app/src/main/java/com/android/example/paging/pagingwithnetwork/reddit/db/RedditPostDao.kt
[4]: app/src/main/java/com/android/example/paging/pagingwithnetwork/reddit/repository/inDb/SubredditBoundaryCallback.kt
[5]: app/src/main/java/com/android/example/paging/pagingwithnetwork/reddit/repository/inMemory/byItem/ItemKeyedSubredditDataSource.kt
[6]: app/src/main/java/com/android/example/paging/pagingwithnetwork/reddit/repository/inMemory/byPage/InMemoryByPageKeyRepository.kt
[7]: app/src/main/java/com/android/example/paging/pagingwithnetwork/reddit/repository/inMemory/byPage/PageKeyedSubredditDataSource.kt
[8]: https://www.reddit.com/dev/api/#listings
[mockwebserver]: https://github.com/square/okhttp/tree/master/mockwebserver
[support-lib]: https://developer.android.com/topic/libraries/support-library/index.html
[arch]: https://developer.android.com/arch
[espresso]: https://google.github.io/android-testing-support-library/docs/espresso/
[retrofit]: http://square.github.io/retrofit
[glide]: https://github.com/bumptech/glide
[mockito]: http://site.mockito.org
[retrofit-mock]: https://github.com/square/retrofit/tree/master/retrofit-mock