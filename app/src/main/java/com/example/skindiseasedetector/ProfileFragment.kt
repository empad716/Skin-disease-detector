package com.example.skindiseasedetector


import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class ProfileFragment : BaseFragment(){
    private lateinit var auth: FirebaseAuth
    private lateinit var signOut: LinearLayout
    private lateinit var accountDetailsBtn: LinearLayout
    private lateinit var databaseReference: DatabaseReference
    private lateinit var users: Users
    private lateinit var uid: String
    private lateinit var builder: AlertDialog.Builder
    private lateinit var tutorial:LinearLayout
    private lateinit var about:LinearLayout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        signOut = view.findViewById(R.id.signOutBtn)
        accountDetailsBtn = view.findViewById(R.id.accountDetails)
        tutorial = view.findViewById(R.id.tutorial)
        about = view.findViewById(R.id.about)
        auth = Firebase.auth
        uid = auth.currentUser?.uid.toString()
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
        if(uid.isNotEmpty()){
            getUserDataWithRetry()
        }

        signOut.setOnClickListener {
            builder = AlertDialog.Builder(activity)
            builder.setTitle("Sign Out")
            builder.setMessage("Are you sure you want to sign out?")
            builder.setCancelable(true)
            builder.setPositiveButton("YES"){dialog,id->
                showProgressBar()
                if (auth.currentUser!=null){
                    if (isConnected()){
                        auth.signOut()
                        hideProgressBar()
                        startActivity(Intent(activity,LoginSelectionActivity::class.java))
                        activity?.overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                    }else{
                        showToast("Please Check your Connection")
                    }

                }
            }
            builder.setNegativeButton("NO"){dialog,id->
                dialog.cancel()
            }
            builder.create().show()
        }
        accountDetailsBtn.setOnClickListener{
            startActivity(Intent(activity, AccountDetailsActivity::class.java))
            activity?.overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
        }
        tutorial.setOnClickListener{
            startActivity(Intent(activity,TutorialActivity::class.java))
            activity?.overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
        }
        about.setOnClickListener{
            startActivity(Intent(activity,ThirdMapsActivity::class.java))
        }

        return view
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
        showToast("Failed to Retrieve data after $retries attempt.")
        auth.signOut()
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
        val text = view?.findViewById<TextView>(R.id.textViewName)
        if (users.username.isNullOrBlank()){
            text?.setText(users.fullname)
        }else{
            text?.setText(users.username)
        }
        val img = view?.findViewById<ImageView>(R.id.profilePicture)
        Glide.with(this@ProfileFragment)
                .load(users.imageUrl)
                .placeholder(R.drawable.profile_picture)
                .circleCrop()
                .into(img!!)

    }

    private fun getUserData() {

        databaseReference.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    users = snapshot.getValue(Users::class.java)!!
                    val text = view?.findViewById<TextView>(R.id.textViewName)
                    if (users.username.isNullOrBlank()){
                        text?.setText(users.fullname)
                    }else{
                        text?.setText(users.username)
                    }
                    val img = view!!.findViewById<ImageView>(R.id.profilePicture)
                    Glide.with(this@ProfileFragment)
                        .load(users.imageUrl)
                        .placeholder(R.drawable.profile_picture)
                        .circleCrop()
                        .into(img)
                }else{
                    auth.signOut()
                    startActivity(Intent(activity,LoginSelectionActivity::class.java))
                    activity?.overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                    showToast("Please Check Your Connection")
                }

            }

            override fun onCancelled(error: DatabaseError) {
                auth.signOut()
                startActivity(Intent(activity,LoginSelectionActivity::class.java))
                activity?.overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                showToast("Failed to Retrieve data: ${error.message}")
            }

        })
    }


}