/**
 * Auramusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.auramusic.app.lyrics

import android.content.Context
import com.auramusic.rush.RushLyrics
import com.auramusic.app.constants.EnableRushLyricsKey
import com.auramusic.app.utils.dataStore
import com.auramusic.app.utils.get

object RushLyricsProvider : LyricsProvider {
    override val name = "RushLyrics"

    override fun isEnabled(context: Context): Boolean = context.dataStore[EnableRushLyricsKey] ?: true

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        album: String?,
    ): Result<String> = RushLyrics.getLyrics(title, artist, duration, album)

    override suspend fun getAllLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        album: String?,
        callback: (String) -> Unit,
    ) {
        RushLyrics.getAllLyrics(title, artist, duration, album, callback)
    }
}
