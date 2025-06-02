package kr.ac.mjc.ssacar.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
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
    private static final String KAKAO_API_KEY = "KakaoAK bd20c86bc2ff5b79ee72828d0da95ca3";

    private EditText searchEditText;
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

    private void initViews() {
        searchEditText = findViewById(R.id.search_edit_text);
        searchResultRecyclerView = findViewById(R.id.search_result_recycler_view);

        // 검색창에 포커스 주고 키보드 표시
        searchEditText.requestFocus();
        searchEditText.setOnKeyListener(this);

        // 뒤로가기 버튼
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        searchResults = new ArrayList<>();
        searchResultAdapter = new SearchResultAdapter(searchResults, this::onLocationSelected);

        searchResultRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchResultRecyclerView.setAdapter(searchResultAdapter);
    }

    // 위치 선택 시 호출되는 메서드
    private void onLocationSelected(LocationDto location) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("place_name", location.getPlace_name());
        resultIntent.putExtra("address", location.getAddress_name());
        resultIntent.putExtra("latitude", location.getY());
        resultIntent.putExtra("longitude", location.getX());

        setResult(RESULT_OK, resultIntent);
        finish();
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

    // 위치 검색 메서드
    private void searchLocation(String keyword) {
        Toast.makeText(this, "'" + keyword + "' 검색 중...", Toast.LENGTH_SHORT).show();

        try {
            String url = "https://dapi.kakao.com/v2/local/search/keyword.json?query=" + keyword;

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .header("Authorization", KAKAO_API_KEY)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "네트워크 요청 실패: " + e.getMessage());
                    runOnUiThread(() -> {
                        Toast.makeText(SearchLocationActivity.this, "검색 실패: 네트워크 오류", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try {
                        if (!response.isSuccessful()) {
                            Log.e(TAG, "HTTP 오류: " + response.code());
                            runOnUiThread(() -> {
                                Toast.makeText(SearchLocationActivity.this,
                                        "검색 실패: HTTP " + response.code(), Toast.LENGTH_SHORT).show();
                            });
                            return;
                        }

                        if (response.body() == null) {
                            Log.e(TAG, "응답 본문이 null입니다.");
                            runOnUiThread(() -> {
                                Toast.makeText(SearchLocationActivity.this,
                                        "검색 실패: 응답 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                            });
                            return;
                        }

                        String json = response.body().string();

                        if (json == null || json.trim().isEmpty()) {
                            Log.e(TAG, "빈 JSON 응답을 받았습니다.");
                            runOnUiThread(() -> {
                                Toast.makeText(SearchLocationActivity.this,
                                        "검색 실패: 빈 응답을 받았습니다.", Toast.LENGTH_SHORT).show();
                            });
                            return;
                        }

                        Log.d(TAG, "API Response: " + json);

                        ResponseDto responseDto = null;
                        try {
                            responseDto = new Gson().fromJson(json, ResponseDto.class);
                        } catch (JsonSyntaxException e) {
                            Log.e(TAG, "JSON 파싱 오류: " + e.getMessage());
                            runOnUiThread(() -> {
                                Toast.makeText(SearchLocationActivity.this,
                                        "검색 결과 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                            });
                            return;
                        }

                        if (responseDto == null) {
                            Log.e(TAG, "ResponseDto가 null입니다.");
                            runOnUiThread(() -> {
                                Toast.makeText(SearchLocationActivity.this,
                                        "검색 결과를 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                            });
                            return;
                        }

                        ResponseDto finalResponseDto = responseDto;
                        runOnUiThread(() -> {
                            try {
                                if (finalResponseDto.getDocuments() != null && !finalResponseDto.getDocuments().isEmpty()) {
                                    // 검색 결과 업데이트
                                    searchResults.clear();
                                    searchResults.addAll(finalResponseDto.getDocuments());
                                    searchResultAdapter.notifyDataSetChanged();

                                    Toast.makeText(SearchLocationActivity.this,
                                            searchResults.size() + "개의 검색 결과를 찾았습니다.", Toast.LENGTH_SHORT).show();
                                } else {
                                    searchResults.clear();
                                    searchResultAdapter.notifyDataSetChanged();
                                    Toast.makeText(SearchLocationActivity.this,
                                            "'" + keyword + "'에 대한 검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "UI 업데이트 중 오류: " + e.getMessage());
                                Toast.makeText(SearchLocationActivity.this,
                                        "검색 결과 표시 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } catch (Exception e) {
                        Log.e(TAG, "전체 응답 처리 오류: " + e.getMessage());
                        runOnUiThread(() -> {
                            Toast.makeText(SearchLocationActivity.this,
                                    "검색 중 예상치 못한 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "검색 요청 생성 중 오류: " + e.getMessage());
            Toast.makeText(this, "검색 요청을 생성할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}