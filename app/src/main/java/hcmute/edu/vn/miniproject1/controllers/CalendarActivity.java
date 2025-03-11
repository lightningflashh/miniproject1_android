package hcmute.edu.vn.miniproject1.controllers;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import hcmute.edu.vn.miniproject1.R;
import hcmute.edu.vn.miniproject1.models.Event;
import hcmute.edu.vn.miniproject1.models.adapters.EventAdapter;
import hcmute.edu.vn.miniproject1.services.EventDatabaseHelper;
import hcmute.edu.vn.miniproject1.services.EventReminderReceiver;

public class CalendarActivity extends AppCompatActivity {
    private CalendarView calendarView;
    private ListView eventListView;
    private ImageView btnAddEvent;
    private EventDatabaseHelper dbHelper;
    private String selectedDate;
    private EventAdapter eventAdapter;

    private final BroadcastReceiver eventReminderReceiver = new EventReminderReceiver();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("DEBUG", "Đăng ký BroadcastReceiver động...");
            IntentFilter filter = new IntentFilter("hcmute.edu.vn.miniproject1.EVENT_REMINDER");
            // Cập nhật cách đăng ký để tránh lỗi
            registerReceiver(eventReminderReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }

        // Ánh xạ View
        calendarView = findViewById(R.id.calendar_View);
        eventListView = findViewById(R.id.ListView_Event);
        btnAddEvent = findViewById(R.id.btn_add_event);
        BottomNavigationView bottomNavigation = findViewById(R.id.nav_bottom);
        dbHelper = new EventDatabaseHelper(this);
        dbHelper.updateExpiredEvents();


