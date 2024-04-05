//package com.smooth.smoothtix;
//
//import android.graphics.Color;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.Handler;
//import android.view.View;
//import android.view.Window;
//import android.view.WindowManager;
//import android.widget.TextView;
//import android.widget.Toast;
//import androidx.appcompat.app.AppCompatActivity;
//import org.json.JSONArray;
//import org.json.JSONObject;
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//
//public class PassengerActivity extends AppCompatActivity {
//    TextView latitude, longitude;
//
//    private final Handler handler = new Handler();
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_passenger);
//        setTransparentNotificationBar();
////        latitude = findViewById(R.id.latitude);
////        longitude = findViewById(R.id.longitude);
////        Runnable fetchDataRunnable = new Runnable() {
////            @Override
////            public void run() {
////                new FetchDataTask().execute("Sh00001");
////                handler.postDelayed(this, 30000);
////            }
////        };
////        handler.post(fetchDataRunnable);
//    }
//    protected void setTransparentNotificationBar() {
//        Window window = getWindow();
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        window.setStatusBarColor(Color.TRANSPARENT);
//        window.getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); // Add this flag
//    }
//
//    private class FetchDataTask extends AsyncTask<String, Void, String> {
//        @Override
//        protected String doInBackground(String... params) {
//            String schedule_id = params[0];
//            try {
//                String apiUrl = "http://10.0.2.2:2000/SmoothTix_war_exploded/locationController";
//                URL url = new URL(apiUrl);
//
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//
//                connection.setRequestMethod("GET");
//                connection.setRequestProperty("Content-Type", "application/json");
//                connection.setRequestProperty("schedule_id", schedule_id);
//
//                int responseCode = connection.getResponseCode();
//                if (responseCode == HttpURLConnection.HTTP_OK) {
//                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
//                        StringBuilder response = new StringBuilder();
//                        String line;
//                        while ((line = reader.readLine()) != null) {
//                            response.append(line);
//                        }
//                        return response.toString();
//                    }
//                } else {
//                    return "Error: " + responseCode;
//                }
//            } catch (IOException e) {
//                return "Error: " + e.getMessage();
//            }
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            try {
//
//                JSONArray jsonArray = new JSONArray(result);
//                JSONObject jsonSchedule = jsonArray.getJSONObject(0);
//
//                latitude.setText(jsonSchedule.getString("latitude"));
//                longitude.setText(jsonSchedule.getString("longitude"));
//
//            } catch (Exception e) {
//                Toast.makeText(PassengerActivity.this, "Error parsing result", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//}

package com.smooth.smoothtix;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PassengerActivity extends AppCompatActivity {

    private static final String server_url = "http://10.0.2.2:2000/SmoothTix_war_exploded";
    private static final String TAG = "PassengerActivity";
    TextView user_name;
    String userName = "";
    String nic = "";
    String userRole = "";
    String p_id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);
        user_name = findViewById(R.id.user_name);
        setTransparentNotificationBar();
        executeCheckSessionTask();

    }
    private void executeCheckSessionTask() {
        CheckSession checkSessionTask = new CheckSession(PassengerActivity.this, new CheckSessionCallback() {
            @Override
            public void onCheckSessionCompleted(String result) {
//                Toast.makeText(DriverActivity.this, result, Toast.LENGTH_SHORT).show();
                try {
                    JSONObject userData = new JSONObject(result);

                    userName = userData.getString("user_name");
                    nic = userData.getString("nic");
                    userRole = userData.getString("user_role");
                    p_id = userData.getString("p_id");
                    user_name.setText(userName);
                    new FetchDataTask().execute(p_id);
                } catch (JSONException e) {
                    // Handle JSONException, e.g., if the JSON string is malformed
                    e.printStackTrace();
                }
            }
        });
        checkSessionTask.execute();
    }

    private class FetchDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String p_id = params[0];
            try {
                String apiUrl = server_url + "/bookingController?p_id=" + p_id;
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
            Log.d("Test12", result);
            try {
                List<Booking> bookings = parseJsonResponse(result);

                Log.d("Test12", String.valueOf(bookings.size()));
                Log.d("Test12", result);

                if (bookings.size() > 0) {
                    showBooking(bookings.get(0), "VISIBLE", "container1");

                    for (int i = 1; i < bookings.size(); i++) {
                        showBooking(bookings.get(i),"GONE", "container2");
                    }
                } else {
                    Log.w("MyTag", "No booking found");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing result: " + e.getMessage());
                Toast.makeText(PassengerActivity.this, "Error parsing result", Toast.LENGTH_SHORT).show();
            }
        }

        private List<Booking> parseJsonResponse(String json) throws JSONException {
            List<Booking> bookings = new ArrayList<>();

            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonSchedule = jsonArray.getJSONObject(i);

                Booking booking = new Booking();
                booking.setScheduleId(jsonSchedule.getString("schedule_id"));
                booking.setBusProfileId(jsonSchedule.getString("booking_id"));
//                schedule.setDateTime(jsonSchedule.getString("date_time"));
//                schedule.setRouteNo(jsonSchedule.getString("route_no"));
//                schedule.setStart(jsonSchedule.getString("start"));
//                schedule.setDestination(jsonSchedule.getString("destination"));

                bookings.add(booking);
            }

            return bookings;
        }

        protected void showBooking(Booking booking, String visibility, String container) {
            LinearLayout container_layout = findViewById(R.id.container1);
            if(container.equals("container1")){
                container_layout = findViewById(R.id.container1);
            } else if (container.equals("container2")) {
                container_layout = findViewById(R.id.container2);
            }

            View inflatedViewCurrent = LayoutInflater.from(PassengerActivity.this).inflate(R.layout.activity_booking, container_layout, false);
            Button btn_showMap = inflatedViewCurrent.findViewById(R.id.btn_showMap);
            if(visibility.equals("GONE")){
                btn_showMap.setEnabled(false);
                int disabledColor = Color.GRAY; // Set your desired color for the disabled state
                btn_showMap.setBackgroundTintList(android.content.res.ColorStateList.valueOf(disabledColor));
            }
            TextView schedule_id = inflatedViewCurrent.findViewById(R.id.schedule_id);
            schedule_id.setText(booking.getScheduleId());
            TextView bus_profile_id = inflatedViewCurrent.findViewById(R.id.bus_profile_id);
            bus_profile_id.setText(booking.getBusProfileId());
            TextView date_time = inflatedViewCurrent.findViewById(R.id.date_time);
            date_time.setText(booking.getDateTime());
            TextView start = inflatedViewCurrent.findViewById(R.id.start);
            start.setText(booking.getStart());
            TextView destination = inflatedViewCurrent.findViewById(R.id.destination);
            destination.setText(booking.getDestination());

            container_layout.addView(inflatedViewCurrent);
            btn_showMap.setOnClickListener(v -> showConfirmationDialog());
        }
        private void showConfirmationDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(PassengerActivity.this);
            builder.setTitle("Confirmation");
            builder.setMessage("Are you sure you want start the journey?");

            builder.setPositiveButton("Yes", (dialog, which) -> {
                Intent intent = new Intent(PassengerActivity.this, LocationActivity.class);
                startActivity(intent);
            });

            builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
    protected void setTransparentNotificationBar() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); // Add this flag
    }
}
