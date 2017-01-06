package com.peacecorps.malaria.activities;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageRequest;
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

/**
 * Created by Ankita on 6/8/2015.
 */
public class PercentSideEffectsFragmentActivity extends FragmentActivity {

    //views and dialogs
    private TextView mPercentSideEffectsLabel,pse;
    private ImageView sideEffectImage;

    private static String TAGPSE = PercentSideEffectsFragmentActivity.class.getSimpleName();

    private ProgressDialog progressDialog;

    //json object response url
    private String urlJsonObj = "http://pc-web-dev.systers.org/api/posts/2/?format=json";
    private String urlImage ="https://cloud.githubusercontent.com/assets/8321130/17277636/4af66d7a-5765-11e6-9030-ddabe7d040cf.jpg";

    // temporary string to show the parsed response
    private String jsonResponse;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //views
        setContentView(R.layout.side_effects_percentage_fragment);

        mPercentSideEffectsLabel = (TextView) findViewById(R.id.percentSideEffectsLabel);
        sideEffectImage =(ImageView)findViewById(R.id.percent_side_effect_image);
        pse=(TextView)findViewById(R.id.pse);

        //"Please Wait" progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        //mking json object Request
        Log.i(TAGPSE, "INSIDE PERCENTAGE SIDE EFECTS");

        //setting fonts
        mPercentSideEffectsLabel.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/garreg.ttf"));
        pse.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/garreg.ttf"));

        makeJsonObjectRequest();

    }

    private void makeJsonObjectRequest(){
        Log.i(TAGPSE, "INSIDE JSON OBJECT REQUEST");
        showpDialog(); //progress dialog shows loading...while the data is being fetched

        //Request to fetch and cache image
        ImageRequest myrequest = new ImageRequest(urlImage,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        sideEffectImage.setImageBitmap(bitmap);
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAGPSE, "Error in loading Image");
                    }
                });

        //making a JSON Object  Request below
        AuthJSONObjectRequest jsonObjReq = new AuthJSONObjectRequest(Request.Method.GET, urlJsonObj, null
                , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAGPSE, response.toString());

                try {
                    Log.i(TAGPSE,"INSIDE JSON RESPONSE");
                    //parsing json object response
                    String name = response.getString("title_post");
                    String desc = response.getString("description_post");

                    jsonResponse = "";
                    //jsonResponse += "Post: \n" + name + "\n\n";
                    jsonResponse += /*"Description: \n" +*/ desc + "\n\n";

                    mPercentSideEffectsLabel.setText(jsonResponse);
                    String content = mPercentSideEffectsLabel.getText().toString();
                    File file;
                    FileOutputStream outputStream;
                    try {
                        // file = File.createTempFile("MyCache", null, getCacheDir());
                        file = new File(getCacheDir(), "PSECache");

                        outputStream = new FileOutputStream(file);
                        outputStream.write(content.getBytes());
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                hidepDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ",error.getMessage());
                Toast.makeText(getApplicationContext(), "Error Retreiving Data! Loading from cache... ", Toast.LENGTH_LONG).show();
                //offline caching
                BufferedReader input = null;
                File file = null;
                try {
                    file = new File(getCacheDir(), "PSECache"); // Pass getFilesDir() and "MyFile" to read file

                    input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                    String line;
                    StringBuffer buffer = new StringBuffer();
                    while ((line = input.readLine()) != null) {
                        buffer.append(line);
			buffer.append("\n");
                    }
                    mPercentSideEffectsLabel.setText(buffer.toString());
                    Log.d(TAGPSE, buffer.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //hide progress dialog
                hidepDialog();
            }
        });

        //Adding Request to request queue
        VolleyApplication.getInstance().addToRequestQueue(jsonObjReq);
        VolleyApplication.getInstance().addToRequestQueue(myrequest);
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
