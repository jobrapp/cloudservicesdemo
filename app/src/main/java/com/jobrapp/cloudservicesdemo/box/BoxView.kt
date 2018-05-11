package com.jobrapp.cloudservicesdemo.box

import android.app.Activity
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jobrapp.cloudservices.services.CloudServiceException
import com.jobrapp.cloudservices.services.FileData
import com.jobrapp.cloudservices.services.FileDataType
import com.jobrapp.cloudservicesdemo.ClickHandler
import com.jobrapp.cloudservices.services.ServiceListener
import com.jobrapp.cloudservices.services.box.BoxService
import com.jobrapp.cloudservices.services.box.BoxServiceConfig
import com.jobrapp.cloudservicesdemo.R
import com.jobrapp.cloudservicesdemo.views.BaseView
import java.io.File

/**
 * View for Box
 */
class BoxView : BaseView(), ClickHandler, ServiceListener {
    lateinit var recyclerView: RecyclerView
    lateinit var viewContext: Context
    lateinit var boxClient : BoxService
    val adapter = BoxAdapter()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
        viewContext = inflater.context
        boxClient = BoxService(viewContext, BoxServiceConfig(viewContext.getString(R.string.box_client_key), viewContext.getString(R.string.box_client_secret)))
        boxClient.serviceListener = this
        recyclerView = inflater.inflate(R.layout.recycler_view, container, false) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(viewContext, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        adapter.handler = this
        return recyclerView
    }

    override fun startAuth() {
        boxClient.auth()
    }


    override fun handleError(exception: CloudServiceException) {
    }

    override fun handleClick(data: Any?) {
        when (data) {
            is FileData -> {
                if (data.isFolder) {
                    boxClient.getFiles(data.id)
                } else {
                    boxClient.downloadFile(data)
                }
            }
        }
    }

    override fun currentFiles(currentPath : String, files: List<FileDataType>) {
        adapter.entries = files as List<FileData>
    }

    override fun fileDownloaded(file: File) {
        viewFile(viewContext, file)
        (viewContext as Activity).onBackPressed()
    }

    override fun cancelled() {
        (viewContext as Activity).onBackPressed()
    }


   companion object {
    }
}