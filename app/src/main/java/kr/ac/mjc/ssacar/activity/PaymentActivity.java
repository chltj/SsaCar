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
import java.util.Set;

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

    // 출발지 정보
    private String startPlaceName;
    private String startAddress;
    private double startLatitude;
    private double startLongitude;

    // 도착지 정보
    private String endPlaceName;
    private String endAddress;
    private double endLatitude;
    private double endLongitude;

    // 시간 정보
    private String departureTime;
    private String arrivalTime;
    private String selectedMonth;
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
        List<String> paymentMethods = new ArrayList<>();

        // 등록된 카드 불러오기
        List<PaymentCard> registeredCards = loadRegisteredCards();

        Log.d("PaymentActivity", "등록된 카드 수: " + registeredCards.size());

        // 등록된 카드가 있다면 먼저 추가
        if (registeredCards != null && !registeredCards.isEmpty()) {
            for (PaymentCard card : registeredCards) {
                // /1234 형태로 표시
                String cardDisplay = card.getCardTypeWithShortNumber();
                paymentMethods.add(cardDisplay);
                Log.d("PaymentActivity", "카드 추가: " + cardDisplay);
            }
        } else {
            paymentMethods.add("신용카드 선택");
            Log.d("PaymentActivity", "등록된 카드 없음 - 기본 선택 추가");
        }

        // 항상 들어가는 페이 결제 수단들
        paymentMethods.add("토스페이");
        paymentMethods.add("카카오페이");
        paymentMethods.add("네이버페이");

        Log.d("PaymentActivity", "총 결제 수단 수: " + paymentMethods.size());

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

        // 출발지 정보 받기
        startPlaceName = intent.getStringExtra("start_place_name");
        startAddress = intent.getStringExtra("start_address");
        startLatitude = intent.getDoubleExtra("start_latitude", 0.0);
        startLongitude = intent.getDoubleExtra("start_longitude", 0.0);

        // 도착지 정보 받기
        endPlaceName = intent.getStringExtra("end_place_name");
        endAddress = intent.getStringExtra("end_address");
        endLatitude = intent.getDoubleExtra("end_latitude", 0.0);
        endLongitude = intent.getDoubleExtra("end_longitude", 0.0);

        // 시간 정보 받기
        departureTime = intent.getStringExtra("departure_time");
        arrivalTime = intent.getStringExtra("arrival_time");
        selectedMonth= intent.getStringExtra("selected_month");

        // 디버그 로그 추가
        Log.d("PaymentActivity", "출발지: " + startPlaceName);
        Log.d("PaymentActivity", "도착지: " + endPlaceName);
        Log.d("PaymentActivity", "출발시간: " + departureTime);
        Log.d("PaymentActivity", "도착시간: " + arrivalTime);

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

        // 위치 정보 표시 - 출발지와 도착지 구분해서 표시
        StringBuilder locationInfo = new StringBuilder();
        if (startAddress != null && endAddress != null) {
            // 출발지와 도착지가 모두 있는 경우
            locationInfo.append("출발: ").append(startAddress).append("\n");
            locationInfo.append("도착: ").append(endAddress);
        } else if (startPlaceName != null) {
            // 출발지만 있는 경우 (이전 버전 호환)
            locationInfo.append("선택 위치: ").append(startPlaceName);
            if (startAddress != null) {
                locationInfo.append("\n").append(startAddress);
            }
        } else {
            // 아무 정보도 없는 경우
            locationInfo.append("명지전문대학교 주차장");
        }
        locationTv.setText(locationInfo.toString());

        // 시간 정보 표시
        StringBuilder timeInfo = new StringBuilder();
        if (departureTime != null && arrivalTime != null) {
            timeInfo.append("출발: ").append(departureTime).append("\n");
            timeInfo.append("반납: ").append(arrivalTime);
        } else if (departureTime != null) {
            timeInfo.append("출발: ").append(departureTime);
        } else {
            timeInfo.append("렌트기간 ").append(selectedMonth);
        }
        usageTimeTv.setText(timeInfo.toString());

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
        // 사용자 ID 기반으로 저장
        SharedPreferences userPrefs = getSharedPreferences("current_user", MODE_PRIVATE);
        String currentUserId = userPrefs.getString("current_user_id", null);
        if (currentUserId == null) return;

        Gson gson = new Gson();
        Type type = new TypeToken<List<PaymentHistory>>() {}.getType();
        SharedPreferences prefs = getSharedPreferences("payment_history", MODE_PRIVATE);
        String json = prefs.getString("history_list_" + currentUserId, null);
        List<PaymentHistory> list = (json != null) ? gson.fromJson(json, type) : new ArrayList<>();

        // PaymentHistory 생성 시 출발지와 도착지 정보 포함
        String locationForHistory;
        if (startPlaceName != null && endPlaceName != null) {
            locationForHistory = startPlaceName + " → " + endPlaceName;
        } else if (startPlaceName != null) {
            locationForHistory = startPlaceName;
        } else {
            locationForHistory = "위치 정보 없음";
        }

        String addressForHistory = startAddress != null ? startAddress : "주소 정보 없음";

        PaymentHistory newItem = new PaymentHistory(
                selectedVehicle.getName(),
                selectedVehicle.getEngineType(),
                locationForHistory,  // 출발지 → 도착지 형태로 저장
                addressForHistory,
                departureTime,
                arrivalTime,
                totalPrice,
                spinnerCard.getSelectedItem().toString(),
                selectedVehicle.getImageUrl()
        );
        list.add(0, newItem);
        saveHistoryForUser(list);

        // 알림 저장
        Type ntype = new TypeToken<List<NotificationItem>>() {}.getType();
        SharedPreferences nprefs = getSharedPreferences("notification_storage", MODE_PRIVATE);
        String njson = nprefs.getString("notifications_" + currentUserId, null);
        List<NotificationItem> notifications = (njson != null) ? gson.fromJson(njson, ntype) : new ArrayList<>();

        NotificationItem item = new NotificationItem(
                "SSACAR 결제 완료",
                spinnerCard.getSelectedItem().toString() + "로 " + String.format("%,d원", totalPrice) + " 결제가 완료되었습니다!",
                System.currentTimeMillis()
        );
        notifications.add(0, item);
        saveNotificationForUser(notifications);

        // 다음 화면 이동
        Intent intent = new Intent(this, UsageHistoryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void saveHistoryForUser(List<PaymentHistory> list) {
        SharedPreferences userPrefs = getSharedPreferences("current_user", MODE_PRIVATE);
        String currentUserId = userPrefs.getString("current_user_id", null);

        if (currentUserId != null) {
            SharedPreferences prefs = getSharedPreferences("payment_history", MODE_PRIVATE);
            prefs.edit().putString("history_list_" + currentUserId, new Gson().toJson(list)).apply();
        }
    }

    private void saveNotificationForUser(List<NotificationItem> notifications) {
        SharedPreferences userPrefs = getSharedPreferences("current_user", MODE_PRIVATE);
        String currentUserId = userPrefs.getString("current_user_id", null);

        if (currentUserId != null) {
            SharedPreferences prefs = getSharedPreferences("notification_storage", MODE_PRIVATE);
            prefs.edit().putString("notifications_" + currentUserId, new Gson().toJson(notifications)).apply();
        }
    }

    // 등록된 카드 불러오기 메서드 수정
    private List<PaymentCard> loadRegisteredCards() {
        List<PaymentCard> cards = new ArrayList<>();
        try {
            SharedPreferences userPrefs = getSharedPreferences("current_user", MODE_PRIVATE);
            String currentUserId = userPrefs.getString("current_user_id", null);

            Log.d("PaymentActivity", "현재 사용자 ID: " + currentUserId);

            if (currentUserId != null) {
                SharedPreferences cardPrefs = getSharedPreferences("ssacar_cards", MODE_PRIVATE);
                Set<String> savedSet = cardPrefs.getStringSet("cards_" + currentUserId, null);

                Log.d("PaymentActivity", "저장된 카드 Set: " + (savedSet != null ? savedSet.size() : "null"));

                if (savedSet != null && !savedSet.isEmpty()) {
                    Gson gson = new Gson();
                    for (String json : savedSet) {
                        try {
                            PaymentCard card = gson.fromJson(json, PaymentCard.class);
                            if (card != null) {
                                cards.add(card);
                                Log.d("PaymentActivity", "카드 로드 성공: " + card.getCardType() + " " + card.getMaskedCardNumber());
                            }
                        } catch (Exception e) {
                            Log.e("PaymentActivity", "카드 파싱 오류: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("PaymentActivity", "카드 불러오기 실패", e);
        }

        Log.d("PaymentActivity", "최종 로드된 카드 수: " + cards.size());
        return cards;
    }
}