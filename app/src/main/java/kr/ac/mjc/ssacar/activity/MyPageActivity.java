package kr.ac.mjc.ssacar.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import kr.ac.mjc.ssacar.R;

public class MyPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        EditText emailEditText = findViewById(R.id.editTextEmail);
        EditText passwordEditText = findViewById(R.id.editTextPassword);
        Button loginButton = findViewById(R.id.buttonConfirm);
        Button signupButton = findViewById(R.id.buttonSignup);
        TextView errorTextView = findViewById(R.id.textViewError);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedEmail = prefs.getString("user_email", "");
        String savedPassword = prefs.getString("user_password", "");

        loginButton.setOnClickListener(v -> {
            String inputEmail = emailEditText.getText().toString().trim();
            String inputPassword = passwordEditText.getText().toString().trim();

            if (inputEmail.isEmpty() || inputPassword.isEmpty()) {
                errorTextView.setText("이메일과 비밀번호를 모두 입력해주세요.");
                errorTextView.setVisibility(View.VISIBLE);
                return;
            }

            if (inputEmail.equals(savedEmail)) {
                if (inputPassword.equals(savedPassword)) {
                    errorTextView.setVisibility(View.GONE);
                    Toast.makeText(MyPageActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();

                    // ✅ 로그인 상태 저장
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.apply();

                    // ✅ 메인화면으로 이동
                    Intent intent = new Intent(MyPageActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    errorTextView.setText("비밀번호가 틀렸습니다.");
                    errorTextView.setVisibility(View.VISIBLE);
                }
            } else {
                errorTextView.setText("등록되지 않은 이메일입니다.");
                errorTextView.setVisibility(View.VISIBLE);
            }
        });

        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyPageActivity.this, signupActivity.class);
            startActivity(intent);
        });
    }
}
