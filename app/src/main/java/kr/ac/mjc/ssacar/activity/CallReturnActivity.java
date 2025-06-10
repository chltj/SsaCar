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

public class CallReturnActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private static final String TAG = "CallReturnActivity";
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

    // TimeSettingActivity에서 전달받은 정보들
    private String departureTime;
    private String arrivalTime;
    private String originalPlaceName;
    private String originalAddress;
    private double originalLatitude;
    private double originalLongitude;

    // 위치 관리자
    LocationManager locationManager;

    // 주소 변환을 위한 Geocoder
    private Geocoder geocoder;
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_return);

        // TimeSettingActivity에서 전달받은 데이터 받기
        receiveIntentData();

        // 초기화
        geocoder = new Geocoder(this, Locale.KOREA);
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

    // TimeSettingActivity에서 전달받은 데이터 받기
    private void receiveIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            departureTime = intent.getStringExtra("departure_time");
            arrivalTime = intent.getStringExtra("arrival_time");
            originalPlaceName = intent.getStringExtra("place_name");
            originalAddress = intent.getStringExtra("address");
            originalLatitude = intent.getDoubleExtra("latitude", 0.0);
            originalLongitude = intent.getDoubleExtra("longitude", 0.0);

            Log.d(TAG, "받은 데이터 - 출발시간: " + departureTime +
                    ", 도착시간: " + arrivalTime +
                    ", 원본 장소: " + originalPlaceName);
        }
    }

    private void initViews() {

        selectedLocationTv = findViewById(R.id.selected_location_tv);
        confirmLocationBtn = findViewById(R.id.confirm_location_btn);
        myLocationBtn = findViewById(R.id.my_location_btn);

        // 이벤트 리스너 설정
        searchInputArea.setOnClickListener(v -> openSearchActivity());
        confirmLocationBtn.setOnClickListener(v -> confirmSelectedLocation());
        myLocationBtn.setOnClickListener(v -> moveToMyLocation());

        // 돌아올 위치 선택용으로 텍스트 변경
        selectedLocationTv.setText("돌아올 위치를 선택하세요\n(차량이 대기할 장소)");
        confirmLocationBtn.setEnabled(false);
        confirmLocationBtn.setText("돌아올 위치를 선택해주세요");
        searchText.setHint("돌아올 위치를 검색하세요");
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
            String placeName = data.getStringExtra("place_name");
            String address = data.getStringExtra("address");
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);

            if (latitude != 0 && longitude != 0) {
                LatLng searchResult = new LatLng(latitude, longitude);
                searchText.setText(placeName != null ? placeName : "선택된 위치");

                if (mGoogleMap != null) {
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchResult, 16));
                    setLocationMarker(searchResult, placeName != null ? placeName : "검색된 위치");
                    updateLocationInfo(searchResult, placeName, address, "");
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

            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGpsEnabled && !isNetworkEnabled) {
                Toast.makeText(this, "위치 서비스를 활성화해 주세요.", Toast.LENGTH_LONG).show();
                return;
            }

            Location location = null;

            if (isGpsEnabled) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 1, this);
            }

            if (location == null && isNetworkEnabled) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 1, this);
            }

            if (mGoogleMap != null) {
                LatLng targetLocation;

                // 원본 위치 정보가 있으면 그 위치로, 없으면 현재 위치 또는 서울로
                if (originalLatitude != 0.0 && originalLongitude != 0.0) {
                    targetLocation = new LatLng(originalLatitude, originalLongitude);
                } else if (location != null) {
                    targetLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    if (!isValidSeoulLocation(targetLocation)) {
                        targetLocation = new LatLng(37.5665, 126.9780); // 서울 시청
                        Toast.makeText(this, "서울 지역으로 위치를 설정했습니다.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    targetLocation = new LatLng(37.5665, 126.9780);
                    Toast.makeText(this, "위치를 가져올 수 없어 서울 시청으로 설정했습니다.", Toast.LENGTH_SHORT).show();
                }

                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLocation, 15));
                mGoogleMap.setMyLocationEnabled(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "위치 설정 중 오류 발생: " + e.getMessage());
            Toast.makeText(this, "위치 설정 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidSeoulLocation(LatLng latLng) {
        double lat = latLng.latitude;
        double lng = latLng.longitude;
        return (lat >= 37.4 && lat <= 37.7) && (lng >= 126.7 && lng <= 127.2);
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

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            if (location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (mGoogleMap != null) {
                LatLng currentLatLng;

                if (location != null) {
                    currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    if (!isValidSeoulLocation(currentLatLng)) {
                        currentLatLng = new LatLng(37.4979, 127.0276); // 서울 강남역
                        Toast.makeText(this, "서울 지역으로 위치를 설정했습니다.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    currentLatLng = new LatLng(37.4979, 127.0276);
                    Toast.makeText(this, "현재 위치를 가져올 수 없어 강남역으로 설정했습니다.", Toast.LENGTH_SHORT).show();
                }

                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));
                setLocationMarker(currentLatLng, "현재 위치");
                convertLatLngToAddress(currentLatLng, "현재 위치");
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
            LatLng seoulCenter = new LatLng(37.5665, 126.9780);
            this.mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoulCenter, 11));

            this.mGoogleMap.setOnMapClickListener(latLng -> {
                setLocationMarker(latLng, "선택된 위치");
                convertLatLngToAddress(latLng, null);
            });

            this.mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(@NonNull Marker marker) {
                    Toast.makeText(CallReturnActivity.this, "마커를 드래그하여 위치를 조정하세요", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onMarkerDrag(@NonNull Marker marker) {
                }

                @Override
                public void onMarkerDragEnd(@NonNull Marker marker) {
                    LatLng newPosition = marker.getPosition();
                    convertLatLngToAddress(newPosition, "드래그로 조정된 위치");
                    searchText.setText("드래그로 조정된 위치");
                    Toast.makeText(CallReturnActivity.this, "위치가 조정되었습니다", Toast.LENGTH_SHORT).show();
                }
            });

            setMyLocation();
        } catch (Exception e) {
            Log.e(TAG, "맵 초기화 중 오류 발생: " + e.getMessage());
            Toast.makeText(this, "지도 초기화 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setLocationMarker(LatLng latLng, String title) {
        try {
            if (currentLocationMarker != null) {
                currentLocationMarker.remove();
            }

            currentLocationMarker = mGoogleMap.addMarker(
                    new MarkerOptions()
                            .position(latLng)
                            .title(title)
                            .snippet("차량이 이곳에서 대기합니다 (드래그 가능)")
                            .draggable(true)
            );

            if (currentLocationMarker != null) {
                currentLocationMarker.showInfoWindow();
            }
        } catch (Exception e) {
            Log.e(TAG, "마커 설정 중 오류 발생: " + e.getMessage());
        }
    }

    private boolean isSeoulArea(LatLng latLng, Address address) {
        try {
            double lat = latLng.latitude;
            double lng = latLng.longitude;
            boolean inSeoulBounds = (lat >= 37.4 && lat <= 37.7) && (lng >= 126.7 && lng <= 127.2);

            if (address != null) {
                String adminArea = address.getAdminArea();
                String locality = address.getLocality();
                String subAdminArea = address.getSubAdminArea();
                String fullAddress = address.getAddressLine(0);

                boolean hasSeoulKeyword = false;
                if ((adminArea != null && adminArea.contains("서울")) ||
                        (locality != null && locality.contains("서울")) ||
                        (subAdminArea != null && subAdminArea.contains("서울")) ||
                        (fullAddress != null && fullAddress.contains("서울"))) {
                    hasSeoulKeyword = true;
                }

                String[] seoulDistricts = {
                        "강남구", "강동구", "강북구", "강서구", "관악구", "광진구", "구로구", "금천구",
                        "노원구", "도봉구", "동대문구", "동작구", "마포구", "서대문구", "서초구", "성동구",
                        "성북구", "송파구", "양천구", "영등포구", "용산구", "은평구", "종로구", "중구", "중랑구"
                };

                boolean hasSeoulDistrict = false;
                for (String district : seoulDistricts) {
                    if ((locality != null && locality.contains(district)) ||
                            (subAdminArea != null && subAdminArea.contains(district)) ||
                            (fullAddress != null && fullAddress.contains(district))) {
                        hasSeoulDistrict = true;
                        break;
                    }
                }

                return hasSeoulKeyword || hasSeoulDistrict || inSeoulBounds;
            }

            return inSeoulBounds;

        } catch (Exception e) {
            Log.e(TAG, "서울 지역 체크 중 오류: " + e.getMessage());
            double lat = latLng.latitude;
            double lng = latLng.longitude;
            return (lat >= 37.4 && lat <= 37.7) && (lng >= 126.7 && lng <= 127.2);
        }
    }

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
            Address addressObj = null;

            try {
                if (geocoder != null && Geocoder.isPresent()) {
                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

                    if (addresses != null && !addresses.isEmpty()) {
                        addressObj = addresses.get(0);

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

            boolean isSeoul = isSeoulArea(latLng, addressObj);

            final String finalAddress = address;
            final String finalDetailedAddress = detailedAddress;

            mainHandler.post(() -> {
                if (!isSeoul) {
                    Toast.makeText(CallReturnActivity.this, "서울 안에서만 차량 호출이 가능합니다.", Toast.LENGTH_LONG).show();
                    selectedLocationTv.setText("🚫 서울 외 지역입니다.\n" + finalAddress);
                    confirmLocationBtn.setEnabled(false);
                    confirmLocationBtn.setText("선택 불가 (서울 외 지역)");
                    if (currentLocationMarker != null) {
                        currentLocationMarker.remove();
                    }
                } else {
                    selectedAddress = finalAddress;
                    searchText.setText(finalAddress);
                    updateLocationInfo(latLng, selectedPlaceName, finalAddress, finalDetailedAddress);
                }
            });
        });
    }

    private void updateLocationInfo(LatLng latLng, String placeName, String address, String detailedAddress) {
        try {
            selectedLatLng = latLng;
            selectedPlaceName = placeName != null ? placeName : "선택된 위치";
            selectedAddress = address != null ? address : "주소 정보 없음";

            StringBuilder displayText = new StringBuilder();
            displayText.append("🚗 차량 대기 위치\n");
            displayText.append(selectedPlaceName);

            if (detailedAddress != null && !detailedAddress.isEmpty()) {
                displayText.append("\n📍 ").append(detailedAddress);
            }

            displayText.append("\n🏠 ").append(selectedAddress);

            selectedLocationTv.setText(displayText.toString());
            confirmLocationBtn.setEnabled(true);
            confirmLocationBtn.setText("이 위치에서 대기");
            confirmLocationBtn.setBackgroundColor(0xFF6366F1);

        } catch (Exception e) {
            Log.e(TAG, "위치 정보 업데이트 중 오류 발생: " + e.getMessage());
            selectedLocationTv.setText("위치 정보를 업데이트할 수 없습니다");
        }
    }

    // 🔧 핵심: VehicleListActivity로 이동
    private void confirmSelectedLocation() {
        if (selectedLatLng != null) {
            try {
                Log.d(TAG, "=== VehicleListActivity로 이동 ===");

                Intent intent = new Intent(CallReturnActivity.this, VehicleListActivity.class);

                // 원본 정보 (출발지)
                intent.putExtra("place_name", originalPlaceName != null ? originalPlaceName : "출발지");
                intent.putExtra("address", originalAddress != null ? originalAddress : "");
                intent.putExtra("latitude", originalLatitude);
                intent.putExtra("longitude", originalLongitude);

                // 시간 정보
                intent.putExtra("departure_time", departureTime != null ? departureTime : "");
                intent.putExtra("arrival_time", arrivalTime != null ? arrivalTime : "");

                // 돌아올 위치 정보
                intent.putExtra("return_place_name", selectedPlaceName);
                intent.putExtra("return_address", selectedAddress);
                intent.putExtra("return_latitude", selectedLatLng.latitude);
                intent.putExtra("return_longitude", selectedLatLng.longitude);

                // 출처 정보
                intent.putExtra("source", "callreturn");

                Log.d(TAG, "전달 데이터:");
                Log.d(TAG, "- 출발지: " + originalPlaceName);
                Log.d(TAG, "- 돌아올 곳: " + selectedPlaceName);
                Log.d(TAG, "- 시간: " + departureTime + " ~ " + arrivalTime);

                startActivity(intent);

            } catch (Exception e) {
                Log.e(TAG, "VehicleListActivity 이동 실패", e);
                Toast.makeText(this, "화면 전환 중 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "돌아올 위치를 먼저 선택해주세요.", Toast.LENGTH_SHORT).show();
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
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (locationManager != null && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(this);
            }

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
            if (locationManager != null && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (isGpsEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 1, this);
                }
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 1, this);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "재시작 중 오류: " + e.getMessage());
        }
    }
}