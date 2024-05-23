package com.example.skindiseasedetector

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.skindiseasedetector.databinding.ActivityMain2Binding
import com.example.skindiseasedetector.databinding.ActivityMainBinding

class MainActivity2 : AppCompatActivity() {
    private var binding: ActivityMain2Binding? =null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding?.root)
        onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
            }

        })

        binding?.button?.setOnClickListener {
            val intent = Intent(this, MainActivity3::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
        }
        binding?.skip?.setOnClickListener{
            startActivity(Intent(this, LoginSelectionActivity::class.java))
        }

    }
}