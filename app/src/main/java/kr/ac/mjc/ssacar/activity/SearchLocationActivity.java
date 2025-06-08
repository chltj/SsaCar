package kr.ac.mjc.ssacar.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import kr.ac.mjc.ssacar.LocationDto;
import kr.ac.mjc.ssacar.R;
import kr.ac.mjc.ssacar.ResponseDto;
import kr.ac.mjc.ssacar.SearchResultAdapter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchLocationActivity extends AppCompatActivity implements View.OnKeyListener {

    private static final String TAG = "SearchLocationActivity";
    // 🔑 새로운 카카오 REST API 키로 교체하세요
    private static final String KAKAO_API_KEY = "KakaoAK bd20c86bc2ff5b79ee72828d0da95ca3";

    private EditText searchEditText;
    private ImageView searchButton;
    private ImageView backButton;
    private RecyclerView searchResultRecyclerView;
    private SearchResultAdapter searchResultAdapter;
    private List<LocationDto> searchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);

        initViews();
        setupRecyclerView();
    }

    @SuppressLint("WrongViewCast")
    private void initViews() {
        // UI 요소 초기화
        searchEditText = findViewById(R.id.search_edit_text);
        searchResultRecyclerView = findViewById(R.id.search_result_recycler_view);

        // 검색 버튼과 뒤로가기 버튼 찾기 (ID는 레이아웃에 따라 조정)
        if (searchButton == null) {
            // ID가 다를 수 있으니 다른 가능한 ID들로 시도
            searchButton = findViewById(R.id.btn_search);
        }

        backButton = findViewById(R.id.back_button);
        if (backButton == null) {
            backButton = findViewById(R.id.btn_back);
        }

        // 검색창 설정
        searchEditText.requestFocus();
        searchEditText.setOnKeyListener(this);

        // 실시간 검색을 위한 TextWatcher 추가 (선택사항)
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // 3글자 이상일 때 자동 검색 (선택사항)
                String keyword = s.toString().trim();
                if (keyword.length() >= 2) {
                    // 자동 검색을 원하지 않으면 이 부분을 주석 처리
                    // searchLocation(keyword);
                }
            }
        });

        // 검색 버튼 클릭 리스너
        if (searchButton != null) {
            searchButton.setOnClickListener(v -> {
                String keyword = searchEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(keyword)) {
                    searchLocation(keyword);
                } else {
                    Toast.makeText(this, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.w(TAG, "검색 버튼을 찾을 수 없습니다. 레이아웃의 ID를 확인하세요.");
        }

        // 뒤로가기 버튼 클릭 리스너
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                setResult(RESULT_CANCELED);
                finish();
            });
        } else {
            Log.w(TAG, "뒤로가기 버튼을 찾을 수 없습니다. 레이아웃의 ID를 확인하세요.");
        }
    }

    private void setupRecyclerView() {
        searchResults = new ArrayList<>();
        searchResultAdapter = new SearchResultAdapter(searchResults, this::onLocationSelected);

        searchResultRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchResultRecyclerView.setAdapter(searchResultAdapter);
    }

    // 위치 선택 시 호출되는 메서드
    private void onLocationSelected(LocationDto location) {
        Log.d(TAG, "선택된 위치: " + location.getPlace_name() +
                ", 좌표: " + location.getY() + ", " + location.getX());

        try {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("place_name", location.getPlace_name());
            resultIntent.putExtra("address", location.getAddress_name());

            // 안전한 좌표 변환 (String/double 모두 처리)
            double latitude = convertToDouble(location.getY(), 37.5665);
            double longitude = convertToDouble(location.getX(), 126.9780);

            resultIntent.putExtra("latitude", latitude);
            resultIntent.putExtra("longitude", longitude);

            setResult(RESULT_OK, resultIntent);
            finish();

        } catch (Exception e) {
            Log.e(TAG, "위치 선택 처리 중 오류: " + e.getMessage());
            Toast.makeText(this, "위치 선택 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 범용 좌표 변환 메서드 (String 또는 double 모두 처리)
    private double convertToDouble(Object value, double defaultValue) {
        try {
            if (value == null) {
                return defaultValue;
            }

            // toString()으로 문자열 변환 후 파싱 (모든 타입 처리 가능)
            String strValue = value.toString().trim();
            if (strValue.isEmpty()) {
                return defaultValue;
            }

            return Double.parseDouble(strValue);

        } catch (NumberFormatException e) {
            Log.e(TAG, "좌표 변환 오류: " + value);
            return defaultValue;
        } catch (Exception e) {
            Log.e(TAG, "예상치 못한 변환 오류: " + e.getMessage());
            return defaultValue;
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            String keyword = searchEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(keyword)) {
                searchLocation(keyword);
            } else {
                Toast.makeText(this, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    // 위치 검색 메서드 - 완전히 개선된 버전
    private void searchLocation(String keyword) {
        Log.d(TAG, "=== 검색 시작 ===");
        Log.d(TAG, "검색어: " + keyword);
        Log.d(TAG, "API 키: " + KAKAO_API_KEY);

        Toast.makeText(this, "'" + keyword + "' 검색 중...", Toast.LENGTH_SHORT).show();

        try {
            // URL 인코딩
            String encodedKeyword = URLEncoder.encode(keyword, "UTF-8");
            String url = "https://dapi.kakao.com/v2/local/search/keyword.json" +
                    "?query=" + encodedKeyword +
                    "&size=15" +
                    "&page=1";

            Log.d(TAG, "요청 URL: " + url);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Authorization", KAKAO_API_KEY)
                    .build();

            Log.d(TAG, "HTTP 요청 전송 중...");

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "❌ 네트워크 요청 실패: " + e.getMessage(), e);
                    runOnUiThread(() -> {
                        Toast.makeText(SearchLocationActivity.this,
                                "네트워크 오류: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    Log.d(TAG, "✅ HTTP 응답 받음");
                    Log.d(TAG, "응답 코드: " + response.code());
                    Log.d(TAG, "응답 메시지: " + response.message());

                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "No error body";
                        Log.e(TAG, "❌ HTTP 오류 " + response.code() + ": " + errorBody);

                        runOnUiThread(() -> {
                            String errorMsg = "";
                            switch (response.code()) {
                                case 401:
                                    errorMsg = "API 키가 유효하지 않습니다";
                                    break;
                                case 403:
                                    errorMsg = "API 사용 권한이 없습니다";
                                    break;
                                case 429:
                                    errorMsg = "API 호출 한도를 초과했습니다";
                                    break;
                                default:
                                    errorMsg = "HTTP 오류: " + response.code();
                            }
                            Toast.makeText(SearchLocationActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        });
                        return;
                    }

                    String json = response.body().string();
                    Log.d(TAG, "✅ 응답 JSON: " + json);

                    try {
                        ResponseDto responseDto = new Gson().fromJson(json, ResponseDto.class);
                        Log.d(TAG, "✅ JSON 파싱 성공");

                        if (responseDto.getDocuments() != null) {
                            Log.d(TAG, "검색 결과 개수: " + responseDto.getDocuments().size());
                            for (int i = 0; i < responseDto.getDocuments().size() && i < 3; i++) {
                                LocationDto location = responseDto.getDocuments().get(i);
                                Log.d(TAG, "결과 " + i + ": " + location.getPlace_name() +
                                        " (" + location.getX() + ", " + location.getY() + ")");
                            }
                        } else {
                            Log.d(TAG, "documents가 null입니다");
                        }

                        runOnUiThread(() -> {
                            try {
                                if (responseDto.getDocuments() != null && !responseDto.getDocuments().isEmpty()) {
                                    searchResults.clear();
                                    searchResults.addAll(responseDto.getDocuments());
                                    searchResultAdapter.notifyDataSetChanged();

                                    Log.d(TAG, "✅ " + searchResults.size() + "개 결과 표시됨");
                                    Toast.makeText(SearchLocationActivity.this,
                                            "✅ " + searchResults.size() + "개 결과 찾음", Toast.LENGTH_SHORT).show();
                                } else {
                                    searchResults.clear();
                                    searchResultAdapter.notifyDataSetChanged();
                                    Log.d(TAG, "검색 결과 없음");
                                    Toast.makeText(SearchLocationActivity.this,
                                            "'" + keyword + "'에 대한 검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "UI 업데이트 중 오류: " + e.getMessage(), e);
                                Toast.makeText(SearchLocationActivity.this,
                                        "검색 결과 표시 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } catch (JsonSyntaxException e) {
                        Log.e(TAG, "❌ JSON 파싱 오류: " + e.getMessage(), e);
                        Log.e(TAG, "파싱 실패한 JSON: " + json);
                        runOnUiThread(() -> {
                            Toast.makeText(SearchLocationActivity.this,
                                    "JSON 파싱 오류: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                }
            });

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "❌ URL 인코딩 오류: " + e.getMessage(), e);
            Toast.makeText(this, "검색어 인코딩 오류", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "❌ 예상치 못한 오류: " + e.getMessage(), e);
            Toast.makeText(this, "검색 중 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}