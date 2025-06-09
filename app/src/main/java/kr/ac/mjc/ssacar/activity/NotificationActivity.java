package kr.ac.mjc.ssacar.activity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kr.ac.mjc.ssacar.NotificationItem;
import kr.ac.mjc.ssacar.R;

public class NotificationActivity extends AppCompatActivity {

    ListView listView;
    List<NotificationItem> notificationList;
    ArrayAdapter<String> adapter;
    List<String> displayList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        listView = findViewById(R.id.notification_list_view);
        displayList = new ArrayList<>();

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        loadNotifications();
    }

    private void loadNotifications() {
        SharedPreferences prefs = getSharedPreferences("notification_storage", MODE_PRIVATE);
        String json = prefs.getString("notifications", null);
        Gson gson = new Gson();

        Type type = new TypeToken<List<NotificationItem>>() {}.getType();
        notificationList = json != null ? gson.fromJson(json, type) : new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (NotificationItem item : notificationList) {
            displayList.add(item.getTitle() + "\n" + item.getMessage() + "\n" + sdf.format(new Date(item.getTimestamp())));
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        listView.setAdapter(adapter);
    }
}
