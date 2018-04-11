package com.jobrapp.cloudservicesdemo.views

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.jobrapp.cloudservicesdemo.R

/**
 * Hold a text and image view for a file
 */
class FileHolder(view : View) : RecyclerView.ViewHolder(view) {
    val fileName : TextView
    val fileIcon : ImageView
    init {
        fileName = view.findViewById(R.id.file_name)
        fileIcon = view.findViewById(R.id.file_icon)
    }
}