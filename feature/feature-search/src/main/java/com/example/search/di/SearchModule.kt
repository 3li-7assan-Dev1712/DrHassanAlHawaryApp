package com.example.search.di


import com.algolia.instantsearch.searcher.hits.HitsSearcher
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.example.hassanalhawary.feature.search.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object SearchModule {


    @Provides
    @Singleton
    fun provideHitsSearcher(
    ): HitsSearcher {
        return HitsSearcher(
            applicationID = ApplicationID(BuildConfig.ALGOLIA_APP_ID),
            apiKey = APIKey(BuildConfig.ALGOLIA_API_KEY),
            indexName = IndexName("content_search")
        )
    }


}