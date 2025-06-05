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

        client = new OkHttpClient();

        initViews();
        setupRecyclerView();
        setupButtons();
        setupIconListeners();
        loadCarsFromAPI();
    }

    private void initViews() {
        carRecyclerView = findViewById(R.id.carRecyclerView);
        notificationIcon = findViewById(R.id.notificationIcon);
        mypageIcon = findViewById(R.id.mypageIcon);
        Log.d(TAG, "뷰 초기화 완료");
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        carRecyclerView.setLayoutManager(layoutManager);
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

        btnCallHere.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CallHereActivity.class)));
        btnPickup.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PickUpActivity.class)));
        btnOneway.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, OnewayActivity.class)));
        btnLongterm.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LongtermActivity.class)));

        Log.d(TAG, "버튼 설정 완료");
    }

    public void goToCustomer(View view) {
        Intent intent = new Intent(MainActivity.this, CustomerCenterActivity.class);
        startActivity(intent);
    }

    public void goToHistory(View view) {
        Intent intent = new Intent(MainActivity.this, UsageHistoryActivity.class);

        Intent receivedIntent = getIntent();
        if (receivedIntent.getBooleanExtra("payment_completed", false)) {
            intent.putExtra("carName", receivedIntent.getStringExtra("carName"));
            intent.putExtra("carImageUrl", receivedIntent.getStringExtra("carImageUrl"));
            intent.putExtra("startTime", receivedIntent.getStringExtra("startTime"));
            intent.putExtra("endTime", receivedIntent.getStringExtra("endTime"));
            intent.putExtra("pickupLocation", receivedIntent.getStringExtra("pickupLocation"));
            intent.putExtra("returnLocation", receivedIntent.getStringExtra("returnLocation"));
        }

        startActivity(intent);
    }

    private void setupIconListeners() {
        notificationIcon.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, NotificationActivity.class)));
        mypageIcon.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MyPageActivity.class)));
        Log.d(TAG, "아이콘 리스너 설정 완료");
    }

    private void loadCarsFromAPI() {
        Log.d(TAG, "현대자동차 API에서 차량 데이터 로드 시작");
        addLoadingCars();
        String[] popularCars = {"아이오닉", "산타페", "투싼", "쏘나타", "캐스퍼"};
        for (String carName : popularCars) {
            loadCarsByQuery(carName);
        }
    }

    private void addLoadingCars() {
        carList.add(new Car("차량 로딩 중...", "₩로딩중", R.drawable.sample_car));
        carAdapter.notifyDataSetChanged();
        carRecyclerView.postDelayed(() -> {
            if (carList.size() <= 3) {
                Log.d(TAG, "API 로딩 실패 추정, 기본 차량으로 교체");
                addFallbackCars();
            }
        }, 10000);
    }

    private void loadCarsByQuery(String query) {
        String apiUrl = "https://www.hyundai.com/kr/ko/e-srv/search.search-service?site_code=hmk&collection=EP_TOTAL_MODEL&query=" + query + "&start_count=0&count=3";
        Request request = new Request.Builder().url(apiUrl).addHeader("User-Agent", "Mozilla/5.0").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "API 호출 실패: " + query, e);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    parseAPIResponse(response.body().string());
                } else {
                    Log.e(TAG, "API 응답 실패: " + response.code());
                }
            }
        });
    }

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
                    String carName = getStringFromJson(doc, "REPN_CARN", "");
                    if (!carName.isEmpty() && !carName.equals("null")) {
                        Car car = parseCarFromDocument(doc);
                        if (car != null) newCars.add(car);
                    }
                }
            }
            runOnUiThread(() -> {
                if (carList.size() > 0 && carList.get(0).getName().contains("로딩")) {
                    carList.clear();
                }
                for (Car car : newCars) {
                    if (!isCarAlreadyExists(car.getName())) {
                        carList.add(car);
                    }
                }
                carAdapter.notifyDataSetChanged();
            });
        } catch (Exception e) {
            Log.e(TAG, "API 응답 파싱 실패", e);
        }
    }

    private Car parseCarFromDocument(JsonObject doc) {
        try {
            String carName = getStringFromJson(doc, "REPN_CARN", "차량명 없음").replaceAll("<[^>]*>", "").trim();
            String imagePath = getStringFromJson(doc, "URL_ADR_SBC", "");
            String minPrice = getStringFromJson(doc, "MIN_PCE_AMT", "");
            String fullImageUrl = imagePath.isEmpty() ? "https://picsum.photos/400/250?random=" + Math.abs(carName.hashCode() % 100) : "https://www.hyundai.com" + imagePath;
            String priceText = (!minPrice.isEmpty() && !minPrice.equals("0")) ? String.format("₩%,d / 1시간", Long.parseLong(minPrice) / 1000) : getDefaultPrice(carName);
            turn String engineType;
            new Car(carName, priceText, fullImageUrl, engineType, fuelEfficiency, carCode);
        } catch (Exception e) {
            Log.e(TAG, "차량 파싱 실패", e);
            return null;
        }
    }

    private String getDefaultPrice(String carName) {
        if (carName.contains("제네시스")) return "₩45,000 / 1시간";
        else if (carName.contains("아이오닉")) return "₩30,000 / 1시간";
        else if (carName.contains("산타페")) return "₩35,000 / 1시간";
        else if (carName.contains("캐스퍼")) return "₩15,000 / 1시간";
        else return "₩25,000 / 1시간";
    }

    private String getStringFromJson(JsonObject json, String key, String defaultValue) {
        try {
            if (json.has(key) && !json.get(key).isJsonNull()) return json.get(key).getAsString();
        } catch (Exception e) {
            Log.w(TAG, "JSON 파싱 실패: " + key, e);
        }
        return defaultValue;
    }

    private boolean isCarAlreadyExists(String carName) {
        for (Car car : carList) {
            if (car.getName().equals(carName)) return true;
        }
        return false;
    }

    private void addFallbackCars() {
        carList.clear();
        carList.add(new Car("현대 아반떼", "₩20,000 / 1시간", R.drawable.sample_car));
        carList.add(new Car("기아 쏘렌토", "₩25,000 / 1시간", R.drawable.sample_car));
        carList.add(new Car("쉐보레 스파크", "₩18,000 / 1시간", R.drawable.sample_car));
        carAdapter.notifyDataSetChanged();
        Toast.makeText(this, "기본 차량 목록을 표시합니다.", Toast.LENGTH_SHORT).show();
    }
}
