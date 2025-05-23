package kr.ac.mjc.ssacar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView notificationIcon, mypageIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView carRecyclerView = findViewById(R.id.carRecyclerView);
        carRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Car> cars = new ArrayList<>();
        cars.add(new Car("현대 아반떼", "₩20,000 / 1시간", R.drawable.sample_car));
        cars.add(new Car("기아 쏘렌토", "₩25,000 / 1시간", R.drawable.sample_car));
        cars.add(new Car("쉐보레 스파크", "₩18,000 / 1시간", R.drawable.sample_car));

        CarAdapter adapter = new CarAdapter(this, cars);
        carRecyclerView.setAdapter(adapter);

        Button btnCallHere, btnPickup, btnOneway, btnLongterm;

        btnCallHere = findViewById(R.id.btn_call_here);
        btnPickup = findViewById(R.id.btn_pickup);
        btnOneway = findViewById(R.id.btn_oneway);
        btnLongterm = findViewById(R.id.btn_longterm);

        btnCallHere.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CallHereActivity.class)));

        btnPickup.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, PickupActivity.class)));

        btnOneway.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, OnewayActivity.class)));

        btnLongterm.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LongtermActivity.class)));

        notificationIcon = findViewById(R.id.notificationIcon);
        mypageIcon = findViewById(R.id.mypageIcon);

        notificationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
                startActivity(intent);
            }
        });

        mypageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MyPageActivity.class);
                startActivity(intent);
            }
        });
    }


}
