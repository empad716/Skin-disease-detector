package com.example.skindiseasedetector

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.example.skindiseasedetector.databinding.ActivityAccountDetailsBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AccountDetailsActivity : BaseActivity() {
    private var binding: ActivityAccountDetailsBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var uid: String
    private lateinit var users: Users
    private lateinit var pb:Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        binding = ActivityAccountDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        auth = Firebase.auth
        uid = auth.currentUser?.uid.toString()
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
        if (uid.isNotEmpty()) {
            getUserDataWithRetry()
        }
        notConnected()
        binding!!.imageButton.setOnClickListener {
            val intent = Intent(this@AccountDetailsActivity, HomeActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        binding!!.edit.setOnClickListener {
            val intent = Intent(this@AccountDetailsActivity, EditAccountActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }
    private fun getUserDataWithRetry(retryCount: Int = 3){

        CoroutineScope(Dispatchers.Main).launch {
            retryFirebaseOperation(retryCount)
        }
    }
    private suspend fun retryFirebaseOperation(retries: Int) {
        var currentAttempt = 0
        var delayDuration = 1000L

        while (currentAttempt<retries){
            try {
                val result = getUserDataFromDatabase()
                if (result != null){
                    processUserData(result)

                    return
                }
            }catch (e:Exception){
                Log.e("FirebaseRetry","Attempt ${currentAttempt +1}failed: ${e.message}")
            }
            delay(delayDuration)
            delayDuration *= 2
            currentAttempt++
        }

        showToast(this,"Failed to Retrieve data after $retries attempt.")
    }
    private suspend fun getUserDataFromDatabase():Users? = suspendCoroutine { continuation ->
        databaseReference.child(uid).addListenerForSingleValueEvent(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = snapshot.getValue(Users::class.java)
                continuation.resume(users)
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWithException(Exception(error.message))
            }

        })
    }
    private fun processUserData(users: Users){
        binding?.userName?.setText(users.username)
        binding?.fullName?.setText(users.fullname)
        binding?.email?.setText(users.email)
        binding?.age?.setText(users.age)
        binding?.birthDate?.setText(users.birthdate)
        binding?.address?.setText(users.address)
        Glide.with(applicationContext)
            .load(users.imageUrl)
            .placeholder(R.drawable.profile_picture)
            .circleCrop()
            .into(binding!!.profilePicture)
    }

    private fun userData() {
        databaseReference.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                users = snapshot.getValue(Users::class.java)!!
                binding?.userName?.setText(users.username)
                binding?.fullName?.setText(users.fullname)
                binding?.email?.setText(users.email)
                binding?.age?.setText(users.age)
                binding?.birthDate?.setText(users.birthdate)
                binding?.address?.setText(users.address)
                Glide.with(applicationContext)
                    .load(users.imageUrl)
                    .placeholder(R.drawable.profile_picture)
                    .circleCrop()
                    .into(binding!!.profilePicture)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

}