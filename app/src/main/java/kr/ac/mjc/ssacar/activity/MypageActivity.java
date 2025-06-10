package kr.ac.mjc.ssacar.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import kr.ac.mjc.ssacar.R;

public class MypageActivity extends AppCompatActivity {

    private TextView textViewId, textViewName, textViewPhone, textViewLicenseStatus;
    private Button buttonLicenseRegister, buttonHome, buttonLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        // UI 요소 초기화
        textViewId = findViewById(R.id.textViewId);
        textViewName = findViewById(R.id.textViewName);
        textViewPhone = findViewById(R.id.textViewPhone);
        textViewLicenseStatus = findViewById(R.id.textViewLicenseStatus);
        buttonLicenseRegister = findViewById(R.id.buttonLicenseRegister);
        buttonHome = findViewById(R.id.buttonHome);
        buttonLogout = findViewById(R.id.buttonLogout);

        // 사용자 정보 로드
        loadUserInfo();

        // 면허 등록 버튼 클릭 리스너
        buttonLicenseRegister.setOnClickListener(v -> {
            SharedPreferences currentUserPrefs = getSharedPreferences("current_user", MODE_PRIVATE);
            String currentUserId = currentUserPrefs.getString("current_user_id", null);

            if (currentUserId != null) {
                SharedPreferences userDataPrefs = getSharedPreferences("user_data", MODE_PRIVATE);
                String userDataJson = userDataPrefs.getString(currentUserId, null);

                if (userDataJson != null) {
                    try {
                        JSONObject userInfo = new JSONObject(userDataJson);
                        boolean hasLicense = userInfo.optBoolean("hasLicense", false);

                        Intent intent;
                        if (hasLicense) {
                            // 면허 등록이 되어 있다면 면허 목록 보기로 이동
                            intent = new Intent(MypageActivity.this, LicenseListActivity.class);
                        } else {
                            // 등록되어 있지 않으면 등록 화면으로 이동
                            intent = new Intent(MypageActivity.this, LicenseRegistrationActivity.class);
                        }

                        startActivity(intent);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        // 홈으로 가기 버튼 클릭 리스너
        buttonHome.setOnClickListener(v -> {
            Intent intent = new Intent(MypageActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // 로그아웃 버튼 클릭 리스너 (정상 구현)
        buttonLogout.setOnClickListener(v -> {
            // 현재 사용자 정보 삭제
            SharedPreferences prefs = getSharedPreferences("current_user", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();  // current_user_id 제거
            editor.apply();

            // 로그인 화면으로 이동
            Intent intent = new Intent(MypageActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // 마이페이지 종료
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        // 화면이 다시 표시될 때마다 사용자 정보 새로고침
        loadUserInfo();
    }

    private void loadUserInfo() {
        // 현재 로그인한 사용자 ID 가져오기
        SharedPreferences currentUserPrefs = getSharedPreferences("current_user", MODE_PRIVATE);
        String currentUserId = currentUserPrefs.getString("current_user_id", null);

        if (currentUserId != null) {
            // 사용자 정보 가져오기
            SharedPreferences userDataPrefs = getSharedPreferences("user_data", MODE_PRIVATE);
            String userDataJson = userDataPrefs.getString(currentUserId, null);

            if (userDataJson != null) {
                try {
                    JSONObject userInfo = new JSONObject(userDataJson);

                    // UI에 사용자 정보 표시
                    textViewId.setText("아이디: " + userInfo.getString("id"));
                    textViewName.setText("이름: " + userInfo.getString("name"));
                    textViewPhone.setText("전화번호: " + userInfo.getString("phone"));

                    // 면허 등록 여부 확인
                    boolean hasLicense = userInfo.optBoolean("hasLicense", false);
                    if (hasLicense) {
                        textViewLicenseStatus.setText("면허 등록: 완료");
                        buttonLicenseRegister.setText("면허 정보 보기");
                    } else {
                        textViewLicenseStatus.setText("면허 등록: 미완료");
                        buttonLicenseRegister.setText("면허 등록하기");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}