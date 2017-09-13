package com.example.android.travelpic;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.util.List;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.ClarifaiResponse;
import clarifai2.api.request.ClarifaiRequest;
import clarifai2.dto.input.ClarifaiImage;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Prediction;


/**
 * Created by Nos on 21/08/2017.
 */


public class ClassifyImageTask extends AsyncTask<File, Void, String>{

    Context context;


    public ClassifyImageTask(Context context){

        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(File... params) {

        File imageFile = params[0];

        //Call to image classification service
        ClarifaiClient client = new ClarifaiBuilder("bd7cea8d2aad4777a6b8c5d3ca2c3a8d").buildSync();
        return client.predict("main").withInputs(
                ClarifaiInput.forImage(ClarifaiImage.of(imageFile))
        ).executeSync().rawBody();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        Intent intent = new Intent(context, PlaceActivity.class);
        intent.putExtra("RESULT", s);
        context.startActivity(intent);
    }
}
