package com.smooth.smoothtix;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
public class DriverActivity extends AppCompatActivity {


    private final int current = 1;
    private final int upcoming = 20;
    TextView schedule_id, bus_profile_id, date_time, start, destination;
    Button btn_showMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        if(current == 1){
            // Assuming you have a reference to the LinearLayout
            LinearLayout container1 = findViewById(R.id.container1);

            View inflatedViewCurrent = LayoutInflater.from(this).inflate(R.layout.activity_schedule, container1, false);
            btn_showMap = inflatedViewCurrent.findViewById(R.id.btn_showMap);
            btn_showMap.setVisibility(View.VISIBLE);
            schedule_id = inflatedViewCurrent.findViewById(R.id.schedule_id);
            schedule_id.setText("SH00001");
            bus_profile_id = inflatedViewCurrent.findViewById(R.id.bus_profile_id);
            bus_profile_id.setText("BP00001");
            date_time = inflatedViewCurrent.findViewById(R.id.date_time);
            date_time.setText("2023/12/23");
            start = inflatedViewCurrent.findViewById(R.id.start);
            start.setText("Colombo");
            destination = inflatedViewCurrent.findViewById(R.id.destination);
            destination.setText("Galle");
            container1.addView(inflatedViewCurrent);
        }
        if(upcoming >=1){
            LinearLayout container2 = findViewById(R.id.container2);

            for (int i = 0; i < upcoming; i++) {
                View inflatedViewUpComing = LayoutInflater.from(this).inflate(R.layout.activity_upcoming_schedules, container2, false);
                schedule_id = inflatedViewUpComing.findViewById(R.id.schedule_id);
                schedule_id.setText("SH0000" + (i + 1));
                bus_profile_id = inflatedViewUpComing.findViewById(R.id.bus_profile_id);
                bus_profile_id.setText("BP0000"+ (i + 1));
                date_time = inflatedViewUpComing.findViewById(R.id.date_time);
                date_time.setText("2023/12/" +(i + 1));
                start = inflatedViewUpComing.findViewById(R.id.start);
                start.setText("Colombo");
                destination = inflatedViewUpComing.findViewById(R.id.destination);
                destination.setText("Galle");
                container2.addView(inflatedViewUpComing);
            }
        }
    }
}
