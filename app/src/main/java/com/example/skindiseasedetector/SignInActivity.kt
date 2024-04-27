package com.example.skindiseasedetector

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.skindiseasedetector.databinding.ActivitySignInBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class SignInActivity : BaseActivity() {
    private var binding: ActivitySignInBinding? = null
    private lateinit var auth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        auth = Firebase.auth
        binding?.textViewNR?.setOnClickListener {
         startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
        binding?.textViewForgotPass?.setOnClickListener {
            startActivity(Intent(this,ForgotPasswordActivity::class.java))
        }
        binding?.btnSignIn?.setOnClickListener {
            signInUser()
        }
    }

    private fun signInUser(){
        val email = binding?.emailIn?.text.toString()
        val pass = binding?.passIn?.text.toString()
        if(validateForm(email,pass)){
            showProgressBar()

            auth.signInWithEmailAndPassword(email,pass)
                .addOnCompleteListener{task ->
                    if (task.isSuccessful){
                         startActivity(Intent(this,HomeActivity::class.java))
                        finish()
                        hideProgressBar()
                    }
                    else{
                        showToast(this, "Incorrect Email or Password. Please Try Again")
                        hideProgressBar()
                    }
                }
        }
    }

    private fun validateForm(email: String, pass: String): Boolean {
        return when{
            TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()->{
                binding?.emailLayout?.error = "Enter Valid Email Address"
                false
            }
            TextUtils.isEmpty(pass)->{
                binding?.passwordLayout?.error = "Enter Password"
                false
            }
            else->{true}
        }
    }
}