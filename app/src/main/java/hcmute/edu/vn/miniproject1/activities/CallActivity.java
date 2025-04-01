package hcmute.edu.vn.miniproject1.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import hcmute.edu.vn.miniproject1.R;
import hcmute.edu.vn.miniproject1.services.CallService;

public class CallActivity extends AppCompatActivity {

    // Khai báo các thành phần giao diện
    private EditText etPhoneNumber; // Ô nhập số điện thoại
    private StringBuilder phoneNumber = new StringBuilder(); // Chuỗi chứa số điện thoại nhập vào
    private BottomNavigationView bottomNavigationView; // Thanh điều hướng
    private ImageButton btnConfirm; // Nút gọi
    private static final int REQUEST_CALL_PHONE = 1;
    private static final int REQUEST_READ_PHONE_STATE = 100;; // Hằng số yêu cầu quyền gọi điện

    private static final int REQUEST_CALL_LOG_PERMISSION = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Kích hoạt chế độ giao diện toàn màn hình
        setContentView(R.layout.activity_call); // Gán layout cho Activity

        // Ánh xạ các thành phần từ giao diện XML vào biến Java
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        btnConfirm = findViewById(R.id.btnCall);

        // Đặt icon mặc định là "Dial" khi mở ứng dụng
        bottomNavigationView.setSelectedItemId(R.id.nav_dial);

        // Xử lý khi chọn các mục trong thanh điều hướng
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_sms) {
                // Mở màn hình nhắn tin (SMSActivity)
                startActivity(new Intent(CallActivity.this, SMSActivity.class));
                return true;

            }
            return true;
        });

        setButtonListeners(); // Thiết lập sự kiện cho các nút số và nút gọi
        // Gọi kiểm tra quyền
        checkPermissions();
        requestCallLogPermission();
    }

    // Gán sự kiện cho các nút bấm số và nút gọi
    private void setButtonListeners() {
        int[] buttonIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
                R.id.btn10, R.id.btn11
        };

        for (int id : buttonIds) {
            MaterialButton button = findViewById(id);
            button.setOnClickListener(v -> {
                String text = button.getText().toString();
                addNumber(text); // Gọi phương thức để thêm số vào ô nhập liệu
            });
        }

        // Xử lý khi bấm nút gọi
        btnConfirm.setOnClickListener(v -> makeCall());
    }

    // Thêm số vào ô nhập liệu
    private void addNumber(String number) {
        if (phoneNumber.length() < 15) { // Giới hạn tối đa 15 ký tự
            phoneNumber.append(number);
            etPhoneNumber.setText(phoneNumber.toString());
        }
    }

    // Xử lý cuộc gọi
    private void makeCall() {
        String phone = etPhoneNumber.getText().toString().trim(); // Lấy số điện thoại nhập vào

        if (TextUtils.isEmpty(phone)) { // Kiểm tra nếu chưa nhập số
            Toast.makeText(this, "Vui lòng nhập số điện thoại!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra quyền gọi điện thoại
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // Nếu chưa có quyền, yêu cầu cấp quyền
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE);
        } else {
            // Nếu đã có quyền, gọi CallService để thực hiện cuộc gọi
            Intent intent = new Intent(this, CallService.class);
            intent.putExtra("phoneNumber", phone);
            startService(intent);
        }
    }

    // Xử lý kết quả khi người dùng chọn "Cho phép" hoặc "Từ chối" quyền gọi điện
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeCall(); // thực hiện lại nếu vừa được cấp quyền
            } else {
                Toast.makeText(this, "Quyền gọi điện bị từ chối!", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQUEST_READ_PHONE_STATE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Đã cấp quyền đọc trạng thái cuộc gọi", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Không thể phát hiện cuộc gọi nếu thiếu quyền!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void requestCallLogPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CALL_LOG},
                    REQUEST_CALL_LOG_PERMISSION);
        }
    }


    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        }
    }


    // Khi quay lại màn hình chính, đặt lại biểu tượng điều hướng về "Dial"
    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_dial);
    }
}
