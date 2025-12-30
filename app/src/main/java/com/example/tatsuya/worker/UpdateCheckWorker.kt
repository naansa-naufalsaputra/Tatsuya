package com.example.tatsuya.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tatsuya.R
import com.example.tatsuya.data.local.MangaDao
import com.example.tatsuya.data.remote.MangaDexApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class UpdateCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val dao: MangaDao,
    private val api: MangaDexApi
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // 1. Ambil list favorite dari DB
            val favorites = dao.getFavoriteManga().first()
            if (favorites.isEmpty()) return Result.success()

            var updatesFound = 0
            val updatedMangaNames = mutableListOf<String>()

            // 2. Cek setiap manga ke API
            favorites.forEach { manga ->
                try {
                    // Limit 1 cukup untuk cek total count di response header/body
                    val response = api.getMangaChapters(manga.id, limit = 1)
                    val onlineTotal = response.total
                    
                    // Logic update: Jika online > local
                    if (onlineTotal > manga.totalChapters) {
                        updatesFound++
                        updatedMangaNames.add(manga.title)
                        
                        // Update DB agar tidak notif berulang
                        dao.updateTotalChapters(manga.id, onlineTotal)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Lanjut ke manga berikutnya jika error
                }
            }

            // 3. Kirim Notifikasi jika ada update
            if (updatesFound > 0) {
                sendNotification(updatesFound, updatedMangaNames)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun sendNotification(count: Int, titles: List<String>) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "manga_updates_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Manga Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        
        // Cek Izin Notifikasi (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Izin belum diberi, skip notif (ideal: request di UI)
                return
            }
        }

        val contentTitle = "New Chapters Available! \uD83D\uDCD6"
        val contentText = if (count == 1) {
            "${titles.firstOrNull()} has a new chapter!"
        } else {
            "$count manga have new chapters: ${titles.take(3).joinToString(", ")}..."
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ganti icon yg valid nanti
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(101, notification)
    }
}
