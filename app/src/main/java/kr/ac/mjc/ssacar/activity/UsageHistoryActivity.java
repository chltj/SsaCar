package kr.ac.mjc.ssacar.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import kr.ac.mjc.ssacar.R;

public class UsageHistoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_history);

        // 인텐트로부터 데이터 받기
        String carName = getIntent().getStringExtra("carName");
        String carImageUrl = getIntent().getStringExtra("carImageUrl");
        String startTime = getIntent().getStringExtra("startTime");
        String endTime = getIntent().getStringExtra("endTime");
        String pickupLocation = getIntent().getStringExtra("pickupLocation");
        String returnLocation = getIntent().getStringExtra("returnLocation");

        // 뷰 연결
        TextView carNameView = findViewById(R.id.text_car_name);
        TextView pickupView = findViewById(R.id.text_pickup_location);
        TextView returnView = findViewById(R.id.text_return_location);
        ImageView carImageView = findViewById(R.id.image_car);

        // 텍스트 설정
        carNameView.setText(carName != null ? carName : "차량 이름 없음");
        pickupView.setText("대여 장소: " + (pickupLocation != null ? pickupLocation : "없음"));
        returnView.setText("반납 장소: " + (returnLocation != null ? returnLocation : "없음"));

        // 이미지 로딩 (Glide 필요)
        if (carImageUrl != null && !carImageUrl.isEmpty()) {
            Glide.with(this).load(carImageUrl).into(carImageView);
        } else {
            carImageView.setImageResource(R.drawable.sample_car); // 기본 이미지
        }
    }
}
