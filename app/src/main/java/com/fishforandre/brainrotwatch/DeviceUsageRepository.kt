package com.fishforandre.brainrotwatch

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Process
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

    private fun isAttentionApp(packageManager: PackageManager, packageName: String, selfPackage: String): Boolean {
        if (packageName == selfPackage || EXCLUDED_SYSTEM_PACKAGES.contains(packageName)) return false

        // 1. Direct match with curated attention app list
        if (TARGET_ATTENTION_PACKAGES.contains(packageName)) return true

        // 2. Android System Category fallback (Social, Video, News)
        try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                when (appInfo.category) {
                    ApplicationInfo.CATEGORY_SOCIAL,
                    ApplicationInfo.CATEGORY_VIDEO,
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
    ): UserScreenTime {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val packageManager = context.packageManager
        val now = System.currentTimeMillis()
        val todayStart = startOfDay(now)
        val yesterdayStart = todayStart - DAY_IN_MILLIS

        // Calculate interactive (screen-ON only) screen time for today and yesterday
        val todayAppsMap = calculateInteractiveScreenTime(usageStatsManager, packageManager, todayStart, now, context.packageName)
        val yesterdayAppsMap = calculateInteractiveScreenTime(usageStatsManager, packageManager, yesterdayStart, todayStart, context.packageName)

        val apps = todayAppsMap.entries
            .map { entry ->
                AppUsageSnapshot(
                    name = entry.key,
                    minutes = (entry.value / 60000L).toInt()
                )
            }
            .filter { it.minutes > 0 }
            .sortedByDescending { it.minutes }

        val totalMinutes = apps.sumOf { it.minutes }
        val yesterdayTotalMinutes = (yesterdayAppsMap.values.sumOf { it } / 60000L).toInt()
        val deltaMinutes = totalMinutes - yesterdayTotalMinutes
        val topMinutes = max(1, apps.firstOrNull()?.minutes ?: 1)

        return UserScreenTime(
            displayName = displayName,
            initials = initials,
            avatarUrl = avatarUrl,
            totalMinutes = totalMinutes,
            deltaMinutes = deltaMinutes,
            apps = apps.take(6).map { snapshot ->
                val colors = colorPalette[snapshot.name.length % colorPalette.size]
                AppUsage(
                    name = snapshot.name,
                    minutes = snapshot.minutes,
                    fillFraction = snapshot.minutes.toFloat() / topMinutes.toFloat(),
                    accentColor = colors.first,
                    trackColor = colors.second
                )
            },
            summary = if (totalMinutes > 0) {
                buildSummary(totalMinutes, apps)
            } else {
                fallbackSummary
            }
        )
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
    ): Map<String, Long> {
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

        // Map package names to clean human-friendly app labels
        val labelMap = mutableMapOf<String, Long>()
        for ((pkg, millis) in packageTimeMillis) {
            val label = getCleanAppLabel(packageManager, pkg)
            labelMap[label] = (labelMap[label] ?: 0L) + millis
        }

        return labelMap
    }

    private fun calculateFallbackUsageStats(
        usageStatsManager: UsageStatsManager,
        packageManager: PackageManager,
        startTimeMillis: Long,
        endTimeMillis: Long,
        selfPackage: String
    ): Map<String, Long> {
        val result = mutableMapOf<String, Long>()
        val statsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTimeMillis,
            endTimeMillis
        ).orEmpty()

        for (stat in statsList) {
            val millis = stat.totalTimeInForeground
            if (millis > 0L && isAttentionApp(packageManager, stat.packageName, selfPackage)) {
                val label = getCleanAppLabel(packageManager, stat.packageName)
                result[label] = (result[label] ?: 0L) + millis
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

    private fun buildSummary(totalMinutes: Int, apps: List<AppUsageSnapshot>): String {
        val topThree = apps.take(3)
            .joinToString(separator = ", ") { snapshot -> "${snapshot.minutes} min on ${snapshot.name}" }
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

    private data class AppUsageSnapshot(
        val name: String,
        val minutes: Int,
    )

    private val colorPalette: List<Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color>> = listOf(
        androidx.compose.ui.graphics.Color(0xFF6460FF) to androidx.compose.ui.graphics.Color(0xFFD9E7F5),
        androidx.compose.ui.graphics.Color(0xFF23C8D7) to androidx.compose.ui.graphics.Color(0xFFD9E7F5),
        androidx.compose.ui.graphics.Color(0xFFFF6D84) to androidx.compose.ui.graphics.Color(0xFFD9E7F5),
        androidx.compose.ui.graphics.Color(0xFF9B6BFF) to androidx.compose.ui.graphics.Color(0xFFE8DBFF),
        androidx.compose.ui.graphics.Color(0xFF2FC37F) to androidx.compose.ui.graphics.Color(0xFFDFF6EA),
        androidx.compose.ui.graphics.Color(0xFFFFA24B) to androidx.compose.ui.graphics.Color(0xFFFFE7CC)
    )

    private const val DAY_IN_MILLIS = 24L * 60L * 60L * 1000L
}
