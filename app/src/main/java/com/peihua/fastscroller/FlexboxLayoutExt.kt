package com.peihua.fastscroller

import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

//fun <H : ViewHolder> FlexboxLayout.setAdapter(adapter: RecyclerView.Adapter<H>) {
//    this.removeAllViews()
//    val itemCount = adapter.itemCount
//    for (index in 0 until itemCount) {
//        val itemType = adapter.getItemViewType(index)
//        val holder = adapter.createViewHolder(this, itemType)
//        val itemView = holder.itemView
//        adapter.bindViewHolder(holder, index)
//        addView(itemView)
//    }
//}

fun <H : ViewHolder> LinearLayout.setAdapter(adapter: RecyclerView.Adapter<H>) {
    this.removeAllViews()
    val itemCount = adapter.itemCount
    for (index in 0 until itemCount) {
        val itemType = adapter.getItemViewType(index)
        val holder = adapter.createViewHolder(this, itemType)
        val itemView = holder.itemView
        adapter.bindViewHolder(holder, index)
        addView(itemView)
    }
}
