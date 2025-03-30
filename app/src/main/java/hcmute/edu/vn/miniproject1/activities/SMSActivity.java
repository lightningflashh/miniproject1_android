package hcmute.edu.vn.miniproject1.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import hcmute.edu.vn.miniproject1.R;
import hcmute.edu.vn.miniproject1.services.SMSService;

public class SMSActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_SEND_SMS = 101;
    private EditText etPhoneNumber;
    private StringBuilder phoneNumber = new StringBuilder();
    private ImageButton btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnConfirm = findViewById(R.id.btnSend);

        // Ánh xạ Toolbar từ layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Bật nút quay về (Back button)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Xử lý sự kiện khi bấm vào nút quay về
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Đóng Activity và quay về MainActivity
            }
        });

        setButtonListeners();
    }

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
                addNumber(text);
            });
        }

        btnConfirm.setOnClickListener(v -> openSMSApp());
    }

    private void addNumber(String number) {
        if (phoneNumber.length() < 15) {
            phoneNumber.append(number);
            etPhoneNumber.setText(phoneNumber.toString());
        }
    }

    private void openSMSApp() {
        String phone = etPhoneNumber.getText().toString().trim();

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra quyền gửi SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_SEND_SMS);
        } else {
            // Gọi SMSService để mở khung chat SMS
            Intent intent = new Intent(this, SMSService.class);
            intent.putExtra("phoneNumber", phone);
            startService(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openSMSApp();
            } else {
                Toast.makeText(this, "Quyền gửi SMS bị từ chối!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
