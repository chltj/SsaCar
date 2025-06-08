package kr.ac.mjc.ssacar.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import kr.ac.mjc.ssacar.R;

public class LongtermActivity extends AppCompatActivity {

    Spinner monthSpinner;
    Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_longterm);

        monthSpinner = findViewById(R.id.month_spinner);
        btnContinue = findViewById(R.id.btn_continue);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.long_term_rent_months_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(adapter);

        btnContinue.setOnClickListener(v -> {
            String selectedMonth = monthSpinner.getSelectedItem().toString();
            Toast.makeText(LongtermActivity.this, selectedMonth + " 선택됨", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(LongtermActivity.this, VehicleListActivity.class);
            intent.putExtra("selected_month", selectedMonth);
            startActivity(intent);
        });
    }

}
