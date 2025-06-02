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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pickup); // 레이아웃 이름 변경
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
        ImageView backButton = findViewById(R.id.back_button); // ID 확인 필요
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                finish(); // 또는 원하는 동작
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

        if (mGoogleMap != null && location != null) {
            mGoogleMap.moveCamera(
                    CameraUpdateFactory.newLatLng(
                            new LatLng(location.getLatitude(), location.getLongitude())
                    )
            );
            mGoogleMap.setMyLocationEnabled(true);
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
        if (mGoogleMap != null) {
            mGoogleMap.moveCamera(
                    CameraUpdateFactory.newLatLng(
                            new LatLng(location.getLatitude(), location.getLongitude())
                    )
            );
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        setMyLocation();
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            String Keyword = KeywordEt.getText().toString();
            search(Keyword);
        }
        return false;
    }

    public void search(String keyword) {
        // 권한 검사
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

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
                // 에러 처리
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json = response.body().string();
                ResponseDto responseDto = new Gson().fromJson(json, ResponseDto.class);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        drawMarker(responseDto.getDocuments());
                    }
                });
            }
        });
    }

    public void drawMarker(List<LocationDto> locationList) {
        for (LocationDto location : locationList) {
            mGoogleMap.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(location.getY(), location.getX()))
                            .title(location.getPlace_name())
                            .snippet("위치 정보") // 간단한 설명
            );
        }
    }

    // 마커 클릭 이벤트 처리
    @Override
    public boolean onMarkerClick(@NonNull com.google.android.gms.maps.model.Marker marker) {
        // 마커 클릭 시 시간 설정 액티비티로 이동
        Intent intent = new Intent(PickUpActivity.this, TimeSettingActivity.class);

        // 선택된 위치 정보를 Intent에 담아서 전달
        intent.putExtra("place_name", marker.getTitle());
        intent.putExtra("address", marker.getSnippet());
        intent.putExtra("latitude", marker.getPosition().latitude);
        intent.putExtra("longitude", marker.getPosition().longitude);

        startActivity(intent);

        return true; // 기본 동작(정보창 표시)을 막으려면 true, 허용하려면 false
    }
}