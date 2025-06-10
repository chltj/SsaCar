package kr.ac.mjc.ssacar.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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

    private List<String> timeList;

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

        // 출발시간 변경 리스너 추가
        departureTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateArrivalTimeOptions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 확인 버튼 클릭 리스너
        confirmButton.setOnClickListener(v -> {
            String departureTime = departureTimeSpinner.getSelectedItem().toString();
            String arrivalTime = arrivalTimeSpinner.getSelectedItem().toString();

            android.util.Log.d("TimeSettingActivity", "출발시간: " + departureTime);
            android.util.Log.d("TimeSettingActivity", "도착시간: " + arrivalTime);

            // 시간 유효성 검사
            if (!isValidTimeSelection(departureTime, arrivalTime)) {
                Toast.makeText(TimeSettingActivity.this, "도착시간은 출발시간보다 늦어야 합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

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
        timeList = generateTimeList();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                timeList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        departureTimeSpinner.setAdapter(adapter);
        arrivalTimeSpinner.setAdapter(adapter);

        // 기본 선택값 설정
        if (timeList.size() > 1) {
            departureTimeSpinner.setSelection(0);
            arrivalTimeSpinner.setSelection(1);
        }
    }

    private List<String> generateTimeList() {
        List<String> timeList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM월 dd일 HH:mm", Locale.KOREA);

        // 현재 시간을 오늘 오전 9시로 설정
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // 만약 현재 시간이 오전 9시 이전이라면 오늘부터, 아니면 내일부터 시작
        Calendar now = Calendar.getInstance();
        if (now.get(Calendar.HOUR_OF_DAY) >= 12) { // 오후 12시 이후라면 내일부터
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // 일주일간 생성 (7일 * 3시간 = 21개 시간대)
        for (int day = 0; day < 7; day++) {
            for (int hour = 9; hour <= 11; hour++) { // 오전 9시부터 오전 11시까지
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                timeList.add(sdf.format(calendar.getTime()));
            }
            // 오후 12시 (정오) 추가
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            timeList.add(sdf.format(calendar.getTime()));

            calendar.add(Calendar.DAY_OF_YEAR, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 9); // 다음 날 9시로 리셋
        }

        return timeList;
    }

    private void updateArrivalTimeOptions() {
        int selectedDepartureIndex = departureTimeSpinner.getSelectedItemPosition();

        // 도착시간은 출발시간 이후만 선택 가능하도록 필터링
        List<String> filteredArrivalTimes = new ArrayList<>();
        for (int i = selectedDepartureIndex + 1; i < timeList.size(); i++) {
            filteredArrivalTimes.add(timeList.get(i));
        }

        if (filteredArrivalTimes.isEmpty()) {
            // 선택 가능한 도착시간이 없는 경우
            filteredArrivalTimes.add("선택 가능한 도착시간이 없습니다");
        }

        ArrayAdapter<String> arrivalAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                filteredArrivalTimes
        );
        arrivalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        arrivalTimeSpinner.setAdapter(arrivalAdapter);

        // 첫 번째 항목 선택
        if (filteredArrivalTimes.size() > 0 && !filteredArrivalTimes.get(0).contains("선택 가능한")) {
            arrivalTimeSpinner.setSelection(0);
        }
    }

    private boolean isValidTimeSelection(String departureTime, String arrivalTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM월 dd일 HH:mm", Locale.KOREA);
            Date departure = sdf.parse(departureTime);
            Date arrival = sdf.parse(arrivalTime);

            return arrival != null && departure != null && arrival.after(departure);
        } catch (Exception e) {
            android.util.Log.e("TimeSettingActivity", "시간 유효성 검사 실패", e);
            return false;
        }
    }
}