package main.com.notaryagent.tabactivity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

import cc.cloudist.acplibrary.ACProgressConstant;
import main.com.notaryagent.R;
import main.com.notaryagent.activity.SplashActivity;
import main.com.notaryagent.app.Config;
import main.com.notaryagent.constant.ACProgressCustom;
import main.com.notaryagent.constant.BaseUrl;
import main.com.notaryagent.constant.MySession;
import main.com.notaryagent.databinding.SigningLayoutBinding;
import main.com.notaryagent.draglocation.LoadAdressSyncPlaceId;
import main.com.notaryagent.draglocation.MyTask;
import main.com.notaryagent.draglocation.WebOperations;
import main.com.notaryagent.paymentclasses.Utils;
import main.com.notaryagent.restapi.ApiClient;
import main.com.notaryagent.service.GPSTracker;
import main.com.notaryagent.utils.NotificationUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SigningActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 0; // in Milliseconds
    Location location;
    Location location_ar;
    LocationManager locationManager;
    private GoogleApiClient googleApiClient;
    public static double longitude = 0.0, latitude = 0.0;
    private Marker agentmarker;
    private ACProgressCustom ac_dialog;
    private MySession mySession;
    private String user_log_data = "", user_id = "", time_zone = "", request_id = "";
    BroadcastReceiver mRegistrationBroadcastReceiver;
    private String signing_code="",amount_str="",card_id="",cust_id="",client_id="";
    private SigningLayoutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(this,R.layout.signing_layout);
        checklocation();
        Calendar c = Calendar.getInstance();
        TimeZone tz = c.getTimeZone();
        Log.e("TIME ZONE >>", tz.getDisplayName());
        Log.e("TIME ZONE ID>>", tz.getID());
        time_zone = tz.getID();
        getCurrentLocation();
        ac_dialog = new ACProgressCustom.Builder(this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text(getResources().getString(R.string.pleasewait))
                .textSize(20).textMarginTop(5)
                .fadeColor(Color.DKGRAY).build();

        // checkGps();
        mySession = new MySession(this);
        user_log_data = mySession.getKeyAlldata();
        if (user_log_data == null) {

        } else {
            try {
                JSONObject jsonObject = new JSONObject(user_log_data);
                String message = jsonObject.getString("status");
                if (message.equalsIgnoreCase("1")) {
                    JSONObject jsonObject1 = jsonObject.getJSONObject("result");
                    user_id = jsonObject1.getString("id");

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        clickevetn();

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    String message = intent.getStringExtra("message");
                    Log.e("Push notification: ", "" + message);
                    JSONObject data = null;
                    try {
                        data = new JSONObject(message);
                        String keyMessage = data.getString("key").trim();
                        Log.e("KEY_SIGNING =", "" + keyMessage);
                        if (keyMessage.equalsIgnoreCase("your booking request is Cancel")) {
                            request_id = data.getString("request_id");
                            usercancelRide();
                        } else if (keyMessage.equalsIgnoreCase("your payment is successfull")) {
                            new RideDetailAsc().execute();
                        }

                        else if (keyMessage.equalsIgnoreCase("user pay the signing amount")) {
                            new RideDetailAsc().execute();
                             }else if (keyMessage.equalsIgnoreCase("Client paid the signing amount")) {
                            new RideDetailAsc().execute();
                             }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                new RideDetailAsc().execute();
            }
        };
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && !bundle.isEmpty()) {
            request_id = bundle.getString("request_id");

            new RideDetailAsc().execute();
        }
    }

    private void clickevetn() {
        binding.checkoutTv.setOnClickListener(v->finish());
        binding.exitAppBut.setOnClickListener(v->finish());
       /* paytv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            String sec_code=security_code.getText().toString();
            if (sec_code==null||sec_code.equalsIgnoreCase("")){
                Toast.makeText(SigningActivity.this,getResources().getString(R.string.pleasetakecodefromuser),Toast.LENGTH_LONG).show();
            }
            else {
                if (sec_code.equalsIgnoreCase(signing_code)){
                    paymentwithcard();
                }
                else {
                    Toast.makeText(SigningActivity.this,getResources().getString(R.string.youenteredwrongcode),Toast.LENGTH_LONG).show();

                }
            }
            }
        });*/
    }
    private void paymentwithcard() {
        // Tag used to cancel the request
        if (Utils.isConnected(getApplicationContext())) {
            //new CreateCardCustomer().execute();
            // new MakePaymentAsc().execute();
            //   new SubmitPayment().execute();
            paymentAs();

        } else {
            Toast.makeText(getApplicationContext(), "Please Cheeck network conection..", Toast.LENGTH_SHORT).show();
        }
    }
    private void paymentAs() {

        if (ac_dialog != null) {
            ac_dialog.show();
        }

        Call<ResponseBody> call = ApiClient.getApiInterface().makePayment(request_id, client_id, card_id, cust_id, user_id, amount_str, "Card", "Card", time_zone, "usd");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (ac_dialog != null) {
                    ac_dialog.dismiss();
                }
                String postReceiverUrl_r = BaseUrl.baseurl + "strips_payment?";

                Log.e("First Payment ", " >> " + response);
                if (response.isSuccessful()) {

                    try {
                        String responseData = response.body().string();


                        JSONObject jsonObject = new JSONObject(responseData);
                        if (jsonObject.getString("status").equalsIgnoreCase("1")) {
                            // JSONObject jsonObject1 = jsonObject.getJSONObject("result");
                            //  WalletActivity.cust_id =  jsonObject1.getString("id");
                            // Log.e("customer_id >> ", " >> "+WalletActivity.cust_id);

                            Toast.makeText(SigningActivity.this, getResources().getString(R.string.paymentandratsucc), Toast.LENGTH_LONG).show();
                            finish();




                        } else if (jsonObject.getString("status").equalsIgnoreCase("2")) {

                            Toast.makeText(SigningActivity.this, getResources().getString(R.string.paymentandratsucc), Toast.LENGTH_LONG).show();
finish();
                           /* Intent i = new Intent(RateAgentAct.this, MainActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(i);*/
                        } else {
                            Toast.makeText(SigningActivity.this, getResources().getString(R.string.somethingwrong), Toast.LENGTH_LONG).show();

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(SigningActivity.this, getResources().getString(R.string.somethingwrong), Toast.LENGTH_LONG).show();

                    Log.e("responseData ", " >> " + response);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Log error here since request failed
                t.printStackTrace();
                if (ac_dialog != null) {
                    ac_dialog.dismiss();
                }
                Toast.makeText(SigningActivity.this, getResources().getString(R.string.somethingwrong), Toast.LENGTH_LONG).show();

                Log.e("TAG", t.toString());
            }
        });
    }

    private void usercancelRide() {
        //   Log.e("War Msg in dialog", war_msg);
        final Dialog canceldialog = new Dialog(SigningActivity.this);
        canceldialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        canceldialog.setCancelable(false);
        canceldialog.setContentView(R.layout.surecancelride_lay);
        canceldialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        final TextView yes_tv = (TextView) canceldialog.findViewById(R.id.yes_tv);
        final TextView message_tv = (TextView) canceldialog.findViewById(R.id.message_tv);
        yes_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NotificationUtils.r != null && NotificationUtils.r.isPlaying()) {
                    NotificationUtils.r.stop();
                }

                canceldialog.dismiss();
               // new GetCurrentBooking().execute();

            }
        });
        canceldialog.show();


    }


    private void checklocation() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        location_ar = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MINIMUM_TIME_BETWEEN_UPDATES, MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, new SigningActivity.MyLocationListener());
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (location_ar != null) {
            //Getting longitude and latitude
            longitude = location_ar.getLongitude();
            latitude = location_ar.getLatitude();
            if (latitude == 0.0) {
                latitude = SplashActivity.latitude;
                longitude = SplashActivity.longitude;

            }
            /*address_complete = loadAddress(latitude, longitude);
            pickuplocation.setText(address_complete);*/
            //loadAddress1(latitude, longitude);


        } else {
            System.out.println("----------------geting Location from GPS----------------");
            GPSTracker tracker = new GPSTracker(this);
            location_ar = tracker.getLocation();
            if (location_ar == null) {
                latitude = SplashActivity.latitude;
                longitude = SplashActivity.longitude;
                /*address_complete = loadAddress(latitude, longitude);
                pickuplocation.setText(address_complete);*/
                //loadAddress1(latitude, longitude);


            } else {
                longitude = location_ar.getLongitude();
                latitude = location_ar.getLatitude();

                if (latitude == 0.0) {
                    latitude = SplashActivity.latitude;
                    longitude = SplashActivity.longitude;

                }
                /*address_complete = loadAddress(latitude, longitude);
                pickuplocation.setText(address_complete);*/
                //loadAddress1(latitude, longitude);


            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    private String loadAddress1(double latitude, double longitude) {

        String address = "", address_short = "";
        //  prgressbar.setVisibility(View.VISIBLE);

        WebOperations wo = new WebOperations(SigningActivity.this.getApplicationContext());
        //wo.setUrl("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude + "&key="+getResources().getString(R.string.google_search)+"&location_type=ROOFTOP&result_type=street_address");
        wo.setUrl("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude + "&key=" + getResources().getString(R.string.googlekey_other));
        new MyTask(wo, 3) {
            @Override
            protected void onPostExecute(String s) {


                new LoadAdressSyncPlaceId() {
                    @Override
                    protected void onPostExecute(String s) {
                        if (s != null && !s.equalsIgnoreCase("null") && !s.equalsIgnoreCase("")) {
                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                String place_id = jsonObject.getString("place_id");
                                Log.e("place_id >>", " >> " + place_id);
                                String address_complete = jsonObject.getString("address");
                                binding.signinglocation.setText("" + address_complete);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }.execute(s);


            }
        }.execute();

        return "";

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {

            longitude = location.getLongitude();
            latitude = location.getLatitude();


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(SigningActivity.this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));
        LocalBroadcastManager.getInstance(SigningActivity.this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));
        NotificationUtils.clearNotifications(SigningActivity.this.getApplicationContext());

      //  new GetCurrentBooking().execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(SigningActivity.this).unregisterReceiver(mRegistrationBroadcastReceiver);

    }

    private class RideDetailAsc extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (ac_dialog != null) {
                ac_dialog.show();
            }
            try {
                super.onPreExecute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
//http://halatx.halasmart.com/hala/webservice/get_ride_detail?user_id=76&request_id=379
            try {
                String postReceiverUrl = BaseUrl.baseurl + "get_ride_detail?";
                URL url = new URL(postReceiverUrl);
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("user_id", user_id);
                params.put("request_id", request_id);

                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }
                String urlParameters = postData.toString();
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(urlParameters);
                writer.flush();
                String response = "";
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    response += line;
                }
                writer.close();
                reader.close();
                return response;
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //progressbar.setVisibility(View.GONE);
            if (ac_dialog != null) {
                ac_dialog.dismiss();
            }
            if (result == null) {
            } else if (result.isEmpty()) {
            } else {
                try {
                    Log.e("ride_details", "=====>" + result);
                    JSONObject jsonObject = new JSONObject(result);
                    String msg = jsonObject.getString("message");
                    if (msg.equalsIgnoreCase("successfull")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("result");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            signing_code = jsonObject1.getString("signing_code");
                            amount_str = jsonObject1.getString("ride_fare");

                           String book_by = jsonObject1.getString("book_by");
                           if (book_by.equalsIgnoreCase("Admin")){
                               card_id = jsonObject1.getString("card_id");
                           }
                           else {
                           }
                            String status = jsonObject1.getString("status");
                            String payment_sts = jsonObject1.getString("payment_status");
                            String car_type_name = jsonObject1.getString("car_type_name");
                            String car_type_image = jsonObject1.getString("car_image");
                            String picklaterdate = jsonObject1.getString("picklaterdate");
                            binding.price.setText("$"+jsonObject1.getString("ride_fare"));
                            binding.signinglocation.setText("" + jsonObject1.getString("picuplocation"));
                            binding.locationtype.setText( jsonObject1.getString("location_type"));
                            binding.realstatesigning.setText(jsonObject1.getString("realstate_signing"));
                            binding.numberofwitness.setText( jsonObject1.getString("number_of_witness"));
                            binding.nameEt.setText("" + jsonObject1.getString("name"));
                            binding.phoneEt.setText("" + jsonObject1.getString("mobile"));
                            binding.emailEt.setText("" + jsonObject1.getString("email"));
                            binding.typeofsigning.setText( jsonObject1.getString("type_of_signing_name"));
                            binding.numberofsigning.setText( jsonObject1.getString("number_of_signing"));
                            binding.dateTv.setText("" + jsonObject1.getString("picklaterdate").trim() + " " + jsonObject1.getString("picklatertime").trim());
                            binding.extracharge.setText(" $"+jsonObject1.getString("today_charge"));
                            binding.nightcharge.setText(" $" + jsonObject1.getString("night_charge"));




                            if (jsonObject1.getString("realstate_signing").equalsIgnoreCase("No")) {
                                binding.selectrealstatelay.setVisibility(View.GONE);
                            } else {

                                binding.emailtoprit.setText(getResources().getString(R.string.emailtoprint)+" $"+jsonObject1.getString("emailtoprit"));
                                binding.scanandemail.setText(getResources().getString(R.string.scanandemail)+" $"+jsonObject1.getString("scanandemail"));
                                binding.prepaid.setText(getResources().getString(R.string.prepaid)+" $"+jsonObject1.getString("prepaid"));
                                binding.overnight.setText(getResources().getString(R.string.overnight)+" $"+jsonObject1.getString("overnight"));


                                binding.selectrealstatelay.setVisibility(View.VISIBLE);
                            }

                            JSONArray jsonArray1 = jsonObject1.getJSONArray("user_details");
                            for (int l=0;l<jsonArray1.length();l++){
                                JSONObject jsonObject2 = jsonArray1.getJSONObject(l);
                                cust_id = jsonObject2.getString("cust_id");
                                client_id = jsonObject2.getString("id");


                            }
                            binding.carname.setText("" + car_type_name);

                            if (car_type_image == null || car_type_image.equalsIgnoreCase("") || car_type_image.equalsIgnoreCase(BaseUrl.image_baseurl)) {

                            } else {
                                Picasso.with(SigningActivity.this).load(car_type_image).placeholder(R.drawable.mini).into(binding.carimage);

                            }

                            if (status.equalsIgnoreCase("Pending")) {


                            } else if (status.equalsIgnoreCase("Accept")) {

                            } else if (status.equalsIgnoreCase("Arrived")) {

                            } else if (status.equalsIgnoreCase("Start")) {

                            } else if (status.equalsIgnoreCase("End")) {
                                if (payment_sts.equalsIgnoreCase("Pending")){
                                    binding.checkoutTv.setText(""+getResources().getString(R.string.notpaid));
                                }
                                else {
                                    binding.checkoutTv.setText(""+getResources().getString(R.string.paid));
                                }

                            }else if (status.equalsIgnoreCase("Finish")) {
                                if (payment_sts.equalsIgnoreCase("Pending")){
                                    binding.checkoutTv.setText(""+getResources().getString(R.string.notpaid));
                                }
                                else {
                                    binding.checkoutTv.setText(""+getResources().getString(R.string.paid));
                                }
                            }
                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
