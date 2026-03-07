/**
 * Auramusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.auramusic.app.ui.screens.wrapped

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object WrappedConstants {
    // Get current year for display
    val currentYear: Int
        get() = LocalDate.now().year
    
    // Keep YEAR for backward compatibility with WrappedManager
    const val YEAR = 2025
    
    // Get previous month for wrapped data
    val previousMonth: LocalDate
        get() = LocalDate.now().minusMonths(1)
    
    // Format for display: "March 2026"
    val displayMonthYear: String
        get() {
            val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
            return previousMonth.format(formatter)
        }
    
    // Format for short display: "Mar 2026"  
    val shortDisplayMonthYear: String
        get() {
            val formatter = DateTimeFormatter.ofPattern("MMM yyyy")
            return previousMonth.format(formatter)
        }
    
    // Playlist name with AuraMusic branding
    val PLAYLIST_NAME: String
        get() = "AuraMusic $displayMonthYear"
}
