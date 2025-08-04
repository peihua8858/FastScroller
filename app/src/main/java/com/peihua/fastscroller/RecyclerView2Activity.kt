package com.peihua.fastscroller

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.peihua.fastscroller.databinding.ActivityRecyclerView2Binding
import com.peihua.fastscroller.databinding.ActivityRecyclerViewBinding

class RecyclerView2Activity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_recycler_view2)
        val binding = ActivityRecyclerView2Binding.bind(findViewById(R.id.cl_root))
        binding.apply {
//            CustomFastScroller.initFastScroller(recycleView)
            val mAdapter = DataMainAdapter(this@RecyclerView2Activity)
//            val data = mutableListOf<String>()
//            for (index in 0 until 50) {
//                data.add("item>>>$index")
//            }
//            mAdapter.mData.addAll(data)
            recyclerView.setAdapter(mAdapter)
            recyclerView.setItemAnimator(null)
            recyclerView.setLayoutManager(LinearLayoutManager(this@RecyclerView2Activity))
            recyclerView.setOverScrollMode(2)
            CustomFastScroller.initFastScroller(recyclerView)
//            OldScrollbarHelper.bindRecyclerView(recyclerView,scrollbarView)
        }
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
    }
}