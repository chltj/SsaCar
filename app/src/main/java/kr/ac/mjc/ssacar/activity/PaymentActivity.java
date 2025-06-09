package kr.ac.mjc.ssacar.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import kr.ac.mjc.ssacar.NotificationItem;
import kr.ac.mjc.ssacar.PaymentCard;
import kr.ac.mjc.ssacar.PaymentHistory;
import kr.ac.mjc.ssacar.R;
import kr.ac.mjc.ssacar.Car;

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

    Car selectedVehicle;
    int basePrice = 0;
    int insurancePrice = 0;
    int totalPrice = 0;

    String placeName;
    String address;
    String departureTime;
    String arrivalTime;
    private ImageView carImageView;
    String usageType;

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
        usageType = getIntent().getStringExtra("usage_type");
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
        carImageView = findViewById(R.id.car_image);
    }

    private void setupSpinner() {
        List<PaymentCard> cardList = new ArrayList<>();
        List<String> paymentMethods = new ArrayList<>();

        // 등록된 카드가 있다면 먼저 추가
        if (cardList != null && !cardList.isEmpty()) {
            for (PaymentCard card : cardList) {
                paymentMethods.add(card.getCardType() + " (" + card.getMaskedCardNumber() + ")");
            }
        } else {
            paymentMethods.add("신용카드 선택");
        }

        // 항상 들어가는 페이 결제 수단들
        List<String> payMethods = new ArrayList<>();
        payMethods.add("토스페이");
        payMethods.add("카카오페이");
        payMethods.add("네이버페이");

        // 결합
        paymentMethods.addAll(payMethods);

        // Spinner 어댑터 설정
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
            if (selectedVehicle.hasOnlineImage()) {
                Glide.with(this)
                        .load(selectedVehicle.getImageUrl())
                        .placeholder(R.drawable.sample_car)
                        .into(carImageView);
            } else if (selectedVehicle.getImageResId() != 0) {
                carImageView.setImageResource(selectedVehicle.getImageResId());
            } else {
                carImageView.setImageResource(R.drawable.sample_car); // 기본 이미지
            }
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
        // 1. 결제 정보 저장
        SharedPreferences prefs = getSharedPreferences("payment_history", MODE_PRIVATE);
        Gson gson = new Gson();



        String json = prefs.getString("history_list", null);
        List<PaymentHistory> list;
        if (json != null) {
            Type type = new TypeToken<List<PaymentHistory>>() {}.getType();
            list = gson.fromJson(json, type);
        } else {
            list = new ArrayList<>();
        }

        // 새 결제 항목 생성 및 추가 (최신순으로 앞에 삽입)
        PaymentHistory newItem = new PaymentHistory(
                selectedVehicle.getName(),
                selectedVehicle.getEngineType(),
                placeName,
                address,
                departureTime,
                arrivalTime,
                totalPrice,
                spinnerCard.getSelectedItem().toString(),
                selectedVehicle.getImageUrl()
        );

        list.add(0, newItem);
        prefs.edit().putString("history_list", gson.toJson(list)).apply();
        Log.d("PaymentHistory", "Saved list size: " + list.size());

        // 2. 다음 화면으로 이동
        Intent intent = new Intent(this, UsageHistoryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

        // 알림 데이터 저장
        SharedPreferences nprefs = getSharedPreferences("notification_storage", MODE_PRIVATE);
        String njson = nprefs.getString("notifications", null);
        List<NotificationItem> notifications;

        Type ntype = new TypeToken<List<NotificationItem>>() {}.getType();
        Gson ngson = new Gson();

        if (njson != null) {
            notifications = ngson.fromJson(njson, ntype);
        } else {
            notifications = new ArrayList<>();
        }

        NotificationItem item = new NotificationItem(
                "SSACAR 결제 완료",
                spinnerCard.getSelectedItem().toString() + "로 " + String.format("%,d원", totalPrice) + " 결제가 완료되었습니다!",
                System.currentTimeMillis()
        );

        notifications.add(0, item);
        nprefs.edit().putString("notifications", ngson.toJson(notifications)).apply();

    }


}
