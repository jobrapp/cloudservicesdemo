package com.jobrapp.cloudservices.services.googledrive

/**
 * Pass in the title for Google Drive's Activity and the type of files to retrieve
 */
data class GoogleDriveConfig(val activityTitle: String? = null, val mimeTypes: List<String>? = null)