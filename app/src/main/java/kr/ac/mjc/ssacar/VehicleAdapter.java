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

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {
    private static final String TAG = "VehicleAdapter";

    private Context context;
    private List<Car> carList;
    private OnVehicleClickListener onVehicleClickListener;
    private int selectedPosition = -1;

    public interface OnVehicleClickListener {
        void onVehicleClick(Car car);
    }

    public VehicleAdapter(Context context, List<Car> carList) {
        this.context = context;
        this.carList = carList;
    }

    public VehicleAdapter(Context context, List<Car> carList, OnVehicleClickListener listener) {
        this.context = context;
        this.carList = carList;
        this.onVehicleClickListener = listener;
    }

    public void setOnVehicleClickListener(OnVehicleClickListener listener) {
        this.onVehicleClickListener = listener;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vehicle, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        final int currentPosition = position;
        Car car = carList.get(position);

        try {
            // 🔧 올바른 ID로 수정
            if (holder.carName != null) {
                holder.carName.setText(car.getName());
            } else {
                Log.e(TAG, "carName is null at position " + position);
            }

            if (holder.carPrice != null) {
                holder.carPrice.setText(car.getPrice());
            } else {
                Log.e(TAG, "carPrice is null at position " + position);
            }

            if (holder.carType != null) {
                holder.carType.setText(car.getEngineType());
            } else {
                Log.e(TAG, "carType is null at position " + position);
            }

            // 이미지 설정
            if (holder.carImage != null) {
                if (car.hasOnlineImage()) {
                    Glide.with(context)
                            .load(car.getImageUrl())
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_dialog_alert)
                            .into(holder.carImage);
                    Log.d(TAG, "온라인 이미지 로딩: " + car.getName() + " -> " + car.getImageUrl());
                } else if (car.getImageResId() != 0) {
                    holder.carImage.setImageResource(car.getImageResId());
                    Log.d(TAG, "로컬 이미지 사용: " + car.getName());
                } else {
                    holder.carImage.setImageResource(android.R.drawable.ic_menu_gallery);
                    Log.d(TAG, "기본 이미지 사용: " + car.getName());
                }
            } else {
                Log.e(TAG, "carImage is null at position " + position);
            }

            // 선택 상태 표시
            if (position == selectedPosition) {
                holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
                holder.itemView.setAlpha(0.9f);
            } else {
                holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.white));
                holder.itemView.setAlpha(1.0f);
            }

            // 클릭 이벤트
            holder.itemView.setOnClickListener(v -> {
                int clickPosition = holder.getAdapterPosition();
                if (clickPosition == RecyclerView.NO_POSITION) return;

                Log.d(TAG, "차량 클릭: " + car.getName());

                // 선택 상태 업데이트
                int previousPosition = selectedPosition;
                selectedPosition = clickPosition;

                // UI 업데이트
                if (previousPosition != -1) {
                    notifyItemChanged(previousPosition);
                }
                notifyItemChanged(selectedPosition);

                // 클릭 리스너 호출
                if (onVehicleClickListener != null) {
                    onVehicleClickListener.onVehicleClick(car);
                } else {
                    // 기본 동작: CarDetailActivity로 이동
                    Intent intent = new Intent(context, CarDetailActivity.class);
                    intent.putExtra("car_name", car.getName());
                    intent.putExtra("car_price", car.getPrice());
                    intent.putExtra("car_engine_type", car.getEngineType());
                    intent.putExtra("car_image_url", car.getImageUrl());
                    intent.putExtra("car_code", car.getCarCode());
                    intent.putExtra("car_image_res", car.getImageResId());

                    try {
                        context.startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "CarDetailActivity 시작 실패", e);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder 오류 at position " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        return carList != null ? carList.size() : 0;
    }

    public void updateCarList(List<Car> newCarList) {
        if (this.carList != null) {
            this.carList.clear();
            this.carList.addAll(newCarList);
        } else {
            this.carList = newCarList;
        }
        selectedPosition = -1;
        notifyDataSetChanged();
        Log.d(TAG, "차량 목록 업데이트: " + (newCarList != null ? newCarList.size() : 0) + "개");
    }

    public Car getSelectedCar() {
        if (selectedPosition != -1 && carList != null && selectedPosition < carList.size()) {
            return carList.get(selectedPosition);
        }
        return null;
    }

    public void clearSelection() {
        int previousPosition = selectedPosition;
        selectedPosition = -1;
        if (previousPosition != -1) {
            notifyItemChanged(previousPosition);
        }
    }

    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;

        if (previousPosition != -1) {
            notifyItemChanged(previousPosition);
        }
        if (selectedPosition != -1) {
            notifyItemChanged(selectedPosition);
        }
    }

    public static class VehicleViewHolder extends RecyclerView.ViewHolder {
        TextView carName, carPrice, carType;
        ImageView carImage;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);

            try {
                // 🔧 item_vehicle.xml의 실제 ID 사용
                carName = itemView.findViewById(R.id.car_name);      // car_name_tv -> car_name
                carPrice = itemView.findViewById(R.id.car_price);    // price_tv -> car_price
                carType = itemView.findViewById(R.id.car_type);      // car_type_tv -> car_type
                carImage = itemView.findViewById(R.id.car_image);    // carImage -> car_image

                // null 체크 로그
                if (carName == null) Log.e("VehicleAdapter", "car_name not found in layout");
                if (carPrice == null) Log.e("VehicleAdapter", "car_price not found in layout");
                if (carType == null) Log.e("VehicleAdapter", "car_type not found in layout");
                if (carImage == null) Log.e("VehicleAdapter", "car_image not found in layout");

            } catch (Exception e) {
                Log.e("VehicleAdapter", "ViewHolder 초기화 오류", e);
            }
        }
    }
}