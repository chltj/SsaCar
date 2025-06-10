package kr.ac.mjc.ssacar.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import kr.ac.mjc.ssacar.R;

public class LicenseRegistrationActivity extends AppCompatActivity {

    private EditText editTextLicenseNumber, editTextIssueDate, editTextLicenseType,
            editTextIssuedBy, editTextExpirationDate;
    private Button buttonRegister;
    private boolean fromSignup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license_registration);

        // Intent에서 회원가입에서 온 것인지 확인
        fromSignup = getIntent().getBooleanExtra("from_signup", false);

        // UI 요소 초기화
        editTextLicenseNumber = findViewById(R.id.editTextLicenseNumber);
        editTextIssueDate = findViewById(R.id.editTextIssueDate);
        editTextLicenseType = findViewById(R.id.editTextLicenseType);
        editTextIssuedBy = findViewById(R.id.editTextIssuedBy);
        editTextExpirationDate = findViewById(R.id.editTextExpirationDate);
        buttonRegister = findViewById(R.id.buttonRegister);

        // 면허 등록 버튼 클릭 리스너
        buttonRegister.setOnClickListener(v -> {
            if (validateInput()) {
                saveLicenseInfo();
                updateUserLicenseStatus();

                Toast.makeText(this, "면허 등록이 완료되었습니다.", Toast.LENGTH_SHORT).show();

                if (fromSignup) {
                    // 회원가입에서 온 경우 로그인 화면으로 이동
                    Intent intent = new Intent(LicenseRegistrationActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // 마이페이지에서 온 경우 면허 목록 화면으로 이동
                    Intent intent = new Intent(LicenseRegistrationActivity.this, LicenseListActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private boolean validateInput() {
        String licenseNumber = editTextLicenseNumber.getText().toString().trim();
        String issueDate = editTextIssueDate.getText().toString().trim();
        String licenseType = editTextLicenseType.getText().toString().trim();
        String issuedBy = editTextIssuedBy.getText().toString().trim();
        String expirationDate = editTextExpirationDate.getText().toString().trim();

        if (licenseNumber.isEmpty()) {
            editTextLicenseNumber.setError("면허번호를 입력해주세요.");
            return false;
        }

        if (issueDate.isEmpty()) {
            editTextIssueDate.setError("발급일자를 입력해주세요.");
            return false;
        }

        if (licenseType.isEmpty()) {
            editTextLicenseType.setError("면허종류를 입력해주세요.");
            return false;
        }

        if (issuedBy.isEmpty()) {
            editTextIssuedBy.setError("발급기관을 입력해주세요.");
            return false;
        }

        if (expirationDate.isEmpty()) {
            editTextExpirationDate.setError("유효기간을 입력해주세요.");
            return false;
        }

        return true;
    }

    private void saveLicenseInfo() {
        String licenseNumber = editTextLicenseNumber.getText().toString().trim();
        String issueDate = editTextIssueDate.getText().toString().trim();
        String licenseType = editTextLicenseType.getText().toString().trim();
        String issuedBy = editTextIssuedBy.getText().toString().trim();
        String expirationDate = editTextExpirationDate.getText().toString().trim();

        JSONObject licenseInfo = new JSONObject();
        try {
            licenseInfo.put("licenseNumber", licenseNumber);
            licenseInfo.put("issueDate", issueDate);
            licenseInfo.put("licenseType", licenseType);
            licenseInfo.put("issuedBy", issuedBy);
            licenseInfo.put("expirationDate", expirationDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 현재 로그인한 사용자 ID 가져오기
        SharedPreferences currentUserPrefs = getSharedPreferences("current_user", MODE_PRIVATE);
        String currentUserId = currentUserPrefs.getString("current_user_id", null);

        if (currentUserId != null) {
            SharedPreferences licensePrefs = getSharedPreferences("licenses", MODE_PRIVATE);
            SharedPreferences.Editor editor = licensePrefs.edit();
            editor.putString(currentUserId, licenseInfo.toString()); // 사용자 ID로 저장
            editor.apply();
        }
    }


    private void updateUserLicenseStatus() {
        // 현재 로그인한 사용자의 면허 등록 상태 업데이트
        SharedPreferences currentUserPrefs = getSharedPreferences("current_user", MODE_PRIVATE);
        String currentUserId = currentUserPrefs.getString("current_user_id", null);

        if (currentUserId != null) {
            SharedPreferences userDataPrefs = getSharedPreferences("user_data", MODE_PRIVATE);
            String userDataJson = userDataPrefs.getString(currentUserId, null);

            if (userDataJson != null) {
                try {
                    JSONObject userInfo = new JSONObject(userDataJson);
                    userInfo.put("hasLicense", true); // 면허 등록 완료로 업데이트

                    // 업데이트된 사용자 정보 저장
                    SharedPreferences.Editor editor = userDataPrefs.edit();
                    editor.putString(currentUserId, userInfo.toString());
                    editor.apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}