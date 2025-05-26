package kr.ac.mjc.ssacar;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
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
    EditText KeywordEt;


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

        KeywordEt = findViewById(R.id.keyword_et);
        KeywordEt.setOnKeyListener(this);
        Button searchButton = findViewById(R.id.search_button);  // ← 이 줄 추가
        searchButton.setOnClickListener(v -> {
            String keyword = KeywordEt.getText().toString().trim();
            if (!keyword.isEmpty()) {
                search(keyword); // 기존 검색 함수 호출
            } else {
                Toast.makeText(this, "검색어를 입력하세요", Toast.LENGTH_SHORT).show();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        int fineLocation = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocation = checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);

        if (fineLocation != PERMISSION_GRANTED | coarseLocation != PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1000);
        }
        //권한 있을 떄
        else {
            setMyLocation();
        }
    }

    //위치 권한이 있으면 내 위치를 지도에 표시
    public void setMyLocation() {
        LocationManager locationManager = getSystemService(LocationManager.class);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            String Keyword = KeywordEt.getText().toString();
            search(Keyword);

        }
        return false;
    }

    public void search(String keyword) {
        LocationManager locationManager = getSystemService(LocationManager.class);
        Location location = null;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        String url = "https://dapi.kakao.com/v2/local/search/keyword.json?query=" + keyword;
        if (location != null) {
            url += "&x=" + location.getLongitude() + "&y=" + location.getLatitude();
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
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json = response.body().string();
                ResponseDto responseDto = new Gson().fromJson(json, ResponseDto.class);

                if (!responseDto.getDocuments().isEmpty()) {
                    // 첫 번째 검색 결과 위치로 주차장 검색
                    LocationDto first = responseDto.getDocuments().get(0);
                    searchParking(first.getX(), first.getY());
                }
            }
        });
    }

    public void drawMarker(List<LocationDto> locationList) {
        for (LocationDto location : locationList) {
            mGoogleMap.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(location.getY(), location.getX()))
                            .title(location.getPlace_name())
            );
        }
    }

    public void searchParking(double x, double y) {
        String url = "https://dapi.kakao.com/v2/local/search/keyword.json?query=주차장&x=" + x + "&y=" + y + "&radius=1000";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("Authorization", "KakaoAK [당신의_API_키]")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json = response.body().string();
                ResponseDto responseDto = new Gson().fromJson(json, ResponseDto.class);

                runOnUiThread(() -> {
                    mGoogleMap.clear(); // 기존 마커 제거
                    drawMarker(responseDto.getDocuments());
                });
            }
        });
    }
}


