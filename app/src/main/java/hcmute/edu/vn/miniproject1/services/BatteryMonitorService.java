package hcmute.edu.vn.miniproject1.services;
import android.app.*;
import android.content.*;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import hcmute.edu.vn.miniproject1.R;

public class BatteryMonitorService extends Service {

    private static final int BATTERY_CRITICAL = 5;
    private static final int BATTERY_LOW = 10;
    private static final int BATTERY_WARNING = 20;
    
    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int batteryPct = (int) ((level / (float) scale) * 100);
            
            // Cập nhật thông báo với phần trăm pin hiện tại
            updateNotification(batteryPct);
            
            // Kiểm tra xem có đang ở chế độ tiết kiệm pin không
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            boolean isPowerSaveMode = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                isPowerSaveMode = powerManager.isPowerSaveMode();
            }
            
            // Chỉ xử lý nếu không ở chế độ tiết kiệm pin
            if (!isPowerSaveMode) {
                handleBatteryLevel(context, batteryPct);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, createNotification(100)); // Khởi tạo với 100%
        return START_STICKY;
    }

    private Notification createNotification(int batteryLevel) {
        String channelId = "BatteryService";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Battery Monitor", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Theo dõi pin")
                .setContentText("Pin hiện tại: " + batteryLevel + "%")
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .build();
    }

    private void updateNotification(int batteryLevel) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, createNotification(batteryLevel));
    }

    private void handleBatteryLevel(Context context, int batteryPct) {
        Intent batteryIntent = new Intent("BATTERY_ALERT");
        batteryIntent.setPackage(context.getPackageName()); // Thêm package name để đảm bảo broadcast chỉ đến đúng ứng dụng
        
        if (batteryPct == BATTERY_CRITICAL) {
            // Xử lý khi pin cực kỳ yếu (<= 5%)
            setBrightness(context, 20);
            setWiFiState(context, false);
            batteryIntent.putExtra("level", BATTERY_CRITICAL);
            batteryIntent.putExtra("message", "Pin cực kỳ yếu! Hãy sạc pin ngay lập tức.");
        } else if (batteryPct == BATTERY_LOW) {
            // Xử lý khi pin yếu (<= 10%)
            setBrightness(context, 30);
            setWiFiState(context, false);
            batteryIntent.putExtra("level", BATTERY_LOW);
            batteryIntent.putExtra("message", "Pin yếu! Hãy sạc pin sớm.");
        } else if (batteryPct == BATTERY_WARNING) {
            // Xử lý khi pin ở mức cảnh báo (<= 20%)
            setBrightness(context, 50);
            setWiFiState(context, true);
            batteryIntent.putExtra("level", BATTERY_WARNING);
            batteryIntent.putExtra("message", "Pin đang ở mức thấp. Hãy cân nhắc sạc pin.");
        } else {
            // Pin ở mức bình thường
            setBrightness(context, 70);
            setWiFiState(context, true);
            return; // Không gửi broadcast nếu pin bình thường
        }
        
        // Gửi broadcast đến MainActivity
        sendBroadcast(batteryIntent);
    }

    private void setBrightness(Context context, int brightness) {
        if (Settings.System.canWrite(context)) {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
        }
    }

    private void setWiFiState(Context context, boolean enabled) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            wifiManager.setWifiEnabled(enabled);
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(batteryReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
