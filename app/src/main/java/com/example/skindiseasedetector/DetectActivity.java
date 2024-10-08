package com.example.skindiseasedetector;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import com.example.skindiseasedetector.databinding.ActivityDetectBinding;
import com.example.skindiseasedetector.ml.Model;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class DetectActivity extends BaseActivity {
    ImageView imageView;
    TextView result;
    int imageSize = 224;
    ActivityDetectBinding binding = null;
    FirebaseAuth auth;
    String uid,dateTime,dateTimeData;
    androidx.appcompat.app.AlertDialog ed;

    FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     //   EdgeToEdge.enable(this);
        binding = ActivityDetectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        result = binding.result;
        imageView = binding.image;
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        uid = auth.getCurrentUser().getUid();
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
         dateTime = currentDateTime.format(dateFormatter) +" "+ currentDateTime.format(timeFormatter);
         dateTimeData = currentDateTime.format(dateFormatter)+ " "+ currentDateTime.format(DateTimeFormatter.ofPattern("hh:mm a"));

        fixStatusBar();
        Bitmap image = getIntent().getParcelableExtra("imageBitmap");
        Uri uri = getIntent().getParcelableExtra("imageUri");
        showProgressBar();
        notConnected();
        if (auth.getCurrentUser()!=null){
            if (image!=null){
                imageView.setImageBitmap(image);
                image = Bitmap.createScaledBitmap(image,imageSize,imageSize,false);
                classifyImage(image);
                hideProgressBar();
            }else if(uri !=null){
                imageView.setImageURI(uri);
                try {
                    image = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                    image = Bitmap.createScaledBitmap(image,imageSize,imageSize,false);
                }catch (IOException e){
                    e.printStackTrace();
                }
                classifyImage(image);
                hideProgressBar();

            }
        }



        binding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(DetectActivity.this)
                        .setTitle("Cancel")
                        .setMessage("Are you sure you don't want to save?")
                        .setPositiveButton("YES",((dialog,which)-> cancelIntent()))
                        .setNegativeButton("NO",null)
                        .create()
                        .show();



            }
        });
        Bitmap finalImage = image;

        binding.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    uploadImage(finalImage);

            }
        });

    }




    private void uploadImage(Bitmap bitmap) {
        showProgressBar();
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] data = baos.toByteArray();


      FirebaseStorage storage = FirebaseStorage.getInstance();
       StorageReference storageRef = storage.getReference();
        StorageReference imageRef= storageRef.child("images/"+auth.getCurrentUser().getUid()+"/"+ dateTime+".png");

        UploadTask uploadTask= imageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
           imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();

                Log.d("FirebaseStorage","Download URL:" +downloadUrl);
               saveImageUrl(downloadUrl);
            });
        }).addOnFailureListener(exception ->{
            showToast(this,"Please Check Your Connection");
            Log.e("Firebase","Upload Failed", exception);
            hideProgressBar();
        });

    }




    private void saveImageUrl(String imageUrl) {
         String diagnosis = binding.result.getText().toString();
         String cause = binding.resultCause.getText().toString();
         String symptoms = binding.resultSymptoms.getText().toString();
         String treatment = binding.resultTreatment.getText().toString();
         if (diagnosis.isEmpty()){
             binding.result.setError("Cannot be empty");
             return;
         }

         if(auth.getCurrentUser() != null){
             DatabaseReference databaseRef = database.getReference().child("users").child(auth.getCurrentUser().getUid()).child("history");
             String uniqueKey  = databaseRef.push().getKey();
             long timestamp = System.currentTimeMillis();

             UserData data = new UserData(diagnosis,cause,symptoms,treatment,dateTimeData,imageUrl,timestamp);

             if(uniqueKey !=null){
                 databaseRef.child(uniqueKey).setValue(data).addOnCompleteListener(task -> {
                     if (task.isSuccessful()){
                         Log.d("RealtimeDatabase","Image URl saved successfully");
                         showToast(DetectActivity.this,"Saved Successfully");
                         hideProgressBar();
                         saveIntent();

                     }else {
                         Log.e("RealtimeDatabase","Failed to save image",task.getException());
                         showToast(DetectActivity.this,"Saving Failed");
                         hideProgressBar();
                     }
                 });
             }



         }

    }


    private void saveIntent() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);


    }

    private void cancelIntent() {
        getOnBackPressedDispatcher().onBackPressed();
    }

    private void classifyImage(Bitmap image) {
        try {
            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            Float undefined = 0.7f;
            if (maxConfidence < undefined) {
                binding.error.setVisibility(View.VISIBLE);
                binding.btnError.setVisibility(View.VISIBLE);
                binding.btnError.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getOnBackPressedDispatcher().onBackPressed();
                    }
                });
                binding.linearLayoutDiagnosis.setVisibility(View.INVISIBLE);
                binding.linearLayoutCauses.setVisibility(View.INVISIBLE);
                binding.linearLayoutSymptoms.setVisibility(View.INVISIBLE);
                binding.linearLayoutTreatment.setVisibility(View.INVISIBLE);
                binding.save.setVisibility(View.INVISIBLE);
                binding.cancel.setVisibility(View.INVISIBLE);

            } else {

            String[] classes = {"Acne", "Eczema", "Healthy Skin", "Nail Fungus", "Psoriasis", "Vitiligo", "Warts"};
            result.setText(classes[maxPos]);

            if (classes[maxPos].equals("Acne")) {
                binding.resultCause.setText(R.string.acne_cause);
                binding.resultSymptoms.setText(R.string.acne_symptoms);
                binding.resultTreatment.setText(R.string.acne_treatment);
            } else if (classes[maxPos].equals("Eczema")) {
                binding.resultCause.setText(R.string.eczema_cause);
                binding.resultSymptoms.setText(R.string.eczema_symptoms);
                binding.resultTreatment.setText(R.string.eczema_treatment);
            } else if (classes[maxPos].equals("Healthy Skin")) {
                binding.secretHealthy.setVisibility(View.VISIBLE);
                binding.linearLayoutCauses.setVisibility(View.INVISIBLE);
                binding.linearLayoutSymptoms.setVisibility(View.INVISIBLE);
                binding.linearLayoutTreatment.setVisibility(View.INVISIBLE);
            }else if (classes[maxPos].equals("Nail Fungus")) {
                binding.resultCause.setText(R.string.nail_fungus_cause);
                binding.resultSymptoms.setText(R.string.nail_fungus_symptoms);
                binding.resultTreatment.setText(R.string.nail_fungus_treatment);
            }else if (classes[maxPos].equals("Psoriasis")) {
                binding.resultCause.setText(R.string.psoriasis_cause);
                binding.resultSymptoms.setText(R.string.psoriasis_symptoms);
                binding.resultTreatment.setText(R.string.psoriasis_treatment);
            }else if (classes[maxPos].equals("Vitiligo")) {
                binding.resultCause.setText(R.string.vitiligo_cause);
                binding.resultSymptoms.setText(R.string.vitiligo_symptoms);
                binding.resultTreatment.setText(R.string.vitiligo_treatment);
            }else if (classes[maxPos].equals("Warts")) {
                binding.resultCause.setText(R.string.warts_cause);
                binding.resultSymptoms.setText(R.string.warts_symptoms);
                binding.resultTreatment.setText(R.string.warts_treatment);
            } else {
                binding.resultCause.setText(R.string.failed);
                binding.resultSymptoms.setText(R.string.failed);
                binding.resultTreatment.setText(R.string.failed);
            }
        }

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}