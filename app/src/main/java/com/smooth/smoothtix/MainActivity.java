package com.smooth.smoothtix;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    String userName = "";
    String nic = "";
    String userRole = "";
    String p_id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTransparentNotificationBar();
    }
    public void loginControl(View view){
        Intent intent_login = new Intent(this, LoginActivity.class);
        Intent intent_driver = new Intent(this, DriverActivity.class);
        Intent intent_conductor = new Intent(this, ConductorActivity.class);
        Intent intent_passenger = new Intent(this, PassengerActivity.class);
        CheckSession checkSessionTask = new CheckSession(MainActivity.this, new CheckSessionCallback() {
            @Override
            public void onCheckSessionCompleted(String result) {
                if(Objects.equals(result, "401")){
                    startActivity(intent_login);
                }
                else{
                    Toast.makeText(MainActivity.this, "result", Toast.LENGTH_SHORT).show();
                    try {
                        JSONObject userData = new JSONObject(result);

                        userName = userData.getString("user_name");
                        nic = userData.getString("nic");
                        userRole = userData.getString("user_role");
                        p_id = userData.getString("p_id");
                        if(Objects.equals(userRole, "6")){
                            startActivity(intent_passenger);
                        }
                        else if(Objects.equals(userRole, "5")){
                            startActivity(intent_conductor);
                        }
                        else if(Objects.equals(userRole, "4")){
                            startActivity(intent_driver);
                        }
                        else{
                            Toast.makeText(MainActivity.this, "You are not allowed to login!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        checkSessionTask.execute();
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