package com.hhk.geolocationalert;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import static android.app.PendingIntent.getActivity;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener{


    private static final int REQUEST_PERMISSION_CODE =123 ;
    private TextView tv;
    private Button btn1,btn3;
    private LocationManager lm;
    private LocationListener ll;
    private Boolean flag = false;
    private String smsBody= null,coord;
    private double lat,lng;
    private ProgressDialog pd;
    private int updateCount=0;
   // private AdView mAdView;

    private EditText et1,et2;
    private Button bt2;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //if you want to lock screen for always Portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        tv = (TextView) findViewById(R.id.textView1);

        btn1 = (Button) findViewById(R.id.button1);
        btn3=(Button) findViewById(R.id.button3);

        btn1.setOnClickListener(this);
        btn3.setOnClickListener(this);

       // mAdView = (AdView) findViewById(R.id.adView);
        //Comment " .addTestDevice("FE202E17A8F488C26F61F520F287DEB9") " before release
        //AdRequest adRequest = new AdRequest.Builder()
     //           .addTestDevice("FE202E17A8F488C26F61F520F287DEB9")
       //         .build();
        //mAdView.loadAd(adRequest);


        et1=(EditText) findViewById(R.id.editText1);
        et2=(EditText) findViewById(R.id.editText2);
        bt2=(Button) findViewById(R.id.button2);

        bt2.setOnClickListener(this);

        et1.setText(readFromFile1(this));
        et2.setText(readFromFile2(this));




        if(!displayGpsStatus()){
            tv.setText("Tap on \nENABLE LOCATION");

            btn1.setVisibility(View.GONE);
        }else{
            btn3.setVisibility(View.GONE);
        }


        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);



  if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED||
       ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
                   ActivityCompat.requestPermissions(this,new String[]
                        {Manifest.permission.SEND_SMS,Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_PERMISSION_CODE);


    }

    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.button1:

                flag = displayGpsStatus();
                if (flag) {

                    pd=new ProgressDialog(v.getContext());
                    pd.setCancelable(false);
                    pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pd.setMessage("Move your device few meters and wait.....");
                    pd.getProgress();
                    pd.show();

                    ll = new MyLocationListener();

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling

                        lm.requestLocationUpdates(lm.GPS_PROVIDER, 5000, 10, ll);

                    }

                    //  lm.requestLocationUpdates(lm.NETWORK_PROVIDER, 0, 0, ll);
                } else {
                    alertbox("GPS Status", "Your GPS is OFF");
                }


                break;
            case R.id.button2:
                writeToFile1(et1.getText().toString(),this);
                writeToFile2(et2.getText().toString(),this);

                et1.setText(readFromFile1(this));
                et2.setText(readFromFile2(this));

                break;

            case R.id.button3:

                alertbox("GPS Status", "Your GPS is OFF");
                break;
        }
    }

    private Boolean displayGpsStatus() {
        ContentResolver contentResolver = getBaseContext().getContentResolver();
        boolean gpsStatus = Settings.Secure.isLocationProviderEnabled(contentResolver, lm.GPS_PROVIDER);
        if (gpsStatus) {
            return true;

        } else {
            return false;
        }
    }

    protected void alertbox(String title, String mymessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Turn ON Location Services")
                .setCancelable(false)
                //.setTitle("GPS is Disable")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // finish the current activity
                        // AlertBoxAdvance.this.finish();
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                        dialog.cancel();

                        tv.setText("Tap on \nGET LOCATION");
                        btn1.setVisibility(View.VISIBLE);
                        btn3.setVisibility(View.GONE);

                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // cancel the dialog box
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public class MyLocationListener extends Service implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            lat=loc.getLatitude();
            lng=loc.getLongitude();
            coord = loc.getLatitude()+","+loc.getLongitude();
            //tv.setText(coord);
            updateCount++;
            tv.setText("Location Update Count: "+updateCount);

            //    tv.setText("Location Updated");
            btn1.setVisibility(View.GONE);
            pd.dismiss();
//Send SMS
            //if(lat>=31.508533&&lng>=74.309609&&lat<=31.509486&&lng<=74.311342) Home
     if(lng>=74.209277&&lng<=74.212618&&lat>=31.400903&&lat<=31.405709)
             {
                sender();
                    finish();
                    System.exit(0);
                    }


        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }



    public void onRequestPermissionsResult(int requestCode, String[] permissions, int []grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if ( grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
 if (ContextCompat.checkSelfPermission(this,Manifest.permission. ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){

                        //Request location updates:
                        //lm.requestLocationUpdates(lm.GPS_PROVIDER, 5000, 10, ll);
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

        }
    }


    public void sendSMS(String phoneNo,String msg){

        try {
            SmsManager sm = SmsManager.getDefault();
            sm.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(getApplicationContext(),getString(R.string.message_success),Toast.LENGTH_LONG).show();
        }catch(Exception e)
        {
            Toast.makeText(getApplicationContext(),e.getMessage().toString(),Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void writeToFile1(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("phoneNumber.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            Toast.makeText(this,"Number Saved",Toast.LENGTH_SHORT).show();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void writeToFile2(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("message.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            Toast.makeText(this,"Message Saved",Toast.LENGTH_SHORT).show();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String readFromFile1(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("phoneNumber.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    private String readFromFile2(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("message.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    public void sender(){

                sendSMS(readFromFile1(this),readFromFile2(this));
                  }


}
