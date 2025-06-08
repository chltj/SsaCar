package kr.ac.mjc.ssacar.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import kr.ac.mjc.ssacar.PaymentHistory;
import kr.ac.mjc.ssacar.R;

public class UsageHistoryAdapter extends RecyclerView.Adapter<UsageHistoryAdapter.ViewHolder> {

    private final List<PaymentHistory> historyList;
    private final Context context;

    public UsageHistoryAdapter(List<PaymentHistory> historyList, Context context) {
        this.historyList = historyList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usage_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentHistory item = historyList.get(position);
        holder.carNameTv.setText(item.getCarName());
        holder.engineTypeTv.setText("타입: " + item.getEngineType());
        holder.placeTv.setText(item.getPlaceName() + "\n" + item.getAddress());
        holder.timeTv.setText("출발: " + item.getDepartureTime() + "\n반납: " + item.getArrivalTime());
        holder.priceTv.setText(String.format("총 결제금액: %,d원", item.getTotalPrice()));

        if (item.getImageUrl() != null && item.getImageUrl().startsWith("http")) {
            Glide.with(context).load(item.getImageUrl()).placeholder(R.drawable.sample_car).into(holder.carImage);
        } else {
            holder.carImage.setImageResource(R.drawable.sample_car);
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView carImage;
        TextView carNameTv, engineTypeTv, placeTv, timeTv, priceTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            carImage = itemView.findViewById(R.id.car_image);
            carNameTv = itemView.findViewById(R.id.car_name_tv);
            engineTypeTv = itemView.findViewById(R.id.engine_type_tv);
            placeTv = itemView.findViewById(R.id.place_tv);
            timeTv = itemView.findViewById(R.id.time_tv);
            priceTv = itemView.findViewById(R.id.price_tv);
        }
    }
}
