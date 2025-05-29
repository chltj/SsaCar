package kr.ac.mjc.ssacar;

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

import androidx.appcompat.app.AppCompatActivity;

        public class PaymentActivity extends AppCompatActivity {

            private ImageView btnBack;
            private RadioGroup radioInsurance;
            private Spinner spinnerCard;
            private CheckBox checkboxAgree;
            private Button btnPay;
            private TextView carNameTv;
            private TextView carTypeTv;
            private TextView locationTv;
            private TextView usageTimeTv;
            private TextView finalPriceTv;

            private String placeName;
            private String departureTime;
            private String arrivalTime;
            private CarInfo selectedCar;
            private int basePrice = 31000; // 기본 요금
            private int insurancePrice = 0; // 보험료

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_paymente);

                // UI 초기화
                initViews();

                // Intent에서 데이터 받기
                getIntentData();

                // 차량 정보 표시
                displayCarInfo();

                // 위치 정보 표시
                displayLocationInfo();

                // 이용 시간 표시
                displayUsageTime();

                // 보험 상품 설정
                setupInsurance();

                // 결제 수단 설정
                setupPaymentMethod();

                // 뒤로가기 버튼 설정
                setupBackButton();

                // 결제 버튼 설정
                setupPaymentButton();

                // 초기 결제 금액 표시
                updateFinalPrice();
            }

            private void initViews() {
                try {
                    btnBack = findViewById(R.id.btn_back);
                    android.util.Log.d("PaymentActivity", "btnBack 초기화 완료");
                } catch (Exception e) {
                    android.util.Log.e("PaymentActivity", "btnBack 초기화 실패", e);
                }

                try {
                    radioInsurance = findViewById(R.id.radio_insurance);
                    android.util.Log.d("PaymentActivity", "radioInsurance 초기화 완료");
                } catch (Exception e) {
                    android.util.Log.e("PaymentActivity", "radioInsurance 초기화 실패", e);
                }

                try {
                    spinnerCard = findViewById(R.id.spinner_card);
                    android.util.Log.d("PaymentActivity", "spinnerCard 초기화 완료");
                } catch (Exception e) {
                    android.util.Log.e("PaymentActivity", "spinnerCard 초기화 실패", e);
                }

                try {
                    checkboxAgree = findViewById(R.id.checkbox_agree);
                    android.util.Log.d("PaymentActivity", "checkboxAgree 초기화 완료");
                } catch (Exception e) {
                    android.util.Log.e("PaymentActivity", "checkboxAgree 초기화 실패", e);
                }

                try {
                    btnPay = findViewById(R.id.btn_pay);
                    android.util.Log.d("PaymentActivity", "btnPay 초기화 완료");
                } catch (Exception e) {
                    android.util.Log.e("PaymentActivity", "btnPay 초기화 실패", e);
                }

                try {
                    carNameTv = findViewById(R.id.car_name_tv);
                    android.util.Log.d("PaymentActivity", "carNameTv 초기화 완료");
                } catch (Exception e) {
                    android.util.Log.e("PaymentActivity", "carNameTv 초기화 실패", e);
                }

                try {
                    carTypeTv = findViewById(R.id.car_type_tv);
                    android.util.Log.d("PaymentActivity", "carTypeTv 초기화 완료");
                } catch (Exception e) {
                    android.util.Log.e("PaymentActivity", "carTypeTv 초기화 실패", e);
                }

                try {
                    locationTv = findViewById(R.id.location_tv);
                    android.util.Log.d("PaymentActivity", "locationTv 초기화 완료");
                } catch (Exception e) {
                    android.util.Log.e("PaymentActivity", "locationTv 초기화 실패", e);
                }

                try {
                    usageTimeTv = findViewById(R.id.usage_time_tv);
                    android.util.Log.d("PaymentActivity", "usageTimeTv 초기화 완료");
                } catch (Exception e) {
                    android.util.Log.e("PaymentActivity", "usageTimeTv 초기화 실패", e);
                }

                try {
                    finalPriceTv = findViewById(R.id.final_price_tv);
                    android.util.Log.d("PaymentActivity", "finalPriceTv 초기화 완료");
                } catch (Exception e) {
                    android.util.Log.e("PaymentActivity", "finalPriceTv 초기화 실패", e);
                }
            }

            private void getIntentData() {
                Intent intent = getIntent();
                placeName = intent.getStringExtra("place_name");
                departureTime = intent.getStringExtra("departure_time");
                arrivalTime = intent.getStringExtra("arrival_time");
                selectedCar = (CarInfo) intent.getSerializableExtra("selected_car");
            }

            private void displayCarInfo() {
                if (selectedCar != null) {
                    carNameTv.setText(selectedCar.getCarName());
                    carTypeTv.setText(selectedCar.getCarType() + " | " + selectedCar.getRating());
                } else {
                    carNameTv.setText("선택된 차량");
                    carTypeTv.setText("차량 정보 없음");
                }
            }

            private void displayLocationInfo() {
                if (placeName != null) {
                    locationTv.setText("📍 " + placeName);
                } else {
                    locationTv.setText("📍 위치 정보 없음");
                }
            }

            private void displayUsageTime() {
                if (departureTime != null && arrivalTime != null) {
                    usageTimeTv.setText("이용 시간: " + departureTime + " ~ " + arrivalTime);
                } else {
                    usageTimeTv.setText("이용 시간: 시간 정보 없음");
                }
            }

            private void setupInsurance() {
                radioInsurance.setOnCheckedChangeListener((group, checkedId) -> {
                    // 라디오 버튼 선택에 따른 보험료 계산
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

                    updateFinalPrice();
                });
            }

            private void setupPaymentMethod() {
                // 결제 수단 스피너 설정
                String[] paymentMethods = {
                        "신용카드 선택",
                        "삼성카드",
                        "현대카드",
                        "국민카드",
                        "신한카드",
                        "토스페이",
                        "카카오페이"
                };

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        paymentMethods
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCard.setAdapter(adapter);
            }

            private void setupBackButton() {
                btnBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish(); // 현재 액티비티 종료하고 이전 화면으로 돌아가기
                    }
                });
            }

            private void setupPaymentButton() {
                btnPay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 약관 동의 확인
                        if (!checkboxAgree.isChecked()) {
                            Toast.makeText(PaymentActivity.this, "예약 정보 확인 및 약관에 동의해주세요.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 결제 수단 선택 확인
                        if (spinnerCard.getSelectedItemPosition() == 0) {
                            Toast.makeText(PaymentActivity.this, "결제 수단을 선택해주세요.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 결제 처리
                        processPayment();
                    }
                });
            }

            private void updateFinalPrice() {
                int totalPrice = basePrice + insurancePrice;

                // 결제 금액 텍스트 업데이트
                finalPriceTv.setText("총 결제금액: " + String.format("%,d", totalPrice) + "원");

                // 결제 버튼 텍스트 업데이트
                btnPay.setText("총 " + String.format("%,d", totalPrice) + "원 결제하기");
            }

            private void processPayment() {
                // 결제 처리 중 메시지
                btnPay.setEnabled(false);
                btnPay.setText("결제 처리 중...");

                Toast.makeText(this, "결제를 진행합니다...", Toast.LENGTH_SHORT).show();

                // 2초 후 결제 완료 처리 (실제로는 결제 API 연동)
                btnPay.postDelayed(() -> {
                    // 결제 완료
                    int totalPrice = basePrice + insurancePrice;
                    String selectedPayment = spinnerCard.getSelectedItem().toString();

                    Toast.makeText(PaymentActivity.this,
                            "결제가 완료되었습니다!\n" +
                                    "차량: " + (selectedCar != null ? selectedCar.getCarName() : "선택된 차량") + "\n" +
                                    "금액: " + String.format("%,d", totalPrice) + "원\n" +
                                    "결제수단: " + selectedPayment,
                            Toast.LENGTH_LONG).show();

                    // 결제 완료 후 결과 반환
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("payment_success", true);
                    resultIntent.putExtra("total_price", totalPrice);
                    resultIntent.putExtra("reserved_car", selectedCar != null ? selectedCar.getCarName() : "선택된 차량");
                    setResult(RESULT_OK, resultIntent);

                    // 액티비티 종료
                    finish();

                }, 2000);
            }
        }