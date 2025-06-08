package kr.ac.mjc.ssacar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.CardViewHolder> {

    private Context context;
    private List<PaymentCard> cardList;
    private OnCardClickListener listener;

    public interface OnCardClickListener {
        void onCardClick(PaymentCard card, int position);
        void onCardDelete(PaymentCard card, int position);
        void onCardSetDefault(PaymentCard card, int position);
    }

    public CardListAdapter(Context context, List<PaymentCard> cardList, OnCardClickListener listener) {
        this.context = context;
        this.cardList = cardList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_payment_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        PaymentCard card = cardList.get(position);

        try {
            // 카드 타입 아이콘 설정
            holder.ivCardIcon.setImageResource(card.getCardIconResource());

            // 카드 타입과 번호
            holder.tvCardType.setText(card.getCardType());
            holder.tvCardNumber.setText(card.getMaskedCardNumber());

            // 카드 소유자명
            holder.tvCardholderName.setText(card.getCardholderName());

            // 만료일
            holder.tvExpiryDate.setText("만료: " + card.getExpiryDate());

            // 기본 카드 표시
            if (card.isDefault()) {
                holder.tvDefaultBadge.setVisibility(View.VISIBLE);
                holder.tvDefaultBadge.setText("기본 카드");
                // 기본 카드는 다른 배경색으로 표시
                holder.itemView.setBackgroundResource(R.drawable.card_item_default_background);
            } else {
                holder.tvDefaultBadge.setVisibility(View.GONE);
                holder.itemView.setBackgroundResource(R.drawable.card_item_background);
            }

            // 만료된 카드 표시
            if (card.isExpired()) {
                holder.tvExpiryDate.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                holder.tvExpiryDate.append(" (만료됨)");
            } else {
                holder.tvExpiryDate.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            }

            // 클릭 리스너 설정
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCardClick(card, position);
                }
            });

            // 롱클릭 리스너 (옵션 메뉴)
            holder.itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onCardClick(card, position);
                }
                return true;
            });

            // 더보기 버튼 클릭
            holder.ivMore.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCardClick(card, position);
                }
            });

        } catch (Exception e) {
            // 오류 발생 시 기본값 설정
            holder.tvCardType.setText("카드");
            holder.tvCardNumber.setText("**** **** **** ****");
            holder.tvCardholderName.setText("카드 소유자");
            holder.tvExpiryDate.setText("만료: --/--");
            holder.tvDefaultBadge.setVisibility(View.GONE);
            holder.ivCardIcon.setImageResource(R.drawable.ic_card_default);
        }
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    // 데이터 업데이트 메서드
    public void updateCardList(List<PaymentCard> newCardList) {
        this.cardList = newCardList;
        notifyDataSetChanged();
    }

    // 카드 추가
    public void addCard(PaymentCard card) {
        cardList.add(card);
        notifyItemInserted(cardList.size() - 1);
    }

    // 카드 제거
    public void removeCard(int position) {
        if (position >= 0 && position < cardList.size()) {
            cardList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCardIcon;
        ImageView ivMore;
        TextView tvCardType;
        TextView tvCardNumber;
        TextView tvCardholderName;
        TextView tvExpiryDate;
        TextView tvDefaultBadge;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);

            try {
                ivCardIcon = itemView.findViewById(R.id.iv_card_icon);
                ivMore = itemView.findViewById(R.id.iv_more);
                tvCardType = itemView.findViewById(R.id.tv_card_type);
                tvCardNumber = itemView.findViewById(R.id.tv_card_number);
                tvCardholderName = itemView.findViewById(R.id.tv_cardholder_name);
                tvExpiryDate = itemView.findViewById(R.id.tv_expiry_date);
                tvDefaultBadge = itemView.findViewById(R.id.tv_default_badge);
            } catch (Exception e) {
                // findViewById 실패 시 로그 출력 (실제 앱에서는 적절한 오류 처리)
                android.util.Log.e("CardViewHolder", "뷰 초기화 실패", e);
            }
        }
    }
}