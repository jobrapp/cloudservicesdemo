package com.jobrapp.cloudservicesdemo.views

import android.content.Intent
import android.view.View
import com.jobrapp.cloudservicesdemo.ClickHandler

/**
 * Base class. Holds the view and click handler
 */
abstract class BaseView : ViewInterface {
    var handler : ClickHandler? = null
    lateinit var view : View

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    }

    override fun onViewCreated(view: View) {
        this.view = view
    }

    override fun setClickHandler(clickHandler: ClickHandler) {
        handler = clickHandler
    }
}