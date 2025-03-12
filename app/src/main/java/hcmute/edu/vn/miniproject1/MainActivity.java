package hcmute.edu.vn.miniproject1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import hcmute.edu.vn.miniproject1.controllers.CalendarActivity;
import hcmute.edu.vn.miniproject1.controllers.CallActivity;
import hcmute.edu.vn.miniproject1.controllers.ListSongActivity;

public class MainActivity extends AppCompatActivity {

    Button btnPlayMusic, btnSMSAndCall, btnAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPlayMusic = findViewById(R.id.btn_play_music);
        btnSMSAndCall = findViewById(R.id.btn_sms_call);
        btnAlarm = findViewById(R.id.btn_alarm);

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
}