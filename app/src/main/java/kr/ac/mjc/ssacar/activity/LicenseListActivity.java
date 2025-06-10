package kr.ac.mjc.ssacar.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import kr.ac.mjc.ssacar.R;

public class LicenseListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license_list);

        TextView textView = findViewById(R.id.text_license_info);

        // 현재 로그인한 사용자 ID 불러오기
        SharedPreferences currentUserPrefs = getSharedPreferences("current_user", MODE_PRIVATE);
        String currentUserId = currentUserPrefs.getString("current_user_id", null);

        if (currentUserId != null) {
            // 사용자별로 저장된 면허 정보 불러오기
            SharedPreferences licensePrefs = getSharedPreferences("licenses", MODE_PRIVATE);
            String licenseJson = licensePrefs.getString(currentUserId, null);

            if (licenseJson != null) {
                try {
                    JSONObject obj = new JSONObject(licenseJson);

                    String licenseNumber = obj.optString("licenseNumber", "N/A");
                    String issueDate = obj.optString("issueDate", "N/A");
                    String licenseType = obj.optString("licenseType", "N/A");
                    String issuedBy = obj.optString("issuedBy", "N/A");
                    String expirationDate = obj.optString("expirationDate", "N/A");

                    String info = "면허번호: " + licenseNumber + "\n"
                            + "발급일자: " + issueDate + "\n"
                            + "면허종류: " + licenseType + "\n"
                            + "발급기관: " + issuedBy + "\n"
                            + "유효기간: " + expirationDate;

                    textView.setText(info);
                } catch (JSONException e) {
                    e.printStackTrace();
                    textView.setText("면허 정보 파싱 중 오류가 발생했습니다.");
                }
            } else {
                textView.setText("등록된 면허가 없습니다.");
            }
        } else {
            textView.setText("로그인 정보가 없습니다.");
        }

        // 홈으로 가기 버튼
        Button homeButton = findViewById(R.id.button_home);
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(LicenseListActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
