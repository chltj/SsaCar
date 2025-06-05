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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_redistration);

        Log.d(TAG, "카드 등록 화면 시작");

        initViews();
        setupTextWatchers();
        setupButtons();
    }

    private void initViews() {
        // 입력 필드들
        etCardNumber = findViewById(R.id.et_card_number);
        etExpiryDate = findViewById(R.id.et_expiry_date);
        etCvc = findViewById(R.id.et_cvc);
        etCardholderName = findViewById(R.id.et_cardholder_name);

        // 카드 정보 표시
        tvCardType = findViewById(R.id.tv_card_type);

        // 버튼들
        btnRegisterCard = findViewById(R.id.btn_register_card);
        btnCancel = findViewById(R.id.btn_cancel);
        ivBack = findViewById(R.id.iv_back);

        // 초기 상태 설정
        btnRegisterCard.setEnabled(false);

        Log.d(TAG, "뷰 초기화 완료");
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
                String input = s.toString().replaceAll("\\s", ""); // 공백 제거

                if (input.length() > 16) {
                    input = input.substring(0, 16);
                }

                // 4자리마다 공백 추가
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < input.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(input.charAt(i));
                }

                cardNumber = input;

                // 텍스트 업데이트 (무한 루프 방지)
                if (!s.toString().equals(formatted.toString())) {
                    etCardNumber.removeTextChangedListener(this);
                    etCardNumber.setText(formatted.toString());
                    etCardNumber.setSelection(formatted.length());
                    etCardNumber.addTextChangedListener(this);
                }

                // 카드 타입 감지
                detectCardType(input);
                updateCardPreview();
                validateForm();
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
                String input = s.toString().replaceAll("/", "");

                if (input.length() > 4) {
                    input = input.substring(0, 4);
                }

                // MM/YY 형식으로 포맷
                StringBuilder formatted = new StringBuilder(input);
                if (input.length() >= 2) {
                    formatted.insert(2, "/");
                }

                expiryDate = input;

                if (!s.toString().equals(formatted.toString())) {
                    etExpiryDate.removeTextChangedListener(this);
                    etExpiryDate.setText(formatted.toString());
                    etExpiryDate.setSelection(formatted.length());
                    etExpiryDate.addTextChangedListener(this);
                }

                updateCardPreview();
                validateForm();
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
                cvc = s.toString();
                if (cvc.length() > 4) {
                    cvc = cvc.substring(0, 4);
                    etCvc.setText(cvc);
                    etCvc.setSelection(cvc.length());
                }
                validateForm();
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
                cardholderName = s.toString().trim();
                updateCardPreview();
                validateForm();
            }
        });
    }

    private void detectCardType(String cardNumber) {
        if (cardNumber.length() < 1) {
            cardType = "";
            tvCardType.setText("카드 타입");
            setCardTypeIcon(0);
            return;
        }

        char firstDigit = cardNumber.charAt(0);

        if (firstDigit == '4') {
            cardType = "VISA";
            tvCardType.setText("VISA");
            setCardTypeIcon(R.drawable.ic_visa); // VISA 아이콘 필요
        } else if (firstDigit == '5' || (cardNumber.length() >= 2 && cardNumber.startsWith("22"))) {
            cardType = "MasterCard";
            tvCardType.setText("MasterCard");
            setCardTypeIcon(R.drawable.ic_mastercard); // MasterCard 아이콘 필요
        } else if (cardNumber.length() >= 2 && (cardNumber.startsWith("34") || cardNumber.startsWith("37"))) {
            cardType = "American Express";
            tvCardType.setText("AMEX");
            setCardTypeIcon(R.drawable.ic_amex); // AMEX 아이콘 필요
        } else {
            cardType = "기타";
            tvCardType.setText("카드");
            setCardTypeIcon(R.drawable.ic_card_default); // 기본 카드 아이콘
        }

        Log.d(TAG, "카드 타입 감지: " + cardType);
    }

    private void setCardTypeIcon(int resourceId) {
        if (ivCardType != null && resourceId != 0) {
            try {
                ivCardType.setImageResource(resourceId);
                ivCardType.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                Log.w(TAG, "카드 아이콘 설정 실패: " + e.getMessage());
                ivCardType.setVisibility(View.GONE);
            }
        } else if (ivCardType != null) {
            ivCardType.setVisibility(View.GONE);
        }
    }

    private void updateCardPreview() {
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
    }

    private void validateForm() {
        boolean isValid = true;

        // 카드 번호 검증 (16자리)
        if (cardNumber.length() != 16) {
            isValid = false;
        }

        // 만료일 검증 (4자리)
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
        btnRegisterCard.setEnabled(isValid);

        Log.d(TAG, "폼 검증 결과: " + isValid);
    }

    private void setupButtons() {
        // 뒤로가기 버튼
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        // 취소 버튼
        btnCancel.setOnClickListener(v -> {
            showCancelDialog();
        });

        // 카드 등록 버튼
        btnRegisterCard.setOnClickListener(v -> {
            registerCard();
        });
    }

    private void showCancelDialog() {
        new AlertDialog.Builder(this)
                .setTitle("카드 등록 취소")
                .setMessage("카드 등록을 취소하시겠습니까?\n입력한 정보가 모두 삭제됩니다.")
                .setPositiveButton("취소", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .setNegativeButton("계속 작성", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void registerCard() {
        Log.d(TAG, "카드 등록 시작");

        // 로딩 상태로 변경
        btnRegisterCard.setEnabled(false);
        btnRegisterCard.setText("등록 중...");

        // 실제로는 서버에 카드 정보를 전송해야 하지만, 여기서는 시뮬레이션
        new Thread(() -> {
            try {
                // 네트워크 요청 시뮬레이션
                Thread.sleep(2000);

                runOnUiThread(() -> {
                    // 성공 처리
                    showSuccessDialog();
                });

            } catch (InterruptedException e) {
                runOnUiThread(() -> {
                    // 실패 처리
                    btnRegisterCard.setEnabled(true);
                    btnRegisterCard.setText("카드 등록");
                    Toast.makeText(this, "카드 등록에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("카드 등록 완료")
                .setMessage("카드가 성공적으로 등록되었습니다.\n이제 차량 대여 시 결제할 수 있습니다.")
                .setPositiveButton("확인", (dialog, which) -> {
                    dialog.dismiss();

                    // 결과를 PaymentLicenseActivity로 전달
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("card_registered", true);
                    resultIntent.putExtra("card_type", cardType);
                    resultIntent.putExtra("card_last_four", cardNumber.substring(cardNumber.length() - 4));
                    resultIntent.putExtra("cardholder_name", cardholderName);

                    // 만료일 포맷팅 (MMYY -> MM/YY)
                    String formattedExpiry = expiryDate.length() >= 4 ?
                            expiryDate.substring(0, 2) + "/" + expiryDate.substring(2, 4) : expiryDate;
                    resultIntent.putExtra("expiry_date", formattedExpiry);

                    setResult(RESULT_OK, resultIntent);

                    finish();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showCancelDialog();
    }
}