package com.jobrapp.cloudservices.services.box

import android.content.Context
import android.os.Environment
import android.util.Log
import com.box.androidsdk.content.BoxApiFile
import com.box.androidsdk.content.BoxApiFolder
import com.box.androidsdk.content.BoxConfig
import com.box.androidsdk.content.models.BoxFolder
import com.box.androidsdk.content.models.BoxItem
import com.box.androidsdk.content.models.BoxSession
import com.jobrapp.cloudservices.services.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.io.File

/**
 * Handle Box Services
 * Store the client key & secret in the config class
 */
class BoxService(val context: Context, config: BoxServiceConfig) : BaseService() {
    val client: BoxSession
    val prefs: Prefs

    init {
        BoxConfig.CLIENT_ID = config.clientKey
        BoxConfig.CLIENT_SECRET = config.clientSecret
        client = BoxSession(context)
        prefs = Prefs(context)
    }

    override fun auth() {
        val token : String? = client.authInfo?.accessToken()
        if (token != null && token.isNotEmpty()) {
            getFiles(ROOT_FOLDER)
        } else {
            client.authenticate(context)
                    .addOnCompletedListener { response ->
                        if (response.isSuccess) {
                            val token = response.result.authInfo.accessToken()
                            if (token != null) {
                                prefs.putString(BOX_ACCESS_TOKEN, response.result.authInfo.accessToken())
                                getFiles(ROOT_FOLDER)
                            }
                        } else {
                            serviceListener?.handleError(CloudServiceException("Problems Authenticating Box", response.exception))
                        }
                    }
        }
    }

    override fun logout() {
        client.logout()
    }

    override fun downloadFile(data: FileDataType?) {
        if (data == null || (data !is FileData)) {
            serviceListener?.handleError(CloudServiceException("No download file provided"))
            return
        }
        launch(CommonPool) {
            val storageDir = this@BoxService.context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val file = File(storageDir, data.name)
            file.createNewFile()
            val fileApi = BoxApiFile(client)
            val download = fileApi.getDownloadRequest(file, data.id)
                    .send()

            if (download != null) {
                launch(UI) {
                    serviceListener?.fileDownloaded(file)
                }
            } else {
                launch(UI) {
                    serviceListener?.handleError(CloudServiceException("Problems downloading file"))
                }
            }
        }
    }

    override fun getFiles(path: String?) {
        launch(CommonPool) {
            try {
                val folderAPI = BoxApiFolder(client)
                val items = folderAPI.getItemsRequest(path).send()
                if (items != null) {
                    launch(UI) {
                        serviceListener?.currentFiles(convertEntries(items.entries))
                    }
                }

            } catch (e : Exception) {
                Log.e(TAG, "Problems getting Box files", e)
                serviceListener?.handleError(CloudServiceException("Problems Getting files for path $path"))
            }
        }
    }

    /**
     * Take specific BoxItem and convert it to a FileData class
     */
    private fun convertEntries(entries : List<BoxItem>) : List<FileData> {
        val items = ArrayList<FileData>()
        for (entry in entries) {
            val item = FileData(entry.id, entry.name, entry is BoxFolder, entry.id)
            items.add(item)
        }
        return items
    }

    companion object {
        const val BOX_ACCESS_TOKEN = "BOX_ACCESS_TOKEN"
        const val ROOT_FOLDER = "0"
    }
}