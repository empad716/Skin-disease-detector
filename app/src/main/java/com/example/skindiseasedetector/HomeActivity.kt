package com.example.skindiseasedetector


import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.Fragment
import com.example.skindiseasedetector.databinding.ActivityHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener



class HomeActivity : BaseActivity() {
    private lateinit var auth:FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var users: Users
    private lateinit var uid: String
    private var binding:ActivityHomeBinding? = null
    private lateinit var bottomNavigationView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        auth = Firebase.auth
        uid = auth.currentUser?.uid.toString()
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
        bottomNavigationView = findViewById(R.id.bottom_nav)
     if(uid.isNotEmpty()){
           getUserData()
        }

        binding?.signOutBtn?.setOnClickListener{
            showProgressBar()
           if (auth.currentUser!=null){
             auth.signOut()
             startActivity(Intent(this,LoginSelectionActivity::class.java))
              finish()
               hideProgressBar()
           }
    }

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                 R.id.btn_home->{
                     replaceFragment(HomeFragment())
                     true
                 }
                R.id.btn_maps ->{
                    replaceFragment(MapsFragment())
                    true
                }
                R.id.btn_add ->{
                    showToast(this,"Undefined")
                    true
                }
                R.id.btn_history ->{
                    replaceFragment(HistoryFragment())
                    true
                }
                R.id.btn_profile ->{
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
        replaceFragment(HomeFragment())
    }

    private fun getUserData() {
        databaseReference.child(uid).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                users = snapshot.getValue(Users::class.java)!!
                binding!!.textViewName.setText(users.name)

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun replaceFragment(fragment: Fragment){
        val user = FirebaseAuth.getInstance().currentUser
        val bundle = Bundle()
        bundle.putParcelable("user",user)
        fragment.arguments =bundle
        supportFragmentManager.beginTransaction().replace(R.id.frame_container,fragment).commit()
    }

}