package hcmute.edu.vn.miniproject1.services;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("CallReceiver", " Nhận broadcast PHONE_STATE");

        // Lấy số gọi đến từ intent
        String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        Log.d("CallReceiver", " Số nhận qua intent: " + incomingNumber);

        // Truyền số qua Service nếu cần
        Intent serviceIntent = new Intent(context, CallStateService.class);
        serviceIntent.putExtra("incomingNumber", incomingNumber); // 👈 truyền số

        context.startService(serviceIntent); // chỉ dùng startService
    }
}


