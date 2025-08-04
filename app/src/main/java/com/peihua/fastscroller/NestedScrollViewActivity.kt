package com.peihua.fastscroller

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.peihua.fastscroller.databinding.ActivityNestedScrollViewBinding

class NestedScrollViewActivity : AppCompatActivity(R.layout.activity_nested_scroll_view) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityNestedScrollViewBinding.bind(findViewById(R.id.cl_root))
        binding.apply {
            llContent.setAdapter(DataMainAdapter(this@NestedScrollViewActivity))
        }
    }
}