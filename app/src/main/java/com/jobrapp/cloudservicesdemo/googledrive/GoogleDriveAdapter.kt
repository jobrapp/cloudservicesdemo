package com.jobrapp.cloudservicesdemo.googledrive

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jobrapp.cloudservices.services.FileData
import com.jobrapp.cloudservicesdemo.ClickHandler
import com.jobrapp.cloudservicesdemo.R
import com.jobrapp.cloudservicesdemo.views.FileHolder

/**
 *
 */
class GoogleDriveAdapter : RecyclerView.Adapter<FileHolder>() {
    var handler : ClickHandler? = null
    var entries : List<FileData> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileHolder {
        return FileHolder(LayoutInflater.from(parent.context).inflate(R.layout.file_layout, parent, false))
    }

    override fun getItemCount() = entries.size

    override fun onBindViewHolder(holder: FileHolder, position: Int) {
        val entry = entries[position]
        holder.fileName.text = entry.name
        holder.fileIcon.setImageResource(R.drawable.ic_file)
        holder.itemView.setOnClickListener {
            handler?.handleClick(entry)
        }
        if (entry.isFolder) {
            holder.fileIcon.setImageResource(R.drawable.ic_folder)
        }
    }


}