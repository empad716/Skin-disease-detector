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
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DetectActivity extends BaseActivity {
    ImageView imageView;
    TextView result;
    int imageSize = 128;
    ActivityDetectBinding binding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDetectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        result = binding.result;
        imageView = binding.image;

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
                saveIntent();
            }
        });
      //  camera.setOnClickListener(new View.OnClickListener() {
        //    @Override
          //  public void onClick(View view) {
            //    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
              //      Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //    startActivityForResult(cameraIntent, 3);
                //} else {
                  //  requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                //}
            //}
        //});
        //gallery.setOnClickListener(new View.OnClickListener() {
          //  @Override
            //public void onClick(View view) {
              //  Intent cameraIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //startActivityForResult(cameraIntent, 1);
           // }
        //});
    }

    private void saveIntent() {
        showProgressBar();
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
        hideProgressBar();
    }

    private void cancelIntent() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
    //@Override
  //  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    //    if(resultCode == RESULT_OK){
      //      if(requestCode == 3){
        //        Bitmap image = (Bitmap) data.getExtras().get("data");
          //      int dimension = Math.min(image.getWidth(), image.getHeight());
            //    image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
             //   imageView.setImageBitmap(image);

               // image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                //classifyImage(image);
            //}else{
              //  Uri dat = data.getData();
                //Bitmap image = null;
                //try {
                  //  image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dat);
                //} catch (IOException e) {
                  //  e.printStackTrace();
                //}
                //imageView.setImageBitmap(image);

                //image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                //classifyImage(image);
            //}

        //}
        //super.onActivityResult(requestCode, resultCode, data);
    //}
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
            //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
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
            // find the index of the class with the biggest confidence.
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            String[] classes = {"Acne", "Eczema","Healthy Skin","Melanocytic Nevi","Melanoma","Psoriasis"};
            result.setText(classes[maxPos]);

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }

    }
}