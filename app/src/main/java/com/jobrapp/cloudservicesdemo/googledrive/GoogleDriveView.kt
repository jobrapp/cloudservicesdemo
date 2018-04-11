package com.jobrapp.cloudservicesdemo.googledrive

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
import com.jobrapp.cloudservices.services.googledrive.GoogleDriveConfig
import com.jobrapp.cloudservices.services.googledrive.GoogleDriveService
import com.jobrapp.cloudservicesdemo.R
import com.jobrapp.cloudservicesdemo.views.BaseView
import java.io.File


/**
 * Show Google Drive files
 */
class GoogleDriveView : BaseView(), ClickHandler, ServiceListener {
    lateinit var recyclerView: RecyclerView
    lateinit var viewContext: Context
    val adapter = GoogleDriveAdapter()
    lateinit var googleDriveService : GoogleDriveService

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
        viewContext = inflater.context
        googleDriveService = GoogleDriveService(viewContext as Activity, GoogleDriveConfig(viewContext.getString(R.string.source_google_drive), GoogleDriveService.documentMimeTypes))
        googleDriveService.serviceListener = this
        recyclerView = inflater.inflate(R.layout.recycler_view, container, false) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(viewContext, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        adapter.handler = this
        return recyclerView
    }

    override fun startAuth() {
        googleDriveService.auth()
    }

    override fun cancelled() {
        (viewContext as Activity).onBackPressed()
    }

    override fun currentFiles(files: List<FileDataType>) {
        adapter.entries = files as List<FileData>
    }

    override fun fileDownloaded(file: File) {
        (viewContext as Activity).onBackPressed()
    }

    override fun handleError(exception: CloudServiceException) {
    }

    override fun handleClick(data: Any?) {
        when (data) {
            is FileData -> {
                if (data.isFolder) {
                    googleDriveService.getFiles(data.id)
                } else {
                    googleDriveService.downloadFile(data)

                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        googleDriveService.onActivityResult(requestCode, resultCode, data)
    }

}