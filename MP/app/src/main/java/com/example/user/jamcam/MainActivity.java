package com.example.user.jamcam;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.user.jamcam.Helper.InternetCheck;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabel;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.ByteArrayOutputStream;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    CameraView cameraView;
    Button btnDetect;
    android.app.AlertDialog waitingDialog;

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        cameraView = (CameraView) findViewById(R.id.camera_view);
        btnDetect = (Button) findViewById(R.id.btn_detect);

        waitingDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Processing image...")
                .setCancelable(false).build();

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                waitingDialog.show();
                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap, cameraView.getWidth(), cameraView.getHeight(), false);
                cameraView.stop();

                runDetector(bitmap);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

        btnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.start();
                cameraView.captureImage();
            }

        });
    }

    private void runDetector(final Bitmap bitmap) {

        final FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        new InternetCheck(new InternetCheck.Consumer() {
            @Override
            public void accept(boolean internet) {
//                if(internet){
//                    //If have internet we will use Cloud
//                    FirebaseVisionCloudDetectorOptions options =
//                            new FirebaseVisionCloudDetectorOptions.Builder()
//                            .setMaxResults(1) //Get 1 result with highest confidence threshold
//                            .build();
//                    FirebaseVisionCloudLabelDetector detector = FirebaseVision.getInstance().getVisionCloudLabelDetector(options);
//
//                    detector.detectInImage(image)
//                            .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLabel>>() {
//                                @Override
//                                public void onSuccess(List<FirebaseVisionCloudLabel> firebaseVisionCloudLabels) {
//                                    processDataResultCloud(firebaseVisionCloudLabels);
//
//                                }
//                            })
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Log.d("EDMTERROR",e.getMessage());
//                                }
//                            });
//                }
//                else{
                FirebaseVisionLabelDetectorOptions options =
                        new FirebaseVisionLabelDetectorOptions.Builder()
                                .setConfidenceThreshold(0.8f) //Get highest confidence threshold
                                .build();
                FirebaseVisionLabelDetector detector = FirebaseVision.getInstance().getVisionLabelDetector(options);

                detector.detectInImage(image)
                        .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionLabel>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionLabel> firebaseVisionLabels) {
                                processDataResult(firebaseVisionLabels, bitmap);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("EDMTERROR", e.getMessage());
                            }
                        });
//                }
            }

        });
    }

    private void processDataResultCloud(List<FirebaseVisionCloudLabel> firebaseVisionCloudLabels) {
        for (FirebaseVisionCloudLabel label : firebaseVisionCloudLabels) {
            Toast.makeText(this, "Cloud result: " + label.getLabel(), Toast.LENGTH_SHORT).show();
        }

        if (waitingDialog.isShowing())
            waitingDialog.dismiss();
    }

    private void processDataResult(List<FirebaseVisionLabel> firebaseVisionLabels, Bitmap bitmap) {
        String detection = "";
        StringBuilder detections = new StringBuilder("");
        int ctr = 0;
        for (FirebaseVisionLabel label : firebaseVisionLabels) {
//            Toast.makeText(this,"Device result: "+ label.getLabel(),Toast.LENGTH_SHORT).show();
//            detection = detection + ", " + label.getLabel();
            if(ctr > 0){
                detections.append(", ");
            }
            detections.append(label.getLabel());
            ctr++;
        }
        if(detections.equals("")){
            detections.append("Image not recognized. Try again.");
        }

        if (waitingDialog.isShowing())
            waitingDialog.dismiss();

        encodeBitmap(bitmap, detections.toString());
    }

    public void encodeBitmap(Bitmap bitmap, String detection) {  // your bitmap
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bs);
        Intent intent = new Intent(MainActivity.this, ProcessedActivity.class);
        intent.putExtra("byteArray", bs.toByteArray()); //Image
        intent.putExtra("detections", detection); //Description of image
        startActivity(intent);
    }

}
