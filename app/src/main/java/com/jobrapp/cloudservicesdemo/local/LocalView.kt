package com.jobrapp.cloudservicesdemo.local

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jobrapp.cloudservices.services.BaseService
import com.jobrapp.cloudservices.services.CloudServiceException
import com.jobrapp.cloudservices.services.FileDataType
import com.jobrapp.cloudservices.services.ServiceListener
import com.jobrapp.cloudservicesdemo.R
import com.jobrapp.cloudservicesdemo.views.BaseView
import okio.Okio
import java.io.File

/**
 * Handle Local Files
 */
class LocalView : BaseView(), ServiceListener {
    lateinit var viewContext: Context


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
        viewContext = inflater.context
        val recyclerView = inflater.inflate(R.layout.recycler_view, container, false) as RecyclerView
        return recyclerView
    }

    override fun startAuth() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "application/msword" ,"application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        intent.type = "*/*"
        if (intent.resolveActivity(viewContext.packageManager) != null) {
            (viewContext as Activity).startActivityForResult(intent, LOCAL_FILES_REQUEST)
        }
    }

    override fun currentFiles(currentPath : String, files: List<FileDataType>) {
    }

    override fun cancelled() {
    }

    override fun handleError(exception: CloudServiceException) {
    }

    override fun fileDownloaded(file: File) {
        viewFile(viewContext, file)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when(requestCode) {
            LOCAL_FILES_REQUEST-> {
                if (data != null && data.data != null) {
                    processUri(data.data)
                }
            }
        }
    }

    fun processUri(uri : Uri) {
        val resolver = viewContext.contentResolver
        try {
            val cursor = resolver.query(uri, null, null, null, null, null)
            cursor?.let {
                if (it.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

                    // We can't upload files without knowing the size
                    if (cursor.isNull(sizeIndex)) {
                        return
                    }

                    val name = cursor.getString(nameIndex)
                    val size = cursor.getInt(sizeIndex)
                    val mimeType = resolver.getType(uri)
                    val storageDir = viewContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(storageDir, name)
                    file.createNewFile()
                    val sink = Okio.buffer(Okio.sink(file))
                    sink.writeAll(Okio.source(resolver.openInputStream(uri)))
                    sink.close()

                }
                it.close()
            }
        } catch (e : Exception) {
            Log.e("FileStack", "Problems getting uri", e)
        }
    }

    companion object {
        const val LOCAL_FILES_REQUEST = 2
    }
}