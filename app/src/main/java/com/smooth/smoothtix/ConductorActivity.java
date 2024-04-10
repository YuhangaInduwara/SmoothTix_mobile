package com.smooth.smoothtix;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ConductorActivity extends AppCompatActivity {

    private static final String server_url = Constants.server_url;
    TextView user_name, schedule_no, bus_no, route_no, route, driver, date, time, status;
    ImageView userImageView, refresh_image;
    Button action_button1, action_button2;
    String userName, nic, userRole, p_id, conductorId, schedule_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conductor);
        executeCheckSessionTask();

        user_name = findViewById(R.id.user_name);
        schedule_no = findViewById(R.id.schedule_no);
        bus_no = findViewById(R.id.bus_no);
        route_no = findViewById(R.id.route_no);
        route = findViewById(R.id.route);
        driver = findViewById(R.id.driver);
        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        status = findViewById(R.id.status);
        userImageView = findViewById(R.id.userImage);
        action_button1 = findViewById(R.id.action_button1);
        action_button2 = findViewById(R.id.action_button2);
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

        if (action_button1 != null) {
            action_button1.setOnClickListener(v -> {
                scanCode();
            });
        } else {
            Log.e("ConductorActivity", "action_button1 is null");
        }

        if (action_button2 != null) {
            action_button2.setOnClickListener(v -> {
                Intent intent = new Intent(ConductorActivity.this, QRActivity.class);
                startActivity(intent);
            });
        } else {
            Log.e("ConductorActivity", "action_button2 is null");
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
                    Logout logoutTask = new Logout(ConductorActivity.this, new LogoutCallback() {
                        @Override
                        public void onLogoutCompleted(String result) {
                            startActivity(new Intent(ConductorActivity.this, MainActivity.class));
                            finish();
                        }
                    });
                    logoutTask.execute();
                }
                else if (item.getItemId() == R.id.menu_passenger) {
                    startActivity(new Intent(ConductorActivity.this, PassengerActivity.class));
                    finish();
                }
                return true;
            }
        });

        popupMenu.show();
    }

    private void executeCheckSessionTask() {
        CheckSession checkSessionTask = new CheckSession(ConductorActivity.this, new CheckSessionCallback() {
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
                String apiUrl = server_url + "/conductorController";
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

                conductorId = jsonObject.getString("conductor_id");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            try {
                String apiUrl = server_url + "/scheduleController";
                URL url = new URL(apiUrl);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("conductor_id", conductorId);


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
                Toast.makeText(ConductorActivity.this, "No upcoming schedules!", Toast.LENGTH_SHORT).show();
            }
            else if(Objects.equals(result, "400") || Objects.equals(result, "401") || Objects.equals(result, "402") || Objects.equals(result, "500")){
                Toast.makeText(ConductorActivity.this, "Invalid request or Server error", Toast.LENGTH_SHORT).show();
            }
            else{
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    schedule_no.setText("Schedule No: " + jsonObject.getString("schedule_id"));
                    bus_no.setText("Bus No: " + jsonObject.getString("reg_no"));
                    route_no.setText("Route No: " + jsonObject.getString("route_no"));
                    route.setText("Route: " + jsonObject.getString("route"));
                    driver.setText("Driver: " + jsonObject.getString("driver_name"));
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
//                        action_button1.setEnabled(false);
//                        action_button2.setBackgroundColor(getResources().getColor(R.color.gray));//                        action_button1.setEnabled(false);
//                        action_button2.setBackgroundColor(getResources().getColor(R.color.gray));
//                    }
//                    else {
//                          action_button1.setEnabled(timeDate.after(currentTime));
//                          action_button2.setEnabled(timeDate.after(currentTime));
//                    }

                } catch (Exception e) {
                    Toast.makeText(ConductorActivity.this, "Error parsing result", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    private void scanCode()
    {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result->
    {
        if(result.getContents() !=null)
        {
            String message;
            String contents = result.getContents();
            String[] parts = contents.split("\\s+");
            String scheduleId = parts[0];
            String bookingId = parts[1];

//            if(Objects.equals(scheduleId, schedule_id)){
                new ConductorActivity.FetchBookingData().execute(bookingId);
//            }
//            else{
//                message = "The schedule is not matched!";
//                showBookingDetails(message, 0, null);
//
//            }
        }
    });

    private class FetchBookingData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String booking_id = params[0];
//            return booking_id;
            try {
                String apiUrl = server_url + "/bookingController?booking_id=" + booking_id;
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

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {
//            Toast.makeText(ConductorActivity.this, result, Toast.LENGTH_SHORT).show();
            if(Objects.equals(result, "[]")){
                Toast.makeText(ConductorActivity.this, "No upcoming schedules!", Toast.LENGTH_SHORT).show();
            }
            else if(Objects.equals(result, "400") || Objects.equals(result, "401") || Objects.equals(result, "402") || Objects.equals(result, "500")){
                Toast.makeText(ConductorActivity.this, "Invalid request or Server error", Toast.LENGTH_SHORT).show();
            }
            else{
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    String message = "Booking Id: " + jsonObject.getString("booking_id") + "/n" + "Bus No: " + jsonObject.getString("reg_no") + "/n" + "Start: " + jsonObject.getString("start") + "/n" + "Destination: " + jsonObject.getString("destination") + "/n" + "Date: " + jsonObject.getString("date") + "/n" + "Time: " + jsonObject.getString("time") + "/n" + "Status: " + jsonObject.getString("status") + "/n" + "Booking Id: " + jsonObject.getString("booking_id") + "/n";
                    showBookingDetails(message, 1, jsonObject.getString("booking_id"), jsonObject.getString("status"));
                } catch (Exception e) {
//                    Toast.makeText(ConductorActivity.this, "Error parsing result", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    private void showBookingDetails(String message, int flag, String booking_id, String status){
        // Replacing "/n" with HTML line break tag "<br>" for formatting
        message = message.replace("/n", "<br>");
        // Building an HTML list
        StringBuilder htmlList = new StringBuilder();
        htmlList.append("<html><body><ul>");
        htmlList.append("<li>").append(message).append("</li>");
        htmlList.append("</ul></body></html>");

        AlertDialog.Builder builder = new AlertDialog.Builder(ConductorActivity.this);
        builder.setTitle("Booking Details");

        // Setting message as HTML content
        builder.setMessage(Html.fromHtml(htmlList.toString(), Html.FROM_HTML_MODE_COMPACT));

        if(flag == 1 && Objects.equals(status, "0")){
            builder.setPositiveButton("Admit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(booking_id != null){
                        new ConductorActivity.AdmitPassenger().execute(booking_id);
                    }
                    dialogInterface.dismiss();
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
        }
        else if (flag == 1 && Objects.equals(status, "1")){
            Toast.makeText(ConductorActivity.this, "Already admitted", Toast.LENGTH_SHORT).show();
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
        }
        else{
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
        }
        builder.show();
    }

    private class AdmitPassenger extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String booking_id = params[0];
            try {
                String apiUrl = server_url + "/bookingController?action=admit";
                URL url = new URL(apiUrl);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("booking_id", booking_id);
                connection.setRequestProperty("status", "1");

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
            Toast.makeText(ConductorActivity.this, result, Toast.LENGTH_SHORT).show();
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
