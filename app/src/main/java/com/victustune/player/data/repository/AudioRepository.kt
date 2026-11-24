package com.victustune.player.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.victustune.player.data.local.dao.FavoriteDao
import com.victustune.player.data.model.Album
import com.victustune.player.data.model.Artist
import com.victustune.player.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class AudioRepository(
    private val context: Context,
    private val favoriteDao: FavoriteDao
) {

    private val _songsFlow = MutableStateFlow<List<Song>>(emptyList())
    val songsFlow: StateFlow<List<Song>> = _songsFlow.asStateFlow()

    private val _albumsFlow = MutableStateFlow<List<Album>>(emptyList())
    val albumsFlow: StateFlow<List<Album>> = _albumsFlow.asStateFlow()

    private val _artistsFlow = MutableStateFlow<List<Artist>>(emptyList())
    val artistsFlow: StateFlow<List<Artist>> = _artistsFlow.asStateFlow()

    init {
        // Observe favorite changes and update song states
        favoriteDao.getAllFavoriteSongIds()
            .onEach { favIds ->
                val favSet = favIds.toSet()
                _songsFlow.update { list ->
                    list.map { it.copy(isFavorite = favSet.contains(it.id)) }
                }
            }
            .launchIn(kotlinx.coroutines.CoroutineScope(Dispatchers.IO))
    }

    suspend fun refreshMedia() = withContext(Dispatchers.IO) {
        val mediaStoreSongs = queryMediaStoreSongs()
        val songs = if (mediaStoreSongs.isNotEmpty()) {
            mediaStoreSongs
        } else {
            getDemoTracks()
        }

        val favIds = favoriteDao.getAllFavoriteSongIds().first().toSet()
        val mappedSongs = songs.map { it.copy(isFavorite = favIds.contains(it.id)) }
        _songsFlow.value = mappedSongs

        // Build Albums
        val albums = mappedSongs.groupBy { it.albumId to it.album }.map { (key, songList) ->
            val first = songList.first()
            Album(
                id = key.first,
                title = key.second,
                artist = first.artist,
                year = first.year,
                trackCount = songList.size,
                albumArtUri = first.albumArtUri
            )
        }.sortedBy { it.title.lowercase() }
        _albumsFlow.value = albums

        // Build Artists
        val artists = mappedSongs.groupBy { it.artist }.map { (artistName, songList) ->
            val albumCount = songList.map { it.album }.distinct().size
            Artist(
                id = artistName.hashCode().toLong(),
                name = artistName,
                trackCount = songList.size,
                albumCount = albumCount
            )
        }.sortedBy { it.name.lowercase() }
        _artistsFlow.value = artists
    }

    private fun queryMediaStoreSongs(): List<Song> {
        val songList = mutableListOf<Song>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.SIZE
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 10000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Unknown Track"
                    val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                    val album = cursor.getString(albumCol) ?: "Unknown Album"
                    val albumId = cursor.getLong(albumIdCol)
                    val durationMs = cursor.getLong(durationCol)
                    val trackNumber = cursor.getInt(trackCol)
                    val year = cursor.getInt(yearCol)
                    val dateAdded = cursor.getLong(dateAddedCol)
                    val sizeBytes = cursor.getLong(sizeCol)

                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val albumArtUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    )

                    songList.add(
                        Song(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            albumId = albumId,
                            durationMs = durationMs,
                            contentUri = contentUri,
                            albumArtUri = albumArtUri,
                            trackNumber = trackNumber,
                            year = year,
                            dateAdded = dateAdded,
                            sizeBytes = sizeBytes
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return songList
    }

    private fun getDemoTracks(): List<Song> {
        return listOf(
            Song(
                id = 9001L,
                title = "Midnight Odyssey",
                artist = "Solaris Resonance",
                album = "Echoes of the Void",
                albumId = 501L,
                durationMs = 214000L,
                contentUri = Uri.parse("https://commondatastorage.googleapis.com/codeskulptor-demos/DDR_assets/Kangaroo_MusiQue_-_The_Neverwritten_Role_Playing_Game.mp3"),
                albumArtUri = Uri.parse("https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80"),
                trackNumber = 1,
                year = 2024,
                dateAdded = System.currentTimeMillis() - 86400000L * 5,
                sizeBytes = 8500000L
            ),
            Song(
                id = 9002L,
                title = "Cybernetic Horizon",
                artist = "Neon Mirage",
                album = "Synthetic Dreams",
                albumId = 502L,
                durationMs = 188000L,
                contentUri = Uri.parse("https://commondatastorage.googleapis.com/codeskulptor-demos/DDR_assets/Sevish_-__nbsp_.mp3"),
                albumArtUri = Uri.parse("https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=600&auto=format&fit=crop&q=80"),
                trackNumber = 2,
                year = 2024,
                dateAdded = System.currentTimeMillis() - 86400000L * 4,
                sizeBytes = 7200000L
            ),
            Song(
                id = 9003L,
                title = "Subsurface Flow",
                artist = "Deep Diver",
                album = "Aquatic Echoes",
                albumId = 503L,
                durationMs = 245000L,
                contentUri = Uri.parse("https://commondatastorage.googleapis.com/codeskulptor-assets/Epoq-Lepidoptera.ogg"),
                albumArtUri = Uri.parse("https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80"),
                trackNumber = 3,
                year = 2023,
                dateAdded = System.currentTimeMillis() - 86400000L * 3,
                sizeBytes = 9400000L
            ),
            Song(
                id = 9004L,
                title = "Astral Drift",
                artist = "Solaris Resonance",
                album = "Echoes of the Void",
                albumId = 501L,
                durationMs = 196000L,
                contentUri = Uri.parse("https://actions.google.com/sounds/v1/ambient/rain_heavy.ogg"),
                albumArtUri = Uri.parse("https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80"),
                trackNumber = 2,
                year = 2024,
                dateAdded = System.currentTimeMillis() - 86400000L * 2,
                sizeBytes = 7800000L
            ),
            Song(
                id = 9005L,
                title = "Velocity Protocol",
                artist = "Victus Tune Project",
                album = "Quantum Frequency",
                albumId = 504L,
                durationMs = 172000L,
                contentUri = Uri.parse("https://actions.google.com/sounds/v1/weather/strong_wind.ogg"),
                albumArtUri = Uri.parse("https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80"),
                trackNumber = 1,
                year = 2024,
                dateAdded = System.currentTimeMillis() - 86400000L,
                sizeBytes = 6900000L
            )
        )
    }
}
