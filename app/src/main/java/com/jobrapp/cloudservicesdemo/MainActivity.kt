package com.jobrapp.cloudservicesdemo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.jobrapp.cloudservices.services.PermissionsManager
import com.jobrapp.cloudservices.services.gmail.GmailService
import com.jobrapp.cloudservicesdemo.box.BoxView
import com.jobrapp.cloudservicesdemo.box.GmailView
import com.jobrapp.cloudservicesdemo.data.Sources
import com.jobrapp.cloudservicesdemo.dropbox.DropboxView
import com.jobrapp.cloudservicesdemo.googledrive.GoogleDriveView
import com.jobrapp.cloudservicesdemo.local.LocalView
import com.jobrapp.cloudservicesdemo.onedrive.OneDriveView
import com.jobrapp.cloudservicesdemo.views.ServicesView
import com.jobrapp.cloudservicesdemo.views.ViewCreator
import com.jobrapp.cloudservicesdemo.views.ViewInterface

class MainActivity : AppCompatActivity(), ClickHandler {
    lateinit var frame : FrameLayout
    lateinit var currentView : ViewInterface
    val servicesView = ServicesView()
//    lateinit var googleService : GoogleDriveService
    lateinit var gmailService: GmailService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        googleService = GoogleDriveService(this, GoogleDriveConfig(getString(R.string.source_google_drive), GoogleDriveService.documentMimeTypes))
        gmailService = GmailService(this)

        currentView = servicesView
        currentView.setClickHandler(this)

        frame = findViewById(R.id.main_layout)
        addView(ViewCreator.setupView(this, frame, currentView))
    }

    override fun onPause() {
        currentView.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        currentView.onResume()
        if (!PermissionsManager.hasPermissions(this, listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.GET_ACCOUNTS))) {
            PermissionsManager.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.GET_ACCOUNTS), REQUEST_PERMISSIONS)
        }
    }

    override fun onBackPressed() {
        if (!(currentView is ServicesView)) {
            addView(servicesView.view)
            return
        }
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        currentView.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {
            REQUEST_PERMISSIONS -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }

            }

        }
    }

    override fun handleClick(data: Any?) {
        when (data) {
            is ServicesData -> {
                when (data.source) {
                    Sources.DEVICE -> {
                        currentView = LocalView()
                        ViewCreator.setupView(this, frame, currentView)
                        currentView.startAuth()
                    }
                    Sources.GOOGLE_DRIVE -> {
                        currentView = GoogleDriveView()
                        addView(ViewCreator.setupView(this, frame, currentView))
                        currentView.startAuth()
                    }
                    Sources.DROPBOX -> {
                        currentView = DropboxView()
                        addView(ViewCreator.setupView(this, frame, currentView))
                        currentView.startAuth()
                    }
                    Sources.BOX -> {

                        currentView = BoxView()
                        addView(ViewCreator.setupView(this, frame, currentView))
                        currentView.startAuth()
                    }
                    Sources.GMAIL -> {

                        currentView = GmailView()
                        addView(ViewCreator.setupView(this, frame, currentView))
                        currentView.startAuth()
                    }
                    Sources.ONEDRIVE -> {

                        currentView = OneDriveView()
                        addView(ViewCreator.setupView(this, frame, currentView))
                        currentView.startAuth()
                    }
/*
                    Sources.AMAZON_DRIVE -> {

                        currentView = AmazonView()
                        addView(ViewCreator.setupView(this, frame, currentView))
                        currentView.startAuth()
                    }
*/
                    else -> {
                        Log.e(TAG, "Source not found ${data.source}")
                    }
                }


            }
        }
    }

    fun addView(view : View) {
        frame.removeAllViews()
        frame.addView(view)
    }

    companion object {
        const val REQUEST_PERMISSIONS = 4
        const val TAG = "MainActivity"
    }
}
