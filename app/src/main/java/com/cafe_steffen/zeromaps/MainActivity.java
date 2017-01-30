package com.cafe_steffen.zeromaps;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

// gps data
    LocationManager locationManager;
    double longitudeBest, latitudeBest; // variables to send to server.
    double longitudeGPS;
    double latitudeGPS; // send these variables to server // toast
    double longitudeNetwork, latitudeNetwork; //
    TextView longitudeValueBest, latitudeValueBest;
    TextView longitudeValueGPS, latitudeValueGPS;
    TextView longitudeValueNetwork, latitudeValueNetwork;

// sql db

    private WebView mWebView; // adding webview

    // jscript parsing
    StringGetter stringGetter;
    StringGetter stringGetter2;

    String key;
    String lat;
    //    WebView webView;
    String url;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED); // locks screen orientation so not to destroy data. Other options for later.

        setContentView(R.layout.activity_main);

        key="-82.5"; // this needs to be the gps cords. can be string or value
        lat="27.53";

//        url ="http://cloud.sofwerx.org:43110/1P1RFkC5zJwvvM3Gt7r4myaYStjr23mH9q/"; // set to zeroNet url on record.
//        url ="http://127.0.0.1:43110/1P1RFkC5zJwvvM3Gt7r4myaYStjr23mH9q/"; // local zeronet url. record.

//        url ="http://cloud.sofwerx.org:43110/1CyX49a6rLzzVqzKd5PkHCEhJi5bNAk7g"; // zerochat page.
//        url ="http://127.0.0.1:43110/1CyX49a6rLzzVqzKd5PkHCEhJi5bNAk7g"; // zerochat page.

        url ="http://cloud.sofwerx.org:43110/1JRNaphiQJKr4h3x7yXxFs6kSmbDRwPpRy"; // updated clone page.


// getters for lat and long to send to server
        stringGetter = new StringGetter(this);
        stringGetter2 = new StringGetter(this);


// gps / network data
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        longitudeValueBest = (TextView) findViewById(R.id.longitudeValueBest);
        latitudeValueBest = (TextView) findViewById(R.id.latitudeValueBest);
        longitudeValueGPS = (TextView) findViewById(R.id.longitudeValueGPS);
        latitudeValueGPS = (TextView) findViewById(R.id.latitudeValueGPS);
        longitudeValueNetwork = (TextView) findViewById(R.id.longitudeValueNetwork);
        latitudeValueNetwork = (TextView) findViewById(R.id.latitudeValueNetwork);


        // below is webview test
        mWebView = (WebView) findViewById(R.id.activity_main_webview);

        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);

        // size restrictions
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);

        mWebView.loadUrl(url); // new ZeroNet Test
        mWebView.addJavascriptInterface(new StringGetter(this), "AndroidFunctionLong");
        mWebView.addJavascriptInterface(new StringGetter2(this), "AndroidFunctionLat");

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(mWebView, url);
                Toast.makeText(getApplicationContext(), "Done loading ZeroNet page", Toast.LENGTH_SHORT).show();

            }


            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });



        //mWebView.loadUrl("http://www.vg.no/");

    }

    private boolean checkLocation() {
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    public void toggleGPSUpdates(View view) {
        if (!checkLocation())
            return;
        Button button = (Button) view;
        if (button.getText().equals(getResources().getString(R.string.pause))) {
            locationManager.removeUpdates(locationListenerGPS);
            button.setText(R.string.resume);
        } else {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 2 * 60 * 1000, 10, locationListenerGPS);
            button.setText(R.string.pause);
        }
    }

    public void toggleBestUpdates(View view) {
        if (!checkLocation())
            return;
        Button button = (Button) view;
        if (button.getText().equals(getResources().getString(R.string.pause))) {
            locationManager.removeUpdates(locationListenerBest);
            button.setText(R.string.resume);
        } else {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(true);
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            String provider = locationManager.getBestProvider(criteria, true);
            if (provider != null) {
                locationManager.requestLocationUpdates(provider, 2 * 60 * 1000, 10, locationListenerBest);
                button.setText(R.string.pause);
                Toast.makeText(this, "Best Provider is " + provider, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void toggleNetworkUpdates(View view) {
        if (!checkLocation())
            return;
        Button button = (Button) view;
        if (button.getText().equals(getResources().getString(R.string.pause))) {
            locationManager.removeUpdates(locationListenerNetwork);
            button.setText(R.string.resume);
        } else {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 60 * 1000, 10, locationListenerNetwork);
            Toast.makeText(this, "Network provider started running", Toast.LENGTH_LONG).show();
            button.setText(R.string.pause);
        }
    }

    private final LocationListener locationListenerBest = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitudeBest = location.getLongitude();
            latitudeBest = location.getLatitude();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    longitudeValueBest.setText(longitudeBest + "");
                    latitudeValueBest.setText(latitudeBest + "");
                    Toast.makeText(MainActivity.this, "Best Provider update", Toast.LENGTH_SHORT).show();
                }
            });
        }


        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private final LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitudeNetwork = location.getLongitude();
            latitudeNetwork = location.getLatitude();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    longitudeValueNetwork.setText(longitudeNetwork + "");
                    latitudeValueNetwork.setText(latitudeNetwork + "");
                    Toast.makeText(MainActivity.this, "Network Provider update", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private final LocationListener locationListenerGPS = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitudeGPS = location.getLongitude();
            latitudeGPS = location.getLatitude();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    longitudeValueGPS.setText(longitudeGPS + "");
                    latitudeValueGPS.setText(latitudeGPS + "");
                    Toast.makeText(MainActivity.this, "GPS Provider update", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };


    public class StringGetter {
        Context jContext;

        StringGetter(Context context){
            jContext = context;
        }
        @JavascriptInterface
        public String getString() {
            return Double.toString(longitudeGPS);

        }

    }


    public class StringGetter2 {
        Context jContext;

        StringGetter2(Context context){
            jContext = context;
        }
        @JavascriptInterface
        public String getString() {
            return Double.toString(latitudeGPS);

        }


    }

}

/*
*
* 1. Add layout w buttons for GPS. ---
*
* 2. Test webView with static variables. ---
*
* 3. Test Activate GPS ---
*
* 4. Set static vars to GPS variables
*
* 5. test updates
*
* 6. Make sure jscript is listening for changes... or at least have a button that refrehses the variable.
* Which should be same as now.
*
* 7. make sure user login works. This should be done first when posting by hitting a button first. so we wll get gps update. then submit button as we have
* the update cords button in WebView app right now. Then after that we will just do a condition to always search for updated gps in JS and when that is done post it
* to the chat db. if not logged in it will ask to be logged in.
*
* */