package com.smooth.smoothtix;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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

public class DriverActivity extends AppCompatActivity {

    private static final String server_url = "http://10.0.2.2:2000/SmoothTix_war_exploded";
    private static final String TAG = "DriverActivity";
    TextView user_name;
    String userName = "";
    String nic = "";
    String userRole = "";
    String p_id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        user_name = findViewById(R.id.user_name);
        setTransparentNotificationBar();
        executeCheckSessionTask();

        ImageView userImageView = findViewById(R.id.userImage);

        userImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show your logout menu or perform any other actions
                showLogoutMenu(v);
            }
        });

    }

    private void showLogoutMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.logout_menu); // Assuming you have a menu resource file

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle menu item clicks
                if (item.getItemId() == R.id.menu_logout) {
                    // Add your logout logic here
                    // For example, you might launch a login activity and finish the current one
                    startActivity(new Intent(DriverActivity.this, LoginActivity.class));
                    finish();
                }
                else if (item.getItemId() == R.id.menu_passenger) {
                    // Add your logout logic here
                    // For example, you might launch a login activity and finish the current one
                    startActivity(new Intent(DriverActivity.this, PassengerActivity.class));
                    finish();
                }
                return true;
            }
        });

        popupMenu.show();
    }

    private void executeCheckSessionTask() {
        CheckSession checkSessionTask = new CheckSession(DriverActivity.this, new CheckSessionCallback() {
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
                    new GetDriverId().execute(p_id);
                } catch (JSONException e) {
                    // Handle JSONException, e.g., if the JSON string is malformed
                    e.printStackTrace();
                }
            }
        });
        checkSessionTask.execute();
    }

    private class GetDriverId extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String p_id = params[0];
            try {
                String apiUrl = server_url + "/driverController";
                URL url = new URL(apiUrl);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("p_id", p_id);

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
            new FetchDataTask().execute(result);
        }
    }

    private class FetchDataTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                String driverId = params[0];
                try {
                    String apiUrl = server_url + "/scheduleController";
                    URL url = new URL(apiUrl);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("driver_id", driverId);

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
            try {
                List<Schedule> schedules = parseJsonResponse(result);

                Log.d("MyTag", String.valueOf(schedules.size()));

                if (schedules.size() > 0) {
                    showSchedule(schedules.get(0), "VISIBLE", "container1");

                    for (int i = 1; i < schedules.size(); i++) {
                        showSchedule(schedules.get(i),"GONE", "container2");
                    }
                } else {
                    Log.w("MyTag", "No schedules found");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing result: " + e.getMessage());
                Toast.makeText(DriverActivity.this, "Error parsing result", Toast.LENGTH_SHORT).show();
            }
        }

        private List<Schedule> parseJsonResponse(String json) throws JSONException {
            List<Schedule> schedules = new ArrayList<>();

            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonSchedule = jsonArray.getJSONObject(i);

                Schedule schedule = new Schedule();
                schedule.setScheduleId(jsonSchedule.getString("schedule_id"));
                schedule.setBusProfileId(jsonSchedule.getString("bus_profile_id"));
                schedule.setDateTime(jsonSchedule.getString("date_time"));
                schedule.setRouteNo(jsonSchedule.getString("route_no"));
                schedule.setStart(jsonSchedule.getString("start"));
                schedule.setDestination(jsonSchedule.getString("destination"));

                schedules.add(schedule);
            }

            return schedules;
        }

        protected void showSchedule(Schedule schedule, String visibility, String container) {
            LinearLayout container_layout = findViewById(R.id.container1);
            if(container.equals("container1")){
                    container_layout = findViewById(R.id.container1);
                } else if (container.equals("container2")) {
                    container_layout = findViewById(R.id.container2);
                }

            View inflatedViewCurrent = LayoutInflater.from(DriverActivity.this).inflate(R.layout.activity_schedule, container_layout, false);
            Button btn_showMap = inflatedViewCurrent.findViewById(R.id.btn_showMap);
            if(visibility.equals("GONE")){
                btn_showMap.setEnabled(false);
                int disabledColor = Color.GRAY; // Set your desired color for the disabled state
                btn_showMap.setBackgroundTintList(android.content.res.ColorStateList.valueOf(disabledColor));
            }
            TextView schedule_id = inflatedViewCurrent.findViewById(R.id.schedule_id);
            schedule_id.setText(schedule.getScheduleId());
            TextView bus_profile_id = inflatedViewCurrent.findViewById(R.id.bus_profile_id);
            bus_profile_id.setText(schedule.getBusProfileId());
            TextView date_time = inflatedViewCurrent.findViewById(R.id.date_time);
            date_time.setText(schedule.getDateTime());
            TextView start = inflatedViewCurrent.findViewById(R.id.start);
            start.setText(schedule.getStart());
            TextView destination = inflatedViewCurrent.findViewById(R.id.destination);
            destination.setText(schedule.getDestination());

            container_layout.addView(inflatedViewCurrent);
            btn_showMap.setOnClickListener(v -> showConfirmationDialog());
        }
        private void showConfirmationDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(DriverActivity.this);
            builder.setTitle("Confirmation");
            builder.setMessage("Are you sure you want start the journey?");

            builder.setPositiveButton("Yes", (dialog, which) -> {
                Intent intent = new Intent(DriverActivity.this, LocationActivity.class);
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
