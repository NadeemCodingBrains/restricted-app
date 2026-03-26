package com.sp.restricted

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LauncherAdapter(
    private val apps: List<AppInfo>,
    private val onAppClick: (String) -> Unit
) : RecyclerView.Adapter<LauncherAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivAppIcon)
        val tvName: TextView = view.findViewById(R.id.tvAppName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_launcher_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.ivIcon.setImageDrawable(app.icon)
        holder.tvName.text = app.name
        holder.itemView.setOnClickListener { onAppClick(app.packageName) }
    }

    override fun getItemCount() = apps.size
}