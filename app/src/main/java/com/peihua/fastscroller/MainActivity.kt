package com.peihua.fastscroller

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.peihua.fastscroller.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val binding = ActivityMainBinding.bind(findViewById(R.id.cl_root))
        binding.apply {
//            CustomFastScroller.initFastScroller(recycleView)
            val mAdapter = MainAdapter(this@MainActivity)
            val data = mutableListOf<String>()
            for (index in 0 until 50) {
                data.add("item>>>$index")
            }
            mAdapter.mData.addAll(data)
            recyclerView.setAdapter(mAdapter)
            recyclerView.setItemAnimator(null)
            recyclerView.setLayoutManager(LinearLayoutManager(this@MainActivity))
            recyclerView.setOverScrollMode(2)
//            OldScrollbarHelper.bindRecyclerView(recyclerView,scrollbarView)
        }
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
    }

    class MainAdapter(private val context: Context) : RecyclerView.Adapter<BaseViewHolder>() {
        val mData = mutableListOf<String>()
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

    class BaseViewHolder(itemView: View) : ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.text_view)
    }
}