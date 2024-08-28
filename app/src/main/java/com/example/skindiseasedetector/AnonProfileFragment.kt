package com.example.skindiseasedetector

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class AnonProfileFragment : BaseFragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var button: LinearLayout
    private lateinit var builder: AlertDialog.Builder
    private lateinit var tutorial: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_anon_profile, container, false)
        auth = Firebase.auth
        button = view.findViewById(R.id.signOutBtn)
        tutorial = view.findViewById(R.id.tutorial)
        //view.findViewById<TextView>(R.id.textViewAnon).text = auth.currentUser?.uid
        button.setOnClickListener {
            builder = AlertDialog.Builder(activity)
            builder.setTitle("Sign Out")
            builder.setMessage("Are you sure you want to sign out?")
            builder.setCancelable(true)
            builder.setPositiveButton("YES"){dialog,id->
                if (auth.currentUser!=null){
                    auth.currentUser?.delete()?.addOnCompleteListener { task->
                        if (task.isSuccessful){
                            showProgressBar()
                            auth.signOut()
                            hideProgressBar()
                            startActivity(Intent(activity,LoginSelectionActivity::class.java))
                            activity?.overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                        }else{
                            Toast.makeText(activity,"Please Check Your Connection", Toast.LENGTH_SHORT).show()
                        }

                    }

                }
            }
            builder.setNegativeButton("NO"){dialog,id->
                dialog.cancel()
            }
            builder.create().show()
        }
        tutorial.setOnClickListener{
            startActivity(Intent(activity,TutorialActivity::class.java))
            activity?.overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
        }

        return view
    }


}