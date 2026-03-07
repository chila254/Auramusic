package com.auramusic.music.betterlyrics

import com.auramusic.music.betterlyrics.models.TTMLResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object BetterLyrics {
    // Logging capability - ArchiveTune approach
    var logger: ((String) -> Unit)? = null

    private val client by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        isLenient = true
                        ignoreUnknownKeys = true
                    },
                )
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 15000
                connectTimeoutMillis = 10000
                socketTimeoutMillis = 15000
            }

            defaultRequest {
                url("https://lyrics-api.boidu.dev")
                headers {
                    append("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    append("Accept", "application/json")
                }
            }

            expectSuccess = false
        }
    }

    private suspend fun fetchTTML(
        artist: String,
        title: String,
        duration: Int = -1,
        album: String? = null,
    ): String? = runCatching {
        val response = client.get("/getLyrics") {
            parameter("s", title)
            parameter("a", artist)
            if (duration > 0) {
                parameter("d", duration)
            }
            if (!album.isNullOrBlank()) {
                parameter("al", album)
            }
        }
        if (response.status == HttpStatusCode.OK) {
            response.body<TTMLResponse>().ttml
        } else {
            logger?.invoke("BetterLyrics: Request failed with status: ${response.status}")
            null
        }
    }.getOrNull()

    /**
     * Get raw TTML lyrics - ArchiveTune approach
     * Returns the raw TTML format for the UI to handle parsing
     */
    suspend fun getRawTTML(
        title: String,
        artist: String,
        duration: Int,
        album: String? = null,
    ): Result<String> = runCatching {
        logger?.invoke("BetterLyrics: Fetching raw TTML for '$title' by '$artist'")
        
        val cleanTitle = title.trim()
        val cleanArtist = artist.trim()
        val cleanAlbum = album?.trim()

        val ttml = fetchTTML(cleanArtist, cleanTitle, duration, cleanAlbum)
            ?: throw IllegalStateException("Lyrics unavailable")
        
        logger?.invoke("BetterLyrics: Received TTML (length: ${ttml.length})")
        ttml
    }

    suspend fun getLyrics(
        title: String,
        artist: String,
        duration: Int,
        album: String? = null,
    ) = runCatching {
        // Use exact title and artist - no normalization to ensure correct sync
        // Normalizing can return wrong lyrics (e.g., radio edit vs original)
        logger?.invoke("BetterLyrics: Fetching lyrics for '$title' by '$artist'")
        
        val ttml = fetchTTML(artist, title, duration, album)
            ?: throw IllegalStateException("Lyrics unavailable")
        
        logger?.invoke("BetterLyrics: Parsing TTML to LRC")
        val parsedLines = TTMLParser.parseTTML(ttml)
        if (parsedLines.isEmpty()) {
            throw IllegalStateException("Failed to parse lyrics")
        }
        
        TTMLParser.toLRC(parsedLines)
    }

    suspend fun getAllLyrics(
        title: String,
        artist: String,
        duration: Int,
        album: String? = null,
        callback: (String) -> Unit,
    ) {
        getLyrics(title, artist, duration, album)
            .onSuccess { lrcString ->
                logger?.invoke("BetterLyrics: Successfully fetched lyrics")
                callback(lrcString)
            }
            .onFailure { error ->
                logger?.invoke("BetterLyrics: Failed to fetch lyrics: ${error.message}")
            }
    }
}
