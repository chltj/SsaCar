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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license_registration);

        Button submitButton = findViewById(R.id.btn_submit);
        submitButton.setOnClickListener(v -> submitLicense());
    }

    private void submitLicense() {
        EditText licenseEdit = findViewById(R.id.edit_license_number);
        EditText issueDateEdit = findViewById(R.id.edit_issue_date);
        EditText licenseTypeEdit = findViewById(R.id.edit_license_type);
        EditText issuedByEdit = findViewById(R.id.edit_issued_by);
        EditText expirationDateEdit = findViewById(R.id.edit_expiration_date);

        String licenseNumber = licenseEdit.getText().toString().trim();
        String issueDate = issueDateEdit.getText().toString().trim();
        String licenseType = licenseTypeEdit.getText().toString().trim();
        String issuedBy = issuedByEdit.getText().toString().trim();
        String expirationDate = expirationDateEdit.getText().toString().trim();

        if (licenseNumber.isEmpty() || issueDate.isEmpty() || licenseType.isEmpty() ||
                issuedBy.isEmpty() || expirationDate.isEmpty()) {
            Toast.makeText(this, "모든 정보를 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("licenses", MODE_PRIVATE);
        String existingData = prefs.getString("license_list", "[]");

        try {
            JSONArray array = new JSONArray(existingData);
            JSONObject obj = new JSONObject();
            obj.put("licenseNumber", licenseNumber);
            obj.put("issueDate", issueDate);
            obj.put("licenseType", licenseType);
            obj.put("issuedBy", issuedBy);
            obj.put("expirationDate", expirationDate);
            array.put(obj);

            prefs.edit().putString("license_list", array.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, LicenseListActivity.class);
        startActivity(intent);
        finish();
    }
}
