package com.example.user.jamcam;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

public class MenuActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        ImageButton scanButton = findViewById(R.id.imageButton3);
        ImageButton galleryButton = findViewById(R.id.imageButton5);

        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //(1) Intents can be used to tell the application a new activity is about
                //    to be created.
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                //(2) To start a new activity, the startActivity function should be called
                //    that is found in the MainActivity.
                MenuActivity.this.startActivity(intent);
            }
        });

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ImageList.class);
                MenuActivity.this.startActivity(intent);
            }
        });

    }
}
