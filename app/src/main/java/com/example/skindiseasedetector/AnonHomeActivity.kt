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
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.skindiseasedetector.databinding.ActivityAnonHomeBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import java.io.IOException

class AnonHomeActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var builder: AlertDialog.Builder
    private var binding: ActivityAnonHomeBinding? = null
    private var image: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAnonHomeBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        auth = Firebase.auth
        notConnected()
        //findViewById<TextView>(R.id.textViewAnon).text = auth.currentUser?.uid
        onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {

                builder = AlertDialog.Builder(this@AnonHomeActivity)
                builder.setTitle("Sign Out")
                builder.setMessage("Are you sure you want to Sign Out?")
                builder.setCancelable(true)
                builder.setPositiveButton("YES"){dialog,id->
                    if (auth.currentUser!=null){
                        auth.currentUser?.delete()?.addOnCompleteListener { task->
                            if (task.isSuccessful){
                                showProgressBar()
                                auth.signOut()
                                hideProgressBar()
                                startActivity(Intent(this@AnonHomeActivity,LoginSelectionActivity::class.java))
                                this@AnonHomeActivity.overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                            }else{
                                showToast(this@AnonHomeActivity,"Please Check Your Connection")
                            }

                        }
                    }
                }
                builder.setNegativeButton("NO"){dialog,id->
                    dialog.cancel()
                    hideProgressBar()
                }
                builder.create().show()
                hideProgressBar()
            }
        })
        binding?.signOutBtn?.setOnClickListener{
            showProgressBar()
                if (auth.currentUser!=null){
                    auth.signOut()
                    startActivity(Intent(this,LoginSelectionActivity::class.java))
                    hideProgressBar()
                }


        }
        binding?.bottomNav?.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.btn_home->{
                    replaceFragment(AnonHomeFragment())
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
                    replaceFragment(AnonHistoryFragment())
                    true
                }
                R.id.btn_profile ->{
                    replaceFragment(AnonProfileFragment())
                    true
                }
                else -> false
            }
        }
        replaceFragment(AnonHomeFragment())
    }
    private fun replaceFragment(fragment: Fragment){
        val user = FirebaseAuth.getInstance().currentUser
        val bundle = Bundle()
        bundle.putParcelable("user",user)
        fragment.arguments =bundle
        supportFragmentManager.beginTransaction().replace(R.id.frame_container,fragment).commit()
    }
    private fun showDialog() {
        val dialog: Dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.selection_dialog)

        val cameraLayout: LinearLayout = dialog.findViewById(R.id.camera)
        val galleryLayout: LinearLayout = dialog.findViewById(R.id.gallery)
        val cancelLayout: LinearLayout = dialog.findViewById(R.id.cancel)

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
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
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
                val dat: Uri? = data?.data
                try {
                    image = MediaStore.Images.Media.getBitmap(this.contentResolver, dat)
                }catch (e: IOException){
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
        val intent = Intent(this, AnonDetectActivity::class.java).apply {
            putExtra("imageBitmap", imageBitmap)
        }
        startActivity(intent)
    }

}