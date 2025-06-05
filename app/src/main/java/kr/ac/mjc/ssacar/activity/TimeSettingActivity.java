package kr.ac.mjc.ssacar.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kr.ac.mjc.ssacar.R;

public class TimeSettingActivity extends AppCompatActivity {

    private TextView locationInfoTv;
    private Spinner departureTimeSpinner;
    private Spinner arrivalTimeSpinner;
    private Button confirmButton;

    private String placeName;
    private String address;
    private double latitude;
    private double longitude;
    private String source; // 출처 구분용

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_setting);

        // UI 초기화
        locationInfoTv = findViewById(R.id.location_info_tv);
        departureTimeSpinner = findViewById(R.id.departure_time_spinner);
        arrivalTimeSpinner = findViewById(R.id.arrival_time_spinner);
        confirmButton = findViewById(R.id.confirm_button);

        // Intent에서 위치 정보 및 출처 받기
        Intent intent = getIntent();
        placeName = intent.getStringExtra("place_name");
        address = intent.getStringExtra("address");
        latitude = intent.getDoubleExtra("latitude", 0.0);
        longitude = intent.getDoubleExtra("longitude", 0.0);
        source = intent.getStringExtra("source"); // 추가: callhere / pickup

        // 위치 정보 표시
        locationInfoTv.setText(placeName + "\n" + (address != null ? address : ""));

        // 시간 스피너 설정
        setupTimeSpinners();

        // 확인 버튼 클릭 리스너
        confirmButton.setOnClickListener(v -> {
            String departureTime = departureTimeSpinner.getSelectedItem().toString();
            String arrivalTime = arrivalTimeSpinner.getSelectedItem().toString();

            android.util.Log.d("TimeSettingActivity", "출발시간: " + departureTime);
            android.util.Log.d("TimeSettingActivity", "도착시간: " + arrivalTime);

            try {
                Intent nextIntent;

                if ("callhere".equals(source)) {
                    nextIntent = new Intent(TimeSettingActivity.this, CallReturnActivity.class);
                    nextIntent.putExtra("address", address);
                    nextIntent.putExtra("latitude", latitude);
                    nextIntent.putExtra("longitude", longitude);

                } else {
                    nextIntent = new Intent(TimeSettingActivity.this, VehicleListActivity.class);
                }

                nextIntent.putExtra("place_name", placeName);
                nextIntent.putExtra("departure_time", departureTime);
                nextIntent.putExtra("arrival_time", arrivalTime);

                startActivity(nextIntent);

            } catch (Exception e) {
                android.util.Log.e("TimeSettingActivity", "Intent 처리 실패", e);
                Toast.makeText(TimeSettingActivity.this, "오류 발생: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupTimeSpinners() {
        List<String> timeList = generateTimeList();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                timeList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        departureTimeSpinner.setAdapter(adapter);
        arrivalTimeSpinner.setAdapter(adapter);

        if (timeList.size() > 2) {
            departureTimeSpinner.setSelection(1);
            arrivalTimeSpinner.setSelection(3);
        }
    }

    private List<String> generateTimeList() {
        List<String> timeList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM월 dd일 HH:mm", Locale.KOREA);

        for (int i = 0; i < 48; i++) {
            timeList.add(sdf.format(calendar.getTime()));
            calendar.add(Calendar.HOUR_OF_DAY, 1);
        }

        return timeList;
    }

    private boolean isValidTimeSelection(String departureTime, String arrivalTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM월 dd일 HH:mm", Locale.KOREA);
            Date departure = sdf.parse(departureTime);
            Date arrival = sdf.parse(arrivalTime);

            return arrival.after(departure);
        } catch (Exception e) {
            return false;
        }
    }
}
