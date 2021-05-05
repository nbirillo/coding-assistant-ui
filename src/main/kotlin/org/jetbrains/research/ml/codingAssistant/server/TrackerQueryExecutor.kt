package org.jetbrains.research.ml.codingAssistant.server

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.jetbrains.research.ml.codingAssistant.Plugin
import org.jetbrains.research.ml.codingAssistant.tracking.StoredInfoWrapper
import java.io.File
import java.net.URL


object TrackerQueryExecutor : QueryExecutor() {
    private const val HINT_FILE_FIELD = "hintitem"

    var userId: String? = null

    init {
        StoredInfoWrapper.info.userId?.let { userId = it } ?: run {
            initUserId()
            StoredInfoWrapper.updateStoredInfo(userId = userId)
        }
    }

    private fun initUserId() {
        val currentUrl = URL("${baseUrl}user")
        logger.info("${Plugin.PLUGIN_NAME}: ...generating user id")
        val requestBody = ByteArray(0).toRequestBody(null, 0, 0)
        val request = Request.Builder().url(currentUrl).post(requestBody).build()
        userId = executeQuery(request)?.let { it.body?.string() }
    }

    private fun getRequestForSendingHintDataQuery(
        file: File
    ): Request {
        if (file.exists()) {
            logger.info("${Plugin.PLUGIN_NAME}: ...sending file ${file.name}")
            val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            requestBody.addFormDataPart(HINT_FILE_FIELD, file.name, file.asRequestBody("text/csv".toMediaType()))
            return Request.Builder().url(baseUrl + "hint-item").method("POST", requestBody.build()).build()
        } else {
            throw IllegalStateException("File ${file.name} for $HINT_FILE_FIELD doesn't exist")
        }
    }

    private fun executeTrackerQuery(request: Request): String? {
        val response = executeQuery(request)
        if (response.isSuccessful()) {
            return response?.let { it.body?.string() }
        }
        throw IllegalStateException("Unsuccessful server response")
    }

    private fun sendHintsData(files: List<File>): List<String?> {
        return files.map {
            val request = getRequestForSendingHintDataQuery(it)
            executeTrackerQuery(request)
        }
    }

    fun sendData(hintsTrackerFiles: List<File>) {
        sendHintsData(hintsTrackerFiles)
    }
}
