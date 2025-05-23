package kr.ac.mjc.ssacar;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class CarDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_detail);

        TextView name = findViewById(R.id.detailCarName);
        TextView price = findViewById(R.id.detailCarPrice);
        ImageView image = findViewById(R.id.detailCarImage);

        // 인텐트로 데이터 받기
        String carName = getIntent().getStringExtra("car_name");
        String carPrice = getIntent().getStringExtra("car_price");
        int imageResId = getIntent().getIntExtra("car_image", R.drawable.sample_car);

        name.setText(carName);
        price.setText(carPrice);
        image.setImageResource(imageResId);
    }
}
