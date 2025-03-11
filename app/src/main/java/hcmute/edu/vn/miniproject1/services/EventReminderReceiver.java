package hcmute.edu.vn.miniproject1.services;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

import hcmute.edu.vn.miniproject1.R;
import hcmute.edu.vn.miniproject1.controllers.CalendarActivity;
import hcmute.edu.vn.miniproject1.models.Event;

public class EventReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long eventId = intent.getLongExtra("event_id", -1);
        String title = intent.getStringExtra("event_title");
        if (eventId == -1) return;
        // Kiểm tra sự kiện có còn trong database không
        if (isEventCompletedOrDeleted(context, eventId)) {
            cancelRepeatNotification(context, eventId); // Hủy thông báo lặp lại
            return;
        }

        // Kiểm tra và đánh dấu sự kiện quá hạn
        markEventAsOverdue(context, eventId);

        // Hiển thị thông báo
        showNotification(context, eventId, title);

        // Lặp lại thông báo sau 5 phút
        scheduleRepeatNotification(context, eventId, title);
    }

    // Kiểm tra sự kiện đã hoàn thành hoặc bị xóa chưa
    private boolean isEventCompletedOrDeleted(Context context, long eventId) {
        EventDatabaseHelper dbHelper = new EventDatabaseHelper(context);
        Event event = dbHelper.getEventById(eventId);
        if (event == null) {
            cancelRepeatNotification(context, eventId); // Hủy thông báo ngay nếu sự kiện bị xóa
            return true;
        }
        return event.getStatus() == 1; // Trả về true nếu sự kiện đã hoàn thành
    }

    // Hiển thị thông báo
    private void showNotification(Context context, long eventId, String title) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent openAppIntent = new Intent(context, CalendarActivity.class); // Khi nhấn mở app
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, (int) eventId, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "event_channel")
                .setSmallIcon(R.drawable.baseline_calendar_today_24)
                .setContentTitle("Bạn có 1 sự kiện!")
                .setContentText("Tiêu đề: "+ title)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent); //  Nhấn vào thông báo để mở app
        notificationManager.notify((int) eventId, builder.build());
    }
    // Đặt báo thức lặp lại sau 5 phút
    private void scheduleRepeatNotification(Context context, long eventId, String title) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, EventReminderReceiver.class);
        intent.putExtra("event_id", eventId);
        intent.putExtra("event_title", title);

        // Xóa PendingIntent cũ (nếu có) trước khi đặt lại
        PendingIntent oldPendingIntent = PendingIntent.getBroadcast(
                context, (int) eventId, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (oldPendingIntent != null) {
            alarmManager.cancel(oldPendingIntent);
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, (int) eventId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerAtMillis = System.currentTimeMillis() + 30 * 60 * 1000; // 30 phút sau
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }


    //  Đánh dấu sự kiện là "quá hạn" trong database
    private void markEventAsOverdue(Context context, long eventId) {
        EventDatabaseHelper dbHelper = new EventDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", 2); // 2 là trạng thái quá hạn
        db.update("events", values, "id = ?", new String[]{String.valueOf(eventId)});
        db.close();
    }


    // Hủy thông báo lặp lại
    private void cancelRepeatNotification(Context context, long eventId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, EventReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, (int) eventId, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
