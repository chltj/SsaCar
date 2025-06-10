package kr.ac.mjc.ssacar.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
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
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.TimeUnit;




public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ImageView notificationIcon, mypageIcon;
    private RecyclerView carRecyclerView;
    private CarAdapter carAdapter;
    private List<Car> carList;
    private OkHttpClient client;
    private ImageView carImageView;
    private TextView carNameText;
    private TextView carPriceText;
    private String imageUrl;



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

        btnCallHere.setOnClickListener(v -> {
            if (checkLoginAndRedirectIfNeeded()) {
                startActivity(new Intent(MainActivity.this, CallHereActivity.class));
            }
        });

        btnPickup.setOnClickListener(v ->{
        if (checkLoginAndRedirectIfNeeded()) {
                startActivity(new Intent(MainActivity.this, PickUpActivity.class));
        }
    });

        btnOneway.setOnClickListener(v ->{
        if (checkLoginAndRedirectIfNeeded()) {
            startActivity(new Intent(MainActivity.this, OnewayActivity.class));
        }
    });
        btnLongterm.setOnClickListener(v ->{
        if (checkLoginAndRedirectIfNeeded()) {
            startActivity(new Intent(MainActivity.this, LongtermActivity.class));
        }
    });
        Log.d(TAG, "버튼 설정 완료");
    }



    public void goToHistory(View view) {
        if (checkLoginAndRedirectIfNeeded()) {
            startActivity(new Intent(this, UsageHistoryActivity.class));
        }
    }
    public void goToPayment(View view) {
        if (checkLoginAndRedirectIfNeeded()) {
            try {
                Log.d(TAG, "결제 화면으로 이동 시도");
                Intent intent = new Intent(this, PaymentLicenseActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "결제 화면 이동 실패: " + e.getMessage());
                Toast.makeText(this, "결제 기능은 준비 중입니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void goToSmartkey(View view) {
        if (checkLoginAndRedirectIfNeeded()) {
            startActivity(new Intent(this, SamrtKeyActivity.class));
        }
    }
    public void goTodrive(View view) {
        if (checkLoginAndRedirectIfNeeded()) {
            startActivity(new Intent(this, LicenseListActivity.class));
        }
    }

    private void setupIconListeners() {
        notificationIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        mypageIcon.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("current_user", MODE_PRIVATE);
            String currentUserId = prefs.getString("current_user_id", null);

            Intent intent;
            if (currentUserId != null) {
                // 로그인 상태면 마이페이지로 이동
                intent = new Intent(MainActivity.this, MypageActivity.class);
            } else {
                // 비로그인 상태면 로그인 페이지로 이동
                intent = new Intent(MainActivity.this, LoginActivity.class);
            }
            startActivity(intent);
        });

        Log.d(TAG, "아이콘 리스너 설정 완료");
    }


    // 현대자동차 API에서 차량 데이터 로드
    // ★ 이렇게 수정하세요!
    private void loadCarsFromAPI() {
        Log.d(TAG, "현대자동차 API에서 차량 데이터 로드 시작");

        addLoadingCars();

        // 더 많은 현대차 모델들
        String[] allHyundaiCars = {
                // 인기 모델
                "아이오닉 5", "산타페", "투싼", "쏘나타", "아반떼",
                // 프리미엄/대형
                "팰리세이드", "그랜저", "제네시스 G90", "제네시스 GV70", "제네시스 G80",
                // 소형/경차
                "캐스퍼", "코나", "베뉴", "벨로스터",
                // 전기차
                "아이오닉 6", "코나 일렉트릭", "포터 일렉트릭",
                // 상용차/기타
                "스타리아", "포터"
        };

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

        // ★ 더 많은 현대차 모델들 추가
        // 인기 세단/해치백
        carList.add(new Car("현대 아반떼", "₩20,000 / 1시간", R.drawable.sample_car, "", "가솔린", "14.2km/l", "AVANTE"));
        carList.add(new Car("현대 쏘나타", "₩25,000 / 1시간", R.drawable.sample_car, "", "가솔린", "13.8km/l", "SONATA"));
        carList.add(new Car("현대 그랜저", "₩35,000 / 1시간", R.drawable.sample_car, "", "가솔린", "11.5km/l", "GRANDEUR"));

        // SUV 라인업
        carList.add(new Car("현대 산타페", "₩30,000 / 1시간", R.drawable.sample_car, "", "가솔린", "12.1km/l", "SANTAFE"));
        carList.add(new Car("현대 투싼", "₩28,000 / 1시간", R.drawable.sample_car, "", "가솔린", "13.0km/l", "TUCSON"));
        carList.add(new Car("현대 팰리세이드", "₩45,000 / 1시간", R.drawable.sample_car, "", "가솔린", "10.2km/l", "PALISADE"));
        carList.add(new Car("현대 코나", "₩22,000 / 1시간", R.drawable.sample_car, "", "가솔린", "14.5km/l", "KONA"));
        carList.add(new Car("현대 베뉴", "₩18,000 / 1시간", R.drawable.sample_car, "", "가솔린", "15.2km/l", "VENUE"));

        // 전기차 라인업
        carList.add(new Car("현대 아이오닉 5", "₩35,000 / 1시간", R.drawable.sample_car, "", "전기", "305km", "IONIQ5"));
        carList.add(new Car("현대 아이오닉 6", "₩40,000 / 1시간", R.drawable.sample_car, "", "전기", "429km", "IONIQ6"));
        carList.add(new Car("현대 코나 일렉트릭", "₩30,000 / 1시간", R.drawable.sample_car, "", "전기", "259km", "KONA_EV"));

        // 소형차/경차
        carList.add(new Car("현대 캐스퍼", "₩15,000 / 1시간", R.drawable.sample_car, "", "가솔린", "17.3km/l", "CASPER"));
        carList.add(new Car("현대 벨로스터", "₩25,000 / 1시간", R.drawable.vel, "", "가솔린", "12.8km/l", "VELOSTER"));

        // 제네시스 라인업
        carList.add(new Car("제네시스 G90", "₩60,000 / 1시간", R.drawable.g90, "", "가솔린", "9.1km/l", "G90"));
        carList.add(new Car("제네시스 GV70", "₩50,000 / 1시간", R.drawable.gv70, "", "가솔린", "10.5km/l", "GV70"));
        carList.add(new Car("제네시스 G80", "₩55,000 / 1시간", R.drawable.g80, "", "가솔린", "10.2km/l", "G80"));

        // 상용차
        carList.add(new Car("현대 스타리아", "₩40,000 / 1시간", R.drawable.sample_car, "", "디젤", "11.3km/l", "STARIA"));
        carList.add(new Car("현대 포터", "₩25,000 / 1시간", R.drawable.sample_car, "", "디젤", "12.5km/l", "PORTER"));

        carAdapter.notifyDataSetChanged();

        // ★ 모든 차량의 실제 이미지로 교체
        updateAllCarsWithRealImages();
    }

    // ★ 모든 차량의 이미지를 업데이트하는 새로운 메서드
    private void updateAllCarsWithRealImages() {
        Log.d(TAG, "실제 이미지 업데이트 시작");
        for (int i = 0; i < carList.size(); i++) {
            Car car = carList.get(i);
            String searchKeyword = car.getName().replace("현대 ", "").replace("제네시스 ", "");
            updateCarImage(i, searchKeyword);

            // API 호출 간격을 두어 서버 부하 방지
            try {
                Thread.sleep(100); // 0.1초 간격
            } catch (InterruptedException e) {
                Log.w(TAG, "Thread sleep 중단됨");
            }
        }
        updateCarImageSequentially(0);
    }

    private void updateCarImageSequentially(int currentIndex) {


        if (currentIndex >= carList.size()) {
            Log.d(TAG, "모든 차량 이미지 업데이트 완료");
            return;
        }

        Car car = carList.get(currentIndex);
        String searchKeyword = getOptimizedKeyword(car.getName());
        Log.d("SEARCH_KEYWORD", "검색할 키워드: " + car.getName() + " -> " + searchKeyword);

        Log.d(TAG, "이미지 업데이트 중: " + car.getName() + " (인덱스: " + currentIndex + ")");

        String url = "https://www.hyundai.com/kr/ko/e/api/search/search/search?query=" + searchKeyword + "&collection=EP_TOTAL_ALL&sort=RANK&viewCount=10&pageNum=1";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .addHeader("Accept", "application/json")
                .build();

        // ★ 각 요청마다 새로운 클라이언트 생성 (충돌 방지)
        OkHttpClient sequentialClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();

        sequentialClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "이미지 API 호출 실패: " + car.getName(), e);

                // ★ 실패해도 다음 차량 계속 처리 (500ms 후)
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    updateCarImageSequentially(currentIndex + 1);
                }, 500);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String body = response.body().string();
                    Log.d("API_RESPONSE", "응답 성공: " + car.getName());

                    JsonObject jsonObject = new Gson().fromJson(body, JsonObject.class);
                    JsonArray collections = jsonObject.getAsJsonObject("data").getAsJsonArray("collections");

                    if (collections.size() > 0) {
                        JsonObject collection = collections.get(0).getAsJsonObject();
                        JsonArray documents = collection.get("document").getAsJsonArray();

                        if (documents.size() > 0) {
                            JsonObject document = documents.get(0).getAsJsonObject();
                            JsonElement element = document.get("URL_ADR_SBC");

                            if (element != null && !element.isJsonNull()) {
                                String imageUrl = "https://www.hyundai.com" + element.getAsString();
                                Log.d("IMAGE_URL", car.getName() + " -> " + imageUrl);

                                runOnUiThread(() -> {
                                    try {
                                        if (currentIndex < carList.size()) {
                                            Car targetCar = carList.get(currentIndex);
                                            if (targetCar != null) {
                                                // ★ 이미지 URL 업데이트
                                                targetCar.setImageUrl(imageUrl);
                                                carAdapter.notifyItemChanged(currentIndex);
                                                Log.d(TAG, "✅ 이미지 업데이트 성공: " + targetCar.getName());
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "이미지 URL 업데이트 실패", e);
                                    }

                                    // ★ 다음 차량 처리 (300ms 후, UI 업데이트 안정화)
                                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                        updateCarImageSequentially(currentIndex + 1);
                                    }, 300);
                                });
                            } else {
                                Log.w(TAG, "URL_ADR_SBC가 null: " + car.getName());
                                // ★ 다음 차량 계속 처리
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    updateCarImageSequentially(currentIndex + 1);
                                }, 300);
                            }
                        } else {
                            Log.w(TAG, "documents 배열이 비어있음: " + car.getName());
                            // ★ 다음 차량 계속 처리
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                updateCarImageSequentially(currentIndex + 1);
                            }, 300);
                        }
                    } else {
                        Log.w(TAG, "collections 배열이 비어있음: " + car.getName());
                        // ★ 다음 차량 계속 처리
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            updateCarImageSequentially(currentIndex + 1);
                        }, 300);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "이미지 API 처리 실패: " + car.getName(), e);
                    // ★ 다음 차량 계속 처리
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        updateCarImageSequentially(currentIndex + 1);
                    }, 300);
                }
            }
        });

    }

    // ★ 검색 키워드 최적화 메서드
    private String getOptimizedKeyword(String carName) {
        // ★ 직접 매핑으로 확실하게 (수정된 버전)
        switch (carName) {
            case "현대 아반떼": return "아반떼";
            case "현대 쏘나타": return "쏘나타";
            case "현대 그랜저": return "그랜저";
            case "현대 산타페": return "산타페";
            case "현대 투싼": return "투싼";
            case "현대 팰리세이드": return "팰리세이드";
            case "현대 코나": return "코나";
            case "현대 베뉴": return "베뉴";
            case "현대 아이오닉 5": return "아이오닉5";
            case "현대 아이오닉 6": return "아이오닉6";
            case "현대 코나 일렉트릭": return "코나";
            case "현대 캐스퍼": return "캐스퍼";
            // ★ 벨로스터 키워드 변경 시도
            case "현대 벨로스터": return "벨로스터";  // 다시 N 추가
            // ★ 제네시스 키워드 변경 시도
            case "제네시스 G90": return "GENESIS"; // 브랜드명만
            case "제네시스 GV70": return "GV70 GENESIS"; // 순서 바꿔서
            case "제네시스 G80": return "G80 GENESIS";
            case "현대 스타리아": return "스타리아";
            case "현대 포터": return "포터";
            default:
                String keyword = carName.replace("현대 ", "").replace("제네시스 ", "");
                Log.d("SEARCH_KEYWORD", carName + " -> " + keyword);
                return keyword;
        }
    }


    // updateCarImage 메서드를 완전히 수정:
    private void updateCarImage(int index, String carName) {
        Log.d("updateCarImage", carName + " (index: " + index + ")");

        String encodedKeyword = carName.replace("현대 ", "");
        String url = "https://www.hyundai.com/kr/ko/e/api/search/search/search?query=" + encodedKeyword + "&collection=EP_TOTAL_ALL&sort=RANK&viewCount=10&pageNum=1";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .addHeader("Accept", "application/json")
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "이미지 API 호출 실패: " + carName, e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String body = response.body().string();
                    Log.d("API_RESPONSE", "응답 성공: " + carName);

                    JsonObject jsonObject = new Gson().fromJson(body, JsonObject.class);
                    JsonArray collections = jsonObject.getAsJsonObject("data").getAsJsonArray("collections");

                    if (collections.size() > 0) {
                        JsonObject collection = collections.get(0).getAsJsonObject();
                        JsonArray documents = collection.get("document").getAsJsonArray();

                        if (documents.size() > 0) {
                            JsonObject document = documents.get(0).getAsJsonObject();
                            JsonElement element = document.get("URL_ADR_SBC");

                            if (element != null && !element.isJsonNull()) {
                                String imageUrl = "https://www.hyundai.com" + element.getAsString();
                                Log.d("IMAGE_URL", carName + " -> " + imageUrl);

                                runOnUiThread(() -> {
                                    try {
                                        if (index < carList.size()) {
                                            Car car = carList.get(index);
                                            if (car != null) {
                                                Log.d(TAG, "이미지 URL 업데이트 시도: " + car.getName());

                                                // ★ Car 객체의 이미지 URL 업데이트
                                                car.setImageUrl(imageUrl);

                                                // ★ RecyclerView 특정 아이템만 업데이트
                                                carAdapter.notifyItemChanged(index);

                                                Log.d(TAG, "✅ 이미지 URL 업데이트 성공: " + car.getName() + " -> " + imageUrl);
                                            }
                                        } else {
                                            Log.e(TAG, "인덱스 범위 초과: " + index + " >= " + carList.size());
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "이미지 URL 업데이트 실패", e);
                                    }
                                });
                            } else {
                                Log.w(TAG, "URL_ADR_SBC가 null이거나 없음: " + carName);
                            }
                        } else {
                            Log.w(TAG, "documents 배열이 비어있음: " + carName);
                        }
                    } else {
                        Log.w(TAG, "collections 배열이 비어있음: " + carName);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "이미지 API 처리 실패: " + carName, e);
                }
            }
        });
    }

    private boolean checkLoginAndRedirectIfNeeded() {
        SharedPreferences prefs = getSharedPreferences("current_user", MODE_PRIVATE);
        String currentUserId = prefs.getString("current_user_id", null);

        if (currentUserId == null) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("로그인 필요")
                    .setMessage("이 기능은 로그인 후 사용 가능합니다.")
                    .setPositiveButton("로그인", (dialog, which) -> {
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("취소", null)
                    .show();
            return false;
        }
        return true;
    }
}