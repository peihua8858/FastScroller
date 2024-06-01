package com.peihua.fastscroller

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.peihua.fastscroller.databinding.ActivityScrollViewBinding

class ScrollViewActivity : AppCompatActivity(R.layout.activity_scroll_view) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityScrollViewBinding.bind(findViewById(R.id.cl_root))
        binding.apply {
            llContent.setAdapter(DataMainAdapter(this@ScrollViewActivity))
        }
    }
}