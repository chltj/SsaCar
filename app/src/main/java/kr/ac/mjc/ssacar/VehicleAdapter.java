package kr.ac.mjc.ssacar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.ViewHolder> {

    private List<Vehicle> vehicleList;
    private OnVehicleClickListener clickListener;

    public interface OnVehicleClickListener {
        void onVehicleClick(Vehicle vehicle);
    }

    public VehicleAdapter(List<Vehicle> vehicleList, OnVehicleClickListener clickListener) {
        this.vehicleList = vehicleList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehicle, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Vehicle vehicle = vehicleList.get(position);

        // 차량 정보 설정
        holder.carNameText.setText(vehicle.getName());
        holder.carPriceText.setText(vehicle.getPrice());
        holder.carTypeText.setText(vehicle.getEngineType());
        holder.carEfficiencyText.setText(vehicle.getFuelEfficiency());

        // 이미지 로딩 (더 안전한 방법)
        String imageUrl = vehicle.getImageUrl();
        android.util.Log.d("VehicleAdapter", "이미지 로딩 시도: " + imageUrl);

        if (imageUrl != null && !imageUrl.isEmpty() && imageUrl.startsWith("http")) {
            // Glide로 이미지 로딩
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery) // 로딩 중
                    .error(android.R.drawable.ic_dialog_alert) // 로딩 실패
                    .timeout(10000) // 10초 타임아웃
                    .into(holder.carImageView);

            android.util.Log.d("VehicleAdapter", "✅ Glide로 이미지 로딩 시작");
        } else {
            // 기본 이미지 사용
            holder.carImageView.setImageResource(android.R.drawable.ic_menu_gallery);
            android.util.Log.d("VehicleAdapter", "❌ 기본 이미지 사용: " + imageUrl);
        }

        // 클릭 이벤트
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onVehicleClick(vehicle);
            }
        });
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    public void updateData(List<Vehicle> newVehicleList) {
        this.vehicleList = newVehicleList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView carImageView;       // 차량 이미지
        TextView carNameText;         // 차량명
        TextView carPriceText;        // 가격
        TextView carTypeText;         // 엔진 타입
        TextView carEfficiencyText;   // 연비

        ViewHolder(View itemView) {
            super(itemView);

            // XML의 ID와 매칭
            carImageView = itemView.findViewById(R.id.car_image);
            carNameText = itemView.findViewById(R.id.car_name);
            carPriceText = itemView.findViewById(R.id.car_price);
            carTypeText = itemView.findViewById(R.id.car_type);
            carEfficiencyText = itemView.findViewById(R.id.car_efficiency);
        }
    }
}