        // Lấy ngày hiện tại
        Calendar calendar = Calendar.getInstance();
        selectedDate = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);

        // Load sự kiện
        loadEvents();

        // Xử lý chọn ngày trên CalendarView
        calendarView.setOnDateChangeListener((view, y, m, d) -> {
            selectedDate = y + "-" + (m + 1) + "-" + d;
            loadEvents();
        });

        // Xử lý khi nhấn nút thêm sự kiện
        btnAddEvent.setOnClickListener(view -> showEventDialog(null, null, null, null));

        // Xử lý khi chọn sự kiện trong danh sách
        eventListView.setOnItemClickListener((parent, view, position, id) -> {
            Event event = (Event) eventAdapter.getItem(position);
            editEvent(event.getId());
        });


        // Xử lý Bottom Navigation
        bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.icon_home) {
                Log.d("DEBUG", "Chọn icon_home, không điều hướng");
                return true;
            } else if (item.getItemId() == R.id.icon_info) {
                Log.d("DEBUG", "Điều hướng sang EventCompletedActivity");
                startActivity(new Intent(CalendarActivity.this, EventCompletedActivity.class));
                return true;
            }
            return false;
        });
    }



    // khởi tạo ra 1 trang dialog cho việc thêm 1 event
    private void showEventDialog(Long eventId, String existingTitle, String existingText, String existingTime) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_add_event);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        EditText editTextTitle = dialog.findViewById(R.id.txt_title);
        EditText editTextEvent = dialog.findViewById(R.id.txt_description);
        View view1 = dialog.findViewById(R.id.kc1);
        View view2 = dialog.findViewById(R.id.kc2);
        Button buttonSave = dialog.findViewById(R.id.btn_save_time_event);
        Button buttonDelete = dialog.findViewById(R.id.btn_delete);
        Button buttonComplete = dialog.findViewById(R.id.btn_complete);
        Button buttonTime = dialog.findViewById(R.id.btn_set_Time);
        TextView textViewTime = dialog.findViewById(R.id.txt_time);
        dialog.show();
        if (existingTitle != null) {
            editTextTitle.setText(existingTitle);
            editTextEvent.setText(existingText);
            textViewTime.setText(existingTime != null ? existingTime : "Chưa chọn giờ");
            buttonDelete.setVisibility(View.VISIBLE);
            buttonComplete.setVisibility(View.VISIBLE);
            view1.setVisibility(View.VISIBLE);
            view2.setVisibility(View.VISIBLE);
        } else {
            view1.setVisibility(View.GONE);
            view2.setVisibility(View.GONE);
            buttonDelete.setVisibility(View.GONE);
            buttonComplete.setVisibility(View.GONE);
        }
        buttonTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
                String amPm = (selectedHour >= 12) ? "PM" : "AM";
                int displayHour = (selectedHour == 0) ? 12 : (selectedHour > 12 ? selectedHour - 12 : selectedHour);
                textViewTime.setText(String.format("%02d:%02d %s", displayHour, selectedMinute, amPm));
            }, hour, minute, true).show();
        });

        final Long[] eventIdWrapper = {eventId}; // Dùng mảng để chứa eventId

        buttonSave.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString().trim();
            String description = editTextEvent.getText().toString().trim();
            String time = textViewTime.getText().toString();

            if (title.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show();
                return;
            }

            long timeInMillis = getTimeInMillis(selectedDate, time);
            if (timeInMillis == -1 || timeInMillis <= System.currentTimeMillis()) {
                Toast.makeText(this, "Vui lòng chọn thời gian hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (eventIdWrapper[0] == null) { // Thêm sự kiện mới
                eventIdWrapper[0] = dbHelper.saveEvent(selectedDate, title, description, time);
                if (eventIdWrapper[0] == -1) {
                    Toast.makeText(this, "Lỗi khi lưu sự kiện!", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else { // Cập nhật sự kiện cũ
                dbHelper.updateEvent(eventIdWrapper[0], selectedDate, title, description, time);
            }

            // Tạo đối tượng Event
            Event newEvent = new Event(eventIdWrapper[0], selectedDate, title, description, time, 0);

            // Đặt báo thức
            setEventReminder(newEvent);

            dialog.dismiss();
            loadEvents();
        });

        buttonComplete.setOnClickListener(v -> {
            if (eventId != null) {
                dbHelper.markEventAsCompleted(eventId);
                dialog.dismiss();
                loadEvents();
                Toast.makeText(this, "Sự kiện đã được đánh dấu hoàn thành!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Sự kiện không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });
        // Xu ly su kien khi an vao button delete
        buttonDelete.setOnClickListener(v -> {
            if (eventId != null) {
                dbHelper.deleteEvent(eventId);
                dialog.dismiss();
                loadEvents();
            } else {
                Toast.makeText(this, "Sự kiện không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void editEvent(long eventId) {
        Event event = dbHelper.getEventById(eventId);
        if (event != null) {
            showEventDialog(eventId, event.getTitle(), event.getDescription(), event.getTime());
        }
    }

    private void loadEvents() {
        List<Event> eventList = dbHelper.getEventsByDate(selectedDate);
        eventAdapter = new EventAdapter(this, eventList);
        eventListView.setAdapter(eventAdapter);
    }


    private void setEventReminder(Event event) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e("DEBUG", "AlarmManager không khả dụng!");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.e("DEBUG", "Thiếu quyền đặt báo thức chính xác!");
            Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(i);
            return;
        }

        long triggerTime = getTimeInMillis(event.getDate(), event.getTime());
        if (triggerTime == -1) {
            Log.e("DEBUG", "Lỗi lấy thời gian, không thể đặt báo thức!");
            return;
        }

        Log.d("DEBUG", "Đang đặt báo thức cho sự kiện ID: " + event.getId() + " vào lúc: " + triggerTime);

        Intent intent = new Intent(this, EventReminderReceiver.class);
        intent.putExtra("event_id", event.getId());
        intent.putExtra("event_title", event.getTitle());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, (int) event.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedTime = sdf.format(new Date(triggerTime));
        Log.d("DEBUG", "Thời gian báo thức: " + formattedTime);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }





    // Xét thời gian bằng giây để xác thực việc đặt event đúng và kog bị sai

    private long getTimeInMillis(String date, String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date dateTime = sdf.parse(date + " " + time);
            return dateTime != null ? dateTime.getTime() : -1;
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigation = findViewById(R.id.nav_bottom);
        bottomNavigation.setSelectedItemId(R.id.icon_home); // Môi khi chạy lại trang main thì nó load lại navBottom cho đúng icon đc hiển thị
    }
}
