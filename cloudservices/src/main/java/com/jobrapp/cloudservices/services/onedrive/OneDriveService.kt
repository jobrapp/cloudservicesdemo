package com.jobrapp.cloudservices.services.onedrive

import android.app.Activity
import android.content.Context
import android.os.Environment
import com.jobrapp.cloudservices.services.BaseService
import com.jobrapp.cloudservices.services.CloudServiceException
import com.jobrapp.cloudservices.services.FileData
import com.jobrapp.cloudservices.services.FileDataType
import com.onedrive.sdk.authentication.ADALAuthenticator
import com.onedrive.sdk.authentication.MSAAuthenticator
import com.onedrive.sdk.concurrency.ICallback
import com.onedrive.sdk.core.ClientException
import com.onedrive.sdk.core.DefaultClientConfig
import com.onedrive.sdk.extensions.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import okio.Okio
import java.io.File
import java.util.ArrayList

/**
 * Service for Microsoft OneDrive
 */
class OneDriveService(val context: Context, val config : OneDriveConfig) : BaseService() {
    lateinit var rootDriveID : String
    var client: IOneDriveClient? = null

    override fun auth() {
        val config = DefaultClientConfig.createWithAuthenticators(MAuthenticator(), AAuthenticator())
        OneDriveClient.Builder().fromConfig(config)
            .loginAndBuildClient(context as Activity, object : ICallback<IOneDriveClient> {
                override fun success(result: IOneDriveClient) {
                    client = result
                    getRoot(true)
                }

                override fun failure(ex: ClientException?) {
                    serviceListener?.handleError(CloudServiceException("Problems Authenticating OneDrive", ex))
                }
            })
    }

    override fun logout() {
        client?.authenticator?.logout()
    }

    override fun getFiles(path: String?) {
        client?.drive?.getItems(path)?.children?.buildRequest()?.get(object : ICallback<IItemCollectionPage> {
            override fun success(result: IItemCollectionPage) {
                if (result is ItemCollectionPage) {
                    serviceListener?.currentFiles(convertEntries(result.currentPage))
                }
            }

            override fun failure(ex: ClientException?) {
                serviceListener?.handleError(CloudServiceException("Problems getting files from OneDrive", ex))
            }
        })
    }

    private fun convertEntries(entries : List<Item>) : List<FileData> {
        val items = ArrayList<FileData>()
        for (entry in entries) {
            val item = FileData(entry.id, entry.name, (entry.folder != null && entry.folder.childCount > 0))
            items.add(item)
        }
        return items
    }

    private fun getRoot(getRootFiles : Boolean = false) {
        launch(CommonPool) {
            client?.drive?.root?.buildRequest()?.get(object : ICallback<Item> {
                override fun success(result: Item) {
                    rootDriveID = result.id
                    if (getRootFiles) {
                        getFiles(rootDriveID)
                    }
                }

                override fun failure(ex: ClientException?) {
                    serviceListener?.handleError(CloudServiceException("Problems getting files from OneDrive", ex))
                }
            })
        }

    }
    override fun downloadFile(data: FileDataType?) {
        if (data == null || (data !is FileData)) {
            return
        }
        launch(CommonPool) {
            client?.let {
                val inputStream = it.drive.getItems(data.id).content.buildRequest().get()
                val storageDir = this@OneDriveService.context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                val file = File(storageDir, data.name)
                file.createNewFile()
                val sink = Okio.buffer(Okio.sink(file))
                sink.writeAll(Okio.source(inputStream))
                sink.close()
                launch(UI) {
                    serviceListener?.fileDownloaded(file)
                }
            }
        }
    }

    inner class MAuthenticator : MSAAuthenticator() {
        override fun getScopes(): Array<String> {
            return arrayOf("onedrive.readonly")
        }

        override fun getClientId(): String {
            return config.appId
        }

    }

    inner class AAuthenticator : ADALAuthenticator() {
        override fun getRedirectUrl(): String {
            return config.redirectUrl
        }

        override fun getClientId(): String {
            return config.appId
        }

    }
}