package kr.ac.mjc.ssacar.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import kr.ac.mjc.ssacar.R;
import kr.ac.mjc.ssacar.Vehicle;

public class PaymentActivity extends AppCompatActivity {

    // UI 요소들
    ImageView btnBack;
    TextView carNameTv;
    TextView carTypeTv;
    TextView locationTv;
    TextView usageTimeTv;
    TextView finalPriceTv;
    RadioGroup radioInsurance;
    Spinner spinnerCard;
    CheckBox checkboxAgree;
    Button btnPay;

    // 데이터
    Vehicle selectedVehicle;
    int basePrice = 0;          // 기본 차량 요금
    int insurancePrice = 0;     // 보험 추가 요금
    int totalPrice = 0;         // 총 결제 금액

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_paymente);

        // 초기화
        initViews();
        setupSpinner();
        setupListeners();
        loadVehicleData();
        calculatePrice();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        carNameTv = findViewById(R.id.car_name_tv);
        carTypeTv = findViewById(R.id.car_type_tv);
        locationTv = findViewById(R.id.location_tv);
        usageTimeTv = findViewById(R.id.usage_time_tv);
        finalPriceTv = findViewById(R.id.final_price_tv);
        radioInsurance = findViewById(R.id.radio_insurance);
        spinnerCard = findViewById(R.id.spinner_card);
        checkboxAgree = findViewById(R.id.checkbox_agree);
        btnPay = findViewById(R.id.btn_pay);
    }

    private void setupSpinner() {
        // 결제 수단 스피너 설정
        String[] paymentMethods = {
                "신용카드 선택",
                "국민카드 (****-1234)",
                "삼성카드 (****-5678)",
                "현대카드 (****-9012)",
                "토스페이",
                "카카오페이",
                "네이버페이"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                paymentMethods
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCard.setAdapter(adapter);
    }

    private void setupListeners() {
        // 뒤로가기 버튼
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 보험 선택 라디오 버튼
        radioInsurance.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateInsurancePrice(checkedId);
                calculatePrice();
            }
        });

        // 약관 동의 체크박스
        checkboxAgree.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnPay.setEnabled(isChecked);
            if (isChecked) {
                btnPay.setBackgroundColor(0xFF000000); // 검은색
            } else {
                btnPay.setBackgroundColor(0xFFCCCCCC); // 회색
            }
        });

        // 결제 버튼
        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkboxAgree.isChecked()) {
                    processPayment();
                } else {
                    Toast.makeText(PaymentActivity.this, "약관에 동의해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadVehicleData() {
        // 전달받은 차량 정보 가져오기
        Intent intent = getIntent();
        selectedVehicle = intent.getParcelableExtra("selected_vehicle");

        if (selectedVehicle != null) {
            // 차량 정보 표시
            carNameTv.setText(selectedVehicle.getName());
            carTypeTv.setText(selectedVehicle.getEngineType());

            // 기본 가격 추출 (쉼표 제거)
            String priceStr = selectedVehicle.getPrice().replace(",", "").replace("원", "");
            try {
                basePrice = Integer.parseInt(priceStr);
            } catch (NumberFormatException e) {
                basePrice = 31000; // 기본값
            }
        } else {
            // fallback 데이터
            carNameTv.setText("선택된 차량");
            carTypeTv.setText("차량 타입");
            basePrice = 31000;
        }

        // 위치 및 시간 정보 (임시)
        locationTv.setText("지하 명지전문대학 주차장");
        usageTimeTv.setText("이용 시간: 1시간");

        // 초기 보험 선택 (자기부담금 최대 70만원)
        radioInsurance.check(R.id.radio_70);
    }

    private void updateInsurancePrice(int checkedId) {
        if (checkedId == R.id.radio_none) {
            insurancePrice = 34300; // 자기부담금 없음
        } else if (checkedId == R.id.radio_10) {
            insurancePrice = 25000; // 자기부담금 최대 10만원
        } else if (checkedId == R.id.radio_30) {
            insurancePrice = 16000; // 자기부담금 최대 30만원
        } else if (checkedId == R.id.radio_70) {
            insurancePrice = 14300; // 자기부담금 최대 70만원
        } else {
            insurancePrice = 0;
        }
    }

    private void calculatePrice() {
        totalPrice = basePrice + insurancePrice;

        // 가격 표시 업데이트
        finalPriceTv.setText("총 결제금액: " + String.format("%,d원", totalPrice));
        btnPay.setText("총 " + String.format("%,d원", totalPrice) + " 결제하기");
    }

    private void processPayment() {
        // 결제 수단 확인
        String selectedPayment = spinnerCard.getSelectedItem().toString();

        if (selectedPayment.equals("신용카드 선택")) {
            Toast.makeText(this, "결제 수단을 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 결제 처리 시뮬레이션
        Toast.makeText(this, "결제 처리 중...", Toast.LENGTH_SHORT).show();

        // 2초 후 결제 완료
        btnPay.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 결제 완료 처리
                Toast.makeText(PaymentActivity.this,
                        selectedPayment + "로 " + String.format("%,d원", totalPrice) + " 결제가 완료되었습니다!",
                        Toast.LENGTH_LONG).show();

                // 결제 완료 후 처리
                finishPayment();
            }
        }, 2000);
    }

    private void finishPayment() {
        // 결제 완료 후 메인 화면으로 이동
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("payment_completed", true);
        intent.putExtra("vehicle_name", selectedVehicle != null ? selectedVehicle.getName() : "");
        intent.putExtra("total_price", totalPrice);
        startActivity(intent);
        finish();
    }
}
public void goToUsageHistory(View view) {
    Intent intent = new Intent(PaymentActivity.this, UsageHistoryActivity.class);

    // 예시: 사용자가 선택한 데이터 가져오기
    String carName = ((TextView) findViewById(R.id.detailCarName)).getText().toString();
    String startTime = "2025-06-02 14:00";  // 실제로는 TimeSettingActivity 등에서 설정한 값 사용
    String endTime = "2025-06-02 18:00";
    String pickupLocation = "서울 서대문구 신촌로 100";  // 선택한 대여 위치
    String returnLocation = "서울 종로구 종로 3가";    // 선택한 반납 위치

    // 이미지 URL도 같이 넘김 (현대 API에서 불러오는 방식이라면 URL 직접 넘기기)
    intent.putExtra("carName", carName);
    intent.putExtra("carImageUrl", "https://example.com/kona.png");
    intent.putExtra("startTime", startTime);
    intent.putExtra("endTime", endTime);
    intent.putExtra("pickupLocation", pickupLocation);
    intent.putExtra("returnLocation", returnLocation);

    startActivity(intent);
}
