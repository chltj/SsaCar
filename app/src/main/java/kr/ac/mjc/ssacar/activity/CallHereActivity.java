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
import android.widget.Button;
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kr.ac.mjc.ssacar.R;

public class CallHereActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private static final String TAG = "CallHereActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final int SEARCH_ACTIVITY_REQUEST_CODE = 2000;

    GoogleMap mGoogleMap;
    LinearLayout searchInputArea;
    TextView searchText;
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
        searchInputArea = findViewById(R.id.search_input_area);
        searchText = findViewById(R.id.search_text);
        selectedLocationTv = findViewById(R.id.selected_location_tv);
        confirmLocationBtn = findViewById(R.id.confirm_location_btn);
        myLocationBtn = findViewById(R.id.my_location_btn);

        // 이벤트 리스너 설정
        searchInputArea.setOnClickListener(v -> openSearchActivity());
        confirmLocationBtn.setOnClickListener(v -> confirmSelectedLocation());
        myLocationBtn.setOnClickListener(v -> moveToMyLocation());

        // 초기 상태 설정
        selectedLocationTv.setText("지도를 터치하여 위치를 선택하세요");
        confirmLocationBtn.setEnabled(false);
        confirmLocationBtn.setText("위치를 선택해주세요");
        searchText.setHint("주소나 건물명을 검색하세요");
    }

    // 검색 화면 열기
    private void openSearchActivity() {
        Intent intent = new Intent(this, SearchLocationActivity.class);
        startActivityForResult(intent, SEARCH_ACTIVITY_REQUEST_CODE);
    }

    // 검색 결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SEARCH_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // 검색 결과 받기
            String placeName = data.getStringExtra("place_name");
            String address = data.getStringExtra("address");
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);

            if (latitude != 0 && longitude != 0) {
                LatLng searchResult = new LatLng(latitude, longitude);

                // 검색 텍스트 업데이트
                searchText.setText(placeName != null ? placeName : "선택된 위치");

                // 지도 이동 및 마커 설정
                if (mGoogleMap != null) {
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchResult, 16));
                    setLocationMarker(searchResult, placeName != null ? placeName : "검색된 위치");
                    updateLocationInfo(searchResult, placeName, address);
                }

                Toast.makeText(this, "검색 완료: " + placeName, Toast.LENGTH_SHORT).show();
            }
        }
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

            // GPS가 활성화되어 있는지 확인
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this, "GPS를 활성화해 주세요.", Toast.LENGTH_LONG).show();
                return;
            }

            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 1, this);

            if (mGoogleMap != null && location != null) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                mGoogleMap.setMyLocationEnabled(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "위치 설정 중 오류 발생: " + e.getMessage());
            Toast.makeText(this, "위치 설정 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 내 위치로 이동
    @SuppressLint("MissingPermission")
    private void moveToMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            LocationManager locationManager = getSystemService(LocationManager.class);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null && mGoogleMap != null) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));

                // 현재 위치에 마커 설정하고 주소 변환
                setLocationMarker(currentLatLng, "현재 위치");
                convertLatLngToAddress(currentLatLng, "현재 위치");

                Toast.makeText(this, "현재 위치로 이동했습니다", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "현재 위치를 가져올 수 없습니다", Toast.LENGTH_SHORT).show();
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
            // 기본 위치 (명지전문대)
            LatLng defaultLocation = new LatLng(37.584650, 126.925178);
            this.mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15));

            // 맵 클릭 리스너 - 클릭한 위치에 마커 이동
            this.mGoogleMap.setOnMapClickListener(latLng -> {
                setLocationMarker(latLng, "선택된 위치");
                convertLatLngToAddress(latLng, "터치로 선택된 위치");

                // 검색 텍스트도 업데이트
                searchText.setText("터치로 선택된 위치");
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
                    searchText.setText("드래그로 조정된 위치");
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
                            .draggable(true) // 드래그 가능하게 설정
            );

            // 마커 정보창 표시
            if (currentLocationMarker != null) {
                currentLocationMarker.showInfoWindow();
            }
        } catch (Exception e) {
            Log.e(TAG, "마커 설정 중 오류 발생: " + e.getMessage());
        }
    }

    // 위도/경도를 주소로 변환하는 메서드
    private void convertLatLngToAddress(LatLng latLng, String placeName) {
        selectedLatLng = latLng;
        selectedPlaceName = placeName != null ? placeName : "선택된 위치";

        // 로딩 상태 표시
        mainHandler.post(() -> {
            selectedLocationTv.setText("주소를 확인하는 중...");
            confirmLocationBtn.setEnabled(false);
            confirmLocationBtn.setText("주소 확인 중...");
        });

        // 백그라운드에서 주소 변환 실행
        executorService.execute(() -> {
            String address = "주소를 찾을 수 없습니다";
            String detailedAddress = "";

            try {
                if (geocoder != null && Geocoder.isPresent()) {
                    List<Address> addresses = geocoder.getFromLocation(
                            latLng.latitude, latLng.longitude, 1);

                    if (addresses != null && !addresses.isEmpty()) {
                        Address addr = addresses.get(0);

                        // 한국 주소 형식으로 구성
                        StringBuilder addressBuilder = new StringBuilder();

                        // 시/도
                        if (addr.getAdminArea() != null) {
                            addressBuilder.append(addr.getAdminArea()).append(" ");
                        }

                        // 시/군/구
                        if (addr.getSubAdminArea() != null) {
                            addressBuilder.append(addr.getSubAdminArea()).append(" ");
                        }

                        // 동/면/읍
                        if (addr.getLocality() != null) {
                            addressBuilder.append(addr.getLocality()).append(" ");
                        }

                        // 상세 주소
                        if (addr.getThoroughfare() != null) {
                            addressBuilder.append(addr.getThoroughfare()).append(" ");
                        }

                        if (addr.getSubThoroughfare() != null) {
                            addressBuilder.append(addr.getSubThoroughfare());
                        }

                        address = addressBuilder.toString().trim();

                        // 만약 위의 방법으로 주소가 제대로 나오지 않으면 getAddressLine 사용
                        if (address.isEmpty() && addr.getMaxAddressLineIndex() >= 0) {
                            address = addr.getAddressLine(0);
                        }

                        // 건물명이나 장소명이 있는 경우
                        if (addr.getFeatureName() != null && !addr.getFeatureName().equals(addr.getSubThoroughfare())) {
                            detailedAddress = addr.getFeatureName();
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "주소 변환 중 오류: " + e.getMessage());
                address = "네트워크 오류로 주소를 가져올 수 없습니다";
            } catch (Exception e) {
                Log.e(TAG, "예상치 못한 오류: " + e.getMessage());
                address = "주소 변환 중 오류가 발생했습니다";
            }

            // 최종 결과를 메인 스레드에서 UI 업데이트
            final String finalAddress = address;
            final String finalDetailedAddress = detailedAddress;

            mainHandler.post(() -> {
                selectedAddress = finalAddress;
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
            confirmLocationBtn.setBackgroundColor(0xFF6366F1); // 보라색 계열

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
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // 위치 확정 버튼 클릭 메서드
    private void confirmSelectedLocation() {
        try {
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
        } catch (Exception e) {
            Log.e(TAG, "위치 확정 중 오류: " + e.getMessage());
            Toast.makeText(this, "위치 확정 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
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
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 1, this);
            }
        } catch (Exception e) {
            Log.e(TAG, "재시작 중 오류: " + e.getMessage());
        }
    }
}