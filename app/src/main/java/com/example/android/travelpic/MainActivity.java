package com.example.android.travelpic;


import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class MainActivity extends AppCompatActivity {
    //Camera mCamera;
    CameraPreview mCameraPreview;
    //ImageButton mCameraButton;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Create our Preview view and set it as the content of our activity.
        mCameraPreview = new CameraPreview(this);
        FrameLayout preview = (FrameLayout)findViewById(R.id.camera_preview);
        preview.addView(mCameraPreview);

        //Get camera button
        //mCameraButton = (ImageButton)findViewById(R.id.camera_button);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        //Re-orient preview when device changes orientation
        mCameraPreview.OrientCamera();
    }

    public void onCameraButtonClick(View v){

        progressBar.setVisibility(View.VISIBLE);
        mCameraPreview.TakePicture();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mCameraPreview.Start();
        mCameraPreview.OrientCamera();

        progressBar.setVisibility(View.INVISIBLE);
    }
}

