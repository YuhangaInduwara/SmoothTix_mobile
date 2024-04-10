package com.smooth.smoothtix;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Logout extends AsyncTask<Void, Void, String> {

    private final Context context;
    private final LogoutCallback callback;
    private static final String server_url = Constants.server_url;

    public Logout(Context context, LogoutCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String jwtToken = preferences.getString("jwt_token", "");

            if (jwtToken.isEmpty()) {
                return "Token not available";
            }

            String apiUrl = server_url + "/loginController?action=logout";
            URL url = new URL(apiUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            connection.setRequestProperty("Authorization", "Bearer " + jwtToken);

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
            }
            return String.valueOf(responseCode);
        } catch (Exception e) {
            e.printStackTrace();
            return "session_error";
        }
    }

    @Override
    protected void onPostExecute(String result) {
//        Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
        callback.onLogoutCompleted(result);
    }
}
