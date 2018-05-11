package com.jobrapp.cloudservicesdemo.gmail

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jobrapp.cloudservices.services.FileDataType
import com.jobrapp.cloudservices.services.GmailFile
import com.jobrapp.cloudservices.services.GmailMessage
import com.jobrapp.cloudservicesdemo.ClickHandler
import com.jobrapp.cloudservicesdemo.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for Gmail emails
 */
class GmailAdapter  : RecyclerView.Adapter<MessageHolder>() {
    val smallDateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val sourceDateFormatter = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US)
    var handler : ClickHandler? = null
    var entries : List<FileDataType>  = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        return MessageHolder(LayoutInflater.from(parent.context).inflate(R.layout.message_layout, parent, false))
    }

    override fun getItemCount() = entries.size

    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        val entry = entries[position]
        when (entry) {
            is GmailMessage -> {
                holder.from.text = removeEmail(entry.from)
                holder.date.text = convertDate(entry.date)
                holder.subject.text = entry.subject
                holder.fileIcon.setImageResource(R.drawable.ic_file)
                holder.itemView.setOnClickListener {
                    handler?.handleClick(entry)
                }
            }
            is GmailFile -> {
                holder.from.text = entry.fileName
                holder.date.text = ""
                holder.subject.text = ""
                holder.fileIcon.setImageResource(R.drawable.ic_file)
                holder.itemView.setOnClickListener {
                    handler?.handleClick(entry)
                }

            }
        }
    }

    fun convertDate(sourceDate : String) : String? {
        return smallDateFormatter.format(sourceDateFormatter.parse(sourceDate))
    }

    fun removeEmail(emailString : String) : String {
        if (!emailString.contains("<")) {
            return emailString
        }
        return emailString.substring(0, emailString.indexOf('<')-1)
    }

    companion object {
    }
}