package kr.ac.mjc.ssacar.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kr.ac.mjc.ssacar.Car;
import kr.ac.mjc.ssacar.CarAdapter;
import kr.ac.mjc.ssacar.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ImageView notificationIcon, mypageIcon;
    private RecyclerView carRecyclerView;
    private CarAdapter carAdapter;
    private List<Car> carList;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "MainActivity 시작");

        // HTTP 클라이언트 초기화
        client = new OkHttpClient();

        // 뷰 초기화
        initViews();

        // RecyclerView 설정
        setupRecyclerView();

        // 버튼 설정
        setupButtons();

        // 아이콘 클릭 리스너 설정
        setupIconListeners();

        // 현대자동차 API에서 차량 데이터 로드
        loadCarsFromAPI();
    }

    private void initViews() {
        carRecyclerView = findViewById(R.id.carRecyclerView);
        notificationIcon = findViewById(R.id.notificationIcon);
        mypageIcon = findViewById(R.id.mypageIcon);

        Log.d(TAG, "뷰 초기화 완료");
    }

    private void setupRecyclerView() {
        // ★ 수평 방향으로 한 줄에 쭉 나열
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        carRecyclerView.setLayoutManager(layoutManager);

        // 초기 빈 리스트로 어댑터 생성
        carList = new ArrayList<>();
        carAdapter = new CarAdapter(this, carList);
        carRecyclerView.setAdapter(carAdapter);

        Log.d(TAG, "RecyclerView 설정 완료");
    }

    private void setupButtons() {
        Button btnCallHere = findViewById(R.id.btn_call_here);
        Button btnPickup = findViewById(R.id.btn_pickup);
        Button btnOneway = findViewById(R.id.btn_oneway);
        Button btnLongterm = findViewById(R.id.btn_longterm);

        btnCallHere.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CallHereActivity.class)));

        btnPickup.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, PickUpActivity.class)));

        btnOneway.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, OnewayActivity.class)));

        btnLongterm.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LongtermActivity.class)));

        Log.d(TAG, "버튼 설정 완료");
    }

    private void setupIconListeners() {
        notificationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
                startActivity(intent);
            }
        });

        mypageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MyPageActivity.class);
                startActivity(intent);
            }
        });

        Log.d(TAG, "아이콘 리스너 설정 완료");
    }

    // 현대자동차 API에서 차량 데이터 로드
    private void loadCarsFromAPI() {
        Log.d(TAG, "현대자동차 API에서 차량 데이터 로드 시작");

        // 먼저 기본 차량 추가 (로딩 중 표시용)
        addLoadingCars();

        // 인기 차량들을 검색하여 메인에 표시
        String[] popularCars = {"아이오닉", "산타페", "투싼", "쏘나타", "캐스퍼"};

        for (String carName : popularCars) {
            loadCarsByQuery(carName);
        }
    }

    // 로딩 중 표시할 기본 차량들
    private void addLoadingCars() {
        carList.add(new Car("차량 로딩 중...", "₩로딩중", R.drawable.sample_car));
        carAdapter.notifyDataSetChanged();

        // 10초 후에 API 로딩이 실패했으면 기본 차량으로 교체
        carRecyclerView.postDelayed(() -> {
            if (carList.size() <= 3) { // API 로딩이 별로 안됐으면
                Log.d(TAG, "API 로딩 실패 추정, 기본 차량으로 교체");
                addFallbackCars();
            }
        }, 10000);
    }

    // 특정 키워드로 차량 검색
    private void loadCarsByQuery(String query) {
        String apiUrl = "https://www.hyundai.com/kr/ko/e-srv/search.search-service?site_code=hmk&collection=EP_TOTAL_MODEL&query=" + query + "&start_count=0&count=3";

        Log.d(TAG, "API 호출: " + query);

        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "API 호출 실패: " + query, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "API 응답 성공: " + query);
                    parseAPIResponse(responseBody);
                } else {
                    Log.e(TAG, "API 응답 실패: " + response.code() + " for " + query);
                }
            }
        });
    }

    // API 응답 파싱
    private void parseAPIResponse(String response) {
        try {
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
            JsonObject data = jsonResponse.getAsJsonObject("data");
            JsonArray collections = data.getAsJsonArray("collections");

            List<Car> newCars = new ArrayList<>();

            for (JsonElement collectionElement : collections) {
                JsonObject collection = collectionElement.getAsJsonObject();
                JsonArray documents = collection.getAsJsonArray("document");

                for (JsonElement docElement : documents) {
                    JsonObject doc = docElement.getAsJsonObject();

                    // 차량 데이터인지 확인
                    String carName = getStringFromJson(doc, "REPN_CARN", "");
                    if (!carName.isEmpty() && !carName.equals("null")) {
                        Car car = parseCarFromDocument(doc);
                        if (car != null) {
                            newCars.add(car);
                        }
                    }
                }
            }

            // UI 업데이트 (메인 스레드)
            runOnUiThread(() -> {
                // 첫 번째 API 응답이면 로딩 차량들 제거
                if (carList.size() > 0 && carList.get(0).getName().contains("로딩")) {
                    carList.clear();
                }

                for (Car car : newCars) {
                    // 중복 확인 후 추가
                    if (!isCarAlreadyExists(car.getName())) {
                        carList.add(car);
                    }
                }
                carAdapter.notifyDataSetChanged();
                Log.d(TAG, "차량 목록 업데이트: 총 " + carList.size() + "개");
            });

        } catch (Exception e) {
            Log.e(TAG, "API 응답 파싱 실패", e);
        }
    }

    // Document에서 Car 객체 생성
    private Car parseCarFromDocument(JsonObject doc) {
        try {
            String carName = getStringFromJson(doc, "REPN_CARN", "차량명 없음");
            String carCode = getStringFromJson(doc, "REPN_CARN_CD", "");
            String engineType = getStringFromJson(doc, "ENG_TP", "");
            String minRange = getStringFromJson(doc, "MIN_ROF_SBC", "");
            String maxRange = getStringFromJson(doc, "MAX_ROF_SBC", "");
            String imagePath = getStringFromJson(doc, "URL_ADR_SBC", "");
            String minPrice = getStringFromJson(doc, "MIN_PCE_AMT", "");

            // HTML 태그 제거
            carName = carName.replaceAll("<[^>]*>", "").trim();

            // 엔진 타입 추정
            if (engineType.isEmpty()) {
                if (carName.toLowerCase().contains("일렉트릭") || carName.toLowerCase().contains("전기")) {
                    engineType = "전기";
                } else if (carName.toLowerCase().contains("하이브리드")) {
                    engineType = "하이브리드";
                } else {
                    engineType = "가솔린";
                }
            }

            // 이미지 URL 생성
            String fullImageUrl = "";
            if (!imagePath.isEmpty()) {
                fullImageUrl = "https://www.hyundai.com" + imagePath;
            } else {
                fullImageUrl = "https://picsum.photos/400/250?random=" + Math.abs(carName.hashCode() % 100);
            }

            // 연비 정보
            String fuelEfficiency = "";
            if (!maxRange.isEmpty() && !maxRange.equals("0")) {
                fuelEfficiency = maxRange + "km/l";
            } else {
                fuelEfficiency = "연비 정보 없음";
            }

            // 가격 정보 (시간당 렌탈료로 변환)
            String priceText = "";
            if (!minPrice.isEmpty() && !minPrice.equals("0")) {
                try {
                    long price = Long.parseLong(minPrice);
                    // 구매가의 0.1%를 시간당 렌탈료로 계산 (예시)
                    long hourlyRate = price / 1000;
                    priceText = String.format("₩%,d / 1시간", hourlyRate);
                } catch (NumberFormatException e) {
                    priceText = getDefaultPrice(carName);
                }
            } else {
                priceText = getDefaultPrice(carName);
            }

            Log.d(TAG, "차량 파싱 성공: " + carName + " - " + priceText);

            return new Car(carName, priceText, fullImageUrl, engineType, fuelEfficiency, carCode);

        } catch (Exception e) {
            Log.e(TAG, "차량 파싱 실패", e);
            return null;
        }
    }

    // 차량별 기본 가격 설정
    private String getDefaultPrice(String carName) {
        if (carName.contains("제네시스")) {
            return "₩45,000 / 1시간";
        } else if (carName.contains("아이오닉")) {
            return "₩30,000 / 1시간";
        } else if (carName.contains("산타페")) {
            return "₩35,000 / 1시간";
        } else if (carName.contains("캐스퍼")) {
            return "₩15,000 / 1시간";
        } else {
            return "₩25,000 / 1시간";
        }
    }

    // JSON에서 문자열 안전하게 추출
    private String getStringFromJson(JsonObject json, String key, String defaultValue) {
        try {
            if (json.has(key) && !json.get(key).isJsonNull()) {
                return json.get(key).getAsString();
            }
        } catch (Exception e) {
            Log.w(TAG, "JSON 파싱 실패: " + key, e);
        }
        return defaultValue;
    }

    // 차량 중복 확인
    private boolean isCarAlreadyExists(String carName) {
        for (Car car : carList) {
            if (car.getName().equals(carName)) {
                return true;
            }
        }
        return false;
    }

    // API 로딩 실패 시 기본 차량들
    private void addFallbackCars() {
        Log.d(TAG, "기본 차량 추가");

        carList.clear();
        carList.add(new Car("현대 아반떼", "₩20,000 / 1시간", R.drawable.sample_car));
        carList.add(new Car("기아 쏘렌토", "₩25,000 / 1시간", R.drawable.sample_car));
        carList.add(new Car("쉐보레 스파크", "₩18,000 / 1시간", R.drawable.sample_car));

        carAdapter.notifyDataSetChanged();

        Toast.makeText(this, "기본 차량 목록을 표시합니다.", Toast.LENGTH_SHORT).show();
    }
}