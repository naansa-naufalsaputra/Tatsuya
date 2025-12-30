package com.example.tatsuya.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.tatsuya.data.local.DownloadedChapterEntity
import com.example.tatsuya.data.local.MangaDao
import com.example.tatsuya.data.remote.MangaDexApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@HiltWorker
class ChapterDownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val api: MangaDexApi,
    private val dao: MangaDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val chapterId = inputData.getString("chapterId") ?: return@withContext Result.failure()
        val mangaId = inputData.getString("mangaId") ?: return@withContext Result.failure()
        val chapterName = inputData.getString("chapterName") ?: "Unknown Chapter"

        try {
            // 1. Get Pages URL
            val pageResponse = api.getChapterPages(chapterId)
            val baseUrl = pageResponse.baseUrl
            val hash = pageResponse.chapter.hash
            val fileNames = pageResponse.chapter.data

            // 2. Prepare Directory
            val downloadDir = File(applicationContext.filesDir, "downloads/$mangaId/$chapterId")
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }

            // 3. Download Images
            fileNames.forEachIndexed { index, fileName ->
                val imageUrl = "$baseUrl/data/$hash/$fileName"
                val file = File(downloadDir, "$index.jpg")

                if (!file.exists()) {
                    URL(imageUrl).openStream().use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }

            // 4. Save to DB
            val savedPath = "downloads/$mangaId/$chapterId"
            dao.insertDownload(
                DownloadedChapterEntity(
                    chapterId = chapterId,
                    mangaId = mangaId,
                    chapterName = chapterName,
                    savedPath = savedPath
                )
            )

            Result.success(workDataOf("chapterId" to chapterId))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(workDataOf("error" to e.message))
        }
    }
}
