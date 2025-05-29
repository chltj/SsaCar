package kr.ac.mjc.ssacar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CarListActivity extends AppCompatActivity {

    private TextView locationInfoTv;
    private TextView timeInfoTv;
    private RecyclerView carListRv;
    private Button selectCompleteButton;
    private CarListAdapter carListAdapter;
    private List<CarInfo> carList;

    // 예약 정보
    private String placeName;
    private String departureTime;
    private String arrivalTime;
    private CarInfo selectedCar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.util.Log.d("CarListActivity", "CarListActivity onCreate 시작");

        try {
            setContentView(R.layout.activity_car_list);

            // UI 초기화
            locationInfoTv = findViewById(R.id.location_info_tv);
            timeInfoTv = findViewById(R.id.time_info_tv);
            carListRv = findViewById(R.id.car_list_rv);
            selectCompleteButton = findViewById(R.id.select_complete_button);

            if (selectCompleteButton == null) {
                android.util.Log.e("CarListActivity", "select_complete_button을 찾을 수 없습니다!");
                Toast.makeText(this, "버튼 초기화 실패", Toast.LENGTH_SHORT).show();
            }

            android.util.Log.d("CarListActivity", "레이아웃 파일 로드 성공");

        } catch (Exception e) {
            android.util.Log.e("CarListActivity", "레이아웃 파일 로드 실패", e);
            Toast.makeText(this, "레이아웃 로드 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Intent에서 데이터 받기
        Intent intent = getIntent();
        placeName = intent.getStringExtra("place_name");
        departureTime = intent.getStringExtra("departure_time");
        arrivalTime = intent.getStringExtra("arrival_time");

        android.util.Log.d("CarListActivity", "받은 데이터 - 장소: " + placeName);
        android.util.Log.d("CarListActivity", "받은 데이터 - 출발시간: " + departureTime);
        android.util.Log.d("CarListActivity", "받은 데이터 - 도착시간: " + arrivalTime);

        // 위치 및 시간 정보 표시
        locationInfoTv.setText("선택된 위치: " + (placeName != null ? placeName : "정보 없음"));

        if (departureTime != null && arrivalTime != null) {
            timeInfoTv.setText("출발: " + departureTime + "\n도착: " + arrivalTime);
        } else {
            timeInfoTv.setText("시간 정보가 없습니다.");
        }

        // 차량 리스트 초기화
        initCarList();

        // RecyclerView 설정
        setupRecyclerView();

        // 차량 선택 완료 버튼 설정
        setupSelectCompleteButton();

        Toast.makeText(this, "차량을 선택해주세요", Toast.LENGTH_SHORT).show();
        android.util.Log.d("CarListActivity", "CarListActivity onCreate 완료");
    }

    private void initCarList() {
        carList = new ArrayList<>();

        // 샘플 데이터
        carList.add(new CarInfo("현대 아반떼", "소형차", "15,000원/일", "⭐4.5", 0));
        carList.add(new CarInfo("기아 K5", "중형차", "25,000원/일", "⭐4.3", 0));
        carList.add(new CarInfo("현대 싼타페", "SUV", "35,000원/일", "⭐4.7", 0));
        carList.add(new CarInfo("BMW 320i", "세단", "45,000원/일", "⭐4.8", 0));
        carList.add(new CarInfo("벤츠 GLC", "SUV", "55,000원/일", "⭐4.6", 0));

        android.util.Log.d("CarListActivity", "차량 리스트 초기화 완료: " + carList.size() + "대");
    }

    private void setupRecyclerView() {
        try {
            carListAdapter = new CarListAdapter(carList, new CarListAdapter.OnCarClickListener() {
                @Override
                public void onCarClick(CarInfo carInfo) {
                    // 이전에 선택된 차량이 있다면 선택 해제 (시각적 효과를 위해)
                    if (selectedCar != null) {
                        android.util.Log.d("CarListActivity", "이전 선택 해제: " + selectedCar.getCarName());
                    }

                    // 새 차량 선택
                    selectedCar = carInfo;
                    Toast.makeText(CarListActivity.this, carInfo.getCarName() + " 선택됨!", Toast.LENGTH_SHORT).show();
                    android.util.Log.d("CarListActivity", "차량 선택: " + carInfo.getCarName());

                    // 선택 완료 버튼 활성화 및 텍스트 변경
                    if (selectCompleteButton != null) {
                        selectCompleteButton.setEnabled(true);
                        selectCompleteButton.setText("'" + carInfo.getCarName() + "' 선택 완료");
                        selectCompleteButton.setBackgroundColor(0xFF4CAF50); // 녹색으로 변경
                    }
                }
            });

            carListRv.setLayoutManager(new LinearLayoutManager(this));
            carListRv.setAdapter(carListAdapter);

            android.util.Log.d("CarListActivity", "RecyclerView 설정 완료");

        } catch (Exception e) {
            android.util.Log.e("CarListActivity", "RecyclerView 설정 실패", e);
            Toast.makeText(this, "차량 리스트 설정 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupSelectCompleteButton() {
        if (selectCompleteButton == null) {
            android.util.Log.e("CarListActivity", "selectCompleteButton이 null입니다!");
            return;
        }

        // 초기에는 버튼 비활성화
        selectCompleteButton.setEnabled(false);
        selectCompleteButton.setText("차량을 선택해주세요");
        selectCompleteButton.setBackgroundColor(0xFFCCCCCC); // 회색으로 설정

        selectCompleteButton.setOnClickListener(v -> {
            if (selectedCar != null) {
                android.util.Log.d("CarListActivity", "선택 완료 버튼 클릭: " + selectedCar.getCarName());

                // PaymentActivity로 이동
                Intent paymentIntent = new Intent(CarListActivity.this, PaymentActivity.class);
                paymentIntent.putExtra("place_name", placeName);
                paymentIntent.putExtra("departure_time", departureTime);
                paymentIntent.putExtra("arrival_time", arrivalTime);
                paymentIntent.putExtra("selected_car", selectedCar);

                Toast.makeText(CarListActivity.this, selectedCar.getCarName() + " 결제 화면으로 이동합니다.", Toast.LENGTH_SHORT).show();

                startActivity(paymentIntent);

                android.util.Log.d("CarListActivity", "PaymentActivity로 이동: " + selectedCar.getCarName());
            } else {
                Toast.makeText(CarListActivity.this, "차량을 먼저 선택해주세요.", Toast.LENGTH_SHORT).show();
                android.util.Log.w("CarListActivity", "차량이 선택되지 않았는데 버튼이 활성화됨");
            }
        });

        android.util.Log.d("CarListActivity", "선택 완료 버튼 설정 완료");
    }
}