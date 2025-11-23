package com.example.widdy;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.widdy.model.GiftModel;

import java.util.ArrayList;

public class GiftReserveAdapter extends RecyclerView.Adapter<GiftReserveAdapter.ViewHolder> {

    private ArrayList<GiftModel> giftsList;
    private OnReserveClickListener onReserveClickListener;

    public interface OnReserveClickListener {
        void onReserveClick(GiftModel gift);
    }

    public GiftReserveAdapter(ArrayList<GiftModel> giftsList, OnReserveClickListener listener) {
        this.giftsList = giftsList;
        this.onReserveClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_gift_item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GiftModel gift = giftsList.get(position);

        // 1. اسم الهدية
        if (gift.getName() != null && !gift.getName().isEmpty()) {
            holder.giftName.setText(gift.getName());
        } else {
            holder.giftName.setText("هدية");
        }

        // 2. صورة الهدية من Firebase
        if (gift.getImageUrl() != null && !gift.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(gift.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.welcome_widdy)
                    .error(R.drawable.welcome_widdy)
                    .into(holder.giftImage);
        } else {
            holder.giftImage.setImageResource(R.drawable.welcome_widdy);
        }

        // 3. السعر المتوقع
        if (gift.getExpectedPrice() != null && !gift.getExpectedPrice().isEmpty()) {
            holder.giftPrice.setText(gift.getExpectedPrice() + " ر.س");
            holder.priceLayout.setVisibility(View.VISIBLE);
        } else {
            holder.priceLayout.setVisibility(View.GONE);
        }

        // 4. الوصف
        if (gift.getDescription() != null && !gift.getDescription().isEmpty()) {
            holder.giftDescription.setText(gift.getDescription());
            holder.giftDescription.setVisibility(View.VISIBLE);
        } else {
            holder.giftDescription.setVisibility(View.GONE);
        }

        // 5. موقع المتجر
        if (gift.getStoreLocation() != null && !gift.getStoreLocation().isEmpty()) {
            holder.giftStore.setText(gift.getStoreLocation());
            holder.storeLayout.setVisibility(View.VISIBLE);
        } else {
            holder.storeLayout.setVisibility(View.GONE);
        }

        // 6. رابط المنتج
        if (gift.getProductLink() != null && !gift.getProductLink().isEmpty()) {
            holder.giftLink.setText("عرض المنتج");
            holder.linkLayout.setVisibility(View.VISIBLE);

            // فتح الرابط عند الضغط
            holder.linkLayout.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(gift.getProductLink()));
                holder.itemView.getContext().startActivity(browserIntent);
            });
        } else {
            holder.linkLayout.setVisibility(View.GONE);
        }

        // 7. الأولوية
        if (gift.getPriority() != null && !gift.getPriority().isEmpty()) {
            String priority = gift.getPriority();
            if (priority.equals("عالية") || priority.equals("مهم")) {
                holder.priorityBadge.setText("⭐ " + priority);
                holder.priorityBadge.setVisibility(View.VISIBLE);
            } else {
                holder.priorityBadge.setVisibility(View.GONE);
            }
        } else {
            holder.priorityBadge.setVisibility(View.GONE);
        }

        // 8. حالة الحجز
        if (gift.isReserved()) {
            // الهدية محجوزة
            holder.reserveButton.setText("محجوزة ✓");
            holder.reserveButton.setEnabled(false);
            holder.reserveButton.setAlpha(0.6f);
            holder.reserveButton.setBackgroundTintList(
                    holder.itemView.getContext().getColorStateList(R.color.grey)
            );

            // إظهار overlay والشارة
            holder.reservedOverlay.setVisibility(View.VISIBLE);
            holder.reservedBadge.setVisibility(View.VISIBLE);
        } else {
            // الهدية متاحة للحجز
            holder.reserveButton.setText("احجز الهدية 🎁");
            holder.reserveButton.setEnabled(true);
            holder.reserveButton.setAlpha(1.0f);
            holder.reserveButton.setBackgroundTintList(
                    holder.itemView.getContext().getColorStateList(R.color.pink2)
            );

            // إخفاء overlay والشارة
            holder.reservedOverlay.setVisibility(View.GONE);
            holder.reservedBadge.setVisibility(View.GONE);
        }

        // 9. عند الضغط على زر الحجز
        holder.reserveButton.setOnClickListener(v -> {
            if (onReserveClickListener != null) {
                onReserveClickListener.onReserveClick(gift);
            }
        });
    }

    @Override
    public int getItemCount() {
        return giftsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView giftImage;
        TextView giftName, giftPrice, giftDescription, giftStore, giftLink, priorityBadge;
        Button reserveButton;
        View reservedOverlay;
        LinearLayout reservedBadge, priceLayout, storeLayout, linkLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // الصورة
            giftImage = itemView.findViewById(R.id.giftImage);

            // النصوص
            giftName = itemView.findViewById(R.id.giftName);
            giftPrice = itemView.findViewById(R.id.giftPrice);
            giftDescription = itemView.findViewById(R.id.giftDescription);
            giftStore = itemView.findViewById(R.id.giftStore);
            giftLink = itemView.findViewById(R.id.giftLink);
            priorityBadge = itemView.findViewById(R.id.priorityBadge);

            // الأزرار والعناصر
            reserveButton = itemView.findViewById(R.id.reserveButton);
            reservedOverlay = itemView.findViewById(R.id.reservedOverlay);
            reservedBadge = itemView.findViewById(R.id.reservedBadge);

            // الـ Layouts
            priceLayout = itemView.findViewById(R.id.priceLayout);
            storeLayout = itemView.findViewById(R.id.storeLayout);
            linkLayout = itemView.findViewById(R.id.linkLayout);
        }
    }
}