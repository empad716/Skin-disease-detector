package com.example.skindiseasedetector

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class LoginSelectionActivity : BaseActivity() {
    private var binding:ActivityLoginSelectionBinding? = null

    private lateinit var auth :FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var googleSignInClient :GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginSelectionBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
            }

        })
        notConnected()
        auth = Firebase.auth
        database = Firebase.database
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)


        binding?.emailSignIn?.setOnClickListener {
            val intent = Intent(this, SignInActivity ::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
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
                showToast(this, "Please Try again")
                hideProgressBar()
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
           showToast(this, "Sign In Google Failed. Please Try Again")
       }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        showProgressBar()
        val credential = GoogleAuthProvider.getCredential(account.idToken,null)

        val usersRef = FirebaseDatabase.getInstance().getReference("users")
        val intent = Intent(this,HomeActivity::class.java)
        auth.signInWithCredential(credential).addOnCompleteListener{task ->
            if (task.isSuccessful){
                val userID =auth.currentUser!!.uid
                val databaseRefe = database.reference.child("users").child(userID)

                databaseRefe.addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()){
                            startActivity(intent)
                            hideProgressBar()
                        }else{
                            val users :Users= Users(null,auth.currentUser!!.displayName,auth.currentUser!!.email,null,null,null,userID,null)
                            databaseRefe.setValue(users).addOnCompleteListener{setValueTask ->
                                if (setValueTask.isSuccessful){
                                    startActivity(intent)
                                    finish()
                                }
                                else{
                                    showToast(this@LoginSelectionActivity,"Failed. Please Try again")
                                }
                                hideProgressBar()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("TAG","Database Error: ${error.message}")
                        hideProgressBar()
                        showToast(this@LoginSelectionActivity,"Sign In Failed. Please try Again")
                    }

                })
            }else{
                hideProgressBar()
                showToast(this@LoginSelectionActivity,"Sign In Failed. Please try Again")
            }
        }

    }



}