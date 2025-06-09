// signupActivity.java

package kr.ac.mjc.ssacar.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import kr.ac.mjc.ssacar.R;

public class signupActivity extends AppCompatActivity {

    private EditText nameEditText, phoneEditText, emailEditText,
            idEditText, passwordEditText, passwordCheckEditText, inviteCodeEditText;
    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        nameEditText = findViewById(R.id.editTextName);
        phoneEditText = findViewById(R.id.editTextPhone);
        emailEditText = findViewById(R.id.editTextEmail);
        idEditText = findViewById(R.id.editTextId);
        passwordEditText = findViewById(R.id.editTextPassword);
        passwordCheckEditText = findViewById(R.id.editTextPasswordCheck);
        inviteCodeEditText = findViewById(R.id.editTextInviteCode);

        signUpButton = findViewById(R.id.buttonSignUp);

        signUpButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String id = idEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String passwordCheck = passwordCheckEditText.getText().toString().trim();
            String inviteCode = inviteCodeEditText.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || id.isEmpty() || password.isEmpty() || passwordCheck.isEmpty()) {
                Toast.makeText(this, "모든 필수 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(passwordCheck)) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 사용자 정보 저장
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("user_email", email);
            editor.putString("user_id", id); // 아이디도 저장
            editor.putString("user_password", password);
            editor.apply();

            Toast.makeText(this, "회원가입이 완료되었습니다!", Toast.LENGTH_SHORT).show();

            // 로그인 화면으로 이동
            Intent intent = new Intent(signupActivity.this, LicenseRegistrationActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
