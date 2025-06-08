package kr.ac.mjc.ssacar.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import kr.ac.mjc.ssacar.R;

public class SamrtKeyActivity extends AppCompatActivity {
    private ImageView btnUnlock, btnLock, btnCustomer, btnHazard, btnHorn, btnExtend, btnReturnNow, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_key);  // XML 파일명에 맞게 설정

        initViews();
        setupListeners();
    }
    private void initViews() {
        btnUnlock = findViewById(R.id.btn_unlock);
        btnLock = findViewById(R.id.btn_lock);
        btnCustomer = findViewById(R.id.btn_customer);
        btnHazard = findViewById(R.id.btn_hazard);
        btnHorn = findViewById(R.id.btn_horn);
        btnExtend = findViewById(R.id.btn_extend);
        btnReturnNow = findViewById(R.id.btn_return_now);
        btnBack = findViewById(R.id.back_button);
    }

    private void setupListeners() {
        btnUnlock.setOnClickListener(v -> showToast("문이 열렸습니다."));
        btnLock.setOnClickListener(v -> showToast("문이 닫혔습니다."));
        btnCustomer.setOnClickListener(v -> showToast("고객센터 연결 중..."));
        btnHazard.setOnClickListener(v -> showToast("비상등이 켜졌습니다."));
        btnHorn.setOnClickListener(v -> showToast("경적을 울립니다."));
        btnExtend.setOnClickListener(v -> showToast("반납 시간이 연장되었습니다."));
        btnReturnNow.setOnClickListener(v -> showToast("차량을 즉시 반납합니다."));
        btnBack.setOnClickListener(v -> finish());
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
