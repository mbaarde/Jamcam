package com.example.user.jamcam;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ProcessedActivity extends AppCompatActivity {

    public static SQLiteHelper sqLiteHelper;
    private ImageView imageView;
    private TextView textView;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processed);

        imageView = findViewById(R.id.imageView2);
        textView = findViewById(R.id.textView);
        button = findViewById(R.id.button);

        sqLiteHelper = new SQLiteHelper(this, "ImageDB.sqlite", null, 1);

        sqLiteHelper.queryData("CREATE TABLE IF NOT EXISTS IMAGES(Id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, image BLOB)");

        if (getIntent().hasExtra("byteArray")) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(
                    getIntent().getByteArrayExtra("byteArray"), 0, getIntent().getByteArrayExtra("byteArray").length);
            imageView.setImageBitmap(bitmap);
            textView.setText(getIntent().getStringExtra("detections"));
        }

        if (getIntent().hasExtra("hide")) {
            button.setVisibility(View.INVISIBLE);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] bytearray = getIntent().getByteArrayExtra("byteArray");

                try {
                    sqLiteHelper.insertData(
                            getIntent().getStringExtra("detections"),
                            bytearray
                    );
                    Toast.makeText(getApplicationContext(), "Added successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProcessedActivity.this, ImageList.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


}
