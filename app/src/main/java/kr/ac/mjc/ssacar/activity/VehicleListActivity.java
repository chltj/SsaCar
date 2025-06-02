package kr.ac.mjc.ssacar.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.ac.mjc.ssacar.R;
import kr.ac.mjc.ssacar.Vehicle;
import kr.ac.mjc.ssacar.VehicleAdapter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class VehicleListActivity extends AppCompatActivity {

    private static final String TAG = "VehicleListActivity";

    private EditText searchEditText;
    private Button searchButton;
    private RecyclerView vehicleRecyclerView;
    private VehicleAdapter vehicleAdapter;
    private ProgressBar progressBar;

    private Vehicle selectedVehicle;
    private Button selectCompleteButton;

    private List<Vehicle> vehicleList;
    private OkHttpClient client;
    private Gson gson;

    // 차량별 가격 설정 (원하는 대로 수정 가능)
    private Map<String, Integer> customPrices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_list);

        initViews();
        initCustomPrices();
        setupRecyclerView();

        client = new OkHttpClient();
        gson = new Gson();

        // 차량 데이터 로드
        loadVehicleData();
    }

    private void initViews() {
        searchEditText = findViewById(R.id.search_edit_text);
        searchButton = findViewById(R.id.search_button);
        vehicleRecyclerView = findViewById(R.id.vehicle_recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        selectCompleteButton = findViewById(R.id.select_complete_button); // 추가된 부분

        vehicleList = new ArrayList<>();

        // 검색 버튼 클릭 리스너
        searchButton.setOnClickListener(v -> performSearch());

        // 엔터키 검색
        searchEditText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                performSearch();
                return true;
            }
            return false;
        });

        // 선택 완료 버튼 클릭 리스너
        selectCompleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedVehicle != null) {
                    // PaymentActivity로 이동
                    Intent intent = new Intent(VehicleListActivity.this, PaymentActivity.class);
                    intent.putExtra("selected_vehicle", selectedVehicle);
                    intent.putExtra("vehicle_name", selectedVehicle.getName());
                    intent.putExtra("vehicle_price", selectedVehicle.getPrice());
                    intent.putExtra("vehicle_type", selectedVehicle.getEngineType());
                    startActivity(intent);
                } else {
                    Toast.makeText(VehicleListActivity.this, "차량을 먼저 선택해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 초기 선택 완료 버튼 비활성화
        updateSelectButton();

        // 뒤로가기 버튼
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }

    private void updateSelectButton() {
        if (selectedVehicle != null) {
            selectCompleteButton.setEnabled(true);
            selectCompleteButton.setAlpha(1.0f);
        } else {
            selectCompleteButton.setEnabled(false);
            selectCompleteButton.setAlpha(0.5f);
        }
    }

    private void performSearch() {
        String keyword = searchEditText.getText().toString().trim();
        if (TextUtils.isEmpty(keyword)) {
            Toast.makeText(this, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        vehicleList.clear();
        vehicleAdapter.notifyDataSetChanged();
        searchVehicles(keyword);
    }

    private void searchVehicles(String keyword) {
        showLoading(true);

        // URL 인코딩을 위해 키워드 처리
        String encodedKeyword = keyword.replace(" ", "%20");
        String url = "https://www.hyundai.com/kr/ko/e/api/search/search/search?query=" + encodedKeyword + "&collection=EP_TOTAL_ALL&sort=RANK&viewCount=10&pageNum=1";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "검색 API 호출 실패: " + keyword, e);
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(VehicleListActivity.this, "'" + keyword + "' 검색에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    loadMockData(keyword); // 실패 시 목업 데이터 사용
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    showLoading(false);

                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String json = response.body().string();
                            Log.d(TAG, "검색 API 응답 (" + keyword + "): " + json);
                            parseVehicleData(json, keyword);
                        } catch (IOException e) {
                            Log.e(TAG, "검색 응답 파싱 실패: " + keyword, e);
                            Toast.makeText(VehicleListActivity.this, "'" + keyword + "' 검색 데이터 파싱에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            loadMockData(keyword);
                        }
                    } else {
                        Log.w(TAG, "검색 API 응답 실패: " + response.code());
                        Toast.makeText(VehicleListActivity.this, "'" + keyword + "' 검색 결과를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                        loadMockData(keyword); // 실패 시 목업 데이터 사용
                    }
                });
            }
        });
    }

    // 커스텀 가격 설정 (원하는 대로 수정하세요!)
    private void initCustomPrices() {
        customPrices = new HashMap<>();

        // === 차량 코드별 가격 설정 (원 단위) ===

        // 현대차 전기차
        customPrices.put("NE05", 57500);    // 아이오닉 5 N
        customPrices.put("NE", 45000);      // 아이오닉 5
        customPrices.put("NF", 38000);      // 아이오닉 6
        customPrices.put("NU", 42000);      // 아이오닉 7

        // 현대차 일반 승용차
        customPrices.put("CN", 25000);      // 아반떼
        customPrices.put("DN", 35000);      // 쏘나타
        customPrices.put("LF", 28000);      // 그랜저
        customPrices.put("AD", 22000);      // 엑센트

        // 현대차 SUV
        customPrices.put("TL", 38000);      // 투싼
        customPrices.put("SM", 75500);      // 산타페
        customPrices.put("OS", 65000);      // 베뉴

        // 제네시스
        customPrices.put("GN", 125500);     // 제네시스 G90
        customPrices.put("DH", 95000);      // 제네시스 G80
        customPrices.put("IK", 85000);      // 제네시스 G70
        customPrices.put("JW", 110000);     // 제네시스 GV80
        customPrices.put("JX", 95000);      // 제네시스 GV70

        // 수입차/스포츠카 (예시)
        customPrices.put("FERRARI", 125500); // 페라리
        customPrices.put("LAMBORGHINI", 150000); // 람보르기니
        customPrices.put("PORSCHE", 95000);   // 포르쉐
        customPrices.put("BMW", 85000);       // BMW
        customPrices.put("BENZ", 90000);      // 벤츠
        customPrices.put("AUDI", 80000);      // 아우디
        customPrices.put("DODGE", 75500);     // 닷지

        // 기본 가격 (매칭되지 않는 경우)
        customPrices.put("DEFAULT", 50000);
    }

    private void setupRecyclerView() {
        vehicleAdapter = new VehicleAdapter(vehicleList, this::onVehicleSelected);
        vehicleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        vehicleRecyclerView.setAdapter(vehicleAdapter);
    }

    // 현대자동차 API 호출
    private void loadVehicleData() {
        showLoading(true);

        String url = "https://www.hyundai.com/kr/ko/e/api/search/search/search?query=아이오닉&collection=EP_TOTAL_ALL&sort=RANK&viewCount=10&pageNum=1";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "API 호출 실패", e);
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(VehicleListActivity.this, "차량 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    loadMockData(); // 실패 시 목업 데이터 사용
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    showLoading(false);

                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String json = response.body().string();
                            Log.d(TAG, "API 응답: " + json);
                            parseVehicleData(json, "아이오닉"); // 기본 키워드 추가
                        } catch (IOException e) {
                            Log.e(TAG, "응답 파싱 실패", e);
                            Toast.makeText(VehicleListActivity.this, "데이터 파싱에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            loadMockData();
                        }
                    } else {
                        Toast.makeText(VehicleListActivity.this, "데이터를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                        loadMockData(); // 실패 시 목업 데이터 사용
                    }
                });
            }
        });
    }

    // JSON 응답 파싱
    private void parseVehicleData(String json, String keyword) {
        try {
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

            if (jsonObject.has("searchResultList")) {
                JsonArray searchResults = jsonObject.getAsJsonArray("searchResultList");

                for (int i = 0; i < searchResults.size(); i++) {
                    JsonObject searchResult = searchResults.get(i).getAsJsonObject();

                    if (searchResult.has("document")) {
                        JsonArray documents = searchResult.getAsJsonArray("document");

                        for (int j = 0; j < documents.size(); j++) {
                            JsonObject doc = documents.get(j).getAsJsonObject();
                            Vehicle vehicle = parseVehicleFromDocument(doc);

                            if (vehicle != null) {
                                vehicleList.add(vehicle);
                            }
                        }
                    }
                }
            }

            if (vehicleList.isEmpty()) {
                loadMockData(keyword); // API 데이터가 없으면 목업 데이터 사용
                Toast.makeText(this, "'" + keyword + "' 검색 결과가 없어 샘플 데이터를 표시합니다.", Toast.LENGTH_SHORT).show();
            } else {
                vehicleAdapter.notifyDataSetChanged();
                Toast.makeText(this, "'" + keyword + "' 검색 완료: " + vehicleList.size() + "대 발견", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "JSON 파싱 실패", e);
            loadMockData(keyword); // 파싱 실패 시 목업 데이터 사용
        }
    }

    // Document에서 Vehicle 객체 생성
    private Vehicle parseVehicleFromDocument(JsonObject doc) {
        try {
            String carName = getStringFromJson(doc, "REPN_CARN", "차량명 없음");
            String carCode = getStringFromJson(doc, "REPN_CARN_CD", "DEFAULT");
            String engineType = getStringFromJson(doc, "ENG_TP", "정보없음");
            String minRange = getStringFromJson(doc, "MIN_ROF_SBC", "");
            String maxRange = getStringFromJson(doc, "MAX_ROF_SBC", "");
            String imagePath = getStringFromJson(doc, "URL_ADR_SBC", "");

            // 실제 가격 정보 (API에서 가져온 값)
            String minPrice = getStringFromJson(doc, "MIN_PCE_AMT", "");
            String maxPrice = getStringFromJson(doc, "MAX_PCE_AMT", "");

            // 디버깅 로그 추가
            Log.d(TAG, "=== 차량 파싱 시작 ===");
            Log.d(TAG, "차량명: " + carName);
            Log.d(TAG, "원본 이미지 경로: '" + imagePath + "'");
            Log.d(TAG, "이미지 경로 길이: " + imagePath.length());
            Log.d(TAG, "이미지 경로가 비어있나? " + imagePath.isEmpty());

            // HTML 태그 제거
            carName = carName.replaceAll("<[^>]*>", "");

            // 이미지 URL 구성 - 강제로 기본 이미지 사용
            String fullImageUrl = "";
            if (!imagePath.isEmpty() && imagePath.trim().length() > 0) {
                fullImageUrl = "https://www.hyundai.com" + imagePath;
                Log.d(TAG, "API 이미지 URL 생성: " + fullImageUrl);
            } else {
                Log.w(TAG, "❌ API에서 이미지 경로 없음, 기본 이미지 사용: " + carName);
                fullImageUrl = getDefaultImageUrl(carCode, carName);
                Log.d(TAG, "기본 이미지 URL: " + fullImageUrl);
            }

            // 연비 정보 구성
            String fuelEfficiency = "";
            if (!minRange.isEmpty() && !maxRange.isEmpty()) {
                if (minRange.equals(maxRange)) {
                    fuelEfficiency = minRange + "km/l";
                } else {
                    fuelEfficiency = minRange + "~" + maxRange + "km/l";
                }
            } else if (!minRange.isEmpty()) {
                fuelEfficiency = minRange + "km/l";
            } else {
                fuelEfficiency = "연비 정보 없음";
            }

            // 가격 정보 구성 (API 가격 우선, 없으면 커스텀 가격)
            String priceText = "";
            if (!minPrice.isEmpty()) {
                try {
                    long price = Long.parseLong(minPrice);
                    priceText = String.format("%,d원", price);
                    Log.d(TAG, "API 가격 사용: " + priceText);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "가격 파싱 실패, 커스텀 가격 사용");
                }
            }

            // API에서 가격을 가져오지 못했으면 커스텀 가격 사용
            if (priceText.isEmpty()) {
                Integer customPrice = customPrices.get(carCode);
                if (customPrice == null) {
                    customPrice = customPrices.get("DEFAULT");
                }
                priceText = String.format("%,d원", customPrice);
                Log.d(TAG, "커스텀 가격 사용: " + priceText);
            }

            Log.d(TAG, "최종 이미지 URL: '" + fullImageUrl + "'");
            Log.d(TAG, "=== 차량 파싱 완료 ===");

            return new Vehicle(
                    carName,
                    priceText,
                    fuelEfficiency,
                    engineType,
                    fullImageUrl,
                    carCode
            );

        } catch (Exception e) {
            Log.e(TAG, "Vehicle 파싱 실패", e);
            return null;
        }
    }

    // 기본 이미지 URL 제공 (API에서 이미지를 못 가져올 때)
    private String getDefaultImageUrl(String carCode, String carName) {
        // 확실한 테스트용 이미지 URL 반환
        Log.d(TAG, "기본 이미지 URL 생성 for: " + carName);

        if (carName.contains("아이오닉 5")) {
            return "https://picsum.photos/400/250?random=1";
        } else if (carName.contains("아이오닉 6")) {
            return "https://picsum.photos/400/250?random=2";
        } else if (carName.contains("G90")) {
            return "https://picsum.photos/400/250?random=3";
        } else if (carName.contains("GV80")) {
            return "https://picsum.photos/400/250?random=4";
        } else if (carName.contains("G80")) {
            return "https://picsum.photos/400/250?random=5";
        } else if (carName.contains("산타페")) {
            return "https://picsum.photos/400/250?random=6";
        } else if (carName.contains("투싼")) {
            return "https://picsum.photos/400/250?random=7";
        }

        // 모든 경우에 대해 테스트 이미지 반환
        String testUrl = "https://picsum.photos/400/250?random=" + Math.abs(carName.hashCode() % 100);
        Log.d(TAG, "기본 테스트 이미지 URL: " + testUrl);
        return testUrl;
    }

    // JSON에서 안전하게 문자열 추출
    private String getStringFromJson(JsonObject json, String key, String defaultValue) {
        try {
            if (json.has(key) && !json.get(key).isJsonNull()) {
                return json.get(key).getAsString();
            }
        } catch (Exception e) {
            Log.w(TAG, "JSON 키 추출 실패: " + key, e);
        }
        return defaultValue;
    }

    // 목업 데이터 (API 실패 시 사용) - 오버로드된 메서드 추가
    private void loadMockData() {
        loadMockData("기본");
    }

    // 목업 데이터 (API 실패 시 사용)
    private void loadMockData(String keyword) {
        vehicleList.clear();

        // 검색 키워드에 따른 샘플 데이터
        if (keyword.contains("아이오닉") || keyword.toLowerCase().contains("ioniq")) {
            vehicleList.add(new Vehicle(
                    "아이오닉 5 N",
                    "57,500원",
                    "3.7km/l",
                    "전기",
                    "",
                    "NE05"
            ));

            vehicleList.add(new Vehicle(
                    "아이오닉 5",
                    "45,000원",
                    "5.1km/l",
                    "전기",
                    "",
                    "NE"
            ));

            vehicleList.add(new Vehicle(
                    "아이오닉 6",
                    "38,000원",
                    "6.2km/l",
                    "전기",
                    "",
                    "NF"
            ));

        } else if (keyword.contains("제네시스") || keyword.toLowerCase().contains("genesis")) {
            vehicleList.add(new Vehicle(
                    "제네시스 G90",
                    "125,500원",
                    "8.5km/l",
                    "가솔린",
                    "",
                    "GN"
            ));

            vehicleList.add(new Vehicle(
                    "제네시스 GV80",
                    "110,000원",
                    "9.2km/l",
                    "가솔린",
                    "",
                    "JW"
            ));

            vehicleList.add(new Vehicle(
                    "제네시스 G80",
                    "95,000원",
                    "9.8km/l",
                    "가솔린",
                    "",
                    "DH"
            ));

        } else if (keyword.contains("산타페") || keyword.toLowerCase().contains("santafe")) {
            vehicleList.add(new Vehicle(
                    "산타페",
                    "75,500원",
                    "11.8km/l",
                    "가솔린",
                    "",
                    "SM"
            ));

        } else if (keyword.contains("투싼") || keyword.toLowerCase().contains("tucson")) {
            vehicleList.add(new Vehicle(
                    "투싼",
                    "38,000원",
                    "13.2km/l",
                    "가솔린",
                    "",
                    "TL"
            ));

        } else if (keyword.contains("쏘나타") || keyword.toLowerCase().contains("sonata")) {
            vehicleList.add(new Vehicle(
                    "쏘나타",
                    "35,000원",
                    "12.4km/l",
                    "가솔린",
                    "",
                    "DN"
            ));

        } else if (keyword.contains("아반떼") || keyword.toLowerCase().contains("avante")) {
            vehicleList.add(new Vehicle(
                    "아반떼",
                    "25,000원",
                    "14.2km/l",
                    "가솔린",
                    "",
                    "CN"
            ));

        } else if (keyword.contains("그랜저") || keyword.toLowerCase().contains("grandeur")) {
            vehicleList.add(new Vehicle(
                    "그랜저",
                    "28,000원",
                    "11.6km/l",
                    "가솔린",
                    "",
                    "LF"
            ));

        } else if (keyword.contains("팰리세이드") || keyword.toLowerCase().contains("palisade")) {
            vehicleList.add(new Vehicle(
                    "팰리세이드",
                    "42,000원",
                    "10.2km/l",
                    "가솔린",
                    "",
                    "NU"
            ));

        } else if (keyword.contains("전기") || keyword.toLowerCase().contains("electric") || keyword.toLowerCase().contains("ev")) {
            vehicleList.add(new Vehicle(
                    "아이오닉 5",
                    "45,000원",
                    "5.1km/l",
                    "전기",
                    "",
                    "NE"
            ));

            vehicleList.add(new Vehicle(
                    "아이오닉 6",
                    "38,000원",
                    "6.2km/l",
                    "전기",
                    "",
                    "NF"
            ));

        } else if (keyword.contains("SUV") || keyword.toLowerCase().contains("suv")) {
            vehicleList.add(new Vehicle(
                    "산타페",
                    "75,500원",
                    "11.8km/l",
                    "가솔린",
                    "",
                    "SM"
            ));

            vehicleList.add(new Vehicle(
                    "투싼",
                    "38,000원",
                    "13.2km/l",
                    "가솔린",
                    "",
                    "TL"
            ));

            vehicleList.add(new Vehicle(
                    "팰리세이드",
                    "42,000원",
                    "10.2km/l",
                    "가솔린",
                    "",
                    "NU"
            ));

        } else {
            // 기본 샘플 데이터 (검색어가 매칭되지 않는 경우)
            vehicleList.add(new Vehicle(
                    "아이오닉 5 N",
                    "57,500원",
                    "3.7km/l",
                    "전기",
                    "",
                    "NE05"
            ));

            vehicleList.add(new Vehicle(
                    "제네시스 G90",
                    "125,500원",
                    "8.5km/l",
                    "가솔린",
                    "",
                    "GN"
            ));

            vehicleList.add(new Vehicle(
                    "산타페",
                    "75,500원",
                    "11.8km/l",
                    "가솔린",
                    "",
                    "SM"
            ));
        }

        vehicleAdapter.notifyDataSetChanged();

        if (vehicleList.isEmpty()) {
            Toast.makeText(this, "'" + keyword + "' 검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "'" + keyword + "' 검색 완료: " + vehicleList.size() + "대 발견", Toast.LENGTH_SHORT).show();
        }
    }

    // 차량 선택 시 호출 (두 가지 방식 지원)
    private void onVehicleSelected(Vehicle vehicle) {
        selectedVehicle = vehicle; // 선택된 차량 저장
        updateSelectButton(); // 버튼 상태 업데이트

        Toast.makeText(this, vehicle.getName() + " 선택됨", Toast.LENGTH_SHORT).show();

        // 즉시 결과 반환하는 경우 (기존 방식)
        // Intent resultIntent = new Intent();
        // resultIntent.putExtra("selected_vehicle", vehicle);
        // setResult(RESULT_OK, resultIntent);
        // finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        vehicleRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}