package com.peacecorps.malaria.activities;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.peacecorps.malaria.utils.AuthJSONObjectRequest;
import com.peacecorps.malaria.R;
import com.peacecorps.malaria.utils.VolleyApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * Created by Chimdi on 7/18/14.
 */

/**
 * Edited by Ankita on 6/8/2015.
 */
public class PeaceCorpsPolicyFragmentActivity extends FragmentActivity {

    private TextView mPeaceCorpsPolicyLabel,pcp;
    private Button ttsButton;
    private TextToSpeech tts;

    private static String TAGPCP = PeaceCorpsPolicyFragmentActivity.class.getSimpleName();

    private ProgressDialog progressDialog;

    //json object response url
    private String urlJsonObj = "http://pc-web-dev.systers.org/api/posts/1/?format=json";

    // temporary string to show the parsed response
    private String jsonResponse;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.peace_corps_policy_fragment);

        mPeaceCorpsPolicyLabel = (TextView) findViewById(R.id.peaceCorpsPolicyLabel);
        pcp = (TextView) findViewById(R.id.pcp);
        ttsButton = (Button) findViewById(R.id.ttsButton);
        //"Please Wait" progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        //mking json object Request
        Log.i(TAGPCP, "INSIDE PEACE CORPS ACTIVITY");

        //setting font
        mPeaceCorpsPolicyLabel.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/garreg.ttf"));
        pcp.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/garreg.ttf"));

        //get the data
        makeJsonObjectRequest();

        //setup the tts language
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.UK);
                }
            }
        });


        //Implement the speak button
        ttsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toSpeak = mPeaceCorpsPolicyLabel.getText().toString();
                tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
            }
        });

    }
    //shutdown text to speech when the back button is pressed
    public void onBackPressed() {
       if(tts !=null){
            tts.stop();
            tts.shutdown();
        }
        finish();
    }

    private void makeJsonObjectRequest(){
        Log.i(TAGPCP, "INSIDE JSON OBJECT REQUEST");
        showpDialog(); //progress dialog shows loading...while the data is being fetched

        //making an authenticated JSON Object  Request below
        AuthJSONObjectRequest jsonObjReq = new AuthJSONObjectRequest(Request.Method.GET, urlJsonObj, null
                , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAGPCP, response.toString());

                try {
                    Log.i(TAGPCP,"INSIDE JSON RESPONSE");
                    //parsing json object response
                    String name = response.getString("title_post");
                    String desc = response.getString("description_post");

                    jsonResponse = "";
                    //jsonResponse += "Post: \n" + name + "\n\n";
                    jsonResponse += /*"Description: \n" +*/ desc + "\n\n";


                    mPeaceCorpsPolicyLabel.setText(jsonResponse);

                    String content = mPeaceCorpsPolicyLabel.getText().toString();
                    File file;
                    FileOutputStream outputStream;
                    try {
                        // file = File.createTempFile("MyCache", null, getCacheDir());
                        file = new File(getCacheDir(), "PCPCache");

                        outputStream = new FileOutputStream(file);
                        outputStream.write(content.getBytes());
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                hidepDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError verror) {
                Log.d(TAGPCP, "Error Retreiving Data!" + verror.getMessage());
                Toast.makeText(getApplicationContext(), "Error Retreiving Data! Loading from cache... ", Toast.LENGTH_LONG).show();

                BufferedReader input = null;
                File file = null;
                try {
                    file = new File(getCacheDir(), "PCPCache"); // Pass getFilesDir() and "MyFile" to read file

                    input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                    String line;
                    StringBuffer buffer = new StringBuffer();
                    while ((line = input.readLine()) != null) {
                        buffer.append(line);
                    }
                    mPeaceCorpsPolicyLabel.setText(buffer.toString());
                    Log.d(TAGPCP, buffer.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //hide progress dialog
                hidepDialog();
            }
        });

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                2000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Adding Request to request queue
        VolleyApplication.getInstance().addToRequestQueue(jsonObjReq);
    }

    private void showpDialog(){
        if(!progressDialog.isShowing())
            progressDialog.show();
        //for showing the loading animation in Activity
    }

    private void hidepDialog(){
        if(progressDialog.isShowing())
            progressDialog.dismiss();
        //for dismissing the loading animation in Activity
    }

}
