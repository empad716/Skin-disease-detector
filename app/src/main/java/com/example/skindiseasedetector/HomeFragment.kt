package com.example.skindiseasedetector

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.w3c.dom.Text

class HomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        auth = Firebase.auth
       // FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        countPhotosUploaded()
        countWithoutProblems("Normal Skin")
        countDiagnoseProblems("Normal Skin")
        return view
    }
private fun countPhotosUploaded(){
    databaseReference = FirebaseDatabase.getInstance().getReference("users").child(auth.currentUser!!.uid).child("history")
    databaseReference.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val itemCount = snapshot.childrenCount
            val photosUploaded = view?.findViewById<TextView>(R.id.photosUploaded)
            photosUploaded?.text = itemCount.toString()
        }

        override fun onCancelled(error: DatabaseError) {
            val photosUploaded = view?.findViewById<TextView>(R.id.photosUploaded)
            photosUploaded?.text =  "Error: ${error.message}"
        }
    })
}
private fun countWithoutProblems(targetDiagnosis:String){
    databaseReference = FirebaseDatabase.getInstance().getReference("users").child(auth.currentUser!!.uid).child("history")
    databaseReference.addValueEventListener(object: ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {
            var count = 0
            for(historySnapshot in snapshot.children){
                val diagnosis = historySnapshot.child("diagnosis").getValue(String::class.java)
                if (diagnosis == targetDiagnosis){
                    count++
                }
            }
            val withoutProblems = view?.findViewById<TextView>(R.id.withoutProblems)
            withoutProblems?.text = count.toString()
        }

        override fun onCancelled(error: DatabaseError) {
            val withoutProblems = view?.findViewById<TextView>(R.id.withoutProblems)
            withoutProblems?.text = "Error: ${error.message}"
        }

    })
}
    private fun countDiagnoseProblems(targetDiagnosis:String){
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(auth.currentUser!!.uid).child("history")
        databaseReference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var count = 0
                for(historySnapshot in snapshot.children){
                    val diagnosis = historySnapshot.child("diagnosis").getValue(String::class.java)
                    if (diagnosis != targetDiagnosis){
                        count++
                    }
                }
                val diagnoseProblems = view?.findViewById<TextView>(R.id.diagnoseProblems)
                diagnoseProblems?.text = count.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                val diagnoseProblems = view?.findViewById<TextView>(R.id.diagnoseProblems)
              diagnoseProblems?.text = "Error: ${error.message}"
            }

        })
    }
}