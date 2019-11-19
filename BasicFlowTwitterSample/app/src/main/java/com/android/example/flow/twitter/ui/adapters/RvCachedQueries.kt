package com.android.example.flow.twitter.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.example.flow.twitter.R


/**
 * Created by Santanu 😁 on 2019-11-19.
 */
class RvCachedQueries(private var submitQuery: (String) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val cachedList: MutableList<String> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CachedQueriesViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_search,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = cachedList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CachedQueriesViewHolder).bind(cachedList[position])
    }

    fun updateList(data: List<String>) {
        cachedList.apply {
            clear()
            addAll(data)
            notifyDataSetChanged()
        }
    }

    inner class CachedQueriesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvQuery = itemView.findViewById<View>(R.id.tvQuery) as TextView

        fun bind(text: String) {
            tvQuery.text = text
            itemView.setOnClickListener {
                submitQuery(cachedList[adapterPosition])
            }
        }
    }
}