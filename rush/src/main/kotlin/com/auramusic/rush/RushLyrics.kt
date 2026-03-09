package com.auramusic.rush

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import com.auramusic.rush.TTMLParser

object RushLyrics {
    private val client by lazy {
        HttpClient(OkHttp) {
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

            expectSuccess = false
        }
    }

    private val servers = listOf(
        "https://lyricsplus.atomix.one",
        "https://lyricsplus-seven.vercel.app",
        "https://lyricsplus.prjktla.workers.dev",
        "https://lyrics-plus-backend.vercel.app",
        "https://youlyplus.binimum.org",
    )

    @Serializable
    private data class TTMLResponse(val ttml: String)

    @Serializable
    private data class LRCResponse(val lrc: String)

    @Serializable
    private data class SearchResponse(
        val results: List<SearchResult> = emptyList()
    )

    @Serializable
    data class SearchResult(
        val id: String = "",
        val title: String = "",
        val artist: String = "",
        val album: String? = null,
        val duration: Int? = null,
        val provider: String = ""
    )

    /**
     * Fetch TTML lyrics from LyricsPlus API
     */
    private suspend fun fetchTTML(
        title: String,
        artist: String,
    ): String? = runCatching {
        for (server in servers) {
            try {
                val response = client.get("$server/v1/ttml/get") {
                    parameter("title", title)
                    parameter("artist", artist)
                }
                if (response.status == HttpStatusCode.OK) {
                    return@runCatching response.body<TTMLResponse>().ttml
                }
            } catch (e: Exception) {
                // Try next server
                continue
            }
        }
        null
    }.getOrNull()

    /**
     * Fetch LRC lyrics directly from LyricsPlus API
     */
    private suspend fun fetchLRC(
        title: String,
        artist: String,
    ): String? = runCatching {
        for (server in servers) {
            try {
                val response = client.get("$server/v1/lrc/get") {
                    parameter("title", title)
                    parameter("artist", artist)
                }
                if (response.status == HttpStatusCode.OK) {
                    return@runCatching response.body<LRCResponse>().lrc
                }
            } catch (e: Exception) {
                // Try next server
                continue
            }
        }
        null
    }.getOrNull()

    /**
     * Search for lyrics using LyricsPlus API
     */
    suspend fun searchLyrics(
        title: String,
        artist: String,
    ): List<SearchResult> = runCatching {
        for (server in servers) {
            try {
                val response = client.get("$server/v1/search") {
                    parameter("title", title)
                    parameter("artist", artist)
                }
                if (response.status == HttpStatusCode.OK) {
                    return@runCatching response.body<SearchResponse>().results
                }
            } catch (e: Exception) {
                // Try next server
                continue
            }
        }
        emptyList()
    }.getOrDefault(emptyList())

    /**
     * Get lyrics - tries TTML first, then falls back to LRC
     */
    suspend fun getLyrics(
        title: String,
        artist: String,
        duration: Int = -1,
        album: String? = null,
    ) = runCatching {
        // Try LRC first (faster and more direct)
        val lrc = fetchLRC(title, artist)
        if (lrc != null && lrc.isNotBlank()) {
            return@runCatching lrc
        }

        // Fall back to TTML and convert to LRC
        val ttml = fetchTTML(title, artist)
            ?: throw IllegalStateException("Lyrics unavailable")
        
        val parsedLines = TTMLParser.parseTTML(ttml)
        if (parsedLines.isEmpty()) {
            throw IllegalStateException("Failed to parse lyrics")
        }
        
        TTMLParser.toLRC(parsedLines)
    }

    /**
     * Get all available lyrics (for search results)
     */
    suspend fun getAllLyrics(
        title: String,
        artist: String,
        duration: Int = -1,
        album: String? = null,
        callback: (String) -> Unit,
    ) {
        getLyrics(title, artist, duration, album)
            .onSuccess { lrcString ->
                callback(lrcString)
            }
    }
}
