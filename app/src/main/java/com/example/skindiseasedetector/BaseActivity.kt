package com.example.skindiseasedetector

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import com.google.android.material.dialog.MaterialAlertDialogBuilder

open class BaseActivity : AppCompatActivity() {
    private lateinit var pb:Dialog
    private lateinit var internet:Dialog
    private lateinit var dialog: AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_base)
    }
    fun notConnected(){
        dialog = MaterialAlertDialogBuilder(this,R.style.MaterialAlertDialog_Rounded)
            .setView(R.layout.internet_connection)
            .setCancelable(false)
            .create()
        val networkManager = NetworkManager(this)
        networkManager.observe(this){
            if (!it){
                if (!dialog.isShowing)
                    dialog.show()
            }else{
                if (dialog.isShowing)
                    dialog.hide()

            }
        }
    }
    fun showProgressBar() {
        pb= Dialog(this)
        pb.setContentView(R.layout.progress_bar)
        pb.setCancelable(false)
        pb.show()
        if (!isConnected()){
            pb.hide()
            notConnected()
        }

    }
    fun hideProgressBar() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            pb.hide()
        },3000)
    }
    fun showToast(activity: Activity, msg:String) {
        Toast.makeText(activity,msg,Toast.LENGTH_SHORT).show()
    }
     fun isConnected():Boolean{
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
         if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
             val network = connectivityManager.activeNetwork ?:return false
             val activeNetwork = connectivityManager.getNetworkCapabilities(network)?:return false

             return when{
                 activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                 activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                 else -> false
             }
         }else{
             @Suppress("DEPRECATION") val networkInfo = connectivityManager.activeNetworkInfo ?:return false
             @Suppress("DEPRECATION") return networkInfo.isConnected
         }

    }
    private fun refreshActivity(){
        finish()
        startActivity(intent)
    }
    fun fixStatusBar(){
        val decorView = window.decorView
        decorView.setOnApplyWindowInsetsListener{v,insets ->
            val left = insets.systemWindowInsetLeft
            val top = insets.systemWindowInsetTop
            val right = insets.systemWindowInsetRight
            val bottom = insets.systemWindowInsetBottom
            v.setPadding(left,top, right,bottom)
            insets.consumeSystemWindowInsets()
        }
    }
}