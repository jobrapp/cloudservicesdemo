package com.jobrapp.cloudservices.services

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

/**
 * Handle Android 6.0 & Above permissions
 */
class PermissionsManager {
    companion object {

        /**
         * If on Marshmallow, check if we have this permission, otherwise return true
         * @param permission
         * @return true if the user has granted this permission
         */
        fun hasPermission(context: Context, permission: String): Boolean {
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }

        /**
         * Check to see if all permissions are granted
         */
        fun hasPermissions(context: Context, permissions: List<String>) : Boolean {
            for (permission in permissions) {
                if (!hasPermission(context, permission)) {
                    return false
                }
            }
            return true
        }

        /**
         * Show we show some sort of rationale for the permission?
         * @param activity
         * @param permission
         * @return true if user already said no 1x, false if deny always
         */
        fun shouldShowRationale(activity: Activity,
                                permission: String): Boolean {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }


        /**
         * Bring up the system permission request dialog
         * @param activity
         * @param permissions
         * @param requestCode
         */
        fun requestPermissions(activity: Activity,
                               permissions: Array<String>, requestCode: Int) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
        }
    }

}
