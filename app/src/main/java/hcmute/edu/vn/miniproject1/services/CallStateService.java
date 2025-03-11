package hcmute.edu.vn.miniproject1.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
public class CallStateService extends Service {

    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    private boolean ongoingCall = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Không cần bind vì đây là service chạy độc lập
    }

    @Override
    public void onCreate() {
        super.onCreate();
        checkPhonePermission();
    }

    private void checkPhonePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Không có quyền truy cập trạng thái cuộc gọi!", Toast.LENGTH_SHORT).show();
                stopSelf(); // Dừng service nếu không có quyền
                return;
            }
        }
        registerCallListener();
    }

    private void registerCallListener() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        // Khi có cuộc gọi đến hoặc đang thực hiện cuộc gọi
                        ongoingCall = true;
                        Toast.makeText(CallStateService.this, "Cuộc gọi đến, tạm dừng nhạc...", Toast.LENGTH_SHORT).show();
                        break;

                    case TelephonyManager.CALL_STATE_IDLE:
                        // Khi cuộc gọi kết thúc
                        if (ongoingCall) {
                            ongoingCall = false;
                            Toast.makeText(CallStateService.this, "Cuộc gọi kết thúc, phát nhạc...", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        };

        // Đăng ký lắng nghe cuộc gọi
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }
}