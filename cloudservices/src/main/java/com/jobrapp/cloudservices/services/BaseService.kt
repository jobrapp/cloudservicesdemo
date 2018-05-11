package com.jobrapp.cloudservices.services

import android.content.Intent

/**
 * Base Service. Hold instance of the service listener
 */
abstract class BaseService : Services {
    var serviceListener : ServiceListener? = null
    // Some services may need to keep track of the current state
    var authState : AuthState = AuthState.Init()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

    }

    companion object {
        const val REQUEST_DROPBOX_AUTH = 500
        /**
         * Request code for the Drive picker
         */
        const val REQUEST_CODE_OPEN_ITEM = 100
        const val REQUEST_CODE_SIGN_IN = 101
        const val TAG = "CloudServices"
    }
}