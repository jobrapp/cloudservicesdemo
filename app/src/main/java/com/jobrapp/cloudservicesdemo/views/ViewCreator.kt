package com.jobrapp.cloudservicesdemo.views

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 *
 */
object ViewCreator {

    fun setupView(activity: Activity, frame : ViewGroup, viewInterface : ViewInterface) : View {
        val view = viewInterface.onCreateView(LayoutInflater.from(activity), frame)
        viewInterface.onViewCreated(view)
        return view
    }
}