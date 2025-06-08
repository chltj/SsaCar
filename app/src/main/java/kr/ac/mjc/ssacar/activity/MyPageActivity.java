// MyPageActivity.java

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

        EditText emailEditText = findViewById(R.id.editTextEmail); // 이 입력창은 이메일/아이디 겸용
        EditText passwordEditText = findViewById(R.id.editTextPassword);
        Button loginButton = findViewById(R.id.buttonConfirm);
        Button signupButton = findViewById(R.id.buttonSignup);
        TextView errorTextView = findViewById(R.id.textViewError);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedEmail = prefs.getString("user_email", "");
        String savedId = prefs.getString("user_id", "");
        String savedPassword = prefs.getString("user_password", "");

        loginButton.setOnClickListener(v -> {
            String inputAccount = emailEditText.getText().toString().trim(); // 이메일 또는 아이디
            String inputPassword = passwordEditText.getText().toString().trim();

            if (inputAccount.isEmpty() || inputPassword.isEmpty()) {
                errorTextView.setText("아이디 또는 이메일과 비밀번호를 모두 입력해주세요.");
                errorTextView.setVisibility(View.VISIBLE);
                return;
            }

            boolean isAccountMatch = inputAccount.equals(savedEmail) || inputAccount.equals(savedId);

            if (isAccountMatch) {
                if (inputPassword.equals(savedPassword)) {
                    errorTextView.setVisibility(View.GONE);
                    Toast.makeText(MyPageActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.apply();

                    Intent intent = new Intent(MyPageActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    errorTextView.setText("비밀번호가 틀렸습니다.");
                    errorTextView.setVisibility(View.VISIBLE);
                }
            } else {
                errorTextView.setText("등록되지 않은 이메일 또는 아이디입니다.");
                errorTextView.setVisibility(View.VISIBLE);
            }
        });

        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyPageActivity.this, signupActivity.class);
            startActivity(intent);
        });
    }
}
