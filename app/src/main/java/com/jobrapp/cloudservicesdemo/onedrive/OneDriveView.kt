package com.jobrapp.cloudservicesdemo.onedrive

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jobrapp.cloudservices.services.CloudServiceException
import com.jobrapp.cloudservices.services.FileData
import com.jobrapp.cloudservices.services.FileDataType
import com.jobrapp.cloudservices.services.ServiceListener
import com.jobrapp.cloudservices.services.onedrive.OneDriveConfig
import com.jobrapp.cloudservices.services.onedrive.OneDriveService
import com.jobrapp.cloudservicesdemo.ClickHandler
import com.jobrapp.cloudservicesdemo.R
import com.jobrapp.cloudservicesdemo.views.BaseView
import java.io.File

/**
 * Show OneDrive files
 */
class OneDriveView : BaseView(), ClickHandler, ServiceListener {
    lateinit var recyclerView: RecyclerView
    lateinit var viewContext: Context
    lateinit var client: OneDriveService
    val adapter = OneDriveAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
        viewContext = inflater.context
        client = OneDriveService(viewContext, OneDriveConfig(viewContext.getString(R.string.onedrive_app_id),
                viewContext.getString(R.string.onedrive_redirect_url)))
        client.serviceListener = this
        recyclerView = inflater.inflate(R.layout.recycler_view, container, false) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(viewContext, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        adapter.handler = this
        return recyclerView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        client.onActivityResult(requestCode, resultCode, data)
    }

    override fun startAuth() {
        client.auth()
    }

    override fun handleError(exception: CloudServiceException) {
    }

    override fun handleClick(data: Any?) {
        when (data) {
            is FileData -> {
                if (data.isFolder) {
                    client.getFiles(data.id)
                } else {
                    client.downloadFile(data)
                }
            }
        }
    }

    override fun currentFiles(currentPath : String, files: List<FileDataType>) {
        adapter.entries = files as List<FileData>
    }

    override fun fileDownloaded(file: File) {
        (viewContext as Activity).onBackPressed()
    }

    override fun cancelled() {
        (viewContext as Activity).onBackPressed()
    }

    companion object {
    }
}