package com.example.android.travelpic;

import android.content.Context;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.WINDOW_SERVICE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

/**
 * Created by Nos on 25/08/2017.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Context mContext;
    private Camera.PictureCallback mPicture;


    public CameraPreview(final Context context){

        super(context);
        mContext = context;

        this.Start();
        this.OrientCamera();

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //Define a take picture callback
        mPicture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {


                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null){
                    Log.d("Error", "Error creating media file, check storage permissions");
                    Toast toast = Toast.makeText(mContext, "Error saving file", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();

                    ClassifyImageTask classifyImageTask = new ClassifyImageTask(getContext());
                    classifyImageTask.execute(pictureFile);

                } catch (FileNotFoundException e) {
                    Log.d("Error", "File not found: " + e.getMessage());
                    Toast toast = Toast.makeText(mContext, "File not found", Toast.LENGTH_LONG);
                    toast.show();
                } catch (IOException e) {
                    Log.d("Error", "Error accessing file: " + e.getMessage());
                    Toast toast = Toast.makeText(mContext, "Access error", Toast.LENGTH_LONG);
                    toast.show();
                }

                //mCamera.startPreview();
            }

        };


    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        //Surface has been created, now tell the camera to display the view
        try {

            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        }
        catch (Exception e){
            Log.d("Error", e.getMessage());
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        //Take care of event when preview changes

        if (mHolder.getSurface() == null){
            //there is no preview
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d("Error", "Error starting camera preview: " + e.getMessage());
        }

    }

    int getCorrectCameraOrientation(Camera.CameraInfo info, Camera camera, Context context) {


        Display display = ((WindowManager) context.getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        int rotation = display.getRotation();
        int degrees = 0;

        switch(rotation){
            case Surface.ROTATION_0:
                degrees = 0;
                break;

            case Surface.ROTATION_90:
                degrees = 90;
                break;

            case Surface.ROTATION_180:
                degrees = 180;
                break;

            case Surface.ROTATION_270:
                degrees = 270;
                break;

        }

        int result;
        if(info.facing==Camera.CameraInfo.CAMERA_FACING_FRONT){
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        }else{
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }


    public void OrientCamera(){

        Camera.CameraInfo info = new Camera.CameraInfo();
        mCamera.getCameraInfo(0,info);
        mCamera.setDisplayOrientation(getCorrectCameraOrientation(info,mCamera,mContext));
        mCamera.getParameters().setRotation(getCorrectCameraOrientation(info,mCamera,mContext));
    }

    public void Start(){

        if (mCamera!=null){
            mCamera.release();
            mCamera = null;
        }

        mCamera = getCameraInstance();
        try {
            mCamera.setPreviewDisplay(mHolder);
        }
        catch (Exception e){
            Log.d("Error", "Error starting camera preview: " + e.getMessage());
        }
        mCamera.startPreview();
    }

    Camera getCameraInstance(){

        Camera c = null;

        try {
            c = Camera.open();

        }

        catch (Exception e) {

            Toast.makeText(mContext, "Camera is not available", Toast.LENGTH_LONG);
        }
        return  c;
    }

    public void TakePicture(){

        this.OrientCamera();
        mCamera.takePicture(null, null, mPicture);
    }


    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "TravelPic");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }
}
