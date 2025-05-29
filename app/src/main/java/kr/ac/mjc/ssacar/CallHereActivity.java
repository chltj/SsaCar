package kr.ac.mjc.ssacar;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CallHereActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, View.OnKeyListener {

    GoogleMap mGoogleMap;
    EditText keywordEt;
    TextView selectedLocationTv;
    Button confirmLocationBtn;

    // 현재 선택된 위치를 나타내는 마커
    Marker currentLocationMarker;
    LatLng selectedLatLng;
    String selectedPlaceName = "";
    String selectedAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_call_here);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // UI 초기화
        initViews();

        // 맵 프래그먼트 초기화
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // 위치 권한 확인
        checkLocationPermissions();
    }

    private void initViews() {
        keywordEt = findViewById(R.id.keyword_et);
        selectedLocationTv = findViewById(R.id.selected_location_tv);
        confirmLocationBtn = findViewById(R.id.confirm_location_btn);

        keywordEt.setOnKeyListener(this);
        confirmLocationBtn.setOnClickListener(v -> confirmSelectedLocation());

        // 초기 상태 설정
        selectedLocationTv.setText("위치를 선택해주세요");
        confirmLocationBtn.setEnabled(false);
        confirmLocationBtn.setText("위치를 선택해주세요");
    }

    private void checkLocationPermissions() {
        int fineLocation = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocation = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);

        if (fineLocation != PERMISSION_GRANTED || coarseLocation != PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1000);
        } else {
            setMyLocation();
        }
    }

    @SuppressLint("MissingPermission")
    public void setMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationManager locationManager = getSystemService(LocationManager.class);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 1, this);

        if (mGoogleMap != null && location != null) {
            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
            mGoogleMap.setMyLocationEnabled(true);

            // 현재 위치에 기본 마커 설정
            setLocationMarker(currentLatLng, "현재 위치");
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.mGoogleMap = googleMap;

        // 기본 위치 (명지전문대)
        LatLng defaultLocation = new LatLng(37.584650, 126.925178);
        this.mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15));

        // 맵 클릭 리스너 - 클릭한 위치에 마커 이동
        this.mGoogleMap.setOnMapClickListener(latLng -> {
            setLocationMarker(latLng, "선택된 위치");
            updateLocationInfo(latLng, "선택된 위치");
        });

        // 마커 드래그 리스너
        this.mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {
                Toast.makeText(CallHereActivity.this, "마커를 드래그하여 위치를 조정하세요", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMarkerDrag(@NonNull Marker marker) {
                // 드래그 중에는 특별한 처리 안함
            }

            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                LatLng newPosition = marker.getPosition();
                updateLocationInfo(newPosition, "드래그로 선택된 위치");
                Toast.makeText(CallHereActivity.this, "위치가 조정되었습니다", Toast.LENGTH_SHORT).show();
            }
        });

        setMyLocation();
    }

    // 위치 마커 설정 메서드
    private void setLocationMarker(LatLng latLng, String title) {
        // 기존 마커 제거
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }

        // 새 마커 추가 (드래그 가능하게 설정)
        currentLocationMarker = mGoogleMap.addMarker(
                new MarkerOptions()
                        .position(latLng)
                        .title(title)
                        .snippet("이 위치에서 차량을 호출합니다")
                        .draggable(true) // 드래그 가능하게 설정
        );
    }

    // 위치 정보 업데이트 메서드 (간단한 버전 - 좌표만 표시)
    private void updateLocationInfo(LatLng latLng, String placeName) {
        selectedLatLng = latLng;
        selectedPlaceName = placeName;
        selectedAddress = String.format("위도: %.4f, 경도: %.4f", latLng.latitude, latLng.longitude);

        // UI 업데이트
        selectedLocationTv.setText(selectedPlaceName + "\n" + selectedAddress);
        confirmLocationBtn.setEnabled(true);
        confirmLocationBtn.setText("이 위치에서 차량 호출");
        confirmLocationBtn.setBackgroundColor(0xFF4CAF50);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (mGoogleMap != null) {
            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setMyLocation();
            }
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            String keyword = keywordEt.getText().toString().trim();
            if (!keyword.isEmpty()) {
                search(keyword);
            }
        }
        return false;
    }

    // 검색 메서드 (간단한 버전)
    public void search(String keyword) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "'" + keyword + "' 검색 중...", Toast.LENGTH_SHORT).show();

        LocationManager locationManager = getSystemService(LocationManager.class);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        String url = "https://dapi.kakao.com/v2/local/search/keyword.json?query=" + keyword;
        if (location != null) {
            url += "&y=" + location.getLatitude() + "&x=" + location.getLongitude();
        }

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
                    Toast.makeText(CallHereActivity.this, "검색 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json = response.body().string();
                ResponseDto responseDto = new Gson().fromJson(json, ResponseDto.class);

                runOnUiThread(() -> {
                    if (responseDto.getDocuments() != null && !responseDto.getDocuments().isEmpty()) {
                        // 첫 번째 검색 결과로 마커 이동
                        LocationDto firstResult = responseDto.getDocuments().get(0);
                        LatLng searchLatLng = new LatLng(firstResult.getY(), firstResult.getX());

                        // 카메라 이동
                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchLatLng, 16));

                        // 마커 설정
                        setLocationMarker(searchLatLng, firstResult.getPlace_name());

                        // 위치 정보 업데이트 (간단한 버전 - 좌표만)
                        updateLocationInfo(searchLatLng, firstResult.getPlace_name());

                        Toast.makeText(CallHereActivity.this, "검색 완료: " + firstResult.getPlace_name(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CallHereActivity.this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // 위치 확정 버튼 클릭 메서드
    private void confirmSelectedLocation() {
        if (selectedLatLng != null) {
            Intent intent = new Intent(CallHereActivity.this, TimeSettingActivity.class);
            intent.putExtra("place_name", selectedPlaceName);
            intent.putExtra("address", selectedAddress);
            intent.putExtra("latitude", selectedLatLng.latitude);
            intent.putExtra("longitude", selectedLatLng.longitude);

            startActivity(intent);
        } else {
            Toast.makeText(this, "위치를 먼저 선택해주세요.", Toast.LENGTH_SHORT).show();
        }
    }
}