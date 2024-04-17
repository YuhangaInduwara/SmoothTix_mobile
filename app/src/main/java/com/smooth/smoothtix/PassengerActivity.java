package com.smooth.smoothtix;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class PassengerActivity extends AppCompatActivity {

    private static final String server_url = Constants.server_url;
    TextView user_name, schedule_no, bus_no, route_no, route, seat_no, date, time, check_in, status;
    ImageView userImageView, refresh_image;
    Button action_button;
    String userName, nic, userRole, p_id, schedule_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);
        executeCheckSessionTask();

        user_name = findViewById(R.id.user_name);
        schedule_no = findViewById(R.id.schedule_no);
        bus_no = findViewById(R.id.bus_no);
        route_no = findViewById(R.id.route_no);
        route = findViewById(R.id.route);
        seat_no = findViewById(R.id.seat_no);
        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        status = findViewById(R.id.status);
        userImageView = findViewById(R.id.userImage);
        action_button = findViewById(R.id.action_button);
        refresh_image = findViewById(R.id.refresh_image);

        action_button.setEnabled(false);
        action_button.setBackgroundColor(getResources().getColor(R.color.gray));

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
                Intent intent_login = new Intent(this, ViewLocationActivity.class);
                intent_login.putExtra("schedule_id", schedule_id);
                startActivity(intent_login);
            });
        } else {
            Log.e("PassengerActivity", "action_button is null");
        }

    }

    private void refreshPage() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private void showLogoutMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.logout_menu_passenger);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_logout) {
                    Logout logoutTask = new Logout(PassengerActivity.this, new LogoutCallback() {
                        @Override
                        public void onLogoutCompleted(String result) {
                            startActivity(new Intent(PassengerActivity.this, MainActivity.class));
                            finish();
                        }
                    });
                    logoutTask.execute();
                }
                return true;
            }
        });

        popupMenu.show();
    }

    private void executeCheckSessionTask() {
        CheckSession checkSessionTask = new CheckSession(PassengerActivity.this, new CheckSessionCallback() {
            @Override
            public void onCheckSessionCompleted(String result) {
                try {
                    JSONObject userData = new JSONObject(result);

                    userName = userData.getString("user_name");
                    nic = userData.getString("nic");
                    userRole = userData.getString("user_role");
                    p_id = userData.getString("p_id");
                    user_name.setText(userName);

                    new FetchDataTask().execute(p_id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        checkSessionTask.execute();
    }

    private class FetchDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
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

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {

            if(Objects.equals(result, "[]")){
                Log.e("PassengerActivity", "No upcoming bookings!");
            }
            else if(Objects.equals(result, "400") || Objects.equals(result, "401") || Objects.equals(result, "402") || Objects.equals(result, "500") || Objects.equals(result, "Error:400") || Objects.equals(result, "Error:401") || Objects.equals(result, "Error:402") || Objects.equals(result, "Error:500")){
                Log.e("PassengerActivity", "Invalid request or Server error");
            }
            else{
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    schedule_id = jsonObject.getString("schedule_id");

                    schedule_no.setText("Booking No: " + jsonObject.getString("booking_id"));
                    bus_no.setText("Bus No: " + jsonObject.getString("reg_no"));
                    route_no.setText("Route No: " + jsonObject.getString("route_no"));
                    route.setText("Route: " + jsonObject.getString("start") + "-" + jsonObject.getString("destination"));
                    seat_no.setText("Seat No: " + jsonObject.getString("seat_no"));
                    date.setText("Date: " + jsonObject.getString("date"));
                    time.setText("Time: " + jsonObject.getString("time"));
                    if(jsonObject.getString("status").equals("0")){
                        status.setText("Status: Pending");
                    }
                    else if(jsonObject.getString("status").equals("1")){
                        status.setText("Status: Checked In");
                    }

                    String timeString = jsonObject.getString("time");

                    String[] timeParts = timeString.split(":");
                    int hour = Integer.parseInt(timeParts[0]);
                    int minute = Integer.parseInt(timeParts[1]);

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);

                    Date timeDate = calendar.getTime();

                    Date currentTime = new Date();


                    if (timeDate.after(currentTime)) {
                        action_button.setEnabled(false);
                        action_button.setBackgroundColor(getResources().getColor(R.color.gray));
                    }
                    else if(jsonObject.getString("status").equals("1")){
                        action_button.setEnabled(timeDate.before(currentTime));
                        action_button.setBackgroundColor(getResources().getColor(R.color.red));
                    }

                } catch (Exception e) {
                    Log.e("PassengerActivity", "Error parsing result");
                    e.printStackTrace();
                }
            }

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
