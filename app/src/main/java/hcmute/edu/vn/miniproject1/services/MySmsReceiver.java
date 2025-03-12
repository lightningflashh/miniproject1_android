package hcmute.edu.vn.miniproject1.services;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MySmsReceiver extends BroadcastReceiver {
    public static final String SMS_BROADCAST = "sms_broadcast";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null) return;

        Object[] pdus = (Object[]) extras.get("pdus");
        if (pdus == null) return;

        for (Object pdu : pdus) {
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
            String sender = sms.getOriginatingAddress();
            String body = sms.getMessageBody();

            // Gửi tin nhắn đến MainActivity qua LocalBroadcast
            Intent smsIntent = new Intent(SMS_BROADCAST);
            smsIntent.putExtra("sender", sender);
            smsIntent.putExtra("body", body);
            LocalBroadcastManager.getInstance(context).sendBroadcast(smsIntent);
        }
    }
}