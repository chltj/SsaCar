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
    private OnCarClickListener onCarClickListener; // ★ 클릭 리스너 추가
    private int selectedPosition = -1; // ★ 선택된 위치 추가

    // ★ 차량 클릭 리스너 인터페이스
    public interface OnCarClickListener {
        void onCarClick(Car car);
    }

    // ★ 기본 생성자 (기존 코드 호환)
    public CarAdapter(Context context, List<Car> carList) {
        this.context = context;
        this.carList = carList;
    }

    // ★ 클릭 리스너 포함 생성자
    public CarAdapter(Context context, List<Car> carList, OnCarClickListener listener) {
        this.context = context;
        this.carList = carList;
        this.onCarClickListener = listener;
    }

    // ★ 클릭 리스너 설정 메서드
    public void setOnCarClickListener(OnCarClickListener listener) {
        this.onCarClickListener = listener;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.new_item_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        final int currentPosition = position; // ★ final 변수로 복사
        Car car = carList.get(position);

        // 차량 정보 설정
        holder.carName.setText(car.getName());
        holder.carPrice.setText(car.getPrice());

        // ★ 선택 상태 표시
        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
            holder.itemView.setAlpha(0.8f);
        } else {
            holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.white));
            holder.itemView.setAlpha(1.0f);
        }

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

        // ★ 클릭 이벤트 수정 (getAdapterPosition 사용)
        holder.itemView.setOnClickListener(v -> {
            int clickPosition = holder.getAdapterPosition(); // ★ 현재 위치 가져오기
            if (clickPosition == RecyclerView.NO_POSITION) return; // ★ 유효하지 않은 위치면 리턴

            Log.d(TAG, "차량 클릭: " + car.getName());

            // ★ 선택 상태 업데이트
            int previousPosition = selectedPosition;
            selectedPosition = clickPosition;

            // ★ UI 업데이트
            if (previousPosition != -1) {
                notifyItemChanged(previousPosition);
            }
            notifyItemChanged(selectedPosition);

            // ★ 클릭 리스너 호출
            if (onCarClickListener != null) {
                onCarClickListener.onCarClick(car);
            } else {
                // ★ 기본 동작: CarDetailActivity로 이동 (기존 동작 유지)
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
            }
        });
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    // ★ 데이터 업데이트 메서드 수정
    public void updateCarList(List<Car> newCarList) {
        this.carList.clear();
        this.carList.addAll(newCarList);
        selectedPosition = -1; // ★ 선택 상태 초기화
        notifyDataSetChanged();
        Log.d(TAG, "차량 목록 업데이트: " + newCarList.size() + "개");
    }

    // ★ 선택된 차량 가져오기
    public Car getSelectedCar() {
        if (selectedPosition != -1 && selectedPosition < carList.size()) {
            return carList.get(selectedPosition);
        }
        return null;
    }

    // ★ 선택 상태 초기화
    public void clearSelection() {
        int previousPosition = selectedPosition;
        selectedPosition = -1;
        if (previousPosition != -1) {
            notifyItemChanged(previousPosition);
        }
    }

    // ★ 특정 위치 선택
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

    public static class CarViewHolder extends RecyclerView.ViewHolder {
        TextView carName, carPrice;
        ImageView carImage;
        ImageView arrowIcon;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            carName = itemView.findViewById(R.id.carName);
            carPrice = itemView.findViewById(R.id.carPrice);
            carImage = itemView.findViewById(R.id.carImage);
            arrowIcon = itemView.findViewById(R.id.ic_arrow_forward);
        }
    }
}