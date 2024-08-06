package com.example.skindiseasedetector;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.skindiseasedetector.databinding.ActivityEditAccountBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditAccountActivity extends BaseActivity {
    ActivityEditAccountBinding binding =null;
    DatabaseReference databaseReference;
    String uid;
    Users users;
    FirebaseAuth auth;
    ImageButton uploadImage;
    CircleImageView profilePicture;
    Uri image=null;
    private ActivityResultLauncher<Intent> galleryLauncher =registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result->{
        if (result.getResultCode()== Activity.RESULT_OK){
            Intent data =result.getData();
            if (data!=null){
                 image = data.getData();
                binding.profilePicture.setImageURI(image);
            }
        }
    });;
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEditAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        uid = auth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        if(uid!=null){
            getUserData();
        }
        binding.birthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();

                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        EditAccountActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year,month,dayOfMonth);
                        Calendar today = Calendar.getInstance();
                        if (selectedDate.compareTo(today)>=0){
                            binding.birthDateLayout.setError("Please Input your Birth Date");
                            return;
                        }else {
                            binding.birthDateLayout.setError(null);
                        }

                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        String formattedDate = dateFormat.format(selectedDate.getTime());
                        binding.birthDate.setText(formattedDate);

                        int age = calculatedAge(selectedDate);
                        binding.age.setText(String.valueOf(age));
                    }
                },
                        year,month,day);
                datePickerDialog.show();

            }

            private int calculatedAge(Calendar birthDate) {
                Calendar today = Calendar.getInstance();
                int age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);

                if (today.get(Calendar.DAY_OF_YEAR)< birthDate.get(Calendar.DAY_OF_YEAR)){
                    age--;
                }

                return age;
            }
        });

        binding.uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(EditAccountActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(EditAccountActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_CODE_READ_EXTERNAL_STORAGE);
                    openGallery();
                }
                


            }



        });
        binding.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();

            }

            private void showDialog() {
                new AlertDialog.Builder(EditAccountActivity.this)
                        .setTitle("Edit Confirmation")
                        .setMessage("Are you sure you want to save")
                        .setPositiveButton("YES",((dialog, which)->uploadToStorage(image)))
                        .setNegativeButton("NO",null)
                        .create()
                        .show();
            }

            private void uploadToStorage(Uri image) {
                showProgressBar();
                if (image !=null){
                    StorageReference reference = FirebaseStorage.getInstance().getReference().child("profiles/"+uid+"/"+uid+".jpg");
                    UploadTask uploadTask = reference.putFile(image);
                    uploadTask.addOnSuccessListener(taskSnapshot ->{
                        reference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();
                            saveToRealTimeDB(downloadUrl);
                            hideProgressBar();
                        });
                    }).addOnFailureListener(e->{
                        Log.e("Firebase Storage","Failed",e);

                    });
                }else {
                    String ImageUrl = users.getImageUrl().toString();
                    saveToRealTimeDB(ImageUrl);
                    hideProgressBar();
                }

            }

            private void saveToRealTimeDB(String downloadUrl) {
                String userName = binding.userName.getText().toString();
                String fullName = binding.fullName.getText().toString();
                String email = binding.email.getText().toString();
                String age = binding.age.getText().toString();
                String birthDate = binding.birthDate.getText().toString();
                String address = binding.address.getText().toString();
                if(validateForm(userName,fullName,age,birthDate,address)){
                    if (auth.getCurrentUser()!=null){
                        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
                        users = new Users(userName,fullName,email,age,birthDate,address,uid,downloadUrl);
                        db.setValue(users).addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                showToast(EditAccountActivity.this,"Saved Successful");
                            }else {
                                showToast(EditAccountActivity.this,"Saved Failed");
                            }
                        });
                    }
                }else {
                    showToast(EditAccountActivity.this,"Saved Failed");
                }


            }
        });
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditAccountActivity.this,AccountDetailsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });
    }



    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

   private boolean validateForm(String userName, String fullName, String age, String birthDate, String address) {
         boolean isValid = true;
        if (TextUtils.isEmpty(userName)){
            binding.userNameLayout.setError("Enter Username");
            isValid = false;
        } else if (userName.length() >9) {
            binding.userNameLayout.setError("Username should be less than equals to 8 Characters");
            isValid = false;
        } else {
            binding.userNameLayout.setError(null);
        }
        if (TextUtils.isEmpty(fullName)){
            binding.fullNameLayout.setError("Enter Full Name");
            isValid = false;
        }else {
            binding.fullNameLayout.setError(null);
        }
        if (age.length() >=4){
            binding.ageLayout.setError("Please input your age");
            isValid = false;
        }else {
            binding.ageLayout.setError(null);
        }


       return isValid;
   }


    private void getUserData() {
        databaseReference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users = snapshot.getValue(Users.class);
                binding.userName.setText(users.getUsername());
               binding.fullName.setText(users.getFullname());
                binding.email.setText(users.getEmail());
                binding.age.setText(users.getAge());
                binding.birthDate.setText(users.getBirthdate());
                binding.address.setText(users.getAddress());


                Glide.with(getApplicationContext())
                        .load(users.getImageUrl())
                        .placeholder(R.drawable.profile_picture)
                        .circleCrop()
                        .into(binding.profilePicture);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}