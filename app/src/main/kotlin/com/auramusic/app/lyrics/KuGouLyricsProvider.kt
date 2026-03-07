/**
 * Auramusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.auramusic.app.lyrics

import android.content.Context
import com.auramusic.kugou.KuGou
import com.auramusic.app.constants.EnableKugouKey
import com.auramusic.app.utils.dataStore
import com.auramusic.app.utils.get

object KuGouLyricsProvider : LyricsProvider {
    override val name = "Kugou"
    override fun isEnabled(context: Context): Boolean =
        context.dataStore[EnableKugouKey] ?: true

    /**
     * Get lyrics - ArchiveTune simplified approach (without album parameter)
     */
    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        album: String?,
    ): Result<String> =
        // Use simplified approach matching ArchiveTune
        KuGou.getLyrics(title, artist, duration)

    /**
     * Get all lyrics options - ArchiveTune simplified approach
     */
    override suspend fun getAllLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        album: String?,
        callback: (String) -> Unit,
    ) {
        // Use simplified approach matching ArchiveTune
        KuGou.getAllPossibleLyricsOptions(title, artist, duration, callback)
    }
}
