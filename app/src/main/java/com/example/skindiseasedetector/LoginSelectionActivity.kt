package com.example.skindiseasedetector

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.example.skindiseasedetector.databinding.ActivityLoginSelectionBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database

class LoginSelectionActivity : BaseActivity() {
    private var binding:ActivityLoginSelectionBinding? = null
    private lateinit var button: Button
    private lateinit var auth :FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var googleSignInClient :GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginSelectionBinding.inflate(layoutInflater)
        setContentView(binding?.root)
         button = findViewById(R.id.emailSignIn)
        auth = Firebase.auth
        database = Firebase.database
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)


        button.setOnClickListener {
            val intent = Intent(this, SignInActivity ::class.java)
            startActivity(intent)
        }
        binding?.googleSignIn?.setOnClickListener {
            signInWithGoogle()
        }

         val auth = Firebase.auth
        if(auth.currentUser != null){
            startActivity(Intent(this,HomeActivity::class.java))
            finish()
        }

        binding?.anonSignIn?.setOnClickListener{
            anonymousAuth()
        }
    }

    private fun anonymousAuth() {
        showProgressBar()
        auth.signInAnonymously()
            .addOnSuccessListener {
                startActivity(Intent(this,AnonHomeActivity::class.java))
                hideProgressBar()
            }
            .addOnFailureListener{
                Log.d("TAG","anonymousAuth: $it")
            }
    }

    private fun signInWithGoogle(){
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result->
        if (result.resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
       if (task.isSuccessful){
           val account:GoogleSignInAccount? = task.result
           if (account!=null){
               updateUI(account)
           }
       }
       else{
           showToast(this, "SigIn Google Failed. Please Try Again")
       }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        showProgressBar()
        val credential = GoogleAuthProvider.getCredential(account.idToken,null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful){
                val databaseRef = database.reference.child("users").child(auth.currentUser!!.uid)
                val users :Users= Users(auth.currentUser!!.displayName,auth.currentUser!!.email,auth.currentUser!!.uid)
                databaseRef.setValue(users)
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            startActivity(Intent(this,HomeActivity::class.java))
                            finish()
                            hideProgressBar()
                        }
                    }

            }
            else{
                showToast(this, "Incorrect Email or Password. Please Try Again")
                hideProgressBar()
            }
        }
    }




}