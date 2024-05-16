package com.example.skindiseasedetector

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.skindiseasedetector.databinding.ActivityForgotPasswordBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class ForgotPasswordActivity : BaseActivity() {
    private var binding:ActivityForgotPasswordBinding? = null
    private lateinit var button: Button
    private lateinit var auth :FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        auth = Firebase.auth
        binding?.submitBtn?.setOnClickListener{
            resetPassword()
        }


    }

    private fun validateForm(email: String): Boolean {
        return when {
            TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding?.emailLayout?.error = "Enter Valid Email Address"
                false
            }

            else -> true
        }
    }

    private fun resetPassword(){
        val email = binding?.emailForgot?.text.toString()
        if(validateForm(email)){
            showProgressBar()
            auth.sendPasswordResetEmail(email).addOnCompleteListener { task->
                if(task.isSuccessful){
                    hideProgressBar()
                    binding?.emailLayout?.visibility = View.GONE
                    binding?.SubmitMsg?.visibility =View.VISIBLE
                    binding?.submitBtn?.visibility = View.GONE
                }
                else{
                    hideProgressBar()
                    showToast(this, "Cannot Reset your Password. Please Try Again")
                }
            }
        }
    }


}