package com.smooth.smoothtix;

import android.annotation.SuppressLint;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;
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

public class DriverActivity extends AppCompatActivity {

    private static final String server_url = Constants.server_url;
    TextView user_name, schedule_no, bus_no, route_no, route, conductor, date, time, status;
    ImageView userImageView, refresh_image;
    Button action_button;
    String userName, nic, userRole, p_id, driverId;

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
            action_button.setOnClickListener(v -> showConfirmationDialog());
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
                Toast.makeText(DriverActivity.this, "No upcoming schedules!", Toast.LENGTH_SHORT).show();
            }
            else if(Objects.equals(result, "400") || Objects.equals(result, "401") || Objects.equals(result, "402") || Objects.equals(result, "500")){
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
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(getResources().getColor(R.color.red));

            Button negativeButton = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_NEGATIVE);
            negativeButton.setTextColor(getResources().getColor(R.color.red));
        });
        dialog.show();
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
