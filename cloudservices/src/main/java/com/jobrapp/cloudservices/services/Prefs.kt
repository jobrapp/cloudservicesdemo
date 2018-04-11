package com.jobrapp.cloudservices.services

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log

/**
 * Handle System Preferences
 */

class Prefs(val context: Context) {
    private var mSharedPreferences: SharedPreferences? = null
    private var currentSharedPreferences: SharedPreferences? = null


    /**
     * Set the preference name so that we can use a different preference file
     * @param preferenceName
     */
    fun setSharedPreferences(preferenceName: String) {
        mSharedPreferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
        currentSharedPreferences = mSharedPreferences
    }

    init {
        setupSharedPrefs()
    }

    private fun setupSharedPrefs() {
        if (mSharedPreferences == null) {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            currentSharedPreferences = mSharedPreferences
        }
    }

    fun containsKey(key: String): Boolean {
        return currentSharedPreferences!!.contains(key)
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        if (!checkSharedPreferences()) {
            return false
        }
        return currentSharedPreferences!!.getBoolean(key, defaultValue)
    }

    fun getBoolean(key: String): Boolean {
        if (!checkSharedPreferences()) {
            return false
        }
        return currentSharedPreferences!!.getBoolean(key, false)
    }


    fun getString(key: String, defaultValue: String): String? {
        if (!checkSharedPreferences()) {
            return null
        }
        return currentSharedPreferences!!.getString(key, defaultValue)
    }

    fun getString(key: String): String? {
        if (!checkSharedPreferences()) {
            return null
        }
        return currentSharedPreferences!!.getString(key, null)
    }


    fun getInt(key: String, defaultValue: Int): Int {
        if (!checkSharedPreferences()) {
            return -1
        }
        return currentSharedPreferences!!.getInt(key, defaultValue)
    }

    fun getInt(key: String): Int {
        if (!checkSharedPreferences()) {
            return -1
        }
        return currentSharedPreferences!!.getInt(key, -1)
    }


    fun getLong(key: String, defaultValue: Long): Long {
        if (!checkSharedPreferences()) {
            return -1
        }
        return currentSharedPreferences!!.getLong(key, defaultValue)
    }

    fun getLong(key: String): Long {
        if (!checkSharedPreferences()) {
            return -1
        }
        return currentSharedPreferences!!.getLong(key, -1)
    }


    fun getFloat(key: String): Float? {
        if (!checkSharedPreferences()) {
            return null
        }
        return currentSharedPreferences!!.getFloat(key, -1f)
    }

    fun getDouble(key: String): Double? {
        if (!checkSharedPreferences()) {
            return null
        }
        return currentSharedPreferences!!.getFloat(key, -1f).toDouble()
    }


    fun putBoolean(key: String, value: Boolean) {
        if (!checkSharedPreferences()) {
            return
        }
        currentSharedPreferences!!.edit().putBoolean(key, value).apply()
    }

    fun putString(key: String, value: String) {
        if (!checkSharedPreferences()) {
            return
        }
        currentSharedPreferences!!.edit().putString(key, value).apply()
    }

    fun putInt(key: String, value: Int) {
        if (!checkSharedPreferences()) {
            return
        }
        currentSharedPreferences!!.edit().putInt(key, value).apply()
    }

    fun putLong(key: String, value: Long) {
        if (!checkSharedPreferences()) {
            return
        }
        currentSharedPreferences!!.edit().putLong(key, value).apply()
    }


    fun removePref(key: String) {
        if (!checkSharedPreferences()) {
            return
        }
        currentSharedPreferences!!.edit().remove(key).apply()
    }

    private fun checkSharedPreferences(): Boolean {
        if (currentSharedPreferences == null) {
            setupSharedPrefs()
            if (currentSharedPreferences == null) {
                Log.e("Prefs", "Shared Preferences not set")
                return false
            }
        }
        return true
    }

    fun putFloat(key: String, value: Float?) {
        if (!checkSharedPreferences()) {
            return
        }
        currentSharedPreferences!!.edit().putFloat(key, value!!).apply()
    }

    fun putDouble(key: String, value: Double?) {
        if (!checkSharedPreferences()) {
            return
        }
        currentSharedPreferences!!.edit().putFloat(key, value!!.toFloat()).apply()

    }

}
