package com.jobrapp.cloudservices.services.dropbox

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.android.AuthActivity
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.*
import com.jobrapp.cloudservices.services.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList

/**
 * Client for Dropbox
 */
class DropboxService(val context: Context, val config: DropboxConfig) : BaseService() {
    private var client: DbxClientV2? = null
    private val prefs = Prefs(context)
    private var accessToken : String? = null

    /**
     * Called from onActivityResult
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_DROPBOX_AUTH) {
            val token: String? = Auth.getOAuth2Token()
            if (token != null && token.isNotEmpty()) {
                authState = AuthState.Authenticated()
                prefs.putString(DROPBOX_ACCESS_TOKEN, token)
                accessToken = token
                createClient(token)
                getFiles(ROOT_FOLDER)
            } else {
                serviceListener?.cancelled()
            }
        }
    }

    override fun auth() {
        authState = AuthState.Authenticating()
        val token : String? = prefs.getString(DROPBOX_ACCESS_TOKEN)
        if (token != null && token.isNotEmpty()) {
            authState = AuthState.Authenticated()
            createClient(token)
            getFiles(ROOT_FOLDER)
        } else {
            (context as Activity).startActivityForResult(AuthActivity.makeIntent(context, config.dropboxKey,
                    null, null), REQUEST_DROPBOX_AUTH)
        }
    }

    override fun logout() {
        authState = AuthState.Init()
        client?.auth()?.tokenRevoke()
    }

    override fun getFiles(path : String?) {
        if (path == null) {
            return
        }
        launch(CommonPool) {
            val files = client?.files()
            if (files != null) {
                val metaDataEntries = filterFiles(getEntries(files, files.listFolder(path)))
                launch(UI) {
                    serviceListener?.currentFiles(path, convertEntries(metaDataEntries))
                }
            }
        }
    }

    private fun getEntries(files: DbxUserFilesRequests, result : ListFolderResult) : List<Metadata> {
        val totalFiles = ArrayList<Metadata>()
        var currentResult = result
        totalFiles.addAll(currentResult.entries)
        while (currentResult.hasMore) {
            currentResult = files.listFolderContinue(currentResult.cursor)
            totalFiles.addAll(currentResult.entries)
        }
        return totalFiles
    }

    private fun filterFiles(files : List<Metadata>) : List<Metadata> {
        val totalFiles = ArrayList<Metadata>()
        for (file in files) {
            if (file is FolderMetadata) {
                totalFiles.add(file)
            } else if (file is FileMetadata) {
                if (file.name.endsWith(".pdf") || file.name.endsWith(".doc") || file.name.endsWith(".docx")) {
                    totalFiles.add(file)
                }
            }
        }
        return totalFiles
    }

    private fun convertEntries(entries : List<Metadata>) : List<FileData> {
        val items = ArrayList<FileData>()
        for (entry in entries) {
            items.add(FileData(entry.name, entry.name, entry is FolderMetadata, entry.pathLower))
        }
        return items
    }

    fun createClient(token : String) {
        // Create Dropbox client
        val config = DbxRequestConfig.newBuilder("DropboxService")
                .withUserLocale(Locale.getDefault().toString())
                .build()
        client = DbxClientV2(config, token)

    }

    override fun downloadFile(data: FileDataType?) {
        if (data != null && (data is FileData)) {
            launch(CommonPool) {
                try {
                    val storageDir = this@DropboxService.context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(storageDir, data.name)
                    file.createNewFile()
                    client?.files()?.download(data.path, data.rev)
                            ?.download(FileOutputStream(file))
                    launch(UI) {
                        serviceListener?.fileDownloaded(file)
                    }
                } catch (e : Exception) {
                    Log.e("DropboxService", "Problems downloading file", e)
                    launch(UI) {
                        serviceListener?.handleError(CloudServiceException("Problems downloading file"))
                    }
                }
            }
        }
    }

    companion object {
        const val DROPBOX_ACCESS_TOKEN = "DROPBOX_ACCESS_TOKEN"
        const val ROOT_FOLDER = ""
    }
}