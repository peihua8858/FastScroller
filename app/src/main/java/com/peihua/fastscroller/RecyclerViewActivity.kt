package com.peihua.fastscroller

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.peihua.fastscroller.databinding.ActivityRecyclerViewBinding

class RecyclerViewActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_recycler_view)
        val binding = ActivityRecyclerViewBinding.bind(findViewById(R.id.cl_root))
        binding.apply {
//            CustomFastScroller.initFastScroller(recycleView)
            val mAdapter = DataMainAdapter(this@RecyclerViewActivity)
//            val data = mutableListOf<String>()
//            for (index in 0 until 50) {
//                data.add("item>>>$index")
//            }
//            mAdapter.mData.addAll(data)
            recyclerView.setAdapter(mAdapter)
            recyclerView.setItemAnimator(null)
            recyclerView.setLayoutManager(LinearLayoutManager(this@RecyclerViewActivity))
            recyclerView.setOverScrollMode(2)
//            OldScrollbarHelper.bindRecyclerView(recyclerView,scrollbarView)
        }
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
    }
}