package com.example.skindiseasedetector

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.skindiseasedetector.databinding.ActivityAnonHomeBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class AnonHomeActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth
    private var binding: ActivityAnonHomeBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAnonHomeBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        auth = Firebase.auth
        findViewById<TextView>(R.id.textViewAnon).text = auth.currentUser?.uid
        binding?.signOutBtn?.setOnClickListener{
            showProgressBar()
            if (auth.currentUser!=null){
                auth.signOut()
                startActivity(Intent(this,LoginSelectionActivity::class.java))
                finish()
                hideProgressBar()
            }
        }
    }
}