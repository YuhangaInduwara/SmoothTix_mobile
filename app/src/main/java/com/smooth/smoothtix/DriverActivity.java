package com.smooth.smoothtix;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DriverActivity extends AppCompatActivity {

    private static final String server_url = Constants.server_url;
    TextView user_name, schedule_no, bus_no, route_no, route, conductor, date, time, status;
    ImageView userImageView, refresh_image;
    Button action_button;
    String userName, nic, userRole, p_id, driverId, schedule_id;
    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FAST_UPDATE_INTERVAL = 5;
    public static final int PERMISSION_FINE_LOCATION = 99;
    TextView journey_desc;
    Button end_button;
    LocationCallback locationCallBack;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    Boolean isEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        executeCheckSessionTask();

        user_name = findViewById(R.id.user_name);
        schedule_no = findViewById(R.id.schedule_no);
        bus_no = findViewById(R.id.bus_no);
        route_no = findViewById(R.id.route_no);
        route = findViewById(R.id.route);
        conductor = findViewById(R.id.conductor);
        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        status = findViewById(R.id.status);
        userImageView = findViewById(R.id.userImage);
        action_button = findViewById(R.id.action_button);
        refresh_image = findViewById(R.id.refresh_image);

        setTransparentNotificationBar();

        userImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutMenu(v);
            }
        });

        refresh_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshPage();
            }
        });

        if (action_button != null) {
            action_button.setOnClickListener(v -> {
                if(isEnabled == false){
                    showConfirmationDialog_1();
                }
                else{
                    showConfirmationDialog_2();
                }
            });
        } else {
            Log.e("DriverActivity", "action_button is null");
        }

    }

    private void refreshPage() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private void showLogoutMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.logout_menu);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_logout) {
                    Logout logoutTask = new Logout(DriverActivity.this, new LogoutCallback() {
                        @Override
                        public void onLogoutCompleted(String result) {
                            startActivity(new Intent(DriverActivity.this, MainActivity.class));
                            finish();
                        }
                    });
                    logoutTask.execute();
                }
                else if (item.getItemId() == R.id.menu_passenger) {
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
                try {
                    JSONObject userData = new JSONObject(result);

                    userName = userData.getString("user_name");
                    nic = userData.getString("nic");
                    userRole = userData.getString("user_role");
                    p_id = userData.getString("p_id");
                    user_name.setText(userName);

                    new GetDriverId().execute(p_id);
                } catch (JSONException e) {
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
            try {
                JSONArray jsonArray = new JSONArray(params[0]);
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                driverId = jsonObject.getString("driver_id");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
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

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {
            if(Objects.equals(result, "[]")){
                action_button.setEnabled(false);
                action_button.setBackgroundColor(getResources().getColor(R.color.gray));
                Toast.makeText(DriverActivity.this, "No upcoming schedules!", Toast.LENGTH_SHORT).show();
            }
            else if(Objects.equals(result, "400") || Objects.equals(result, "401") || Objects.equals(result, "402") || Objects.equals(result, "500")){
                action_button.setEnabled(false);
                action_button.setBackgroundColor(getResources().getColor(R.color.gray));
                Toast.makeText(DriverActivity.this, "Invalid request or Server error", Toast.LENGTH_SHORT).show();
            }
            else{
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    schedule_no.setText("Schedule No: " + jsonObject.getString("schedule_id"));
                    bus_no.setText("Bus No: " + jsonObject.getString("reg_no"));
                    route_no.setText("Route No: " + jsonObject.getString("route_no"));
                    route.setText("Route: " + jsonObject.getString("route"));
                    conductor.setText("Conductor: " + jsonObject.getString("conductor_name"));
                    date.setText("Date: " + jsonObject.getString("date"));
                    time.setText("Time: " + jsonObject.getString("time"));
                    status.setText("Status: " + jsonObject.getString("status"));

                    schedule_id = jsonObject.getString("schedule_id");

                    String timeString = jsonObject.getString("time");

                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    Date timeDate = sdf.parse(timeString);

                    Calendar calendar = Calendar.getInstance();
                    Date currentTime = calendar.getTime();

                    assert timeDate != null;

//                    if (timeDate.before(currentTime)) {
//                        action_button.setEnabled(false);
//                        action_button.setBackgroundColor(getResources().getColor(R.color.gray));
//                    }
//                    else action_button.setEnabled(timeDate.after(currentTime));

                } catch (Exception e) {
                    Toast.makeText(DriverActivity.this, "Error parsing result", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    private void showConfirmationDialog_1() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DriverActivity.this);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure you want start the journey?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            initializeLocationSharing();
            enableButton();
            dialog.dismiss();
        });

        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(getResources().getColor(R.color.red));

            Button negativeButton = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_NEGATIVE);
            negativeButton.setTextColor(getResources().getColor(R.color.red));
        });
        dialog.show();
    }

    private void showConfirmationDialog_2() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DriverActivity.this);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure you want stop the journey?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            stopLocationUpdates();
            disableButton();
            dialog.dismiss();
        });

        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(getResources().getColor(R.color.red));

            Button negativeButton = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_NEGATIVE);
            negativeButton.setTextColor(getResources().getColor(R.color.red));
        });
        dialog.show();
    }

    private void enableButton() {
        isEnabled = true;
        action_button.setText("Stop Journey");
        action_button.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
    }

    private void disableButton() {
        isEnabled = false;
        action_button.setText("Start Journey");
        action_button.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
    }

    private void initializeLocationSharing(){
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        new DriverActivity.newLocation().execute(schedule_id, "0.0", "0.0");

        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateLocationValues(locationResult.getLastLocation());
            }
        };

        updateGPS();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        } else {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
        }
    }

    private void stopLocationUpdates() {
        if (fusedLocationProviderClient != null && locationCallBack != null) {
            try {
                fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
            } catch (SecurityException e) {
                // Handle permission-related exception
                Log.e("DriverActivity", "Permission denied: " + e.getMessage());
            } catch (Exception e) {
                // Handle other exceptions
                Log.e("DriverActivity", "Error stopping location updates: " + e.getMessage());
            }
        } else {
            Log.e("DriverActivity", "fusedLocationProviderClient or locationCallBack is null");
        }
    }



    private void updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                updateLocationValues(location);
                startLocationUpdates();
            });
        } else {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
        }
    }

    private void updateLocationValues(Location location) {
        if (location != null) {
            new DriverActivity.updateLocation().execute(schedule_id, String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
        } else {
            Log.e("SmoothTixError", "This app is not working properly");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateGPS();
            } else {
                Log.e("SmoothTixError", "This app requires permission to be granted in order to work properly");
                finish();
            }
        }
    }

    private class newLocation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String schedule_id = params[0];
            String latitude = params[1];
            String longitude = params[2];

            try {
                String apiUrl = "http://10.0.2.2:2000/SmoothTix_war_exploded/locationController";

                URL url = new URL(apiUrl);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("schedule_id", schedule_id);
                jsonRequest.put("latitude", latitude);
                jsonRequest.put("longitude", longitude);

                try (OutputStream outputStream = connection.getOutputStream()) {
                    byte[] input = jsonRequest.toString().getBytes(StandardCharsets.UTF_8);
                    outputStream.write(input, 0, input.length);
                }

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
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        protected void onPostExecute(String result) {
            try {
                updateGPS();
                locationCallBack = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        updateLocationValues(locationResult.getLastLocation());
                    }
                };
            } catch (Exception e) {
                Log.e("SmoothTixError", "Error parsing result");

            }
        }
    }

    private static class updateLocation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String schedule_id = params[0];
            String latitude = params[1];
            String longitude = params[2];

            try {
                String apiUrl = "http://10.0.2.2:2000/SmoothTix_war_exploded/locationController";

                URL url = new URL(apiUrl);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("schedule_id", schedule_id);
                connection.setDoOutput(true);

                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("latitude", latitude);
                jsonRequest.put("longitude", longitude);

                try (OutputStream outputStream = connection.getOutputStream()) {
                    byte[] input = jsonRequest.toString().getBytes(StandardCharsets.UTF_8);
                    outputStream.write(input, 0, input.length);
                }

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
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        protected void onPostExecute(String result) {

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
                        | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}
