package com.jobrapp.cloudservices.services

import android.content.Intent

/**
 * Interface that all services should implement
 */
interface Services {
    fun getFiles(path : String?)
    fun auth()
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    fun downloadFile(data : FileDataType?)
    fun logout()
}