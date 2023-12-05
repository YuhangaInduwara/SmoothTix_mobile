package com.smooth.smoothtix;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTransparentNotificationBar();
    }

    public void onClickLogin(View view) {
        EditText nic = findViewById(R.id.nic);
        EditText password = findViewById(R.id.password);

        // Get the values from the EditText fields
        String nicValue = nic.getText().toString();
        String passwordValue = password.getText().toString();

        // Execute the AsyncTask to perform the POST request
        new LoginTask().execute(nicValue, passwordValue);
    }

    private class LoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String nicValue = params[0];
            String passwordValue = params[1];

            try {
                // Replace with your actual API endpoint
                String apiUrl = "http://10.0.2.2:2000/SmoothTix_war_exploded/loginController";

                // Create a URL object with the API endpoint
                URL url = new URL(apiUrl);

                // Open a connection to the URL
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Set the request method to POST
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("nic", nicValue);
                jsonRequest.put("password", passwordValue);

                // Write the JSON request body to the connection
                try (OutputStream outputStream = connection.getOutputStream()) {
                    byte[] input = jsonRequest.toString().getBytes(StandardCharsets.UTF_8);
                    outputStream.write(input, 0, input.length);
                }

                // Get the response code
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response from the server
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
                // Parse the JSON response
                JSONObject jsonResponse = new JSONObject(result);

                // Check if the response contains a key named "privilege"
                if (jsonResponse.has("user_role")) {
                    // Extract the privilege value
                    String privilege = jsonResponse.getString("user_role");
                    if(privilege.equals("4")){
                        Intent intent = new Intent(LoginActivity.this, DriverActivity.class);
                        startActivity(intent);
                    }
                    else if(privilege.equals("5")){
                        Intent intent = new Intent(LoginActivity.this, ConductorActivity.class);
                        startActivity(intent);
                    }
                    else if(privilege.equals("6")){
                        Intent intent = new Intent(LoginActivity.this, PassengerActivity.class);
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(LoginActivity.this, "Error: Sorry! You can't access the mobile version.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // If the response does not contain the "privilege" key, handle the error
                    Toast.makeText(LoginActivity.this, "Error: Invalid response format", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                // Handle JSON parsing error
                Toast.makeText(LoginActivity.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
    protected void setTransparentNotificationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
}
