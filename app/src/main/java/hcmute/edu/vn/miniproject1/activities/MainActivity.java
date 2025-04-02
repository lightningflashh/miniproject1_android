package hcmute.edu.vn.miniproject1.activities;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import android.os.PowerManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import hcmute.edu.vn.miniproject1.R;
import hcmute.edu.vn.miniproject1.services.BatteryMonitorService;

public class MainActivity extends AppCompatActivity {

    LinearLayout btnPlayMusic, btnSMS, btnAlarm, btnCall;
    private BroadcastReceiver batteryAlertReceiver;
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int NOTIFICATION_PERMISSION_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Kiểm tra và yêu cầu các quyền cần thiết
        if (!Settings.System.canWrite(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
        checkAndRequestWriteSettings();

        // Kiểm tra và yêu cầu quyền thông báo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 
                    NOTIFICATION_PERMISSION_CODE);
            } else {
                checkAndStartService();
            }
        } else {
            checkAndStartService();
        }

        // Đăng ký receiver cho thông báo pin
        batteryAlertReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("BATTERY_ALERT".equals(intent.getAction())) {
                    String message = intent.getStringExtra("message");
                    int level = intent.getIntExtra("level", 0);
                    showBatteryAlertDialog(message, level);
                }
            }
        };

        // Đăng ký receiver với flag RECEIVER_NOT_EXPORTED
        IntentFilter filter = new IntentFilter("BATTERY_ALERT");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(batteryAlertReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            ContextCompat.registerReceiver(
                this,
                batteryAlertReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            );
        }

        // Khởi tạo các button
        btnPlayMusic = findViewById(R.id.btn_play_music);
        btnSMS = findViewById(R.id.btn_sms);
        btnAlarm = findViewById(R.id.btn_alarm);
        btnCall = findViewById(R.id.btn_call);

        // Xử lý sự kiện click
        btnPlayMusic.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListSongActivity.class);
            startActivity(intent);
        });

        btnSMS.setOnClickListener(v -> {
            Intent intent = new Intent(this, SMSActivity.class);
            startActivity(intent);
        });
        
        btnCall.setOnClickListener(v -> {
            Intent intent = new Intent(this, CallActivity.class);
            startActivity(intent);
        });
        
        btnAlarm.setOnClickListener(v -> {
            Intent intent = new Intent(this, CalendarActivity.class);
            startActivity(intent);
        });
    }

    private void checkAndStartService() {
        // Kiểm tra và yêu cầu quyền FOREGROUND_SERVICE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.FOREGROUND_SERVICE) 
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{android.Manifest.permission.FOREGROUND_SERVICE}, 
                    PERMISSION_REQUEST_CODE);
            } else {
                startBatteryMonitorService();
            }
        } else {
            startBatteryMonitorService();
        }
    }

    private void startBatteryMonitorService() {
        Intent serviceIntent = new Intent(this, BatteryMonitorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBatteryMonitorService();
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkAndStartService();
            }
        }
    }

    private void showBatteryAlertDialog(String message, int level) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cảnh báo pin");
        builder.setMessage(message);
        
        if (level <= 10) { // Chỉ hiển thị nút bật tiết kiệm pin khi pin thấp
            builder.setPositiveButton("Bật tiết kiệm pin", (dialog, which) -> {
                enablePowerSaveMode(this);
            });
        }
        
        builder.setNegativeButton("Đóng", null);
        builder.setCancelable(false); // Không cho phép đóng dialog bằng cách chạm ra ngoài
        builder.show();
    }

    public void checkAndRequestWriteSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }

    public boolean isPowerSaveMode(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return powerManager.isPowerSaveMode();
        }
        return false;
    }

    public void enablePowerSaveMode(Context context) {
        Intent intent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryAlertReceiver);
    }
}