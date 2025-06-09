package kr.ac.mjc.ssacar.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import kr.ac.mjc.ssacar.R;

public class SmartKeyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_key);

        // 인텐트에서 데이터 받기
        Intent intent = getIntent();
        String carName = intent.getStringExtra("carName");
        String carImageUrl = intent.getStringExtra("carImageUrl");
        String startTime = intent.getStringExtra("startTime");
        String endTime = intent.getStringExtra("endTime");
        String pickupLocation = intent.getStringExtra("pickupLocation");
        String returnLocation = intent.getStringExtra("returnLocation");

        // 차량 이름 및 시간 표시할 TextView (실제 XML ID 사용)
        TextView carNameTv = findViewById(R.id.text_car_name);   // 예: 차량 이름 TextView
        TextView timeTv = findViewById(R.id.text_time);          // 예: 시간 정보 TextView

        if (carName != null) {
            carNameTv.setText(carName);
        }

        if (startTime != null && endTime != null) {
            timeTv.setText("출발: " + startTime + "\n반납: " + endTime);
        }

        // 차량 이미지 표시 (Glide 사용)
        ImageView imageCar = findViewById(R.id.image_car); // 예: 이미지뷰 ID
        if (carImageUrl != null) {
            Glide.with(this)
                    .load(carImageUrl)
                    .placeholder(R.drawable.placeholder) // 기본 이미지 설정
                    .into(imageCar);
        }
    }
}
