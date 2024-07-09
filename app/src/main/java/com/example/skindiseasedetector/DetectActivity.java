package com.example.skindiseasedetector;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import com.example.skindiseasedetector.databinding.ActivityDetectBinding;
import com.example.skindiseasedetector.ml.Model;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DetectActivity extends BaseActivity {
    ImageView imageView;
    TextView result;
    int imageSize = 128;
    ActivityDetectBinding binding = null;
    FirebaseAuth auth;
    String uid;
    DatabaseReference databaseReference;
    FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDetectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        result = binding.result;
        imageView = binding.image;
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        uid = auth.getCurrentUser().getUid();
        binding.textViewName.setText(uid);
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
        String dateTime = currentDateTime.format(dateFormatter) +" "+ currentDateTime.format(timeFormatter);
        String dateTimeData = currentDateTime.format(dateFormatter)+ " "+ currentDateTime.format(DateTimeFormatter.ofPattern("hh:mma"));


        Bitmap image = getIntent().getParcelableExtra("imageBitmap");
        imageView.setImageBitmap(image);
        image = Bitmap.createScaledBitmap(image,imageSize,imageSize,false);
        classifyImage(image);




        binding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { cancelIntent();
            }
        });
        binding.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String diagnosis = binding.result.getText().toString();
                String cause = binding.resultCause.getText().toString();
                String symptoms = binding.resultSymptoms.getText().toString();
                String treatment = binding.resultTreatment.getText().toString();

                if (diagnosis.isEmpty()){
                    binding.result.setError("Cannot be empty");
                    return;
                }
                if (cause.isEmpty()){
                    binding.resultCause.setError("Cannot be empty");
                    return;
                }
                if (symptoms.isEmpty()){
                    binding.resultSymptoms.setError("Cannot be empty");
                    return;
                }
                if (treatment.isEmpty()){
                    binding.resultTreatment.setError("Cannot be empty");
                    return;
                }
                showProgressBar();
                if(auth.getCurrentUser() != null){
                   DatabaseReference databaseRef = database.getReference().child("users").child(auth.getCurrentUser().getUid()).child("history")
                           .child(dateTime);
                   UserData data = new UserData(diagnosis,cause,symptoms,treatment,dateTimeData);
                   databaseRef.setValue(data);
                    saveIntent();
                    hideProgressBar();
                }

            }
        });

    }


    private void saveIntent() {

       Intent intent = new Intent(this, HomeActivity.class);
       startActivity(intent);
       overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    private void cancelIntent() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    private void classifyImage(Bitmap image) {
        try {
            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 128, 128, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            for(int i = 0; i < imageSize; i ++){
                for(int j = 0; j < imageSize; j++){
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 1));
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
            String[] classes = {"Acne", "Eczema","Melanocytic Nevi","Melanoma","Normal Skin","Psoriasis"};
            result.setText(classes[maxPos]);

            if(classes[maxPos].equals("Normal Skin")){
                binding.resultCause.setText(R.string.normal_skin_cause);
                binding.resultSymptoms.setText(R.string.normal_skin_symptoms);
                binding.resultTreatment.setText(R.string.normal_skin_treatment);
            } else if (classes[maxPos].equals("Melanoma")) {
                binding.resultCause.setText(R.string.melanoma_cause);
            }
            else {
                binding.resultCause.setText(R.string.failed);
                binding.resultSymptoms.setText(R.string.failed);
                binding.resultTreatment.setText(R.string.failed);
            }

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }

    }
}