package kr.ac.mjc.ssacar.activity;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import kr.ac.mjc.ssacar.LocationDto;
import kr.ac.mjc.ssacar.R;
import kr.ac.mjc.ssacar.ResponseDto;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PickUpActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, View.OnKeyListener, GoogleMap.OnMarkerClickListener {

    GoogleMap mGoogleMap;
    EditText KeywordEt;
    private ImageView btnBack;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pickup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupBackButton();

        KeywordEt = findViewById(R.id.keyword_et);
        KeywordEt.setOnKeyListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        int fineLocation = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocation = checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);

        if (fineLocation != PERMISSION_GRANTED || coarseLocation != PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1000);
        } else {
            setMyLocation();
        }
    }

    private void setupBackButton() {
        ImageView backButton = findViewById(R.id.back_button);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                finish();
            });
        } else {
            Log.e("PickUpActivity", "Back button not found in layout");
        }
    }

    public void setMyLocation() {
        LocationManager locationManager = getSystemService(LocationManager.class);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 1, this);

        if (location != null) {
            currentLocation = location;

            if (mGoogleMap != null) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                mGoogleMap.setMyLocationEnabled(true);

                // ★ 현재 위치 주변 주차장 자동 검색
                searchNearbyParking(location.getLatitude(), location.getLongitude());
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
        this.mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.584650, 126.925178), 15));

        // 마커 클릭 리스너 설정
        this.mGoogleMap.setOnMarkerClickListener(this);

        setMyLocation();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLocation = location;
        if (mGoogleMap != null) {
            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
            mGoogleMap.setMyLocationEnabled(true);

            // ★ 위치 변경 시 주변 주차장 다시 검색
            searchNearbyParking(location.getLatitude(), location.getLongitude());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        setMyLocation();
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            String keyword = KeywordEt.getText().toString().trim();
            if (!keyword.isEmpty()) {
                searchAddressThenParking(keyword);
            }
        }
        return false;
    }

    // ★ 입력된 주소/건물명을 먼저 검색한 후 그 주변의 주차장을 찾는 메서드
    public void searchAddressThenParking(String keyword) {
        String url = "https://dapi.kakao.com/v2/local/search/keyword.json?query=" + keyword;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("Authorization", "KakaoAK bd20c86bc2ff5b79ee72828d0da95ca3")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(PickUpActivity.this, "검색에 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json = response.body().string();
                ResponseDto responseDto = new Gson().fromJson(json, ResponseDto.class);

                runOnUiThread(() -> {
                    List<LocationDto> locations = responseDto.getDocuments();
                    if (locations != null && !locations.isEmpty()) {
                        // 첫 번째 검색 결과의 위치로 이동
                        LocationDto firstLocation = locations.get(0);
                        double lat = firstLocation.getY();
                        double lng = firstLocation.getX();

                        // 지도 이동
                        LatLng searchLocation = new LatLng(lat, lng);
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(searchLocation, 15));

                        // 기존 마커들 지우기
                        mGoogleMap.clear();

                        // 검색된 위치 주변의 주차장 검색
                        searchNearbyParking(lat, lng);

                        Toast.makeText(PickUpActivity.this,
                                firstLocation.getPlace_name() + " 주변 주차장을 찾고 있습니다.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PickUpActivity.this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // ★ 특정 위치 주변의 주차장만 검색하는 메서드
    public void searchNearbyParking(double latitude, double longitude) {
        // 하나의 키워드로 통합 검색
        searchParkingByKeyword("주차장", latitude, longitude);
    }

    private void searchParkingByKeyword(String keyword, double latitude, double longitude) {
        String url = "https://dapi.kakao.com/v2/local/search/keyword.json?query=" + keyword +
                "&y=" + latitude + "&x=" + longitude + "&radius=2000"; // 2km 반경

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("Authorization", "KakaoAK bd20c86bc2ff5b79ee72828d0da95ca3")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("PickUpActivity", "주차장 검색 실패: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json = response.body().string();
                ResponseDto responseDto = new Gson().fromJson(json, ResponseDto.class);

                runOnUiThread(() -> {
                    drawParkingMarkers(responseDto.getDocuments());
                });
            }
        });
    }

    // ★ 주차장 마커만 그리는 메서드 (중복 제거)
    public void drawParkingMarkers(List<LocationDto> locationList) {
        if (locationList == null) return;

        for (LocationDto location : locationList) {
            String placeName = location.getPlace_name();

            // 주차장 관련 장소만 필터링
            if (isParkingRelated(placeName)) {
                mGoogleMap.addMarker(
                        new MarkerOptions()
                                .position(new LatLng(location.getY(), location.getX()))
                                .title(placeName)
                                .snippet("주차장 • " + location.getAddress_name())
                );
            }
        }
    }

    // ★ 주차장 관련 장소인지 확인하는 메서드
    private boolean isParkingRelated(String placeName) {
        if (placeName == null) return false;

        String[] parkingWords = {
                "주차장", "주차타워", "공영주차장", "민영주차장",
                "지하주차장", "노상주차장", "공용주차장", "유료주차장",
                "무료주차장", "타임주차장", "파킹", "PARKING"
        };

        String lowerCaseName = placeName.toLowerCase();
        for (String word : parkingWords) {
            if (lowerCaseName.contains(word.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    // 기존 search 메서드는 제거하고 위의 새로운 메서드들 사용

    public void drawMarker(List<LocationDto> locationList) {
        // 이 메서드는 더 이상 사용하지 않음 (drawParkingMarkers로 대체)
        drawParkingMarkers(locationList);
    }

    // 마커 클릭 이벤트 처리
    @Override
    public boolean onMarkerClick(@NonNull com.google.android.gms.maps.model.Marker marker) {
        // 마커 클릭 시 시간 설정 액티비티로 이동
        Intent intent = new Intent(PickUpActivity.this, TimeSettingActivity.class);

        // 선택된 위치 정보를 Intent에 담아서 전달
        intent.putExtra("start_place_name", marker.getTitle());
        intent.putExtra("start_address", marker.getSnippet());
        intent.putExtra("start_latitude", marker.getPosition().latitude);
        intent.putExtra("start_longitude", marker.getPosition().longitude);

        startActivity(intent);

        return true; // 기본 동작(정보창 표시)을 막으려면 true, 허용하려면 false
    }
}