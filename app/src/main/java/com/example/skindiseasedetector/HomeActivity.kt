package com.example.skindiseasedetector

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.skindiseasedetector.databinding.ActivityHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
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
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private val REQUEST_PERMISSION = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        auth = Firebase.auth
        uid = auth.currentUser?.uid.toString()
        databaseReference = FirebaseDatabase.getInstance().getReference("users")


        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                REQUEST_PERMISSION
            )
        }



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

        binding?.bottomNav?.setOnItemSelectedListener { menuItem ->
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
                    startActivity(Intent(this, UploadActivity::class.java))
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

    private fun showDialog() {
        val dialog:Dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.selection_dialog)

        val cameraLayout:LinearLayout = dialog.findViewById(R.id.camera)
        val galleryLayout:LinearLayout = dialog.findViewById(R.id.gallery)
        val cancelLayout:LinearLayout = dialog.findViewById(R.id.cancel)

        cameraLayout.setOnClickListener{
            dialog.dismiss()
            openCamera()

        }
        galleryLayout.setOnClickListener{
            dialog.dismiss()
            openGallery()

        }
        cancelLayout.setOnClickListener{
            dialog.dismiss()
        }
        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)

    }

    private fun openGallery() {
        val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageIntent.type = "image/*"
        startActivityForResult(pickImageIntent, REQUEST_IMAGE_PICK)


    }



    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                        navigateToImageDisplayActivityA(imageBitmap)

                }
                REQUEST_IMAGE_PICK -> {
                    val imageUri: Uri? = data?.data
                    imageUri?.let {
                        navigateToImageDisplayActivity(it)
                    }
                }
            }
        }
    }
    private fun navigateToImageDisplayActivity(imageUri: Uri) {
        val intent = Intent(this, UploadActivity::class.java).apply {
            putExtra("imageUri", imageUri.toString())
        }
        startActivity(intent)
    }
    private fun navigateToImageDisplayActivityA(imageBitmap: Bitmap) {
        val intent = Intent(this, UploadActivity::class.java).apply {
            putExtra("imageBitmap", imageBitmap)
        }
        startActivity(intent)
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