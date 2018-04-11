package com.jobrapp.cloudservicesdemo.views

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.jobrapp.cloudservicesdemo.R
import com.jobrapp.cloudservicesdemo.ServicesData
import com.jobrapp.cloudservicesdemo.SourceTypes


/**
 *
 */
class ServicesView : BaseView() {
    lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
        recyclerView = inflater.inflate(R.layout.recycler_view, container, false) as RecyclerView

        recyclerView.layoutManager = LinearLayoutManager(inflater.context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = ServicesAdapter()
        return recyclerView
    }

    override fun startAuth() {
    }

    inner class ServicesAdapter : RecyclerView.Adapter<Holder>() {
        val sourceTypes = SourceTypes.values()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            return Holder(LayoutInflater.from(parent.context).inflate(R.layout.source_item, parent, false))
        }

        override fun getItemCount(): Int = sourceTypes.size

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.sourceName.text = sourceTypes[position].getDeviceName(holder.itemView.context)//sources[position]
            holder.sourceIcon.setImageResource(sourceTypes[position].icon) //source_icons[position])
            holder.itemView.setOnClickListener {
                handler?.handleClick(ServicesData(sourceTypes[position].fileStackSource))
            }
        }

    }

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val sourceIcon : ImageView
        val sourceName : TextView
        init {
            sourceIcon = view.findViewById(R.id.source_icon)
            sourceName = view.findViewById(R.id.sourceName)
        }
    }
}