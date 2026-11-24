package com.victustune.player.data.repository

import com.victustune.player.data.local.dao.FavoriteDao
import com.victustune.player.data.local.dao.HistoryDao
import com.victustune.player.data.local.dao.PlaylistDao
import com.victustune.player.data.local.entity.FavoriteEntity
import com.victustune.player.data.local.entity.HistoryEntity
import com.victustune.player.data.local.entity.PlaylistEntity
import com.victustune.player.data.local.entity.PlaylistSongCrossRef
import com.victustune.player.data.model.Playlist
import com.victustune.player.data.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class PlaylistRepository(
    private val playlistDao: PlaylistDao,
    private val favoriteDao: FavoriteDao,
    private val historyDao: HistoryDao,
    private val audioRepository: AudioRepository
) {

    fun getPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().combine(audioRepository.songsFlow) { entities, _ ->
            entities.map { entity ->
                // Note: could query song count
                Playlist(
                    id = entity.id,
                    name = entity.name,
                    createdAt = entity.createdAt,
                    trackCount = 0
                )
            }
        }
    }

    suspend fun createPlaylist(name: String): Long {
        return playlistDao.insertPlaylist(PlaylistEntity(name = name))
    }

    suspend fun renamePlaylist(id: Long, newName: String) {
        playlistDao.renamePlaylist(id, newName)
    }

    suspend fun deletePlaylist(id: Long) {
        playlistDao.clearPlaylistSongs(id)
        playlistDao.deletePlaylistById(id)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        val currentIds = playlistDao.getSongIdsForPlaylist(playlistId).first()
        playlistDao.addSongToPlaylist(
            PlaylistSongCrossRef(
                playlistId = playlistId,
                songId = songId,
                orderIndex = currentIds.size
            )
        )
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>> {
        return playlistDao.getSongIdsForPlaylist(playlistId).combine(audioRepository.songsFlow) { ids, songs ->
            val songMap = songs.associateBy { it.id }
            ids.mapNotNull { songMap[it] }
        }
    }

    fun getFavoriteSongs(): Flow<List<Song>> {
        return favoriteDao.getAllFavoriteSongIds().combine(audioRepository.songsFlow) { ids, songs ->
            val songMap = songs.associateBy { it.id }
            ids.mapNotNull { songMap[it] }
        }
    }

    suspend fun toggleFavorite(songId: Long) {
        val isFav = favoriteDao.isFavorite(songId).first()
        if (isFav) {
            favoriteDao.removeFavorite(songId)
        } else {
            favoriteDao.addFavorite(FavoriteEntity(songId))
        }
    }

    suspend fun recordHistory(songId: Long) {
        historyDao.insertHistory(HistoryEntity(songId = songId))
    }

    fun getRecentSongs(): Flow<List<Song>> {
        return historyDao.getRecentSongIds(50).combine(audioRepository.songsFlow) { ids, songs ->
            val songMap = songs.associateBy { it.id }
            ids.mapNotNull { songMap[it] }.distinctBy { it.id }
        }
    }
}
