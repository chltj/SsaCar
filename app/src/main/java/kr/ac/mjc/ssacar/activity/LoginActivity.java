package kr.ac.mjc.ssacar.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import kr.ac.mjc.ssacar.R;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonConfirm, buttonSignup;
    private TextView textViewError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // UI 요소 초기화
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonConfirm = findViewById(R.id.buttonConfirm);
        buttonSignup = findViewById(R.id.buttonSignup);
        textViewError = findViewById(R.id.textViewError);

        // 로그인 버튼 클릭 리스너
        buttonConfirm.setOnClickListener(v -> {
            String id = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (validateLogin(id, password)) {
                // 로그인 성공 시 현재 사용자 정보 저장
                saveCurrentUser(id);

                // 마이페이지로 이동
                Intent intent = new Intent(LoginActivity.this, MypageActivity.class);
                startActivity(intent);
                finish();
            } else {
                // 로그인 실패
                textViewError.setText("아이디 또는 비밀번호가 잘못되었습니다.");
                textViewError.setVisibility(TextView.VISIBLE);
            }
        });

        // 회원가입 버튼 클릭 리스너
        buttonSignup.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, signupActivity.class);
            startActivity(intent);
        });
    }

    private boolean validateLogin(String id, String password) {
        if (id.isEmpty() || password.isEmpty()) {
            textViewError.setText("아이디와 비밀번호를 입력해주세요.");
            textViewError.setVisibility(TextView.VISIBLE);
            return false;
        }

        // SharedPreferences에서 사용자 정보 확인
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        String userDataJson = prefs.getString(id, null);

        if (userDataJson != null) {
            try {
                JSONObject userInfo = new JSONObject(userDataJson);
                String savedPassword = userInfo.getString("password");
                boolean hasLicense = userInfo.optBoolean("hasLicense", false);

                if (!password.equals(savedPassword)) {
                    return false;
                }

                // 면허 등록 여부 확인
                if (!hasLicense) {
                    textViewError.setText("면허 등록을 완료해주세요.");
                    textViewError.setVisibility(TextView.VISIBLE);
                    return false;
                }

                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    private void saveCurrentUser(String id) {
        SharedPreferences prefs = getSharedPreferences("current_user", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("current_user_id", id);
        editor.apply();
    }
}