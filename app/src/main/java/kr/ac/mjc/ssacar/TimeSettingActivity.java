package kr.ac.mjc.ssacar;

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

public class TimeSettingActivity extends AppCompatActivity {

    private TextView locationInfoTv;
    private Spinner departureTimeSpinner;
    private Spinner arrivalTimeSpinner;
    private Button confirmButton;

    private String placeName;
    private String address;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_setting);

        // UI 초기화
        locationInfoTv = findViewById(R.id.location_info_tv);
        departureTimeSpinner = findViewById(R.id.departure_time_spinner);
        arrivalTimeSpinner = findViewById(R.id.arrival_time_spinner);
        confirmButton = findViewById(R.id.confirm_button);

        // Intent에서 위치 정보 받기
        Intent intent = getIntent();
        placeName = intent.getStringExtra("place_name");
        address = intent.getStringExtra("address");
        latitude = intent.getDoubleExtra("latitude", 0.0);
        longitude = intent.getDoubleExtra("longitude", 0.0);

        // 위치 정보 표시
        locationInfoTv.setText(placeName + "\n" + (address != null ? address : ""));

        // 시간 스피너 설정
        setupTimeSpinners();

        // 확인 버튼 클릭 리스너
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.util.Log.d("TimeSettingActivity", "확인 버튼 클릭됨");

                String departureTime = departureTimeSpinner.getSelectedItem().toString();
                String arrivalTime = arrivalTimeSpinner.getSelectedItem().toString();

                android.util.Log.d("TimeSettingActivity", "출발시간: " + departureTime);
                android.util.Log.d("TimeSettingActivity", "도착시간: " + arrivalTime);

                // 시간 유효성 검사를 일단 생략하고 바로 이동
                android.util.Log.d("TimeSettingActivity", "CarListActivity로 이동 시작");

                try {
                    Intent carListIntent = new Intent(TimeSettingActivity.this, CarListActivity.class);
                    carListIntent.putExtra("place_name", placeName);
                    carListIntent.putExtra("address", address);
                    carListIntent.putExtra("latitude", latitude);
                    carListIntent.putExtra("longitude", longitude);
                    carListIntent.putExtra("departure_time", departureTime);
                    carListIntent.putExtra("arrival_time", arrivalTime);

                    startActivity(carListIntent);
                    android.util.Log.d("TimeSettingActivity", "startActivity 호출 완료");

                } catch (Exception e) {
                    android.util.Log.e("TimeSettingActivity", "Intent 생성 또는 startActivity 실패", e);
                    Toast.makeText(TimeSettingActivity.this, "오류 발생: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setupTimeSpinners() {
        // 시간 리스트 생성 (현재 시간부터 24시간 후까지)
        List<String> timeList = generateTimeList();

        // 어댑터 생성
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                timeList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 스피너에 어댑터 설정
        departureTimeSpinner.setAdapter(adapter);
        arrivalTimeSpinner.setAdapter(adapter);

        // 기본값 설정 (출발: 현재시간+1시간, 도착: 현재시간+3시간)
        if (timeList.size() > 2) {
            departureTimeSpinner.setSelection(1);
            arrivalTimeSpinner.setSelection(3);
        }
    }

    private List<String> generateTimeList() {
        List<String> timeList = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM월 dd일 HH:mm", Locale.KOREA);

        // 현재 시간부터 48시간 후까지 1시간 간격으로 생성
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