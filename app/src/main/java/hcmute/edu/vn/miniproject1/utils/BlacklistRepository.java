package hcmute.edu.vn.miniproject1.utils;

import androidx.core.app.ActivityCompat;


import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.CallLog;
import android.util.Log;

import android.widget.Toast;

import java.util.Arrays;
import java.util.HashSet;

import java.util.Set;

public class BlacklistRepository {

    // 💾 Dummy blacklist, bạn có thể dùng Room/SQLite
    private static final Set<String> BLACKLIST = new HashSet<>(Arrays.asList(
            "12345","+84901234567", "0123456789"
    ));

    public static String getLastIncomingCall(Context context) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("BlacklistHelper", "⚠ Thiếu quyền READ_CALL_LOG");
            return null;
        }

        String lastNumber = null;

        try (Cursor cursor = context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null,
                CallLog.Calls.TYPE + " = " + CallLog.Calls.INCOMING_TYPE,
                null,
                CallLog.Calls.DATE + " DESC"
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(CallLog.Calls.NUMBER);

                if (index != -1) {
                    lastNumber = cursor.getString(index);
                } else {
                    Log.e("BlacklistHelper", " Không tìm thấy cột NUMBER trong CallLog");
                }
            } else {
                Log.w("BlacklistHelper", " Cursor rỗng hoặc không moveToFirst()");
            }
        } catch (Exception e) {
            Log.e("BlacklistHelper", " Lỗi truy vấn CallLog: " + e.getMessage());
        }

        return lastNumber;
    }


    public static boolean isBlacklisted(Context context, String number) {
        if (number == null) return false;

        for (String blocked : BLACKLIST) {
            if (number.contains(blocked)) {
                return true;
            }
        }

        return false;
    }

    public static void handleBlockedCall(Context context, String number) {
        Toast toast = Toast.makeText(context, " Số " + number + " nằm trong blacklist", Toast.LENGTH_LONG);
        toast.show();
    }
}

