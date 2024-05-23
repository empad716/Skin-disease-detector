package com.example.skindiseasedetector

import  android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.skindiseasedetector.databinding.ActivitySignUpBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database

class SignUpActivity : BaseActivity() {
    private var binding:ActivitySignUpBinding? =null
    private lateinit var auth:FirebaseAuth
    private lateinit var database: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        auth = Firebase.auth
        database = Firebase.database


        binding?.textViewAR?.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
          overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
            finish()
        }
        binding?.btnSignUp?.setOnClickListener { registerUser() }

    }



    private fun registerUser(){
            val name = binding?.nameUp?.text.toString()
            val email = binding?.emailUp?.text.toString()
            val pass = binding?.passUp?.text.toString()
            val retypePass = binding?.retypePassUp?.text.toString()
        if (validateForm(name,email,pass,retypePass)){
                showProgressBar()
                auth.createUserWithEmailAndPassword(email,pass)
                    .addOnCompleteListener{task->
                        if (task.isSuccessful){
                            val databaseRef = database.reference.child("users").child(auth.currentUser!!.uid)
                            val users :Users= Users(name,email,auth.currentUser!!.uid)

                            databaseRef.setValue(users)
                                .addOnCompleteListener {task->
                                if (task.isSuccessful){
                                    showToast(this, "User Account Created Successfully")
                                    startActivity(Intent(this,HomeActivity::class.java))
                                    finish()
                                    hideProgressBar()
                                }
                                    else{
                                    showToast(this, "User Account Registration Failed. Please Try Again")
                                    hideProgressBar()
                                }
                            }

                        }
                                else {
                            showToast(this, "User Account Registration Failed. Please Try Again")
                            hideProgressBar()
                        }
                    }
        }
    }


    private fun validateForm(name: String, email: String, pass: String, retypePass: String): Boolean {
        return when{
            TextUtils.isEmpty(name)->{
                binding?.nameLayout?.error = "Enter Name"
                false
            }
            TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()->{
                binding?.emailLayout?.error = "Enter Valid Email Address"
                false
            }
            TextUtils.isEmpty(pass)->{
                binding?.passwordLayout?.error = "Enter Password"
                false
            }
            (pass.length <=  8)->{
                binding?.passwordLayout?.error = "Password should be 8 Characters"
                false
            }
            TextUtils.isEmpty(retypePass)->{
                binding?.RetypePasswordLayout?.error = "Enter Confirm Password"
                false
            }
            (pass != retypePass)->{
                binding?.RetypePasswordLayout?.error = "Password does not match"
                false
            }

            else->{true}
        }
    }
}