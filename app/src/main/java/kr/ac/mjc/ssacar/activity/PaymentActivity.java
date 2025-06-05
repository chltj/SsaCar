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

    Vehicle selectedVehicle;
    int basePrice = 0;
    int insurancePrice = 0;
    int totalPrice = 0;

    String placeName;
    String address;
    String departureTime;
    String arrivalTime;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_paymente);

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
        btnBack.setOnClickListener(v -> finish());

        radioInsurance.setOnCheckedChangeListener((group, checkedId) -> {
            updateInsurancePrice(checkedId);
            calculatePrice();
        });

        checkboxAgree.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnPay.setEnabled(isChecked);
            btnPay.setBackgroundColor(isChecked ? 0xFF000000 : 0xFFCCCCCC);
        });

        btnPay.setOnClickListener(v -> {
            if (checkboxAgree.isChecked()) {
                processPayment();
            } else {
                Toast.makeText(PaymentActivity.this, "약관에 동의해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadVehicleData() {
        Intent intent = getIntent();
        selectedVehicle = intent.getParcelableExtra("selected_vehicle");

        placeName = intent.getStringExtra("place_name");
        address = intent.getStringExtra("address");
        departureTime = intent.getStringExtra("departure_time");
        arrivalTime = intent.getStringExtra("arrival_time");

        if (selectedVehicle != null) {
            carNameTv.setText(selectedVehicle.getName());
            carTypeTv.setText(selectedVehicle.getEngineType());

            String priceStr = selectedVehicle.getPrice().replace(",", "").replace("원", "");
            try {
                basePrice = Integer.parseInt(priceStr);
            } catch (NumberFormatException e) {
                basePrice = 31000;
            }
        } else {
            carNameTv.setText("선택된 차량");
            carTypeTv.setText("차량 타입");
            basePrice = 31000;
        }

        // 위치 및 시간 정보 표시
        String locationInfo = placeName + "\n" + address;
        locationTv.setText(locationInfo);

        String timeInfo = "출발: " + departureTime + "\n반납: " + arrivalTime;
        usageTimeTv.setText(timeInfo);

        radioInsurance.check(R.id.radio_70);
    }

    private void updateInsurancePrice(int checkedId) {
        if (checkedId == R.id.radio_none) {
            insurancePrice = 34300;
        } else if (checkedId == R.id.radio_10) {
            insurancePrice = 25000;
        } else if (checkedId == R.id.radio_30) {
            insurancePrice = 16000;
        } else if (checkedId == R.id.radio_70) {
            insurancePrice = 14300;
        } else {
            insurancePrice = 0;
        }
    }

    private void calculatePrice() {
        totalPrice = basePrice + insurancePrice;
        finalPriceTv.setText("총 결제금액: " + String.format("%,d원", totalPrice));
        btnPay.setText("총 " + String.format("%,d원", totalPrice) + " 결제하기");
    }

    private void processPayment() {
        String selectedPayment = spinnerCard.getSelectedItem().toString();

        if (selectedPayment.equals("신용카드 선택")) {
            Toast.makeText(this, "결제 수단을 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "결제 처리 중...", Toast.LENGTH_SHORT).show();

        btnPay.postDelayed(() -> {
            Toast.makeText(PaymentActivity.this,
                    selectedPayment + "로 " + String.format("%,d원", totalPrice) + " 결제가 완료되었습니다!",
                    Toast.LENGTH_LONG).show();
            finishPayment();
        }, 2000);
    }

    private void finishPayment() {
        Intent intent = new Intent(this, SmartKeyActivity.class);

        intent.putExtra("carName", selectedVehicle != null ? selectedVehicle.getName() : "차량명 없음");
        intent.putExtra("carImageUrl", selectedVehicle != null ? selectedVehicle.getImageUrl() : "");
        intent.putExtra("startTime", departureTime);
        intent.putExtra("endTime", arrivalTime);
        intent.putExtra("pickupLocation", placeName);
        intent.putExtra("returnLocation", address);

        startActivity(intent);
        finish();
    }



}
