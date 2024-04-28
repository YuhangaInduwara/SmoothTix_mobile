package com.smooth.smoothtix;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class ViewLocationActivity extends AppCompatActivity {

    private WebView webView;
    private Timer timer;
    private static final String server_url = Constants.server_url;
    private static String schedule_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        schedule_id = getIntent().getStringExtra("schedule_id");

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("file:///android_asset/leaflet_map.html");

        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                new ViewLocationActivity.GetLocationTask().execute(schedule_id);
            }
        }, 0, 1000);
    }

    private class GetLocationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String schedule_id = params[0];

            try {
                String apiUrl = server_url + "/locationController?schedule_id=" + schedule_id;
                URL url = new URL(apiUrl);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        return response.toString();
                    }
                } else {
                    return "Error: " + responseCode;
                }
            } catch (IOException e) {
                return "Error: " + e.getMessage();
            }
        }
        @Override
        protected void onPostExecute(String result) {
            if(Objects.equals(result, "[]")){
                Log.e("ViewLocationActivity", "No location to show!");
            }
            else if(Objects.equals(result, "400") || Objects.equals(result, "401") || Objects.equals(result, "402") || Objects.equals(result, "500") || Objects.equals(result, "Error:400") || Objects.equals(result, "Error:401") || Objects.equals(result, "Error:402") || Objects.equals(result, "Error:500")){                Log.e("ViewLocationActivity", "Invalid request or Server error");
            }
            else{
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    double latitude = Double.parseDouble(jsonObject.getString("latitude"));
                    double longitude = Double.parseDouble(jsonObject.getString("longitude"));
                    updateMap(latitude, longitude);
                } catch (Exception e) {
                    Log.e("ViewLocationActivity", "Error parsing result");
                }
            }
        }
    }

    private void updateMap(double latitude, double longitude) {
        runOnUiThread(() -> webView.evaluateJavascript(
                "updateMap(" + latitude + ", " + longitude + ");",
                null));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
