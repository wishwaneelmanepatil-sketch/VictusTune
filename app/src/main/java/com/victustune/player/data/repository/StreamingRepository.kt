package com.victustune.player.data.repository

import com.victustune.player.data.local.dao.SearchHistoryDao
import com.victustune.player.data.local.entity.SearchHistoryEntity
import com.victustune.player.data.model.*
import com.victustune.player.data.remote.extractor.StreamResolver
import com.victustune.player.data.remote.innertube.InnerTubeClient
import com.victustune.player.data.remote.innertube.SearchFilter
import kotlinx.coroutines.flow.Flow

class StreamingRepository(
    val innerTubeClient: InnerTubeClient,
    val streamResolver: StreamResolver,
    private val searchHistoryDao: SearchHistoryDao
) {

    val recentSearches: Flow<List<String>> = searchHistoryDao.getRecentSearches()

    suspend fun saveSearchQuery(query: String) {
        if (query.isNotBlank()) {
            searchHistoryDao.insertSearch(SearchHistoryEntity(query.trim()))
        }
    }

    suspend fun deleteSearchQuery(query: String) {
        searchHistoryDao.deleteSearch(query)
    }

    suspend fun clearSearchHistory() {
        searchHistoryDao.clearSearchHistory()
    }

    suspend fun search(
        query: String,
        filter: SearchFilter = SearchFilter.ALL,
        continuation: String? = null
    ): SearchResult {
        return innerTubeClient.search(query, filter, continuation)
    }

    suspend fun getHomeFeed(): BrowseResult {
        return innerTubeClient.getHomeFeed()
    }

    suspend fun getExploreFeed(): BrowseResult {
        return innerTubeClient.browse("FEmusic_explore")
    }

    suspend fun browse(browseId: String, continuation: String? = null): BrowseResult {
        return innerTubeClient.browse(browseId, continuation)
    }

    suspend fun getNextQueue(videoId: String, playlistId: String? = null): List<RemoteTrack> {
        return innerTubeClient.next(videoId, playlistId)
    }

    suspend fun resolveStreamUrl(videoId: String, quality: AudioQuality = AudioQuality.AUTO): String {
        return streamResolver.resolveStreamUrl(videoId, quality)
    }

    fun invalidateStream(videoId: String) {
        streamResolver.invalidate(videoId)
    }
}
