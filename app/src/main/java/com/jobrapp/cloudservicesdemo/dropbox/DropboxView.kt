package com.jobrapp.cloudservicesdemo.dropbox

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jobrapp.cloudservices.services.*
import com.jobrapp.cloudservicesdemo.ClickHandler
import com.jobrapp.cloudservices.services.dropbox.DropboxService
import com.jobrapp.cloudservices.services.dropbox.DropboxConfig
import com.jobrapp.cloudservicesdemo.R
import com.jobrapp.cloudservicesdemo.googledrive.GoogleDriveAdapter
import com.jobrapp.cloudservicesdemo.views.BaseView
import java.io.File


/**
 * Show Dropbox files
 */
class DropboxView : BaseView(), ClickHandler, ServiceListener {
    lateinit var recyclerView: RecyclerView
    lateinit var viewContext: Context
    val adapter = DropboxAdapter()
    lateinit var dropboxClient : DropboxService

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
        viewContext = inflater.context
        dropboxClient = DropboxService(viewContext, DropboxConfig(viewContext.getString(R.string.dropbox_app_key)))
        dropboxClient.serviceListener = this
        recyclerView = inflater.inflate(R.layout.recycler_view, container, false) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(viewContext, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        adapter.handler = this
        return recyclerView
    }


    override fun startAuth() {
        dropboxClient.auth()
    }

    override fun cancelled() {
        (viewContext as Activity).onBackPressed()
    }

    override fun currentFiles(currentPath : String, files: List<FileDataType>) {
        adapter.entries = files as List<FileData>
    }

    override fun fileDownloaded(file: File) {
        viewFile(viewContext, file)
        (viewContext as Activity).onBackPressed()
    }

    override fun handleError(exception: CloudServiceException) {
    }

    override fun handleClick(data: Any?) {
        when (data) {
            is FileData -> {
                if (data.isFolder) {
                    dropboxClient.getFiles(data.path)
                } else {
                    dropboxClient.downloadFile(data)

                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        dropboxClient.onActivityResult(requestCode, resultCode, data)

    }

    companion object {
    }
}