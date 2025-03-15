package com.firestormsw.listflow

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.firestormsw.listflow.worker.SharedListUpdateWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

val TAG = "Listflow"

@HiltAndroidApp
class ListflowApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        // Schedule daily MQTT update job
        val peerUpdateRequest = PeriodicWorkRequestBuilder<SharedListUpdateWorker>(
            12, TimeUnit.HOURS,
            15, TimeUnit.MINUTES
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "peer_list_update",
            ExistingPeriodicWorkPolicy.UPDATE,
            peerUpdateRequest
        )

        WorkManager.getInstance(this)
            .getWorkInfosForUniqueWorkLiveData("peer_list_update")
            .observeForever { workInfos ->
                Log.d(TAG, ">> Worker status: ${workInfos.firstOrNull()?.state} [${PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS}]")
            }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()
}