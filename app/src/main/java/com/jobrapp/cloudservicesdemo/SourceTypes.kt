package com.jobrapp.cloudservicesdemo

import android.content.Context
import com.jobrapp.cloudservicesdemo.data.Sources

/**
 * Hold the string, type and Icon Id
 */
enum class SourceTypes(val sourceNameResource : Int, val fileStackSource : Sources, val icon : Int) {
    DEVICE(R.string.source_local, Sources.DEVICE, R.drawable.ic_source_device),
    GMAIL(R.string.source_gmail, Sources.GMAIL, R.drawable.ic_source_gmail),
    GOOGLE_DRIVE(R.string.source_google_drive, Sources.GOOGLE_DRIVE, R.drawable.ic_source_google_drive),
    DROPBOX(R.string.source_dropbox, Sources.DROPBOX, R.drawable.ic_source_dropbox),
    BOX(R.string.source_box, Sources.BOX, R.drawable.ic_source_box),
    ONEDRIVE(R.string.source_onedrive, Sources.ONEDRIVE, R.drawable.ic_source_onedrive);

    fun getDeviceName(context: Context) : String {
        return context.getString(sourceNameResource)
    }

}