package com.example.android.travelpic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;



public class PlaceActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    TextView nameTextView;
    TextView infoTextView;
    TextView historyTextView;
    ImageView imageView;
    TextToSpeech tts;
    boolean speaking;
    String[] sentences;
    int currentSentence;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);

        nameTextView = (TextView)findViewById(R.id.Name);
        infoTextView = (TextView)findViewById(R.id.INFOTEXT);
        historyTextView = (TextView)findViewById(R.id.HISTORYTEXT);
        imageView = (ImageView)findViewById(R.id.place_image);


        //Get results from image classification
        String json = getIntent().getStringExtra("RESULT");
        String placeID = getPlaceIDFromJSON(json);

        //Query XML for place ID
        Triplet result = getPlaceInfo(placeID);

        //Fill layout with results from query
        if (result != null) {

            String name = result.getFirst().toString();
            nameTextView.setText(name);

            String info = result.getSecond().toString();
            infoTextView.setText(info);

            String history = result.getThird().toString();
            historyTextView.setText(history);

            //Set image according to results
            imageView.setImageResource(getResources().getIdentifier(placeID, "mipmap", getPackageName()));

            //Split texts into sentences, to avoid waiting for large texts to be spoken
            sentences = info.concat(history).split("\\.");
            currentSentence = 0;
            tts = new TextToSpeech(this, this);


        } else {

            Toast toast = Toast.makeText(this, "CAN'T FIND PLACE DATA", Toast.LENGTH_SHORT);
            toast.show();
        }

    }


    public String getPlaceIDFromJSON(String json){

        String placeID = "";

        try {
            JSONObject jsonObject = new JSONObject(json);
            int statusCode = jsonObject.getJSONObject("status").getInt("code");

            if (statusCode == 10000) {

                JSONArray concepts = jsonObject.getJSONArray("outputs").
                        getJSONObject(0).getJSONObject("data").
                        getJSONArray("concepts");

                placeID = concepts.getJSONObject(0).getString("id");

            }

        }

        catch (JSONException jex){
            Log.d("JSONEXC", jex.getMessage());

            Toast toast = Toast.makeText(this,"PROBLEM PARSING JSON", Toast.LENGTH_SHORT);
            toast.show();

        }

        return placeID;
    }

    public Triplet getPlaceInfo(String placeID){

        Triplet result = null;

        String name = "";
        String info = "";
        String history = "";

        XmlResourceParser xmlResourceParser = getResources().getXml(R.xml.places);

        int eventType = -1;

        boolean found = false;

        while (eventType!=XmlResourceParser.END_DOCUMENT){

            String nodeName = xmlResourceParser.getName();
            String attributeValue  = xmlResourceParser.getAttributeValue(null, "id");

            if(nodeName!=null && nodeName.equals("place") &&
                attributeValue!=null && attributeValue.equals(placeID)){

                found = true;

            }

            if (found){

                if(nodeName!=null && nodeName.equals("name") && found) {

                    try {
                        name = xmlResourceParser.nextText();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }

                else if(nodeName!=null && nodeName.equals("info") && found) {

                    try {
                        info = xmlResourceParser.nextText();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }

                else if(nodeName!=null && nodeName.equals("history") && found) {

                    try {
                        history = xmlResourceParser.nextText();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    result = new Triplet(name, info, history);
                    break;
                }
            }

            try {
                eventType = xmlResourceParser.next();
            } catch (XmlPullParserException xpullex) {
                Log.d("XPULLEX", xpullex.getMessage());
            } catch (IOException iex) {
                Log.d("IEX", iex.getMessage());
            }
        }


        return result;
    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);

        }
        else {

            Toast toast = Toast.makeText(this, "SPEECH TO TEXT INIT PROBLEM", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void onVoiceButtonClick(View view){

        for (int i = 0; i < sentences.length; i++) {
           tts.speak(sentences[i] , TextToSpeech.QUEUE_ADD, null);
        }

    }

}



