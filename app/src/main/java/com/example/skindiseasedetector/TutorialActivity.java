package com.example.skindiseasedetector;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.ViewPager;

import com.example.skindiseasedetector.databinding.ActivityTutorialBinding;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TutorialActivity extends AppCompatActivity {
    ActivityTutorialBinding binding =null;
    ViewPager viewPager;
    TabLayout tabLayout;
    List<String> stringList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityTutorialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tabs.setupWithViewPager(binding.viewPager);

        stringList = new ArrayList<>();
        stringList.add("https://firebasestorage.googleapis.com/v0/b/skin-disease-detector-1939c.appspot.com/o/448530351_1926298337787102_7763093319481730867_n.jpg?alt=media&token=15de4620-84f1-48b2-99c4-dde98a490c55");
        stringList.add("https://firebasestorage.googleapis.com/v0/b/skin-disease-detector-1939c.appspot.com/o/448688048_991774765930828_1276809908337420817_n.jpg?alt=media&token=5b49eeba-a7c8-45ab-8c53-b6cd0ca44232");



        binding.viewPager.setAdapter(new AdapterTutorial(this,stringList));
        autoImageSlide();


    }

    private void autoImageSlide() {
        final long Delay_ms = 10000;
        final long Period_ms = 10000;


        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (binding.viewPager.getCurrentItem() == stringList.size()-1){
                    binding.viewPager.setCurrentItem(0);
                }else {
                    binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem()+1,true);
                }
            }
        };
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(runnable);
            }
        },Delay_ms,Period_ms);
    }
}