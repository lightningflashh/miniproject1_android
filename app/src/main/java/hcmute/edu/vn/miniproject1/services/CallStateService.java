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
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import hcmute.edu.vn.miniproject1.utils.BlacklistRepository;

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
        Log.d("CallStateService", " onCreate() được gọi");
        //BlacklistRepository blacklistRepository = new BlacklistRepository();
        checkPhonePermission();
    }


    private void checkPhonePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, " Không có quyền READ_PHONE_STATE!", Toast.LENGTH_SHORT).show();
                Log.e("CallStateService", " Thiếu quyền READ_PHONE_STATE");
                stopSelf();
                return;
            }
        }
        Log.d("CallStateService", " Có quyền, đăng ký listener");
        registerCallListener();
    }


    private void registerCallListener() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                Log.d("CallStateService", " onCallStateChanged: " + state + " - Số: " + incomingNumber);

                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        ongoingCall = true;
                        Toast.makeText(CallStateService.this, "Cuộc gọi đến từ: " + incomingNumber, Toast.LENGTH_SHORT).show();
                        // Kiểm tra blacklist
                        if (BlacklistRepository.isBlacklisted(getApplicationContext(), incomingNumber)) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "⚠️ Số " + incomingNumber + " nằm trong blacklist!", Toast.LENGTH_LONG);
                            toast.show();

                            Log.d("CallStateService", " Phát hiện số trong blacklist: " + incomingNumber);
                        }
                        break;

                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        ongoingCall = true;
                        break;

                    case TelephonyManager.CALL_STATE_IDLE:
                        if (ongoingCall) {
                            ongoingCall = false;
                            Toast.makeText(CallStateService.this, "Cuộc gọi kết thúc", Toast.LENGTH_SHORT).show();
                            Log.d("CallStateService", " Cuộc gọi kết thúc, kiểm tra số");

                            String lastNumber = BlacklistRepository.getLastIncomingCall(getApplicationContext());
                            Log.d("CallStateService", " Số vừa gọi: " + lastNumber);

                            if (BlacklistRepository.isBlacklisted(getApplicationContext(), lastNumber)) {
                                Log.d("CallStateService", " Số trong blacklist, xử lý...");
                                BlacklistRepository.handleBlockedCall(getApplicationContext(), lastNumber);
                            }
                        }
                        break;
                }
            }
        };

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