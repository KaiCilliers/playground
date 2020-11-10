package com.example.playground.paging.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.playground.R

class SeparatorViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    private val description: TextView = view.findViewById(R.id.separator_description)
    fun bind(separatorText: String) {
        description.text = separatorText
    }
    companion object {
        fun create(parent: ViewGroup): SeparatorViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.separator_view_list_item, parent, false)
            return SeparatorViewHolder(view)
        }
    }
}