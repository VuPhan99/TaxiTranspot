package com.dacs.sict.htxv.taxitranspot.View;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dacs.sict.htxv.taxitranspot.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.dacs.sict.htxv.taxitranspot.Common.Common;
import com.dacs.sict.htxv.taxitranspot.Model.FCMResponse;
import com.dacs.sict.htxv.taxitranspot.Model.Notification;
import com.dacs.sict.htxv.taxitranspot.Model.Sender;
import com.dacs.sict.htxv.taxitranspot.Model.Token;
import com.dacs.sict.htxv.taxitranspot.Remote.IFCMService;
import com.dacs.sict.htxv.taxitranspot.Remote.IGoogleApi;

public class CustommerCall extends AppCompatActivity {

    TextView txtTime, txtAddress, txtDistance;
    Button btnCancel, btnAccept;

    MediaPlayer mediaPlayer;

    IGoogleApi mService;
    IFCMService mFCMService;

    String customerId;

    double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_custommer_call);

        mService = Common.getGoogleApi();
        mFCMService = Common.getFCMservice();

        //Init View
        txtAddress = findViewById(R.id.txtAddress);
        txtDistance = findViewById(R.id.txtDistance);
        txtTime = findViewById(R.id.txtTime);

        btnAccept = findViewById(R.id.btnAccept);
        btnCancel = findViewById(R.id.btnDecline);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(customerId)) {
                    cancelBooking(customerId);
                }
            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CustommerCall.this, DriverTracking.class);
                //Send customer location to new activity.
                intent.putExtra("lat", lat);
                intent.putExtra("lng", lng);
                startActivity(intent);
                finish();
            }
        });

        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        if (getIntent() != null) {
            lat = getIntent().getDoubleExtra("lat", -1.0);
            lng = getIntent().getDoubleExtra("lng", -1.0);
            customerId = getIntent().getStringExtra("customer");

            getDirection(lat, lng);
        }
    }

    /*private void cancelBooking(String customerId) {
        Token token = new Token(customerId);

        Notification notification = new Notification("Notice!", "Driver has cancelled your request");
        Sender sender = new Sender(notification, token.getToken());

        mFCMService.sendMessage(sender)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if (response.body().getSuccess() == 1) {
                        Toast.makeText(CustommerCall.this, "Cancelled", Toast.LENGTH_SHORT).show();
                        finish();
                         }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });

    }*/

    private void cancelBooking(String customerId) {
        Token token = new Token(customerId);

        Notification notification = new Notification("Notice!", "Driver has cancelled your request");
        Sender sender = new Sender(token.getToken(), notification);

        mFCMService.sendMessage(sender)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if (response.body().success == 1) {
                            Toast.makeText(CustommerCall.this, "Cancelled!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {

                            Toast.makeText(CustommerCall.this, "Failed!", Toast.LENGTH_LONG).show();


                        }


                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {


                    }
                });
    }

    private void getDirection(double lat, double lng) {

        String requestApi = null;

        try {

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + Common.mLastLocation.getLatitude() + "," + Common.mLastLocation.getLongitude() + "&" +
                    "destination=" + lat + "," + lng + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);

            Log.d("DEV", requestApi); // Print URL for debug

            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());

                        JSONArray routes = jsonObject.getJSONArray("routes");

                        //after get routes, just get first element of routes
                        JSONObject object = routes.getJSONObject(0);

                        //after get first element, we need get array with name "legs"
                        JSONArray legs = object.getJSONArray("legs");

                        //and get first element of legs array
                        JSONObject legsObject = legs.getJSONObject(0);

                        //Now, get Distance
                        JSONObject distance = legsObject.getJSONObject("distance");
                        txtDistance.setText(distance.getString("text"));

                        //get Time
                        JSONObject time = legsObject.getJSONObject("duration");
                        txtTime.setText(time.getString("text"));

                        //Get Address
                        String address = legsObject.getString("end_address");
                        txtAddress.setText(address);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(CustommerCall.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStop() {
        mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        mediaPlayer.release();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer.start();
    }
}
