package com.fishforandre.brainrotwatch.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.fishforandre.brainrotwatch.DeviceUsageRepository
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ScreenTimeSyncWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val session = supabase.auth.currentSessionOrNull() ?: return Result.success()
        val userId = session.user?.id ?: return Result.success()

        if (!DeviceUsageRepository.hasUsageAccess(applicationContext)) {
            return Result.success()
        }

        return try {
            val screenTime = DeviceUsageRepository.loadTodayUserScreenTime(
                context = applicationContext,
                displayName = "User",
                initials = "U",
                fallbackSummary = "",
            )

            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

            val dto = DailyUsageSyncDto(
                userId = userId,
                usageDate = todayDate,
                totalMinutes = screenTime.totalMinutes,
            )

            supabase.from("daily_usage").upsert(dto)

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "ScreenTimeSyncWorker"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<ScreenTimeSyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
private data class DailyUsageSyncDto(
    @SerialName("user_id") val userId: String,
    @SerialName("usage_date") val usageDate: String,
    @SerialName("total_minutes") val totalMinutes: Int,
)
