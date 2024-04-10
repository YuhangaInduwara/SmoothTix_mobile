package com.smooth.smoothtix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    private static final String server_url = Constants.server_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTransparentNotificationBar();
    }

    public void onClickLogin(View view) {
        EditText nic = findViewById(R.id.nic);
        EditText password = findViewById(R.id.password);

        String nicValue = nic.getText().toString();
        String passwordValue = password.getText().toString();

        new LoginTask().execute(nicValue, passwordValue);
    }

    private class LoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String nicValue = params[0];
            String passwordValue = params[1];

            try {
                String apiUrl = server_url + "/loginController";

                URL url = new URL(apiUrl);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("nic", nicValue);
                jsonRequest.put("password", passwordValue);

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
                JSONObject jsonResponse = new JSONObject(result);

                if (jsonResponse.has("token")) {
                    String jwtToken = jsonResponse.getString("token");

                    saveJwtTokenToSharedPreferences(jwtToken);

                    handleUserRole(jsonResponse);

                } else {
                    Toast.makeText(LoginActivity.this, "Error: Invalid response format", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Toast.makeText(LoginActivity.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        private void saveJwtTokenToSharedPreferences(String jwtToken) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("jwt_token", jwtToken);
            editor.apply();
        }

        private void handleUserRole(JSONObject jsonResponse) throws JSONException {
            if (jsonResponse.has("user_role")) {
                int privilege = jsonResponse.getInt("user_role");
                if (privilege == 4) {
                    Intent intent = new Intent(LoginActivity.this, DriverActivity.class);
                    startActivity(intent);
                } else if (privilege == 5) {
                    Intent intent = new Intent(LoginActivity.this, ConductorActivity.class);
                    startActivity(intent);
                } else if (privilege == 6) {
                    Intent intent = new Intent(LoginActivity.this, PassengerActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(LoginActivity.this, PassengerActivity.class);
                    startActivity(intent);
                }
            } else {
                Toast.makeText(LoginActivity.this, "Error: Invalid response format", Toast.LENGTH_SHORT).show();
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
