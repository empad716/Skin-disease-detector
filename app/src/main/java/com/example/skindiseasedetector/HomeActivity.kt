package com.example.skindiseasedetector

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.Fragment
import com.example.skindiseasedetector.databinding.ActivityHomeBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.IOException


class HomeActivity : BaseActivity() {
    private lateinit var auth:FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var users: Users
    private lateinit var uid: String
    private var binding:ActivityHomeBinding? = null
    private var image: Bitmap? = null
    private lateinit var builder: AlertDialog.Builder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        auth = Firebase.auth
        uid = auth.currentUser?.uid.toString()
        databaseReference = FirebaseDatabase.getInstance().getReference("users")

        onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                showProgressBar()
                builder = AlertDialog.Builder(this@HomeActivity)
                builder.setTitle("Sign Out")
                builder.setMessage("Are you sure you want to Sign Out?")
                builder.setCancelable(true)
                builder.setPositiveButton("YES"){dialog,id->
                    if (auth.currentUser!=null){
                        auth.signOut()
                        startActivity(Intent(this@HomeActivity,LoginSelectionActivity::class.java))
                        hideProgressBar()
                    }
                }
                builder.setNegativeButton("NO"){dialog,id->
                    dialog.cancel()
                }
                builder.create().show()
            }
        })

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
                    showDialog()
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
        startActivityForResult(pickImageIntent,1)
    }
    private fun openCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent,3)
        }
        else{
            requestPermissions(arrayOf(Manifest.permission.CAMERA),100)
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == RESULT_OK) {
            if (requestCode ==3){
                var image = data?.extras?.get("data") as Bitmap
                val dimension:Int = Math.min(image.width,image.height)
                image = ThumbnailUtils.extractThumbnail(image ,dimension,dimension)
                navigateToImageDisplayActivity(image)
               // image = Bitmap.createScaledBitmap(image,imageSize,imageSize,false)
                //classifyImage(image)
            }else{
                val dat: Uri? = data!!.data
                try {
                    image = MediaStore.Images.Media.getBitmap(this.contentResolver, dat)

                }catch (e:IOException){
                    e.printStackTrace()
                }
                image?.let { navigateToImageDisplayActivity(it) }
               // image = image?.let { Bitmap.createScaledBitmap(it, imageSize,imageSize,false) }
                //classifyImage(image)
            }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    private fun navigateToImageDisplayActivity(imageBitmap: Bitmap) {
        showProgressBar()
        val intent = Intent(this, DetectActivity::class.java).apply {
            putExtra("imageBitmap", imageBitmap)
        }
        startActivity(intent)
        hideProgressBar()
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