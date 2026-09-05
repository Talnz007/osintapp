package com.noobdevs.osint.util

import android.content.Context
import androidx.work.*
import com.noobdevs.osint.data.AuthManager
import com.noobdevs.osint.data.KtorOsintApiClient
import java.util.concurrent.TimeUnit

class IntelSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val authManager = AuthManager()
        val apiClient = KtorOsintApiClient(authManager)

        return try {
            val response = apiClient.getPosts(page = 1, limit = 10)
            if (response.isSuccess) {
                val posts = response.getOrNull()?.data ?: emptyList()
                val alertItem = posts.firstOrNull {
                    it.attackType == "AMBUSH_FIRE" ||
                    it.attackType == "IED_EXPLOSIVE" ||
                    it.isHighProfile == 1 ||
                    it.isShaheedIncident == 1
                }

                if (alertItem != null) {
                    val attack = alertItem.attackType?.replace("_", " ") ?: "SECURITY INCIDENT"
                    val province = alertItem.detectProvince()
                    NotificationHelper.showIncidentNotification(
                        context,
                        title = "$province [$attack Alert]",
                        body = alertItem.content?.take(120) ?: "New tactical threat intercepted",
                        postId = alertItem.id
                    )
                }
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        private const val WORK_NAME = "OSINT_TACTICAL_THREAT_MONITOR"

        fun schedulePeriodicSync(context: Context, intervalMinutes: Long = 15) {
            val authManager = AuthManager()
            if (!authManager.notificationsEnabled) {
                cancelPeriodicSync(context)
                return
            }

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<IntelSyncWorker>(
                intervalMinutes.coerceAtLeast(15),
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                syncRequest
            )
        }

        fun cancelPeriodicSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        fun triggerOneTimeSync(context: Context) {
            val oneTime = OneTimeWorkRequestBuilder<IntelSyncWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context).enqueue(oneTime)
        }
    }
}
