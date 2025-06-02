package kr.ac.mjc.ssacar;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import kr.ac.mjc.ssacar.activity.CarDetailActivity;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {
    private static final String TAG = "CarAdapter";

    private Context context;
    private List<Car> carList;

    public CarAdapter(Context context, List<Car> carList) {
        this.context = context;
        this.carList = carList;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.new_item_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        Car car = carList.get(position);

        // 차량 정보 설정
        holder.carName.setText(car.getName());
        holder.carPrice.setText(car.getPrice());

        // 이미지 설정 (온라인 이미지 우선, 없으면 로컬 이미지)
        if (car.hasOnlineImage()) {
            // 온라인 이미지 사용 (Glide)
            Glide.with(context)
                    .load(car.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_dialog_alert)
                    .into(holder.carImage);
            Log.d(TAG, "온라인 이미지 로딩: " + car.getName() + " -> " + car.getImageUrl());
        } else if (car.getImageResId() != 0) {
            // 로컬 이미지 사용
            holder.carImage.setImageResource(car.getImageResId());
            Log.d(TAG, "로컬 이미지 사용: " + car.getName());
        } else {
            // 기본 이미지 사용
            holder.carImage.setImageResource(android.R.drawable.ic_menu_gallery);
            Log.d(TAG, "기본 이미지 사용: " + car.getName());
        }

        // 클릭 이벤트 - CarDetailActivity로 모든 정보 전달
        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "차량 클릭: " + car.getName());

            Intent intent = new Intent(context, CarDetailActivity.class);
            intent.putExtra("car_name", car.getName());
            intent.putExtra("car_price", car.getPrice());
            intent.putExtra("car_engine_type", car.getEngineType());
            intent.putExtra("car_efficiency", car.getFuelEfficiency());
            intent.putExtra("car_image_url", car.getImageUrl());
            intent.putExtra("car_code", car.getCarCode());
            intent.putExtra("car_image_res", car.getImageResId());

            try {
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "CarDetailActivity 시작 실패", e);
            }
        });
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    // 데이터 업데이트 메서드
    public void updateCarList(List<Car> newCarList) {
        this.carList.clear();
        this.carList.addAll(newCarList);
        notifyDataSetChanged();
        Log.d(TAG, "차량 목록 업데이트: " + newCarList.size() + "개");
    }

    public static class CarViewHolder extends RecyclerView.ViewHolder {
        TextView carName, carPrice;
        ImageView carImage;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            carName = itemView.findViewById(R.id.carName);
            carPrice = itemView.findViewById(R.id.carPrice);
            carImage = itemView.findViewById(R.id.carImage);
        }
    }
}