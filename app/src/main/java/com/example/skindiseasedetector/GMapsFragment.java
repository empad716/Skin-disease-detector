package com.example.skindiseasedetector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.skindiseasedetector.databinding.ActivityMapsBinding;
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

public class GMapsFragment extends Fragment {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int Request_code = 101;
    private double lat,lng;
    Button derma;
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
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_g_maps, container, false);

        return view;
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

        derma = view.findViewById(R.id.derma);
        derma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
                stringBuilder.append("location="+lat+","+lng);
                stringBuilder.append("&radius=1000");
                stringBuilder.append("&type=hospital");
                stringBuilder.append("&sensor=true");
                stringBuilder.append("&key="+getResources().getString(R.string.google_map_api_key));

                String url = stringBuilder.toString();
                Object dataFetch[]= new Object[2];
                dataFetch[0]=mMap;
                dataFetch[1]=url;

                FetchData fetchData = new FetchData();
                fetchData.execute(dataFetch);
            }
        });

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
                    mMap.addMarker(new MarkerOptions().position(latLng).title("current location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
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
}