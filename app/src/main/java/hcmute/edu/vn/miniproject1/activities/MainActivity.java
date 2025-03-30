package hcmute.edu.vn.miniproject1.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import hcmute.edu.vn.miniproject1.R;
import hcmute.edu.vn.miniproject1.configs.BatteryReceiver;

public class MainActivity extends AppCompatActivity {

    Button btnPlayMusic, btnSMSAndCall, btnAlarm;

    private BatteryReceiver batteryReceiver = new BatteryReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPlayMusic = findViewById(R.id.btn_play_music);
        btnSMSAndCall = findViewById(R.id.btn_sms_call);
        btnAlarm = findViewById(R.id.btn_alarm);

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);

        btnPlayMusic.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListSongActivity.class);
            startActivity(intent);
        });

        btnSMSAndCall.setOnClickListener(v -> {
            Intent intent = new Intent(this, CallActivity.class);
            startActivity(intent);
        });
        btnAlarm.setOnClickListener(v -> {
            Intent intent = new Intent(this, CalendarActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryReceiver);
    }
}