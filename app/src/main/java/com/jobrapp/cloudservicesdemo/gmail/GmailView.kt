package com.jobrapp.cloudservicesdemo.gmail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jobrapp.cloudservices.services.CloudServiceException
import com.jobrapp.cloudservices.services.FileDataType
import com.jobrapp.cloudservices.services.GmailMessage
import com.jobrapp.cloudservices.services.ServiceListener
import com.jobrapp.cloudservices.services.gmail.GmailService
import com.jobrapp.cloudservicesdemo.ClickHandler
import com.jobrapp.cloudservicesdemo.R
import com.jobrapp.cloudservicesdemo.views.BaseView
import java.io.File

/**
 * View for Gmail
 */
class GmailView : BaseView(), ClickHandler, ServiceListener {
    lateinit var recyclerView: RecyclerView
    lateinit var viewContext: Context
    lateinit var gmailService : GmailService
    val adapter = GmailAdapter()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
        viewContext = inflater.context
        gmailService = GmailService(viewContext as Activity)
        gmailService.serviceListener = this
        recyclerView = inflater.inflate(R.layout.recycler_view, container, false) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(viewContext, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        adapter.handler = this
        return recyclerView
    }

    override fun startAuth() {
        gmailService.auth()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        gmailService.onActivityResult(requestCode, resultCode, data)
    }

    override fun handleError(exception: CloudServiceException) {
    }

    override fun handleClick(data: Any?) {
        when (data) {
            is GmailMessage -> {
                gmailService.downloadFile(data)
            }
        }
    }

    override fun currentFiles(currentPath : String, files: List<FileDataType>) {
        adapter.entries = files as List<GmailMessage>
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