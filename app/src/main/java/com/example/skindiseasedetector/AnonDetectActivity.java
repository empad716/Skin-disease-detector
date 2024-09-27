package com.example.skindiseasedetector;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.skindiseasedetector.databinding.ActivityAnonDetectBinding;
import com.example.skindiseasedetector.databinding.ActivityDetectBinding;
import com.example.skindiseasedetector.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AnonDetectActivity extends BaseActivity {
    ImageView imageView;
    TextView result;
    int imageSize = 224;
    ActivityAnonDetectBinding binding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding= ActivityAnonDetectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        result = binding.result;
        imageView = binding.image;
        notConnected();
        Bitmap image = getIntent().getParcelableExtra("imageBitmap");
        imageView.setImageBitmap(image);
        image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
        classifyImage(image);

        fixStatusBar();
        binding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(AnonDetectActivity.this)
                        .setTitle("Cancel")
                        .setMessage("Are you sure you don't want to save?")
                        .setPositiveButton("YES",((dialog,which)-> cancelIntent()))
                        .setNegativeButton("NO",null)
                        .create()
                        .show();
            }
        });

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
            // TODO Handle the exception
        }

    }
}