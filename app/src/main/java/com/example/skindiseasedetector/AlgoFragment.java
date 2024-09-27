package com.example.skindiseasedetector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AlgoFragment extends Fragment {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int Request_code = 101;
    private double lat,lng;
    Button derma;
    Button dermaClinic;
    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            enableLocation();
        }

        private void enableLocation() {
            if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Request_code);
                return;
            }
            mMap.setMyLocationEnabled(true);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_algo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity().getApplicationContext());

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
        FloatingActionButton currentLocation = view.findViewById(R.id.currentLocation);
        currentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {getCurrentLocation();}
        });
        dermaClinic = view.findViewById(R.id.dermaClinic);
        dermaClinic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Search Nearby Derma Clinic", Toast.LENGTH_SHORT).show();
                List<LocationData> locations = new ArrayList<>();
                locations.add(new LocationData(new LatLng(14.203782229397378, 121.15488115290603),"V.E. Eusebio Skin Center (SM Calamba)"));
                locations.add(new LocationData(new LatLng(14.204274101353347, 121.15377458846758),"Dr. Cheryl Rose D. Tana"));
                locations.add(new LocationData(new LatLng(14.203557961016976, 121.15501517477153),"Dermcare - SM Calamba"));
                locations.add(new LocationData(new LatLng(14.204465506577494, 121.15483147534393),"Derma Care Skin Hair and Spa"));
                locations.add(new LocationData(new LatLng(14.20983818150572, 121.16594008114195),"Calm Beauty Lounge"));
                locations.add(new LocationData(new LatLng(14.213725870134118, 121.1660621232304),"DermalCare Skin Clinic"));
                locations.add(new LocationData(new LatLng(14.206751949015391, 121.15632414549842),"Skin Basics Dermatology Clinic"));
                locations.add(new LocationData(new LatLng(14.206634278385877, 121.15504962485717),"Urbanessence Beauty & Wellness Center -- Calamba"));
                locations.add(new LocationData(new LatLng(14.213704396867053, 121.15055837672975),"Asian Derma Clinic - Calamba, Laguna"));
               // locations.add(new LocationData(new LatLng(14.192510681509496, 121.13867486642421),"La Julieta Beauty Center - Calamba"));
                locations.add(new LocationData(new LatLng(14.19306554501082, 121.16564282540413),"Skingoals Aesthetics Calamba Branch"));
                locations.add(new LocationData(new LatLng(14.192962576989796, 121.16590835053773),"AOM Aesthetics, Skin Care & Wellness Clinic"));
                for (LocationData location: locations){
                    new Handler(Looper.getMainLooper()).postDelayed(()->{
                                addCustomMarker(location);
                            },3000);
                    //  addCustomMarker(location);
                }


            }
        });
    }
    private void addCustomMarker(LocationData location) {
        if (ActivityCompat.checkSelfPermission(
                getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Request_code);
            return;
        }
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(60000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(5000);
        LocationCallback locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d("GMapsFragment","location result is="+locationResult);

                if ( locationResult== null){
                    Toast.makeText(getActivity(), "Current location is null", Toast.LENGTH_SHORT).show();

                    return;
                }
                for (Location location:locationResult.getLocations()){
                    if (location!=null){
                        Log.e("GMapsFragment","Current location is"+location.getLongitude()+"  "+location.getLatitude());

                    }
                }
            }
        };
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,null);
        Task<Location> task= fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location!=null){
                    lat = location.getLatitude();
                    lng = location.getLongitude();

                    LatLng latLng = new LatLng(lat,lng);
                    // mMap.addMarker(new MarkerOptions().position(latLng).title("current location"));
                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                   // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,13));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,13));
                }

            }
        });
        mMap.addMarker(new MarkerOptions().position(location.getLatLng()).title(location.getTitle()));

    }
    private void getCurrentLocation(){
        if (ActivityCompat.checkSelfPermission(
                getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Request_code);
            return;
        }
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(60000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(5000);
        LocationCallback locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d("GMapsFragment","location result is="+locationResult);

                if ( locationResult== null){
                    Toast.makeText(getActivity(), "Current location is null", Toast.LENGTH_SHORT).show();

                    return;
                }
                for (Location location:locationResult.getLocations()){
                    if (location!=null){
                        Log.e("GMapsFragment","Current location is"+location.getLongitude()+"  "+location.getLatitude());

                    }
                }
            }
        };
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,null);
        Task<Location> task= fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location!=null){
                    lat = location.getLatitude();
                    lng = location.getLongitude();

                    LatLng latLng = new LatLng(lat,lng);
                    // mMap.addMarker(new MarkerOptions().position(latLng).title("current location"));
                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,16));

                }

            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case Request_code:
                if (grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    getCurrentLocation();
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    public class LocationData{
        private LatLng latLng;
        private String title;

        public LocationData(LatLng latLng, String title){
            this.latLng = latLng;
            this.title = title;
        }

        public LatLng getLatLng() {
            return latLng;
        }
        public String getTitle(){
            return title;
        }
    }
}