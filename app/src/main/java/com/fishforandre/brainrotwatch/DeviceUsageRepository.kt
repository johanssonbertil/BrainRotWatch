package com.fishforandre.brainrotwatch

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Process
import androidx.compose.ui.graphics.Color
import com.fishforandre.brainrotwatch.domain.engine.TrendCalculator
import com.fishforandre.brainrotwatch.domain.model.AppCategory
import com.fishforandre.brainrotwatch.domain.model.MasterCatalog
import com.fishforandre.brainrotwatch.domain.model.SyncIntegrityState
import com.fishforandre.brainrotwatch.domain.model.UsageRecord
import kotlin.math.max

object DeviceUsageRepository {

    /**
     * Map of known package names to clean, properly capitalized human-friendly app labels.
     */
    private val KNOWN_APP_NAMES: Map<String, String> = mapOf(
        "com.zhiliaoapp.musically" to "TikTok",
        "com.ss.android.ugc.trill" to "TikTok",
        "com.instagram.android" to "Instagram",
        "com.google.android.youtube" to "YouTube",
        "com.google.android.apps.youtube.kids" to "YouTube Kids",
        "com.snapchat.android" to "Snapchat",
        "com.twitter.android" to "X (Twitter)",
        "com.facebook.katana" to "Facebook",
        "com.facebook.orca" to "Messenger",
        "com.reddit.frontpage" to "Reddit",
        "com.pinterest" to "Pinterest",
        "com.instagram.barcelona" to "Threads",
        "com.tumblr" to "Tumblr",
        "com.bereal.ft" to "BeReal",
        "tv.twitch.android.app" to "Twitch",
        "com.kick.app" to "Kick",
        "com.discord" to "Discord",
        "com.linkedin.android" to "LinkedIn",
        "com.whatsapp" to "WhatsApp",
        "org.telegram.messenger" to "Telegram",
        "com.netflix.mediaclient" to "Netflix",
        "com.amazon.avod.thirdpartyclient" to "Prime Video",
        "com.disney.disneyplus" to "Disney+",
        "com.hulu.plus" to "Hulu",
        "com.wbd.stream" to "Max",
        "com.hbo.hbonow" to "HBO",
        "com.crunchyroll.crunchyroid" to "Crunchyroll",
        "com.peacocktv.peacockandroid" to "Peacock",
        "com.cbs.app" to "Paramount+",
        "com.apple.atve.androidtv.appletv" to "Apple TV",
        "tv.pluto.android" to "Pluto TV",
        "com.tubitv" to "Tubi",
        "com.vimeo.android" to "Vimeo",
        "com.spotify.music" to "Spotify",
        "com.google.android.apps.youtube.music" to "YouTube Music",
        "com.soundcloud.android" to "SoundCloud",
        "com.audible.application" to "Audible",
        "com.supercell.clashroyale" to "Clash Royale",
        "com.supercell.brawlstars" to "Brawl Stars",
        "com.roblox.client" to "Roblox",
        "com.android.chrome" to "Chrome",
        "org.mozilla.firefox" to "Firefox",
        "com.microsoft.emmx" to "Edge",
        "com.brave.browser" to "Brave",
        "com.sec.android.app.sbrowser" to "Samsung Internet",
        "com.google.android.apps.magazines" to "Google News",
        "bbc.mobile.news.ww" to "BBC News",
        "com.cnn.mobile.android.phone" to "CNN",
        "uk.co.dailymail.android" to "Daily Mail",
        "com.amazon.mShop.android.shopping" to "Amazon",
        "com.zzkko" to "Shein",
        "com.einnovation.temu" to "Temu",
        "com.ebay.mobile" to "eBay"
    )

    /**
     * System framework packages that must be explicitly excluded from tracking.
     */
    private val EXCLUDED_SYSTEM_PACKAGES: Set<String> = setOf(
        "android",
        "com.android.systemui",
        "com.android.settings",
        "com.google.android.permissioncontroller",
        "com.google.android.inputmethod.latin",
        "com.google.android.gms",
        "com.google.android.googlequicksearchbox"
    )

