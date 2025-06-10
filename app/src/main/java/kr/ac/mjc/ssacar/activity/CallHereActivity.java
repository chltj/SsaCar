package kr.ac.mjc.ssacar.activity;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kr.ac.mjc.ssacar.LocationDto;
import kr.ac.mjc.ssacar.R;
import kr.ac.mjc.ssacar.ResponseDto;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CallHereActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, View.OnKeyListener {

    private static final String TAG = "CallHereActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final int SEARCH_ACTIVITY_REQUEST_CODE = 2000;

    GoogleMap mGoogleMap;
    EditText keywordEt;

    TextView selectedLocationTv;
    Button confirmLocationBtn;
    LinearLayout myLocationBtn;

    // 현재 선택된 위치를 나타내는 마커
    Marker currentLocationMarker;
    LatLng selectedLatLng;
    String selectedPlaceName = "";
    String selectedAddress = "";

    // 위치 관리자
    LocationManager locationManager;

    // 주소 변환을 위한 Geocoder
    private Geocoder geocoder;
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_here);

        // 초기화
        geocoder = new Geocoder(this, Locale.getDefault());
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // UI 초기화
        initViews();

        // 맵 프래그먼트 초기화
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // 위치 권한 확인
        checkLocationPermissions();
    }

    private void initViews() {
        keywordEt = findViewById(R.id.keyword_et);
        selectedLocationTv = findViewById(R.id.selected_location_tv);
        confirmLocationBtn = findViewById(R.id.confirm_location_btn);
        myLocationBtn = findViewById(R.id.my_location_btn);
        Button searchBtn = findViewById(R.id.search_btn);

        // 이벤트 리스너 설정
        keywordEt.setOnKeyListener(this);
        confirmLocationBtn.setOnClickListener(v -> confirmSelectedLocation());
        myLocationBtn.setOnClickListener(v -> moveToMyLocation());
        searchBtn.setOnClickListener(v -> {
            String keyword = keywordEt.getText().toString().trim();
            if (!keyword.isEmpty()) {
                searchLocation(keyword);
            } else {
                Toast.makeText(CallHereActivity.this, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        // 초기 상태 설정
        selectedLocationTv.setText("지도를 터치하여 위치를 선택하세요");
        confirmLocationBtn.setEnabled(false);
        confirmLocationBtn.setText("위치를 선택해주세요");
        keywordEt.setHint("건물 혹은 주소를 입력하세요");
    }

    private void checkLocationPermissions() {
        int fineLocation = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocation = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);

        if (fineLocation != PERMISSION_GRANTED || coarseLocation != PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            setMyLocation();
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            String keyword = keywordEt.getText().toString().trim();
            if (!keyword.isEmpty()) {
                searchLocation(keyword);
            }
        }
        return false;
    }

    // 위치 검색 메서드 (지역 제한 제거)
    public void searchLocation(String keyword) {
        Toast.makeText(this, "'" + keyword + "' 검색 중...", Toast.LENGTH_SHORT).show();

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
                    Toast.makeText(CallHereActivity.this, "검색에 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json = response.body().string();
                ResponseDto responseDto = new Gson().fromJson(json, ResponseDto.class);

                runOnUiThread(() -> {
                    List<LocationDto> locations = responseDto.getDocuments();
                    if (locations != null && !locations.isEmpty()) {
                        // 첫 번째 검색 결과 사용
                        LocationDto firstLocation = locations.get(0);
                        double lat = firstLocation.getY();
                        double lng = firstLocation.getX();

                        LatLng searchLocation = new LatLng(lat, lng);

                        // 지도 이동 (지역 제한 제거)
                        if (mGoogleMap != null) {
                            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchLocation, 16));

                            // 마커 설정
                            setLocationMarker(searchLocation, firstLocation.getPlace_name());
                            convertLatLngToAddress(searchLocation, firstLocation.getPlace_name());

                            Toast.makeText(CallHereActivity.this,
                                    "검색 완료: " + firstLocation.getPlace_name(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CallHereActivity.this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void setMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "위치 권한이 없습니다.");
            return;
        }

        try {
            locationManager = getSystemService(LocationManager.class);
            if (locationManager == null) {
                Log.e(TAG, "LocationManager를 가져올 수 없습니다.");
                return;
            }

            // GPS와 네트워크 위치 모두 확인
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGpsEnabled && !isNetworkEnabled) {
                Toast.makeText(this, "위치 서비스를 활성화해 주세요.", Toast.LENGTH_LONG).show();
                return;
            }

            Location location = null;

            // GPS 위치 우선 시도
            if (isGpsEnabled) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
            }

            // GPS로 위치를 못 가져왔으면 네트워크 위치 시도
            if (location == null && isNetworkEnabled) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, this);
            }

            if (mGoogleMap != null) {
                if (location != null) {
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                    Log.d(TAG, "설정된 위치: " + currentLatLng.latitude + ", " + currentLatLng.longitude);
                } else {
                    // 기본 위치 (글로벌 기본 위치로 변경)
                    LatLng defaultLocation = new LatLng(37.5665, 126.9780); // 또는 다른 기본 위치
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 11));
                    Log.d(TAG, "기본 위치로 설정");
                    Toast.makeText(this, "위치를 가져올 수 없어 기본 위치로 설정했습니다.", Toast.LENGTH_SHORT).show();
                }
                mGoogleMap.setMyLocationEnabled(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "위치 설정 중 오류 발생: " + e.getMessage());
            Toast.makeText(this, "위치 설정 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void moveToMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            LocationManager locationManager = getSystemService(LocationManager.class);
            Location location = null;

            // GPS 위치 우선 시도
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            // GPS로 위치를 못 가져왔으면 네트워크 위치 시도
            if (location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (mGoogleMap != null) {
                LatLng currentLatLng;

                if (location != null) {
                    currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                } else {
                    // 위치를 가져올 수 없으면 현재 카메라 위치 사용
                    currentLatLng = mGoogleMap.getCameraPosition().target;
                    Toast.makeText(this, "현재 위치를 가져올 수 없어 지도 중심으로 설정했습니다.", Toast.LENGTH_SHORT).show();
                }

                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));

                // 현재 위치에 마커 설정하고 주소 변환
                setLocationMarker(currentLatLng, "현재 위치");
                convertLatLngToAddress(currentLatLng, "현재 위치");

                Log.d(TAG, "이동된 위치: " + currentLatLng.latitude + ", " + currentLatLng.longitude);
            }
        } catch (Exception e) {
            Log.e(TAG, "내 위치 이동 중 오류: " + e.getMessage());
            Toast.makeText(this, "위치 이동 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.mGoogleMap = googleMap;

        try {
            // 기본 위치 설정 (필요시 다른 기본 위치로 변경 가능)
            LatLng defaultCenter = new LatLng(37.5665, 126.9780);
            this.mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultCenter, 11));

            // 맵 클릭 리스너 - 클릭한 위치에 마커 이동
            this.mGoogleMap.setOnMapClickListener(latLng -> {
                setLocationMarker(latLng, "선택된 위치");
                convertLatLngToAddress(latLng, null);
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
                    convertLatLngToAddress(newPosition, "드래그로 조정된 위치");
                    Toast.makeText(CallHereActivity.this, "위치가 조정되었습니다", Toast.LENGTH_SHORT).show();
                }
            });

            setMyLocation();
        } catch (Exception e) {
            Log.e(TAG, "맵 초기화 중 오류 발생: " + e.getMessage());
            Toast.makeText(this, "지도 초기화 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 위치 마커 설정 메서드
    private void setLocationMarker(LatLng latLng, String title) {
        try {
            // 기존 마커 제거
            if (currentLocationMarker != null) {
                currentLocationMarker.remove();
            }

            // 새 마커 추가 (드래그 가능하게 설정)
            currentLocationMarker = mGoogleMap.addMarker(
                    new MarkerOptions()
                            .position(latLng)
                            .title(title)
                            .snippet("이 위치에서 차량을 호출합니다 (드래그 가능)")
                            .draggable(true)
            );

            // 마커 정보창 표시
            if (currentLocationMarker != null) {
                currentLocationMarker.showInfoWindow();
            }
        } catch (Exception e) {
            Log.e(TAG, "마커 설정 중 오류 발생: " + e.getMessage());
        }
    }

    // 위도/경도를 주소로 변환하는 메서드 (지역 제한 제거)
    private void convertLatLngToAddress(LatLng latLng, String placeName) {
        selectedLatLng = latLng;
        selectedPlaceName = placeName != null ? placeName : "선택된 위치";

        mainHandler.post(() -> {
            selectedLocationTv.setText("주소를 확인하는 중...");
            confirmLocationBtn.setEnabled(false);
            confirmLocationBtn.setText("주소 확인 중...");
        });

        executorService.execute(() -> {
            String address = "주소를 찾을 수 없습니다";
            String detailedAddress = "";

            try {
                if (geocoder != null && Geocoder.isPresent()) {
                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

                    if (addresses != null && !addresses.isEmpty()) {
                        Address addressObj = addresses.get(0);

                        // 주소 구성
                        StringBuilder addressBuilder = new StringBuilder();
                        if (addressObj.getAdminArea() != null) addressBuilder.append(addressObj.getAdminArea()).append(" ");
                        if (addressObj.getSubAdminArea() != null) addressBuilder.append(addressObj.getSubAdminArea()).append(" ");
                        if (addressObj.getLocality() != null) addressBuilder.append(addressObj.getLocality()).append(" ");
                        if (addressObj.getThoroughfare() != null) addressBuilder.append(addressObj.getThoroughfare()).append(" ");
                        if (addressObj.getSubThoroughfare() != null) addressBuilder.append(addressObj.getSubThoroughfare());

                        address = addressBuilder.toString().trim();

                        if (address.isEmpty() && addressObj.getMaxAddressLineIndex() >= 0) {
                            address = addressObj.getAddressLine(0);
                        }

                        if (addressObj.getFeatureName() != null &&
                                !addressObj.getFeatureName().equals(addressObj.getSubThoroughfare())) {
                            detailedAddress = addressObj.getFeatureName();
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "주소 변환 중 IO 오류: " + e.getMessage());
                address = "네트워크 오류로 주소를 가져올 수 없습니다";
            } catch (Exception e) {
                Log.e(TAG, "주소 변환 중 예상치 못한 오류: " + e.getMessage());
                address = "주소 변환 중 오류가 발생했습니다";
            }

            final String finalAddress = address;
            final String finalDetailedAddress = detailedAddress;

            // 지역 제한 제거 - 모든 위치에 대해 허용
            mainHandler.post(() -> {
                selectedAddress = finalAddress;
                keywordEt.setText(finalAddress);
                updateLocationInfo(latLng, selectedPlaceName, finalAddress, finalDetailedAddress);
            });
        });
    }

    // 위치 정보 업데이트 메서드 (주소 포함)
    private void updateLocationInfo(LatLng latLng, String placeName, String address, String detailedAddress) {
        try {
            selectedLatLng = latLng;
            selectedPlaceName = placeName != null ? placeName : "선택된 위치";
            selectedAddress = address != null ? address : "주소 정보 없음";

            // UI 업데이트
            StringBuilder displayText = new StringBuilder();

            // 장소명
            displayText.append(selectedPlaceName);

            // 건물명이나 상세 정보가 있으면 추가
            if (detailedAddress != null && !detailedAddress.isEmpty()) {
                displayText.append("\n📍 ").append(detailedAddress);
            }

            // 주소
            displayText.append("\n🏠 ").append(selectedAddress);

            selectedLocationTv.setText(displayText.toString());
            confirmLocationBtn.setEnabled(true);
            confirmLocationBtn.setText("여기서 차 받기");
            confirmLocationBtn.setBackgroundColor(0xFF6366F1);

        } catch (Exception e) {
            Log.e(TAG, "위치 정보 업데이트 중 오류 발생: " + e.getMessage());
            selectedLocationTv.setText("위치 정보를 업데이트할 수 없습니다");
        }
    }

    // 위치 정보 업데이트 메서드 (오버로드 - 하위 호환성)
    private void updateLocationInfo(LatLng latLng, String placeName, String address) {
        updateLocationInfo(latLng, placeName, address, "");
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(@NonNull Location location) {
        try {
            if (mGoogleMap != null) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mGoogleMap.setMyLocationEnabled(true);
                Log.d(TAG, "위치 업데이트: " + currentLatLng.latitude + ", " + currentLatLng.longitude);
            }
        } catch (Exception e) {
            Log.e(TAG, "위치 변경 처리 중 오류 발생: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setMyLocation();
                Toast.makeText(this, "위치 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // 위치 확정 버튼 클릭 메서드
    private void confirmSelectedLocation() {
        if (selectedLatLng != null) {
            Intent intent = new Intent(CallHereActivity.this, CallReturnActivity.class);
            intent.putExtra("start_place_name", selectedPlaceName);    // 출발지로 전달
            intent.putExtra("start_address", selectedAddress);
            intent.putExtra("start_latitude", selectedLatLng.latitude);
            intent.putExtra("start_longitude", selectedLatLng.longitude);

            Log.d(TAG, "CallReturnActivity로 이동 - 장소: " + selectedPlaceName + ", 주소: " + selectedAddress);
            startActivity(intent);
        } else {
            Toast.makeText(this, "위치를 먼저 선택해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            // 위치 업데이트 중지
            if (locationManager != null && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(this);
            }

            // ExecutorService 종료
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
            }
        } catch (Exception e) {
            Log.e(TAG, "리소스 정리 중 오류: " + e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            // 위치 업데이트 일시 중지
            if (locationManager != null && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(this);
            }
        } catch (Exception e) {
            Log.e(TAG, "일시 중지 중 오류: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            // 위치 업데이트 재시작
            if (locationManager != null && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (isGpsEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
                }
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, this);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "재시작 중 오류: " + e.getMessage());
        }
    }
}