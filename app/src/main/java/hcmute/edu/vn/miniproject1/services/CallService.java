package hcmute.edu.vn.miniproject1.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class CallService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Không cần bind vì đây là service chạy độc lập
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String phoneNumber = intent.getStringExtra("phoneNumber");
            makeCall(phoneNumber);
        }
        return START_NOT_STICKY; // Không restart service khi bị kill
    }

    private void makeCall(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Bắt buộc khi gọi từ Service

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Không có quyền gọi điện!", Toast.LENGTH_SHORT).show();
            return;
        }

        startActivity(callIntent);
    }
}
