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
        authState = AuthState.Authenticating()
        val token : String? = client.authInfo?.accessToken()
        if (token != null && token.isNotEmpty()) {
            getFiles(ROOT_FOLDER)
        } else {
            client.authenticate(context)
                    .addOnCompletedListener { response ->
                        if (response.isSuccess) {
                            val authToken = response.result.authInfo.accessToken()
                            if (authToken != null) {
                                authState = AuthState.Authenticated()
                                prefs.putString(BOX_ACCESS_TOKEN, authToken)
                                getFiles(ROOT_FOLDER)
                            }
                        } else {
                            serviceListener?.handleError(CloudServiceException("Problems Authenticating Box", response.exception))
                        }
                    }
        }
    }

    override fun logout() {
        authState = AuthState.Init()
        client.logout()
    }

    override fun downloadFile(data: FileDataType?) {
        if (data == null || (data !is FileData)) {
            serviceListener?.handleError(CloudServiceException("No download file provided"))
            return
        }
        launch(CommonPool) {
            try {
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
            } catch (e : Exception) {
//                Log.e("BoxService", "Problems downloading file", e)
                launch(UI) {
                    serviceListener?.handleError(CloudServiceException("Problems downloading file"))
                }
            }
        }
    }

    override fun getFiles(path: String?) {
        if (path == null) {
            return
        }
        launch(CommonPool) {
            try {
                val folderAPI = BoxApiFolder(client)
                val items = folderAPI.getItemsRequest(path).send()
                if (items != null) {
                    launch(UI) {
                        serviceListener?.currentFiles(path, convertEntries(items.entries))
                    }
                }

            } catch (e : Exception) {
//                Log.e(TAG, "Problems getting Box files", e)
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
            if (entry is BoxFolder) {
                items.add(item)
            } else if (entry.name.endsWith(".pdf") || entry.name.endsWith(".doc") || entry.name.endsWith(".docx")) {
                items.add(item)
            }
        }
        return items
    }

    companion object {
        const val BOX_ACCESS_TOKEN = "BOX_ACCESS_TOKEN"
        const val ROOT_FOLDER = "0"
    }
}