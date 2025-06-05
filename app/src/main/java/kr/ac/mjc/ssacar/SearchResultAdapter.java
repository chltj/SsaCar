package kr.ac.mjc.ssacar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import kr.ac.mjc.ssacar.LocationDto;
import kr.ac.mjc.ssacar.R;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    private List<LocationDto> locations;
    private OnLocationClickListener onLocationClickListener;

    public interface OnLocationClickListener {
        void onLocationClick(LocationDto location);
    }

    public SearchResultAdapter(List<LocationDto> locations, OnLocationClickListener listener) {
        this.locations = locations;
        this.onLocationClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocationDto location = locations.get(position);

        holder.placeNameTextView.setText(location.getPlace_name());
        holder.addressTextView.setText(location.getAddress_name());

        // 카테고리 정보가 있으면 표시
        if (location.getCategory_name() != null && !location.getCategory_name().isEmpty()) {
            holder.categoryTextView.setText(location.getCategory_name());
            holder.categoryTextView.setVisibility(View.VISIBLE);
        } else {
            holder.categoryTextView.setVisibility(View.GONE);
        }

        // 거리 정보가 있으면 표시
        if (location.getDistance() != null && !location.getDistance().isEmpty()) {
            try {
                int distanceMeters = Integer.parseInt(location.getDistance());
                String distanceText;
                if (distanceMeters >= 1000) {
                    distanceText = String.format("%.1fkm", distanceMeters / 1000.0);
                } else {
                    distanceText = distanceMeters + "m";
                }
                holder.distanceTextView.setText(distanceText);
                holder.distanceTextView.setVisibility(View.VISIBLE);
            } catch (NumberFormatException e) {
                holder.distanceTextView.setVisibility(View.GONE);
            }
        } else {
            holder.distanceTextView.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onLocationClickListener != null) {
                onLocationClickListener.onLocationClick(location);
            }
        });
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView placeNameTextView;
        TextView addressTextView;
        TextView categoryTextView;
        TextView distanceTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            placeNameTextView = itemView.findViewById(R.id.place_name_text_view);
            addressTextView = itemView.findViewById(R.id.address_text_view);
            categoryTextView = itemView.findViewById(R.id.category_text_view);
            distanceTextView = itemView.findViewById(R.id.distance_text_view);
        }
    }
}