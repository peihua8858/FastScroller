package com.peihua.fastscroller

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.peihua.fastscroller.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val binding = ActivityMainBinding.bind(findViewById(R.id.cl_root))
        binding.apply {
        }
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
    }

    fun onRecyclerView(view: View) {
        startActivity(Intent(this, RecyclerViewActivity::class.java))
    }

    fun onScrollView(view: View) {
        startActivity(Intent(this, ScrollViewActivity::class.java))
    }

    fun onNestedScrollView(view: View) {
        startActivity(Intent(this, NestedScrollViewActivity::class.java))
    }

    fun onRecyclerView2(view: View) {
        startActivity(Intent(this, RecyclerView2Activity::class.java))
    }
}