    /**
     * Curated registry of attention-grabbing / social & video streaming applications.
     */
    private val TARGET_ATTENTION_PACKAGES: Set<String> = KNOWN_APP_NAMES.keys

    fun hasUsageAccess(context: Context): Boolean {
        val appOpsManager = context.getSystemService(AppOpsManager::class.java) ?: return false
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOpsManager.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Checks if active audio is currently playing through headphones or speakers.
     */
    fun isAudioCurrentlyPlaying(context: Context): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return false
        return audioManager.isMusicActive
    }

    private fun isAttentionApp(packageManager: PackageManager, packageName: String, selfPackage: String): Boolean {
        if (packageName == selfPackage || EXCLUDED_SYSTEM_PACKAGES.contains(packageName)) return false

        // 1. Direct match with curated attention app list
        if (TARGET_ATTENTION_PACKAGES.contains(packageName)) return true

        // 2. Android System Category fallback (Social, Video, Audio, Games, News)
        try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                when (appInfo.category) {
                    ApplicationInfo.CATEGORY_SOCIAL,
                    ApplicationInfo.CATEGORY_VIDEO,
                    ApplicationInfo.CATEGORY_AUDIO,
                    ApplicationInfo.CATEGORY_GAME,
                    ApplicationInfo.CATEGORY_NEWS -> return true
                }
            }
        } catch (_: Exception) {}

        return false
    }

    fun loadTodayUserScreenTime(
        context: Context,
        displayName: String,
        initials: String,
        fallbackSummary: String,
        avatarUrl: String? = null,
        friendCode: String = "",
        deviceCount: Int = 1
    ): UserScreenTime {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val packageManager = context.packageManager
        val now = System.currentTimeMillis()
        val todayStart = startOfDay(now)
        val yesterdayStart = todayStart - DAY_IN_MILLIS

        // Calculate interactive (screen-ON only) screen time for today and yesterday
        val todayAppsMap = calculateInteractiveScreenTime(usageStatsManager, packageManager, todayStart, now, context.packageName)
        val yesterdayAppsMap = calculateInteractiveScreenTime(usageStatsManager, packageManager, yesterdayStart, todayStart, context.packageName)

        // Check baseline device activity (Touch Grass Floor: did the user unlock/use their phone at all today?)
        val hasBaselineDeviceActivity = checkMinimumActivityFloor(usageStatsManager, todayStart, now)

        val rawRecords = todayAppsMap.entries
            .map { (pkgAndLabel, millis) ->
                val (pkgName, cleanLabel) = pkgAndLabel
                val screenMins = (millis / 60000L).toInt()
                val category = MasterCatalog.inferCategory(cleanLabel)
                val estimatedAudioMins = if (category == AppCategory.AUDIO_STREAMING) {
                    max(screenMins, (screenMins * 1.5).toInt())
                } else {
                    0
                }
                UsageRecord(
                    appId = pkgName,
                    appName = cleanLabel,
                    category = category,
                    screenTimeMinutes = screenMins,
                    audioTimeMinutes = estimatedAudioMins,
                    lastSyncedEpochMillis = now,
                    connectedDeviceCount = deviceCount
                )
            }
            .filter { it.screenTimeMinutes > 0 || it.audioTimeMinutes > 0 }
            .sortedByDescending { it.screenTimeMinutes }

        val totalMinutes = rawRecords.sumOf { it.screenTimeMinutes }
        val yesterdayTotalMinutes = (yesterdayAppsMap.values.sumOf { it } / 60000L).toInt()
        val deltaMinutes = totalMinutes - yesterdayTotalMinutes
        val topMinutes = max(1, rawRecords.firstOrNull()?.screenTimeMinutes ?: 1)

        // Compute 7-day rolling history for TrendCalculator
        val last7Days = (1..7).map { daysAgo ->
            val dayStart = todayStart - (daysAgo * DAY_IN_MILLIS)
            val dayEnd = dayStart + DAY_IN_MILLIS
            val map = calculateFallbackUsageStats(usageStatsManager, packageManager, dayStart, dayEnd, context.packageName)
            (map.values.sumOf { it } / 60000L).toInt()
        }.filter { it > 0 }

        val trendBadge = TrendCalculator.calculateTrend(
            todayMinutes = totalMinutes,
            yesterdayMinutes = yesterdayTotalMinutes,
            last7DaysMinutes = last7Days
        )

        val apps = rawRecords.take(8).map { record ->
            val colors = colorPalette[record.appName.length % colorPalette.size]
            AppUsage(
                appId = record.appId,
                name = record.appName,
                category = record.category,
                minutes = record.screenTimeMinutes,
                audioMinutes = record.audioTimeMinutes,
                fillFraction = record.screenTimeMinutes.toFloat() / topMinutes.toFloat(),
                accentColor = colors.first,
                trackColor = colors.second
            )
        }

        val syncState = if (hasUsageAccess(context)) {
            SyncIntegrityState.Synced(lastSyncMillis = now, deviceCount = deviceCount)
        } else {
            SyncIntegrityState.WarningPending(failedSinceMillis = now)
        }

        return UserScreenTime(
            userId = "current_user",
            displayName = displayName,
            initials = initials,
            avatarUrl = avatarUrl,
            friendCode = friendCode,
            deviceCount = deviceCount,
            totalMinutes = totalMinutes,
            deltaMinutes = deltaMinutes,
            yesterdayMinutes = yesterdayTotalMinutes,
            last7DaysMinutes = last7Days,
            rolling30DayAvgMinutes = if (last7Days.isNotEmpty()) last7Days.average().toInt() else max(totalMinutes, 90),
            apps = apps,
            rawRecords = rawRecords,
            summary = if (totalMinutes > 0) {
                buildSummary(totalMinutes, rawRecords)
            } else {
                fallbackSummary
            },
            meetsMinimumActivityFloor = hasBaselineDeviceActivity,
            syncState = syncState,
            trendBadge = trendBadge
        )
    }

    private fun checkMinimumActivityFloor(
        usageStatsManager: UsageStatsManager,
        startTimeMillis: Long,
        endTimeMillis: Long
    ): Boolean {
        return try {
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTimeMillis,
                endTimeMillis
            ).orEmpty()
            stats.any { it.totalTimeInForeground > 30_000L }
        } catch (_: Exception) {
            true
        }
    }

    /**
     * Process UsageEvents to strictly measure foreground time ONLY when the screen is interactive (ON).
     * Time accumulated when screen is OFF/closed is explicitly ignored.
     */
    private fun calculateInteractiveScreenTime(
        usageStatsManager: UsageStatsManager,
        packageManager: PackageManager,
        startTimeMillis: Long,
        endTimeMillis: Long,
        selfPackage: String
    ): Map<Pair<String, String>, Long> {
        val packageTimeMillis = mutableMapOf<String, Long>()
        try {
            val events = usageStatsManager.queryEvents(startTimeMillis, endTimeMillis)
            val event = UsageEvents.Event()

            // Initialize as false so overnight screen-off time does NOT accumulate
            var isScreenOn = false
            var currentForegroundPackage: String? = null
            var lastEventTime = startTimeMillis
            var eventCount = 0

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                eventCount++
                val eventTime = event.timeStamp
                val pkg = event.packageName

                val currentPkg = currentForegroundPackage
                if (isScreenOn && currentPkg != null) {
                    val duration = eventTime - lastEventTime
                    if (duration > 0) {
                        packageTimeMillis[currentPkg] = (packageTimeMillis[currentPkg] ?: 0L) + duration
                    }
                }

                lastEventTime = eventTime

                when (event.eventType) {
                    UsageEvents.Event.SCREEN_INTERACTIVE,
                    UsageEvents.Event.USER_INTERACTION -> {
                        isScreenOn = true
                    }
                    UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                        isScreenOn = false
                    }
                    UsageEvents.Event.ACTIVITY_RESUMED -> {
                        if (isAttentionApp(packageManager, pkg, selfPackage)) {
                            currentForegroundPackage = pkg
                        }
                    }
                    UsageEvents.Event.ACTIVITY_PAUSED -> {
                        if (currentForegroundPackage == pkg) {
                            currentForegroundPackage = null
                        }
                    }
                }
            }

            val finalPkg = currentForegroundPackage
            if (isScreenOn && finalPkg != null) {
                val duration = endTimeMillis - lastEventTime
                if (duration > 0) {
                    packageTimeMillis[finalPkg] = (packageTimeMillis[finalPkg] ?: 0L) + duration
                }
            }

            // Fallback to UsageStats if queryEvents had no data
            if (eventCount == 0 || packageTimeMillis.isEmpty()) {
                return calculateFallbackUsageStats(usageStatsManager, packageManager, startTimeMillis, endTimeMillis, selfPackage)
            }
        } catch (_: Exception) {
            return calculateFallbackUsageStats(usageStatsManager, packageManager, startTimeMillis, endTimeMillis, selfPackage)
        }

        val labelMap = mutableMapOf<Pair<String, String>, Long>()
        for ((pkg, millis) in packageTimeMillis) {
            val label = getCleanAppLabel(packageManager, pkg)
            val key = pkg to label
            labelMap[key] = (labelMap[key] ?: 0L) + millis
        }

        return labelMap
    }

    private fun calculateFallbackUsageStats(
        usageStatsManager: UsageStatsManager,
        packageManager: PackageManager,
        startTimeMillis: Long,
        endTimeMillis: Long,
        selfPackage: String
    ): Map<Pair<String, String>, Long> {
        val result = mutableMapOf<Pair<String, String>, Long>()
        val statsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTimeMillis,
            endTimeMillis
        ).orEmpty()

        for (stat in statsList) {
            val millis = stat.totalTimeInForeground
            if (millis > 0L && isAttentionApp(packageManager, stat.packageName, selfPackage)) {
                val label = getCleanAppLabel(packageManager, stat.packageName)
                val key = stat.packageName to label
                result[key] = (result[key] ?: 0L) + millis
            }
        }
        return result
    }

    private fun getCleanAppLabel(packageManager: PackageManager, packageName: String): String {
        KNOWN_APP_NAMES[packageName]?.let { return it }

        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val label = packageManager.getApplicationLabel(appInfo).toString().trim()
            if (label.isNotBlank() && label != packageName) {
                label.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
            } else {
                packageName.substringAfterLast('.').replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
            }
        } catch (_: Exception) {
            packageName.substringAfterLast('.').replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
        }
    }

    private fun buildSummary(totalMinutes: Int, records: List<UsageRecord>): String {
        val topThree = records.take(3)
            .joinToString(separator = ", ") { record -> "${record.screenTimeMinutes} min on ${record.appName}" }
        return "$totalMinutes min of screen time today: $topThree."
    }

    private fun startOfDay(timeMillis: Long): Long {
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = timeMillis
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private val colorPalette: List<Pair<Color, Color>> = listOf(
        Color(0xFF6460FF) to Color(0xFFD9E7F5),
        Color(0xFF23C8D7) to Color(0xFFD9E7F5),
        Color(0xFFFF6D84) to Color(0xFFD9E7F5),
        Color(0xFF9B6BFF) to Color(0xFFE8DBFF),
        Color(0xFF2FC37F) to Color(0xFFDFF6EA),
        Color(0xFFFFA24B) to Color(0xFFFFE7CC)
    )

    private const val DAY_IN_MILLIS = 24L * 60L * 60L * 1000L
}
