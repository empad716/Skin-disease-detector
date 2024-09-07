package com.example.skindiseasedetector;

import static com.example.skindiseasedetector.Adapterhistory.context;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.skindiseasedetector.databinding.ActivityInfoBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class InfoActivity extends BaseActivity {
    ActivityInfoBinding binding = null;
    DatabaseReference databaseReference;
    FirebaseAuth auth;
    AlertDialog.Builder builder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        notConnected();


        Intent intent = getIntent();
        String diagnosis = intent.getStringExtra("diagnosis");
        String cause = intent.getStringExtra("cause");
        String symptoms = intent.getStringExtra("symptoms");
        String treatment = intent.getStringExtra("treatment");
        String date = intent.getStringExtra("date");
        String imageUrl = intent.getStringExtra("image");
        long timestamp = intent.getLongExtra("timestamp",0);
        auth= FirebaseAuth.getInstance();
        
        binding.resultDiagnosis.setText(diagnosis);
        binding.resultCause.setText(cause);
        binding.resultSymptoms.setText(symptoms);
        binding.resultTreatment.setText(treatment);
        binding.resultDate.setText(date);



        Glide.with(binding.image.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_background)
                .into(binding.image);


        binding.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }

            private void showDialog() {
                new AlertDialog.Builder(InfoActivity.this)
                        .setTitle("Delete Confirmation")
                        .setMessage("Are you sure you want to delete this item?")
                        .setPositiveButton("YES",((dialog, which) -> deleteItemHistory(timestamp)))
                        .setNegativeButton("NO", null)
                        .create()
                        .show();
            }


            private void backIntent() {
                Intent intent1 = new Intent(InfoActivity.this, HomeActivity.class);
                startActivity(intent1);
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }

            private void deleteItemHistory(long timestamp) {
                showProgressBar();
                databaseReference = FirebaseDatabase.getInstance().getReference()
                        .child("users")
                        .child(auth.getCurrentUser().getUid())
                        .child("history");

                Query query = databaseReference.orderByChild("timestamp").equalTo(timestamp);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                            UserData userData = snapshot.getValue(UserData.class);
                            if (userData!=null){
                                String imageUrl = userData.getImageUrl();
                                snapshot.getRef().removeValue();
                                showToast(InfoActivity.this,"Delete Successfully");
                                hideProgressBar();
                                if (imageUrl !=null && !imageUrl.isEmpty()){
                                    deleteImageFromStorage(imageUrl);
                                }
                            }

                        }
                       backIntent();

                   }

                    private void deleteImageFromStorage(String imageUrl) {
                        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);

                        storageRef.delete().addOnSuccessListener(a ->{
                            Log.d("FirebaseStorage","Image Deleted Successfully");
                        }).addOnFailureListener(exception->{
                            Log.e("FirebaseStorage","Failed to delete image",exception);
                        });
                    }

                    @Override
                   public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(InfoActivity.this,"Failed to delete data "+error.getMessage(),Toast.LENGTH_SHORT).show();
                   }
                });
           }
        });

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               backIntent();

            }

            private void backIntent() {
                Intent intent1 = new Intent(InfoActivity.this, HomeActivity.class);
                startActivity(intent1);
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });

    }
}