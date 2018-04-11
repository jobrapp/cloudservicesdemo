package com.jobrapp.cloudservicesdemo.views

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jobrapp.cloudservicesdemo.ClickHandler

/**
 * All Views must implement this interface
 */
interface ViewInterface {
    fun setClickHandler(clickHandler : ClickHandler) {}
    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?) : View
    fun onViewCreated(view: View)
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    fun startAuth()
    fun onPause() {}
    fun onResume() {}
}