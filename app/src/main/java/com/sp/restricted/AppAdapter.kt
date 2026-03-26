package com.sp.restricted

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(private val apps: List<AppInfo>) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivAppIcon)
        val tvName: TextView = view.findViewById(R.id.tvAppName)
        val cbAllow: CheckBox = view.findViewById(R.id.cbAllow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.ivIcon.setImageDrawable(app.icon)
        holder.tvName.text = app.name
        
        // Remove listener before setting state to avoid recursion issues
        holder.cbAllow.setOnCheckedChangeListener(null)
        holder.cbAllow.isChecked = app.isAllowed

        holder.itemView.setOnClickListener {
            app.isAllowed = !app.isAllowed
            notifyItemChanged(position)
        }

        holder.cbAllow.setOnCheckedChangeListener { _, isChecked ->
            app.isAllowed = isChecked
        }
    }

    override fun getItemCount() = apps.size

    fun getAllowedPackages(): List<String> {
        return apps.filter { it.isAllowed }.map { it.packageName }
    }
}