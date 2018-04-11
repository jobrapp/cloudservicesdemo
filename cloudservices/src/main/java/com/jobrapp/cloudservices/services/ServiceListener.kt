package com.jobrapp.cloudservices.services

import java.io.File

/**
 * Listeners of Services will get notified when the file has been downloaded, will get a list of
 * files to display and can notify that there was an error
 */
interface ServiceListener {
    fun currentFiles(files : List<FileDataType>)
    fun fileDownloaded(file : File)
    fun cancelled()
    fun handleError(exception : CloudServiceException)
}