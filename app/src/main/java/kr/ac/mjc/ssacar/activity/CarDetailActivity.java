package kr.ac.mjc.ssacar.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;

import kr.ac.mjc.ssacar.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CarDetailActivity extends AppCompatActivity {
    private static final String TAG = "CarDetailActivity";

    // UI 요소들
    private ImageView carImageView;
    private TextView carNameText;
    private TextView carPriceText;
    private TextView carEngineTypeText;
    private TextView carEfficiencyText;
    private TextView carFeaturesText;
    private Button purchaseButton;
    private Button backButton;

    // 차량 정보
    private String carName;
    private String carPrice;
    private String carEngineType;
    private String carEfficiency;
    private String carImageUrl;
    private String carCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_detail);

        Log.d(TAG, "CarDetailActivity 시작");

        try {
            // 뷰 초기화
            initViews();

            // Intent에서 데이터 받기
            getIntentData();

            // UI 설정
            setupUI();

            // 클릭 리스너 설정
            setupClickListeners();

        } catch (Exception e) {
            Log.e(TAG, "onCreate 실패", e);
            Toast.makeText(this, "차량 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
            // 오류가 발생해도 앱이 죽지 않도록 기본 정보 표시
            showDefaultInfo();
        }
    }


    public void getApiInfo(String carName){
        Log.d("getApiInfo",carName);
        // URL 인코딩을 위해 키워드 처리
        String encodedKeyword = carName.replace("현대 ", "");
        String url = "https://www.hyundai.com/kr/ko/e/api/search/search/search?query=" + encodedKeyword + "&collection=EP_TOTAL_ALL&sort=RANK&viewCount=10&pageNum=1";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .addHeader("Accept", "application/json")
                .build();
        OkHttpClient client =new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String body = response.body().string();
                    Log.d("searchResults", body);

                    JsonObject jsonObject = new Gson().fromJson(body, JsonObject.class);
                    JsonArray collections = jsonObject.getAsJsonObject("data").getAsJsonArray("collections");

                    // ★ 핵심: 배열 크기 확인
                    if (collections.size() > 0) {
                        JsonObject collection = collections.get(0).getAsJsonObject();
                        JsonArray documents = collection.get("document").getAsJsonArray();

                        // ★ 핵심: documents 배열 크기 확인
                        if (documents.size() > 0) {
                            JsonObject document = documents.get(0).getAsJsonObject();
                            JsonElement element = document.get("URL_ADR_SBC");

                            if (element != null && !element.isJsonNull()) {
                                String url = element.getAsString();
                                Log.d("url", url);

                                runOnUiThread(() -> {
                                    Glide.with(carImageView)
                                            .load("https://www.hyundai.com/" + url)
                                            .into(carImageView);
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "API 처리 실패", e);
                }
            }
        });
    }


    private void initViews() {
        try {
            carImageView = findViewById(R.id.car_detail_image);
            carNameText = findViewById(R.id.car_detail_name);
            carPriceText = findViewById(R.id.car_detail_price);
            carEngineTypeText = findViewById(R.id.car_detail_engine_type);
            carEfficiencyText = findViewById(R.id.car_detail_efficiency);
            backButton = findViewById(R.id.back_button);

            // car_features_text는 없을 수도 있으므로 예외 처리
            try {
                carFeaturesText = findViewById(R.id.car_features_text);
            } catch (Exception e) {
                Log.w(TAG, "car_features_text를 찾을 수 없음 (정상)");
                carFeaturesText = null;
            }

            Log.d(TAG, "뷰 초기화 완료");
        } catch (Exception e) {
            Log.e(TAG, "뷰 초기화 실패", e);
            throw e; // 뷰 초기화 실패는 심각한 오류이므로 다시 던짐
        }
    }

    private void getIntentData() {
        try {
            Intent intent = getIntent();
            if (intent != null) {
                carName = intent.getStringExtra("car_name");
                carPrice = intent.getStringExtra("car_price");
                carEngineType = intent.getStringExtra("car_engine_type");
                carEfficiency = intent.getStringExtra("car_efficiency");
                carImageUrl = intent.getStringExtra("car_image_url");
                carCode = intent.getStringExtra("car_code");

                getApiInfo(carName);
                Log.d(TAG, "받은 차량 정보:");
                Log.d(TAG, "이름: " + carName);
                Log.d(TAG, "가격: " + carPrice);
                Log.d(TAG, "엔진: " + carEngineType);
                Log.d(TAG, "연비: " + carEfficiency);
                Log.d(TAG, "이미지: " + carImageUrl);
                Log.d(TAG, "코드: " + carCode);
            } else {
                Log.e(TAG, "Intent가 null입니다.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Intent 데이터 읽기 실패", e);
            // Intent 데이터가 없어도 기본값으로 진행
        }
    }

    private void setupUI() {
        try {
            // 차량 정보 기본값 설정
            if (carName == null || carName.isEmpty()) carName = "차량명 없음";
            if (carPrice == null || carPrice.isEmpty()) carPrice = "가격 정보 없음";
            if (carEngineType == null || carEngineType.isEmpty()) carEngineType = "엔진 정보 없음";
            if (carEfficiency == null || carEfficiency.isEmpty()) carEfficiency = "연비 정보 없음";

            // 텍스트 설정
            if (carNameText != null) carNameText.setText(carName);
            if (carPriceText != null) carPriceText.setText(carPrice);
            if (carEngineTypeText != null) carEngineTypeText.setText("엔진: " + carEngineType);
            if (carEfficiencyText != null) carEfficiencyText.setText("연비: " + carEfficiency);

            // 이미지 로딩
            setupCarImage();

            // 차량 특징 설정
            setupCarFeatures();

            Log.d(TAG, "UI 설정 완료");
        } catch (Exception e) {
            Log.e(TAG, "UI 설정 실패", e);
            // UI 설정 실패해도 계속 진행
        }
    }

    private void setupCarImage() {
        try {
            if (carImageView == null) {
                Log.w(TAG, "carImageView가 null입니다.");
                return;
            }

            if (carImageUrl != null && !carImageUrl.isEmpty() && carImageUrl.startsWith("http")) {
                // 온라인 이미지 로딩
                Log.d(TAG, "온라인 이미지 로딩 시도: " + carImageUrl);

                Glide.with(this)
                        .load(carImageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_dialog_alert)
                        .timeout(10000) // 10초 타임아웃
                        .into(carImageView);

                Log.d(TAG, "Glide 이미지 로딩 시작");
            } else {
                // 기본 이미지 사용
                carImageView.setImageResource(android.R.drawable.ic_menu_gallery);
                Log.d(TAG, "기본 이미지 사용");
            }
        } catch (Exception e) {
            Log.e(TAG, "이미지 설정 실패", e);
            // 이미지 로딩 실패해도 기본 이미지라도 표시
            try {
                if (carImageView != null) {
                    carImageView.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } catch (Exception e2) {
                Log.e(TAG, "기본 이미지 설정도 실패", e2);
            }
        }
    }

    private void setupCarFeatures() {
        try {
            if (carFeaturesText == null) {
                Log.d(TAG, "carFeaturesText가 없음 (정상)");
                return;
            }

            String features = generateCarFeatures(carName, carEngineType);
            carFeaturesText.setText(features);
            Log.d(TAG, "차량 특징 설정 완료");
        } catch (Exception e) {
            Log.e(TAG, "차량 특징 설정 실패", e);
        }
    }

    private String generateCarFeatures(String carName, String engineType) {
        if (carName == null) carName = "";
        if (engineType == null) engineType = "";

        StringBuilder features = new StringBuilder();

        try {
            if (carName.contains("아이오닉")) {
                features.append("• 전기차 전용 플랫폼 적용\n");
                features.append("• 초고속 충전 기술\n");
                features.append("• V2L 기능 지원\n");
                features.append("• 첨단 운전자 보조 시스템\n");
                features.append("• 친환경 소재 인테리어");
            } else if (carName.contains("제네시스")) {
                features.append("• 프리미엄 럭셔리 브랜드\n");
                features.append("• Face Connect 기술\n");
                features.append("• Genesis Connected Services\n");
                features.append("• 프리미엄 사운드 시스템\n");
                features.append("• 고급 가죽 시트");
            } else if (carName.contains("산타페")) {
                features.append("• 8인승 대형 SUV\n");
                features.append("• SmartSense 안전 기술\n");
                features.append("• 하이브리드 옵션 가능\n");
                features.append("• 대형 디스플레이\n");
                features.append("• 3열 시트 편의성");
            } else if (carName.contains("투싼")) {
                features.append("• 컴팩트 SUV\n");
                features.append("• 터보 엔진 옵션\n");
                features.append("• 마일드 하이브리드\n");
                features.append("• 인포테인먼트 시스템\n");
                features.append("• 4WD 시스템 옵션");
            } else if (carName.contains("캐스퍼")) {
                features.append("• 경형 차량\n");
                features.append("• 도심형 이동성\n");
                features.append("• 경제적 연비\n");
                features.append("• 컴팩트 디자인\n");
                features.append("• 저렴한 유지비");
            } else {
                features.append("• 현대자동차 품질 보증\n");
                features.append("• 5년 10만km 보증\n");
                features.append("• 안전 기술 적용\n");
                features.append("• 편리한 시스템\n");
                features.append("• 우수한 연비 성능");
            }
        } catch (Exception e) {
            Log.e(TAG, "특징 생성 실패", e);
            features.append("• 현대자동차 신뢰성\n• 품질 보증\n• 안전 기술\n• 편의 기능\n• 경제성");
        }

        return features.toString();
    }

    private void setupClickListeners() {
        try {
            // 뒤로가기 버튼
            if (backButton != null) {
                backButton.setOnClickListener(v -> {
                    Log.d(TAG, "뒤로가기 버튼 클릭");
                    finish();
                });
            } else {
                Log.w(TAG, "뒤로가기 버튼을 찾을 수 없음");
            }

            // 구매 버튼
            if (purchaseButton != null) {
                purchaseButton.setOnClickListener(v -> {
                    Log.d(TAG, "구매 버튼 클릭: " + carName);
                    handlePurchaseClick();
                });
            } else {
                Log.w(TAG, "구매 버튼을 찾을 수 없음");
            }

            Log.d(TAG, "클릭 리스너 설정 완료");
        } catch (Exception e) {
            Log.e(TAG, "클릭 리스너 설정 실패", e);
        }
    }

    private void handlePurchaseClick() {
        try {
            // PaymentActivity로 이동 시도
            Intent paymentIntent = new Intent(CarDetailActivity.this, PaymentActivity.class);
            paymentIntent.putExtra("car_name", carName);
            paymentIntent.putExtra("car_price", carPrice);
            paymentIntent.putExtra("car_engine_type", carEngineType);
            paymentIntent.putExtra("car_efficiency", carEfficiency);
            paymentIntent.putExtra("car_image_url", carImageUrl);
            paymentIntent.putExtra("car_code", carCode);

            startActivity(paymentIntent);
            Log.d(TAG, "PaymentActivity 시작 성공");
        } catch (Exception e) {
            Log.e(TAG, "PaymentActivity 시작 실패", e);
            // PaymentActivity가 없거나 오류 시 간단한 메시지만 표시
            Toast.makeText(CarDetailActivity.this,
                    carName + " 예약이 완료되었습니다!\n가격: " + carPrice,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void showDefaultInfo() {
        try {
            Log.d(TAG, "기본 정보 표시");

            if (carNameText != null) carNameText.setText("차량 정보");
            if (carPriceText != null) carPriceText.setText("₩25,000 / 1시간");
            if (carEngineTypeText != null) carEngineTypeText.setText("엔진: 가솔린");
            if (carEfficiencyText != null) carEfficiencyText.setText("연비: 12.5km/l");
            if (carImageView != null) carImageView.setImageResource(android.R.drawable.ic_menu_gallery);
            if (carFeaturesText != null) {
                carFeaturesText.setText("• 현대자동차 신뢰성\n• 품질 보증\n• 안전 기술\n• 편의 기능");
            }
        } catch (Exception e) {
            Log.e(TAG, "기본 정보 표시도 실패", e);
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed 호출");
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "CarDetailActivity 종료");
    }
}