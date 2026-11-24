package com.victustune.player.playback.cache

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@OptIn(UnstableApi::class)
object MediaCacheManager {
    @Volatile
    private var instance: SimpleCache? = null
    private val lock = Any()

    fun getInstance(context: Context, maxCacheSizeMb: Int = 512): SimpleCache {
        return instance ?: synchronized(lock) {
            instance ?: run {
                val cacheDir = File(context.cacheDir, "media_cache")
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }
                val evictor = LeastRecentlyUsedCacheEvictor(maxCacheSizeMb.toLong() * 1024L * 1024L)
                val databaseProvider = StandaloneDatabaseProvider(context)
                SimpleCache(cacheDir, evictor, databaseProvider).also {
                    instance = it
                }
            }
        }
    }

    fun getCacheSizeBytes(): Long {
        return instance?.cacheSpace ?: 0L
    }

    fun clearCache() {
        instance?.let { cache ->
            try {
                cache.keys.forEach { key ->
                    cache.removeResource(key)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
