package kr.ac.mjc.ssacar.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import kr.ac.mjc.ssacar.R;

public class signupActivity extends AppCompatActivity {

    private EditText editTextName, editTextPhone, editTextEmail, editTextId,
            editTextPassword, editTextPasswordCheck, editTextInviteCode;
    private Button buttonSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // UI 요소 초기화
        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextId = findViewById(R.id.editTextId);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPasswordCheck = findViewById(R.id.editTextPasswordCheck);

        buttonSignUp = findViewById(R.id.buttonSignUp);

        // 회원가입 버튼 클릭 리스너
        buttonSignUp.setOnClickListener(v -> {
            if (validateInput()) {
                saveUserData();
                Toast.makeText(this, "회원가입이 완료되었습니다. 면허를 등록해주세요.", Toast.LENGTH_SHORT).show();

                // 현재 가입한 사용자를 임시로 저장 (면허 등록용)
                String id = editTextId.getText().toString().trim();
                saveCurrentUser(id);

                // 면허 등록 화면으로 이동
                Intent intent = new Intent(signupActivity.this, LicenseRegistrationActivity.class);
                intent.putExtra("from_signup", true); // 회원가입에서 온 것임을 표시
                startActivity(intent);
                finish();
            }
        });
        editTextPhone.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            private int previousLength;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previousLength = s.length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                isFormatting = true;

                String raw = s.toString().replaceAll("[^0-9]", "");  // 숫자만 추출
                String formatted = "";

                if (raw.length() >= 10) {
                    if (raw.startsWith("02")) {
                        // 서울 지역번호 (02-XXXX-XXXX)
                        formatted = raw.replaceFirst("(\\d{2})(\\d{4})(\\d+)", "$1-$2-$3");
                    } else {
                        // 일반 휴대폰 (010-XXXX-XXXX 등)
                        formatted = raw.replaceFirst("(\\d{3})(\\d{3,4})(\\d+)", "$1-$2-$3");
                    }
                } else if (raw.length() >= 7) {
                    formatted = raw.replaceFirst("(\\d{3})(\\d+)", "$1-$2");
                } else {
                    formatted = raw;
                }

                editTextPhone.setText(formatted);
                editTextPhone.setSelection(formatted.length()); // 커서 이동

                isFormatting = false;
            }
        });

    }

    private boolean validateInput() {
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String id = editTextId.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String passwordCheck = editTextPasswordCheck.getText().toString().trim();

        if (name.isEmpty()) {
            editTextName.setError("이름을 입력해주세요.");
            return false;
        }

        if (phone.isEmpty()) {
            editTextPhone.setError("전화번호를 입력해주세요.");
            return false;
        }

        if (email.isEmpty()) {
            editTextEmail.setError("이메일을 입력해주세요.");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("올바른 이메일 형식이 아닙니다.");
            return false;
        }

        if (id.isEmpty()) {
            editTextId.setError("아이디를 입력해주세요.");
            return false;
        }

        if (password.isEmpty() || password.length() < 8) {
            editTextPassword.setError("비밀번호는 8자 이상 입력해주세요.");
            return false;
        }

        if (!password.equals(passwordCheck)) {
            editTextPasswordCheck.setError("비밀번호가 일치하지 않습니다.");
            return false;
        }

        return true;
    }

    private void saveUserData() {
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String id = editTextId.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String inviteCode = editTextInviteCode.getText().toString().trim();

        // 사용자 정보를 JSON으로 저장
        JSONObject userInfo = new JSONObject();
        try {
            userInfo.put("name", name);
            userInfo.put("phone", phone);
            userInfo.put("email", email);
            userInfo.put("id", id);
            userInfo.put("password", password);
            userInfo.put("inviteCode", inviteCode);
            userInfo.put("hasLicense", false); // 면허 등록 여부 초기값
            userInfo.put("licenseRequired", true); // 면허 등록 필수 표시
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // SharedPreferences에 저장
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(id, userInfo.toString()); // 아이디를 키로 사용
        editor.apply();
    }

    private void saveCurrentUser(String id) {
        SharedPreferences prefs = getSharedPreferences("current_user", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("current_user_id", id);
        editor.apply();
    }
}