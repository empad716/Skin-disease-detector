package com.example.skindiseasedetector

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast



open class BaseFragment : Fragment() {
    private lateinit var pb:Dialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       val view = inflater.inflate(R.layout.fragment_base, container, false)
        return view
    }

    fun showProgressBar() {
            pb= Dialog(requireContext())
            pb.setContentView(R.layout.progress_bar)
            pb.setCancelable(false)
            pb.show()
            if (!isConnected()){
                pb.hide()
                showToast("Please Check your Connection")
            }
    }

    fun hideProgressBar() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            pb.hide()
        },5000)

    }
    fun showToast(msg:String) {
        Toast.makeText(activity,msg, Toast.LENGTH_SHORT).show()
    }

    fun isConnected():Boolean{
        val connectivityManager = activity?.applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo!=null && connectivityManager.activeNetworkInfo!!.isConnectedOrConnecting

    }

}