# AuraMusic v1.0.4 (Build 5) Changelog

## New Features

### RushLyrics Integration
- Added new RushLyrics lyrics provider using the LyricsPlus API
- Multiple server mirrors for reliability
- Dual lyrics format support (LRC and TTML with word-by-word sync)
- Search functionality for finding lyrics
- Provider priority drag-to-reorder support

### UI/UX Improvements
- Fixed Discord Integration link to point to AuraMusic GitHub
- Fixed provider priority dialog to include newly added providers
- Fixed ContentSettings to properly display RushLyrics toggle

## Bug Fixes
- Fixed RushLyrics not appearing in provider priority settings
- Fixed missing RUSH_LYRICS case in preferred provider dialog
- Fixed LaunchedEffect dependency for provider priority dialog

## Technical Changes
- Created standalone TTML parser in rush module (no longer depends on BetterLyrics)
- Updated LyricsProviderRegistry to automatically add new providers to stored order

---

**Full Changelog**: https://github.com/chila254/Auramusic-v1/compare/v1.0.3...v1.0.4
