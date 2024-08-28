package com.example.skindiseasedetector

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest

class SecondMapsActivity : AppCompatActivity(),OnMapReadyCallback {
    private var mGoogleMap: GoogleMap? = null
    private val locationPermissionRequestCode = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_second_maps)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        Places.initialize(applicationContext, "AIzaSyCiFHG1H8XznGaQMWev53Gu3Ll7PPeedaI")

        val findCurrentLocationButton:Button = findViewById(R.id.btn_find_current_location)
        val findDermaClinicsButton:Button = findViewById(R.id.btn_find_derma_clinics)

        findCurrentLocationButton.setOnClickListener{
            findCurrentLocation()
        }
        findDermaClinicsButton.setOnClickListener {
            findNearbyDermaClinics()
        }

    }

    @SuppressLint("MissingPermission")
    private fun findNearbyDermaClinics() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location:Location? ->
            if (location != null){
                val currentLatLng = LatLng(location.latitude,location.longitude)
                searchForClinics(currentLatLng)
            }else{
                Toast.makeText(this, "Unable to get Location",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchForClinics(currentLatLng:LatLng) {
        val placesClient = Places.createClient(this)
        val locationBias = RectangularBounds.newInstance(
            LatLng(currentLatLng.latitude - 0.05,currentLatLng.longitude - 0.05),
            LatLng(currentLatLng.latitude + 0.05, currentLatLng.longitude + 0.05)
        )
        val request = FindAutocompletePredictionsRequest.builder()
            .setLocationBias(locationBias)
            .setQuery("hospital")
            .build()
        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            for(prediction in response.autocompletePredictions){
                val clinicName = prediction.getPrimaryText(null).toString()
                val placeId = prediction.placeId

                mGoogleMap?.addMarker(MarkerOptions().position(currentLatLng).title(clinicName))
            }
        }.addOnFailureListener{exception ->
            Toast.makeText(this, "Error: ${exception.message}",Toast.LENGTH_SHORT).show()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionRequestCode){
            if ((grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED)){
                enableMyLocation()
            }else{
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun findCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location?->
        if (location !=null){
            val currentLatLng = LatLng(location.latitude,location.longitude)
            mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,15f))
          //  mGoogleMap?.addMarker(MarkerOptions().position(currentLatLng).title("You are here"))
        }else{
            Toast.makeText(this, "Unable to get Location",Toast.LENGTH_SHORT).show()
        }
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        enableMyLocation()
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),locationPermissionRequestCode)
            return
        }
        mGoogleMap!!.isMyLocationEnabled = true
    }
}