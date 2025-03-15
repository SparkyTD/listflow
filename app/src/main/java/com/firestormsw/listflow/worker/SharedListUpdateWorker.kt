package com.firestormsw.listflow.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.firestormsw.listflow.TAG
import com.firestormsw.listflow.data.viewmodel.ListShareManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SharedListUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val shareManager: ListShareManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            if (!shareManager.connectIfHasPeers()) {
                return Result.success()
            }

            shareManager.synchronizeAllPeers()
            shareManager.disconnect()

            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during MQTT check-in", e)
            return Result.retry()
        }
    }
}