package com.tasomaniac.devwidget.main

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tasomaniac.devwidget.widget.DisplayApplicationInfo

internal class AppListAdapter(
    private val appViewHolderFactory: AppViewHolder.Factory,
    private val data: List<DisplayApplicationInfo>
) : RecyclerView.Adapter<AppViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        appViewHolderFactory.createWith(parent)

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size
}
