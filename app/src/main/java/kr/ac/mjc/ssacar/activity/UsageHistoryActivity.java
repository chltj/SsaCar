package kr.ac.mjc.ssacar.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import kr.ac.mjc.ssacar.PaymentHistory;
import kr.ac.mjc.ssacar.R;
import kr.ac.mjc.ssacar.UsageHistoryAdapter;

public class UsageHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UsageHistoryAdapter adapter;
    private List<PaymentHistory> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_history);

        recyclerView = findViewById(R.id.usage_history_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 데이터 불러오기
        historyList = loadHistoryFromPrefs();

        // Adapter 연결
        adapter = new UsageHistoryAdapter(historyList, this);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(UsageHistoryActivity.this, SmartKeyActivity.class);
            intent.putExtra("carName", item.getCarName());
            intent.putExtra("engineType", item.getEngineType());
            intent.putExtra("placeName", item.getPlaceName());
            intent.putExtra("address", item.getAddress());
            intent.putExtra("departureTime", item.getDepartureTime());
            intent.putExtra("arrivalTime", item.getArrivalTime());
            intent.putExtra("imageUrl", item.getImageUrl());
            startActivity(intent);
        });
        findViewById(R.id.button_home).setOnClickListener(v -> {
            Intent intent = new Intent(UsageHistoryActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // 현재 액티비티 종료
        });
    }

    private List<PaymentHistory> loadHistoryFromPrefs() {
        SharedPreferences userPrefs = getSharedPreferences("current_user", MODE_PRIVATE);
        String currentUserId = userPrefs.getString("current_user_id", null);

        if (currentUserId != null) {
            SharedPreferences prefs = getSharedPreferences("payment_history", Context.MODE_PRIVATE);
            String json = prefs.getString("history_list_" + currentUserId, null); // 사용자 ID별 키로 저장

            if (json != null) {
                return new Gson().fromJson(json, new TypeToken<List<PaymentHistory>>(){}.getType());
            }
        }

        return new ArrayList<>();
    }


}
