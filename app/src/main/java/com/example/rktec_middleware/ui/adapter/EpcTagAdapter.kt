package com.example.rktec_middleware.ui.screens

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rktec_middleware.data.model.EpcTag
import com.example.rktec_middleware.R

class EpcTagAdapter(private val tags: List<EpcTag>) : RecyclerView.Adapter<EpcTagAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEpc: TextView = view.findViewById(R.id.tvEpc)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tag, parent, false)
        return ViewHolder(view)
    }
    override fun getItemCount() = tags.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvEpc.text = tags[position].epc
    }
}
