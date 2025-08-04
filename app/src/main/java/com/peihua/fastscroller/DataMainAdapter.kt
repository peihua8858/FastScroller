package com.peihua.fastscroller

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DataMainAdapter(private val context: Context) : RecyclerView.Adapter<BaseViewHolder>() {
    val mData = mutableListOf<String>()
    init {
        for (index in 0 until 50) {
            mData.add("item>>>$index")
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(LayoutInflater.from(context).inflate(R.layout.item_view, parent, false))
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.textView.text = mData[position]
    }

}

class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textView: TextView = itemView.findViewById(R.id.text_view)
}