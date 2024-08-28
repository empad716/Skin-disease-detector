package com.example.skindiseasedetector

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class AnonHistoryFragment : BaseFragment() {
    private lateinit var signUp: TextView
    private lateinit var builder: AlertDialog.Builder
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_anon_history, container, false)
        signUp = view.findViewById(R.id.signUp)
        auth = Firebase.auth
        signUp.setOnClickListener{
            builder = AlertDialog.Builder(activity)
            builder.setTitle("Create Account")
            builder.setMessage("Are you sure you want to Create an Account  ?")
            builder.setCancelable(true)
            builder.setPositiveButton("YES"){dialog,id->
                if (auth.currentUser!=null){
                    auth.currentUser?.delete()?.addOnCompleteListener {task ->
                        if (task.isSuccessful){
                            showProgressBar()
                            auth.signOut()
                            hideProgressBar()
                            startActivity(Intent(activity,LoginSelectionActivity::class.java))
                            activity?.overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                        }else{
                            Toast.makeText(activity,"Please Check Your Connection",Toast.LENGTH_SHORT).show()
                        }

                    }



                }
            }
            builder.setNegativeButton("NO"){dialog,id->
                dialog.cancel()
            }
            builder.create().show()
        }

        return view
    }


}