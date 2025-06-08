package kr.ac.mjc.ssacar.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import kr.ac.mjc.ssacar.R;

public class CardRegistrationActivity extends AppCompatActivity {
    private static final String TAG = "CardRegistration";

    // UI 요소들
    private EditText etCardNumber, etExpiryDate, etCvc, etCardholderName;
    private TextView tvCardType, tvCardPreview;
    private ImageView ivCardType, ivBack;
    private Button btnRegisterCard, btnCancel;
    private LinearLayout cardPreviewLayout;

    // 카드 정보
    private String cardNumber = "";
    private String expiryDate = "";
    private String cvc = "";
    private String cardholderName = "";
    private String cardType = "";

    // TextWatcher 무한 루프 방지용 플래그
    private boolean isUpdatingCardNumber = false;
    private boolean isUpdatingExpiryDate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_redistration); // 레이아웃 파일명 확인

        Log.d(TAG, "카드 등록 화면 시작");

        initViews();
        setupTextWatchers();
        setupButtons();
    }

    private void initViews() {
        try {
            // 입력 필드들
            etCardNumber = findViewById(R.id.et_card_number);
            etExpiryDate = findViewById(R.id.et_expiry_date);
            etCvc = findViewById(R.id.et_cvc);
            etCardholderName = findViewById(R.id.et_cardholder_name);

            // 카드 정보 표시
            tvCardType = findViewById(R.id.tv_card_type);
            tvCardPreview = findViewById(R.id.tv_card_preview);
            ivCardType = findViewById(R.id.iv_card_type);

            // 버튼들
            btnRegisterCard = findViewById(R.id.btn_register_card);
            btnCancel = findViewById(R.id.btn_cancel);
            ivBack = findViewById(R.id.iv_back);

            // 카드 프리뷰 레이아웃
            cardPreviewLayout = findViewById(R.id.card_preview_layout);

            // 초기 상태 설정
            btnRegisterCard.setEnabled(false);

            Log.d(TAG, "뷰 초기화 완료");

        } catch (Exception e) {
            Log.e(TAG, "뷰 초기화 실패", e);
            Toast.makeText(this, "화면 초기화 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupTextWatchers() {
        // 카드 번호 입력 감지
        etCardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingCardNumber) return;

                try {
                    isUpdatingCardNumber = true;

                    String input = s.toString().replaceAll("[^0-9]", "");

                    if (input.length() > 16) {
                        input = input.substring(0, 16);
                    }

                    cardNumber = input;

                    // 4자리마다 공백 추가
                    StringBuilder formatted = new StringBuilder();
                    for (int i = 0; i < input.length(); i++) {
                        if (i > 0 && i % 4 == 0) {
                            formatted.append(" ");
                        }
                        formatted.append(input.charAt(i));
                    }

                    String formattedString = formatted.toString();
                    if (!s.toString().equals(formattedString)) {
                        etCardNumber.setText(formattedString);
                        etCardNumber.setSelection(Math.min(formattedString.length(), etCardNumber.getText().length()));
                    }

                    detectCardType(input);
                    updateCardPreview();
                    validateForm();

                } catch (Exception e) {
                    Log.e(TAG, "카드 번호 처리 오류", e);
                } finally {
                    isUpdatingCardNumber = false;
                }
            }
        });

        // 만료일 입력 감지
        etExpiryDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingExpiryDate) return;

                try {
                    isUpdatingExpiryDate = true;

                    String input = s.toString().replaceAll("[^0-9]", "");

                    if (input.length() > 4) {
                        input = input.substring(0, 4);
                    }

                    expiryDate = input;

                    // MM/YY 형식으로 포맷
                    StringBuilder formatted = new StringBuilder(input);
                    if (input.length() > 2) {
                        formatted.insert(2, "/");
                    }

                    String formattedString = formatted.toString();
                    if (!s.toString().equals(formattedString)) {
                        etExpiryDate.setText(formattedString);
                        etExpiryDate.setSelection(Math.min(formattedString.length(), etExpiryDate.getText().length()));
                    }

                    updateCardPreview();
                    validateForm();

                } catch (Exception e) {
                    Log.e(TAG, "만료일 처리 오류", e);
                } finally {
                    isUpdatingExpiryDate = false;
                }
            }
        });

        // CVC 입력 감지
        etCvc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String input = s.toString().replaceAll("[^0-9]", "");

                    if (input.length() > 4) {
                        input = input.substring(0, 4);
                        etCvc.setText(input);
                        etCvc.setSelection(input.length());
                    }

                    cvc = input;
                    validateForm();

                } catch (Exception e) {
                    Log.e(TAG, "CVC 처리 오류", e);
                }
            }
        });

        // 카드 소유자명 입력 감지
        etCardholderName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    cardholderName = s.toString().trim();
                    updateCardPreview();
                    validateForm();
                } catch (Exception e) {
                    Log.e(TAG, "카드 소유자명 처리 오류", e);
                }
            }
        });
    }

    // 🔧 개선된 카드 타입 감지
    private void detectCardType(String cardNumber) {
        try {
            if (cardNumber.length() < 1) {
                cardType = "";
                if (tvCardType != null) tvCardType.setText("카드 타입");
                setCardTypeIcon(0);
                return;
            }

            Log.d(TAG, "카드 번호 분석: " + cardNumber + " (길이: " + cardNumber.length() + ")");

            char firstDigit = cardNumber.charAt(0);

            // VISA 카드 (4로 시작)
            if (firstDigit == '4') {
                cardType = "VISA";
                if (tvCardType != null) tvCardType.setText("VISA");
                setCardTypeIcon(R.drawable.ic_visa);
                Log.d(TAG, "VISA 카드 감지됨");
            }
            // MasterCard (5로 시작)
            else if (firstDigit == '5') {
                cardType = "MasterCard";
                if (tvCardType != null) tvCardType.setText("MasterCard");
                setCardTypeIcon(R.drawable.ic_mastercard);
                Log.d(TAG, "MasterCard 감지됨 (5로 시작)");
            }
            // MasterCard 새로운 범위 (2221-2720) 또는 American Express 확인
            else if (cardNumber.length() >= 2) {
                String firstTwo = cardNumber.substring(0, 2);

                // American Express (34 또는 37로 시작)
                if (firstTwo.equals("34") || firstTwo.equals("37")) {
                    cardType = "American Express";
                    if (tvCardType != null) tvCardType.setText("AMEX");
                    setCardTypeIcon(R.drawable.ic_amex);
                    Log.d(TAG, "American Express 감지됨 (34/37 시작)");
                }
                // MasterCard 새로운 범위 확인 (4자리가 있을 때)
                else if (cardNumber.length() >= 4) {
                    try {
                        int firstFour = Integer.parseInt(cardNumber.substring(0, 4));
                        if (firstFour >= 2221 && firstFour <= 2720) {
                            cardType = "MasterCard";
                            if (tvCardType != null) tvCardType.setText("MasterCard");
                            setCardTypeIcon(R.drawable.ic_mastercard);
                            Log.d(TAG, "MasterCard 감지됨 (2221-2720 범위)");
                        } else {
                            cardType = "기타";
                            if (tvCardType != null) tvCardType.setText("카드");
                            setCardTypeIcon(R.drawable.ic_card_default);
                            Log.d(TAG, "기타 카드로 분류됨");
                        }
                    } catch (NumberFormatException e) {
                        cardType = "기타";
                        if (tvCardType != null) tvCardType.setText("카드");
                        setCardTypeIcon(R.drawable.ic_card_default);
                        Log.d(TAG, "숫자 변환 실패로 기타 카드로 분류");
                    }
                } else {
                    cardType = "기타";
                    if (tvCardType != null) tvCardType.setText("카드");
                    setCardTypeIcon(R.drawable.ic_card_default);
                    Log.d(TAG, "기타 카드로 분류됨 (짧은 번호)");
                }
            }
            else {
                cardType = "기타";
                if (tvCardType != null) tvCardType.setText("카드");
                setCardTypeIcon(R.drawable.ic_card_default);
                Log.d(TAG, "기타 카드로 분류됨 (매우 짧은 번호)");
            }

            Log.d(TAG, "최종 카드 타입: " + cardType);

        } catch (Exception e) {
            Log.e(TAG, "카드 타입 감지 오류", e);
            cardType = "기타";
            if (tvCardType != null) tvCardType.setText("카드");
            setCardTypeIcon(R.drawable.ic_card_default);
        }
    }

    private void setCardTypeIcon(int resourceId) {
        try {
            if (ivCardType != null && resourceId != 0) {
                ivCardType.setImageResource(resourceId);
                ivCardType.setVisibility(View.VISIBLE);
                Log.d(TAG, "카드 아이콘 설정됨: " + resourceId);
            } else if (ivCardType != null) {
                ivCardType.setVisibility(View.GONE);
                Log.d(TAG, "카드 아이콘 숨김");
            }
        } catch (Exception e) {
            Log.w(TAG, "카드 아이콘 설정 실패: " + e.getMessage());
            if (ivCardType != null) ivCardType.setVisibility(View.GONE);
        }
    }

    private void updateCardPreview() {
        try {
            if (tvCardPreview == null) return;

            StringBuilder preview = new StringBuilder();

            // 카드 번호 (마지막 4자리만 표시)
            if (cardNumber.length() >= 4) {
                String maskedNumber = "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
                preview.append(maskedNumber);
            } else if (cardNumber.length() > 0) {
                preview.append("**** **** **** ").append(cardNumber);
            } else {
                preview.append("**** **** **** ****");
            }

            preview.append("\n");

            // 만료일
            if (expiryDate.length() >= 4) {
                preview.append(expiryDate.substring(0, 2)).append("/").append(expiryDate.substring(2));
            } else {
                preview.append("MM/YY");
            }

            preview.append("  ");

            // 카드 타입
            if (!cardType.isEmpty()) {
                preview.append(cardType);
            }

            preview.append("\n");

            // 카드 소유자명
            if (!cardholderName.isEmpty()) {
                preview.append(cardholderName.toUpperCase());
            } else {
                preview.append("CARDHOLDER NAME");
            }

            tvCardPreview.setText(preview.toString());

        } catch (Exception e) {
            Log.e(TAG, "카드 프리뷰 업데이트 오류", e);
        }
    }

    private void validateForm() {
        try {
            boolean isValid = true;

            // 카드 번호 검증 (16자리)
            if (cardNumber.length() != 16) {
                isValid = false;
            }

            // 만료일 검증 (4자리 + 월 검증)
            if (expiryDate.length() != 4) {
                isValid = false;
            } else {
                try {
                    int month = Integer.parseInt(expiryDate.substring(0, 2));
                    if (month < 1 || month > 12) {
                        isValid = false;
                    }
                } catch (NumberFormatException e) {
                    isValid = false;
                }
            }

            // CVC 검증 (3-4자리)
            if (cvc.length() < 3 || cvc.length() > 4) {
                isValid = false;
            }

            // 카드 소유자명 검증
            if (cardholderName.trim().length() < 2) {
                isValid = false;
            }

            // 등록 버튼 활성화/비활성화
            if (btnRegisterCard != null) {
                btnRegisterCard.setEnabled(isValid);
                btnRegisterCard.setAlpha(isValid ? 1.0f : 0.5f);
            }

            Log.d(TAG, "폼 검증 결과: " + isValid +
                    " (카드번호:" + cardNumber.length() +
                    ", 만료일:" + expiryDate.length() +
                    ", CVC:" + cvc.length() +
                    ", 이름:" + cardholderName.trim().length() + ")");

        } catch (Exception e) {
            Log.e(TAG, "폼 검증 오류", e);
        }
    }

    private void setupButtons() {
        try {
            // 뒤로가기 버튼
            if (ivBack != null) {
                ivBack.setOnClickListener(v -> {
                    Log.d(TAG, "뒤로가기 버튼 클릭");
                    showCancelDialog();
                });
            }

            // 취소 버튼
            if (btnCancel != null) {
                btnCancel.setOnClickListener(v -> {
                    Log.d(TAG, "취소 버튼 클릭");
                    showCancelDialog();
                });
            }

            // 카드 등록 버튼
            if (btnRegisterCard != null) {
                btnRegisterCard.setOnClickListener(v -> {
                    Log.d(TAG, "카드 등록 버튼 클릭");
                    registerCard();
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "버튼 설정 오류", e);
        }
    }

    private void showCancelDialog() {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("카드 등록 취소")
                    .setMessage("카드 등록을 취소하시겠습니까?\n입력한 정보가 모두 삭제됩니다.")
                    .setPositiveButton("취소", (dialog, which) -> {
                        Log.d(TAG, "카드 등록 취소 확인");
                        dialog.dismiss();
                        setResult(RESULT_CANCELED);
                        finish();
                    })
                    .setNegativeButton("계속 작성", (dialog, which) -> {
                        Log.d(TAG, "카드 등록 계속");
                        dialog.dismiss();
                    })
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "취소 다이얼로그 오류", e);
        }
    }

    private void registerCard() {
        Log.d(TAG, "카드 등록 시작 - 타입: " + cardType + ", 번호 끝자리: " +
                (cardNumber.length() >= 4 ? cardNumber.substring(cardNumber.length() - 4) : cardNumber));

        try {
            // 로딩 상태로 변경
            if (btnRegisterCard != null) {
                btnRegisterCard.setEnabled(false);
                btnRegisterCard.setText("등록 중...");
            }

            // 실제로는 서버에 카드 정보를 전송해야 하지만, 여기서는 시뮬레이션
            new Thread(() -> {
                try {
                    // 네트워크 요청 시뮬레이션
                    Thread.sleep(1500);

                    runOnUiThread(() -> {
                        Log.d(TAG, "카드 등록 성공");
                        showSuccessDialog();
                    });

                } catch (InterruptedException e) {
                    Log.e(TAG, "카드 등록 실패", e);
                    runOnUiThread(() -> {
                        if (btnRegisterCard != null) {
                            btnRegisterCard.setEnabled(true);
                            btnRegisterCard.setText("카드 등록");
                        }
                        Toast.makeText(CardRegistrationActivity.this,
                                "카드 등록에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();

        } catch (Exception e) {
            Log.e(TAG, "카드 등록 오류", e);
            Toast.makeText(this, "카드 등록 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();

            if (btnRegisterCard != null) {
                btnRegisterCard.setEnabled(true);
                btnRegisterCard.setText("카드 등록");
            }
        }
    }

    private void showSuccessDialog() {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("카드 등록 완료")
                    .setMessage("카드가 성공적으로 등록되었습니다.\n이제 차량 대여 시 결제할 수 있습니다.")
                    .setPositiveButton("확인", (dialog, which) -> {
                        Log.d(TAG, "카드 등록 완료 확인");
                        dialog.dismiss();

                        // 결과 데이터 준비
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("card_registered", true);
                        resultIntent.putExtra("card_type", cardType);
                        resultIntent.putExtra("card_last_four", cardNumber.length() >= 4 ?
                                cardNumber.substring(cardNumber.length() - 4) : cardNumber);
                        resultIntent.putExtra("cardholder_name", cardholderName);

                        // 만료일 포맷팅 (MMYY -> MM/YY)
                        String formattedExpiry = expiryDate.length() >= 4 ?
                                expiryDate.substring(0, 2) + "/" + expiryDate.substring(2, 4) : expiryDate;
                        resultIntent.putExtra("expiry_date", formattedExpiry);

                        Log.d(TAG, "결과 데이터 설정 완료 - 카드타입: " + cardType +
                                ", 마지막4자리: " + (cardNumber.length() >= 4 ?
                                cardNumber.substring(cardNumber.length() - 4) : cardNumber) +
                                ", 소유자: " + cardholderName +
                                ", 만료일: " + formattedExpiry);

                        setResult(RESULT_OK, resultIntent);
                        finish();
                    })
                    .setCancelable(false)
                    .show();

        } catch (Exception e) {
            Log.e(TAG, "성공 다이얼로그 오류", e);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "하드웨어 뒤로가기 버튼 클릭");
        showCancelDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "CardRegistrationActivity 종료");
    }
}