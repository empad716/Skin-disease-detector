package com.example.skindiseasedetector

import android.Manifest
import android.annotation.SuppressLint
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
import android.content.Context
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.skindiseasedetector.databinding.ActivityHomeBinding
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
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class HomeActivity : BaseActivity() {
    private lateinit var auth:FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var users: Users
    private lateinit var uid: String
    private var binding:ActivityHomeBinding? = null
    private lateinit var pb:Dialog
    private val idleHandler = Handler(Looper.getMainLooper())
    private var idleRunnable: Runnable? = null
    private val idleTimeout = 3 *60 *1000L

    private lateinit var builder: AlertDialog.Builder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        auth = Firebase.auth
        uid = auth.currentUser?.uid.toString()
        fixStatusBar()
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
        notConnected()
        onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                builder = AlertDialog.Builder(this@HomeActivity)
                builder.setTitle("Sign Out")
                builder.setMessage("Are you sure you want to Sign Out?")
                builder.setCancelable(true)
                builder.setPositiveButton("YES"){dialog,id->
                    showProgressBar()
                    if (auth.currentUser!=null){
                        if (isConnected()){
                            auth.signOut()
                            hideProgressBar()
                            startActivity(Intent(this@HomeActivity,LoginSelectionActivity::class.java))
                            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)

                        }else{
                            showToast(this@HomeActivity,"Please Check Your Connection")
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
        if(uid.isNotEmpty()){
            getUserDataWithRetry()
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
                    replaceFragment(AlgoFragment())
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
            }else{
                val data = data?.data
                var image: Bitmap? =null
                 try {
                     // image = MediaStore.Images.Media.getBitmap(this.contentResolver, data)

                    }catch (e:IOException){
                        e.printStackTrace()
                    }
                if (data != null) {
                    navigateToImageDisplayActivityUri(data)
                }
            }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    private fun navigateToImageDisplayActivity(imageBitmap: Bitmap) {
        val intent = Intent(this, DetectActivity::class.java).apply {
            putExtra("imageBitmap", imageBitmap) }
        startActivity(intent)
    }
    private fun navigateToImageDisplayActivityUri(imageUri: Uri){
        val intent = Intent(this,DetectActivity::class.java).apply {
            putExtra("imageUri",imageUri)
        }
        startActivity(intent)
    }
    private fun getUserDataWithRetry(retryCount: Int = 3){
        showProgress()
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
                    hideProgress()
                    return
                }
            }catch (e:Exception){
                Log.e("FirebaseRetry","Attempt ${currentAttempt +1}failed: ${e.message}")
            }
            delay(delayDuration)
            delayDuration *= 2
            currentAttempt++
        }
        hideProgress()
        showToast(this,"Failed to Retrieve data after $retries attempt.")
        auth.signOut()
    }
    private suspend fun getUserDataFromDatabase():Users? = suspendCoroutine { continuation ->
        databaseReference.child(uid).addListenerForSingleValueEvent(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = snapshot.getValue(Users::class.java)
                continuation.resume(users)
            }

            override fun onCancelled(error: DatabaseError) {
                auth.signOut()
                continuation.resumeWithException(Exception(error.message))

            }

        })
    }
    private fun processUserData(users: Users){
        binding!!.textViewName.setText(users.fullname)
    }

    private fun replaceFragment(fragment: Fragment){
        val user = FirebaseAuth.getInstance().currentUser
        val bundle = Bundle()
        bundle.putParcelable("user",user)
        fragment.arguments =bundle
        supportFragmentManager.beginTransaction().replace(R.id.frame_container,fragment).commit()
    }

    fun showProgress() {
        pb= Dialog(this)
        pb.setContentView(R.layout.progress_bar)
        pb.setCancelable(false)
        pb.show()
        if (!isConnected()){
            pb.hide()
            showToast(this,"Please Check your Connection")
        }
    }

    fun hideProgress() {
            pb.hide()
    }

    override fun onResume() {
        super.onResume()
        startIdleTimer()
    }

    override fun onPause() {
        super.onPause()
        stopIdleTimer()
    }
    private fun startIdleTimer(){
        idleRunnable = Runnable{
            getUserDataWithRetry()
        }
        idleHandler.postDelayed(idleRunnable!!,idleTimeout)
    }
    private fun stopIdleTimer(){
        idleRunnable?.let {
            idleHandler.removeCallbacks(it)
        }
    }
    private fun resetIdleTimer(){
        stopIdleTimer()
        startIdleTimer()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        resetIdleTimer()
        return super.onTouchEvent(event)
    }
}