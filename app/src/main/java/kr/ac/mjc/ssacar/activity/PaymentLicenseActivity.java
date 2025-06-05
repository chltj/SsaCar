package kr.ac.mjc.ssacar.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import kr.ac.mjc.ssacar.R;

public class PaymentLicenseActivity extends AppCompatActivity {
    private static final String TAG = "PaymentLicenseActivity";
    private static final int REQUEST_CARD_REGISTRATION = 1001;

    // UI 요소들
    private Button btnAddCard;
    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_license);

        Log.d(TAG, "PaymentLicenseActivity 시작");

        // 뷰 초기화
        initViews();

        // 버튼 설정
        setupButtons();
    }

    private void initViews() {
        btnAddCard = findViewById(R.id.btn_add_card);
        ivBack = findViewById(R.id.iv_back);

        Log.d(TAG, "뷰 초기화 완료");
    }

    private void setupButtons() {
        // 뒤로가기 버튼
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> {
                Log.d(TAG, "뒤로가기 버튼 클릭");
                finish();
            });
        }

        // 카드 추가 버튼
        if (btnAddCard != null) {
            btnAddCard.setOnClickListener(v -> {
                Log.d(TAG, "카드 추가 버튼 클릭");
                goToCardRegistration();
            });
        }

        Log.d(TAG, "버튼 설정 완료");
    }

    // 카드 등록 화면으로 이동
    private void goToCardRegistration() {
        try {
            Log.d(TAG, "카드 등록 화면으로 이동 시도");
            Intent intent = new Intent(this, CardRegistrationActivity.class);
            startActivityForResult(intent, REQUEST_CARD_REGISTRATION);
        } catch (Exception e) {
            Log.e(TAG, "카드 등록 화면 이동 실패: " + e.getMessage());
            Toast.makeText(this, "카드 등록 화면을 열 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // XML에서 onClick으로 호출되는 메서드 (빈 상태 레이아웃의 버튼용)
    public void goToCardRegistration(View view) {
        Log.d(TAG, "빈 상태 버튼에서 카드 등록 호출");
        goToCardRegistration();
    }

    // 카드 등록 결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CARD_REGISTRATION) {
            if (resultCode == RESULT_OK && data != null) {
                boolean cardRegistered = data.getBooleanExtra("card_registered", false);

                if (cardRegistered) {
                    String cardType = data.getStringExtra("card_type");
                    String cardLastFour = data.getStringExtra("card_last_four");

                    Log.d(TAG, "카드 등록 성공: " + cardType + " **** " + cardLastFour);
                    Toast.makeText(this, "카드가 성공적으로 등록되었습니다!", Toast.LENGTH_SHORT).show();

                    // TODO: 여기서 카드 목록을 새로고침하거나 UI 업데이트
                    // 나중에 RecyclerView와 어댑터를 추가할 때 구현
                }
            } else {
                Log.d(TAG, "카드 등록 취소됨");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "PaymentLicenseActivity 다시 시작");
        // TODO: 카드 목록 새로고침
    }
}