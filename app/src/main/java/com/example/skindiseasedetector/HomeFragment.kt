package com.example.skindiseasedetector

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Timer
import java.util.TimerTask

class HomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    lateinit var tabLayout: TabLayout
    lateinit var stringList: List<String>

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
        val tabs = view.findViewById<TabLayout>(R.id.tabs)
        val viewPager = view.findViewById<ViewPager>(R.id.viewPager)
        tabs.setupWithViewPager(viewPager)
        val stringList = ArrayList<String>()
        stringList.add("https://firebasestorage.googleapis.com/v0/b/skin-disease-detector-1939c.appspot.com/o/448530351_1926298337787102_7763093319481730867_n.jpg?alt=media&token=15de4620-84f1-48b2-99c4-dde98a490c55")
        stringList.add("https://firebasestorage.googleapis.com/v0/b/skin-disease-detector-1939c.appspot.com/o/448688048_991774765930828_1276809908337420817_n.jpg?alt=media&token=5b49eeba-a7c8-45ab-8c53-b6cd0ca44232")
        viewPager.adapter = AdapterHome(activity,stringList)
        autoImageSlide()
        return view
    }

    private fun autoImageSlide() {
        val delayMs: Long = 10000
        val periodMs: Long = 10000
        val viewPager = view?.findViewById<ViewPager>(R.id.viewPager)
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if(viewPager?.currentItem == stringList.size - 1){
                    viewPager.currentItem = 0
                }else{
                    viewPager?.setCurrentItem(viewPager.currentItem +1,true)
                }
            }
            
        }
        val timer = Timer()
        timer.schedule(object :TimerTask(){
            override fun run() {
                handler.post(runnable)
            }
        },delayMs,periodMs)
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