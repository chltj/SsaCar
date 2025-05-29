package kr.ac.mjc.ssacar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CarListAdapter extends RecyclerView.Adapter<CarListAdapter.CarViewHolder> {

    private List<CarInfo> carList;
    private OnCarClickListener listener;

    public interface OnCarClickListener {
        void onCarClick(CarInfo carInfo);
    }

    public CarListAdapter(List<CarInfo> carList, OnCarClickListener listener) {
        this.carList = carList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_car, parent, false);
            return new CarViewHolder(view);
        } catch (Exception e) {
            // XML 파일이 없으면 코드로 뷰 생성
            android.util.Log.e("CarListAdapter", "item_car.xml 로드 실패, 코드로 생성", e);
            return createViewHolderProgrammatically(parent);
        }
    }

    private CarViewHolder createViewHolderProgrammatically(ViewGroup parent) {
        // 코드로 아이템 뷰 생성
        android.widget.LinearLayout layout = new android.widget.LinearLayout(parent.getContext());
        layout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        layout.setPadding(16, 16, 16, 16);
        layout.setBackgroundColor(0xFFF5F5F5);

        ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(8, 8, 8, 8);
        layout.setLayoutParams(layoutParams);

        // 자동차 이모지
        TextView emojiTv = new TextView(parent.getContext());
        emojiTv.setText("🚗");
        emojiTv.setTextSize(24);
        emojiTv.setGravity(android.view.Gravity.CENTER);
        android.widget.LinearLayout.LayoutParams emojiParams = new android.widget.LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        );
        emojiTv.setLayoutParams(emojiParams);
        layout.addView(emojiTv);

        // 텍스트 컨테이너
        android.widget.LinearLayout textContainer = new android.widget.LinearLayout(parent.getContext());
        textContainer.setOrientation(android.widget.LinearLayout.VERTICAL);
        android.widget.LinearLayout.LayoutParams textParams = new android.widget.LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 4f
        );
        textParams.setMarginStart(16);
        textContainer.setLayoutParams(textParams);

        // 차량명
        TextView carNameTv = new TextView(parent.getContext());
        carNameTv.setId(R.id.car_name_tv != 0 ? R.id.car_name_tv : android.view.View.generateViewId());
        carNameTv.setTextSize(18);
        carNameTv.setTypeface(null, android.graphics.Typeface.BOLD);
        textContainer.addView(carNameTv);

        // 차량 타입
        TextView carTypeTv = new TextView(parent.getContext());
        carTypeTv.setId(R.id.car_type_tv != 0 ? R.id.car_type_tv : android.view.View.generateViewId());
        carTypeTv.setTextSize(14);
        carTypeTv.setTextColor(0xFF666666);
        textContainer.addView(carTypeTv);

        // 가격
        TextView priceTv = new TextView(parent.getContext());
        priceTv.setId(R.id.price_tv != 0 ? R.id.price_tv : android.view.View.generateViewId());
        priceTv.setTextSize(16);
        priceTv.setTypeface(null, android.graphics.Typeface.BOLD);
        priceTv.setTextColor(0xFF2196F3);
        textContainer.addView(priceTv);

        layout.addView(textContainer);

        return new CarViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        CarInfo carInfo = carList.get(position);

        try {
            holder.carNameTv.setText(carInfo.getCarName());
            holder.carTypeTv.setText(carInfo.getCarType());
            holder.priceTv.setText(carInfo.getPrice());
        } catch (Exception e) {
            android.util.Log.e("CarListAdapter", "onBindViewHolder 에러", e);
            // 에러가 발생하면 간단한 텍스트로 표시
            if (holder.itemView instanceof TextView) {
                ((TextView) holder.itemView).setText(carInfo.getCarName() + " - " + carInfo.getPrice());
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCarClick(carInfo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    static class CarViewHolder extends RecyclerView.ViewHolder {
        TextView carNameTv;
        TextView carTypeTv;
        TextView priceTv;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            try {
                carNameTv = itemView.findViewById(R.id.car_name_tv);
                carTypeTv = itemView.findViewById(R.id.car_type_tv);
                priceTv = itemView.findViewById(R.id.price_tv);
            } catch (Exception e) {
                android.util.Log.e("CarViewHolder", "findViewById 실패", e);
            }
        }
    }
}