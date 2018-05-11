package com.jobrapp.cloudservicesdemo.views

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.content.FileProvider
import android.view.View
import android.webkit.MimeTypeMap
import com.jobrapp.cloudservicesdemo.BuildConfig
import com.jobrapp.cloudservicesdemo.ClickHandler
import java.io.File

/**
 * Base class. Holds the view and click handler
 */
abstract class BaseView : ViewInterface {
    var handler : ClickHandler? = null
    lateinit var view : View

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    }

    override fun onViewCreated(view: View) {
        this.view = view
    }

    override fun setClickHandler(clickHandler: ClickHandler) {
        handler = clickHandler
    }

    companion object {
        fun viewFile(context: Context, file: File) {
            val url = file.toURI().toString()
            val extension = MimeTypeMap.getFileExtensionFromUrl(url)
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val filePath = url.substring("file://".length - 1)
            val uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".file_provider", File(filePath))
            intent.setDataAndType(uri, mimeType)
            if (intent.resolveActivityInfo(context.getPackageManager(), PackageManager.MATCH_ALL) != null) {
                context.startActivity(intent)
            }
        }
    }
}