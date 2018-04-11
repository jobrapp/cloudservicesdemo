package com.jobrapp.cloudservices.services.dropbox

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Environment
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.android.AuthActivity
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FolderMetadata
import com.dropbox.core.v2.files.Metadata
import com.jobrapp.cloudservices.services.BaseService
import com.jobrapp.cloudservices.services.FileData
import com.jobrapp.cloudservices.services.FileDataType
import com.jobrapp.cloudservices.services.Prefs
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.io.FileOutputStream
import java.util.*

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
                prefs.putString(DROPBOX_ACCESS_TOKEN, token)
                accessToken = token
                createClient(token)
                getFiles(ROOT_FOLDER)
            }
        }
    }

    override fun auth() {
        val token : String? = prefs.getString(DROPBOX_ACCESS_TOKEN)
        if (token != null && token.isNotEmpty()) {
            createClient(token)
            getFiles(ROOT_FOLDER)
        } else {
            (context as Activity).startActivityForResult(AuthActivity.makeIntent(context, config.dropboxKey,
                    null, null), REQUEST_DROPBOX_AUTH)
        }
    }

    override fun logout() {
        client?.auth()?.tokenRevoke()
    }

    override fun getFiles(path : String?) {
        launch(CommonPool) {
            val files = client?.files()
            if (files != null) {
                val metaDataEntries = files.listFolder(path).entries
                if (metaDataEntries != null) {
                    launch(UI) {
                        serviceListener?.currentFiles(convertEntries(metaDataEntries))
                    }
                }
            }
        }
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
                val storageDir = this@DropboxService.context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                val file = File(storageDir, data.name)
                file.createNewFile()
                client?.files()?.download(data.path, data.rev)
                        ?.download(FileOutputStream(file))
                launch(UI) {
                    serviceListener?.fileDownloaded(file)
                }
            }
        }
    }

    companion object {
        const val DROPBOX_ACCESS_TOKEN = "DROPBOX_ACCESS_TOKEN"
        const val ROOT_FOLDER = ""
    }
}