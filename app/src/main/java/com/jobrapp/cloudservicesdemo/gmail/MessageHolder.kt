package com.jobrapp.cloudservicesdemo.gmail

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.jobrapp.cloudservicesdemo.R

/**
 * Hold a text and image view for a Message
 */
class MessageHolder(view : View) : RecyclerView.ViewHolder(view) {
    val from : TextView
    val date : TextView
    val subject : TextView
    val fileIcon : ImageView
    init {
        from = view.findViewById(R.id.from)
        date = view.findViewById(R.id.date)
        subject = view.findViewById(R.id.subject)
        fileIcon = view.findViewById(R.id.file_icon)
    }
}