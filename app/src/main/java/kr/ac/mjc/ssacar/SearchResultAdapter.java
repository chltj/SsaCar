package kr.ac.mjc.ssacar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    private List<LocationDto> locations;
    private OnLocationSelectedListener listener;

    public interface OnLocationSelectedListener {
        void onLocationSelected(LocationDto location);
    }

    public SearchResultAdapter(List<LocationDto> locations, OnLocationSelectedListener listener) {
        this.locations = locations;
        this.listener = listener;
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

        holder.placeNameTv.setText(location.getPlace_name());
        holder.addressTv.setText(location.getAddress_name());
        holder.categoryTv.setText(location.getCategory_name());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLocationSelected(location);
            }
        });
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView placeNameTv;
        TextView addressTv;
        TextView categoryTv;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            placeNameTv = itemView.findViewById(R.id.place_name_tv);
            addressTv = itemView.findViewById(R.id.address_tv);
            categoryTv = itemView.findViewById(R.id.category_tv);
        }
    }
}