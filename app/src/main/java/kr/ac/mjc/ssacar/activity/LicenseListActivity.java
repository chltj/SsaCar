package kr.ac.mjc.ssacar.activity;

import android.os.Bundle;
import android.widget.TextView;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import kr.ac.mjc.ssacar.R;

public class LicenseListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license_list);

        TextView textView = findViewById(R.id.text_license_info);
        SharedPreferences prefs = getSharedPreferences("licenses", MODE_PRIVATE);
        String jsonData = prefs.getString("license_list", "[]");

        StringBuilder infoBuilder = new StringBuilder();

        try {
            JSONArray array = new JSONArray(jsonData);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String licenseNumber = obj.optString("licenseNumber", "N/A");
                String issueDate = obj.optString("issueDate", "N/A");
                String licenseType = obj.optString("licenseType", "N/A");
                String issuedBy = obj.optString("issuedBy", "N/A");
                String expirationDate = obj.optString("expirationDate", "N/A");

                infoBuilder.append("면허번호: ").append(licenseNumber).append("\n")
                        .append("발급일자: ").append(issueDate).append("\n")
                        .append("면허종류: ").append(licenseType).append("\n")
                        .append("발급기관: ").append(issuedBy).append("\n")
                        .append("유효기간: ").append(expirationDate).append("\n\n");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        textView.setText(infoBuilder.toString().isEmpty() ? "등록된 면허가 없습니다." : infoBuilder.toString());
    }
}
