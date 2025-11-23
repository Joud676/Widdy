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

        boolean hasDetails = false;

        if (gift.getName() != null && !gift.getName().isEmpty()) {
            holder.giftName.setText(gift.getName());
        } else {
            holder.giftName.setText("هدية");
        }

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

        if (gift.getExpectedPrice() != null && !gift.getExpectedPrice().isEmpty()) {
            holder.giftPrice.setText(gift.getExpectedPrice() + " ريال");
            holder.giftPrice.setVisibility(View.VISIBLE);
        } else {
            holder.giftPrice.setVisibility(View.GONE);
        }

        if (gift.getDescription() != null && !gift.getDescription().isEmpty()) {
            holder.giftDescription.setText(gift.getDescription());
            holder.giftDescription.setVisibility(View.VISIBLE);
        } else {
            holder.giftDescription.setVisibility(View.GONE);
        }

        if (gift.getStoreLocation() != null && !gift.getStoreLocation().isEmpty()) {
            holder.giftStore.setText(gift.getStoreLocation());
            holder.storeLayout.setVisibility(View.VISIBLE);
            hasDetails = true;
        } else {
            holder.storeLayout.setVisibility(View.GONE);
        }

        if (gift.getProductLink() != null && !gift.getProductLink().isEmpty()) {
            holder.giftLink.setText("عرض المنتج");
            holder.linkLayout.setVisibility(View.VISIBLE);
            hasDetails = true;

            holder.linkLayout.setOnClickListener(v -> {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(gift.getProductLink()));
                    holder.itemView.getContext().startActivity(browserIntent);
                } catch (Exception e) {
                    android.widget.Toast.makeText(
                            holder.itemView.getContext(),
                            "خطأ في فتح الرابط",
                            android.widget.Toast.LENGTH_SHORT
                    ).show();
                }
            });
        } else {
            holder.linkLayout.setVisibility(View.GONE);
        }

        if (hasDetails) {
            holder.detailsSection.setVisibility(View.VISIBLE);
            holder.divider.setVisibility(View.VISIBLE);
        } else {
            holder.detailsSection.setVisibility(View.GONE);
            holder.divider.setVisibility(View.GONE);
        }

        if (gift.getPriority() != null && !gift.getPriority().isEmpty()) {
            String priority = gift.getPriority();
            if (priority.equals("عالية") || priority.equals("مهم") || priority.equals("عالي")) {
                holder.priorityBadge.setVisibility(View.VISIBLE);
            } else {
                holder.priorityBadge.setVisibility(View.GONE);
            }
        } else {
            holder.priorityBadge.setVisibility(View.GONE);
        }

        if (gift.isReserved()) {
            holder.reserveButton.setText("محجوزة ✓");
            holder.reserveButton.setEnabled(false);
            holder.reserveButton.setAlpha(0.6f);
            holder.reserveButton.setBackgroundTintList(
                    holder.itemView.getContext().getColorStateList(R.color.grey)
            );

            holder.reservedOverlay.setVisibility(View.VISIBLE);
            holder.reservedBadge.setVisibility(View.VISIBLE);
        } else {
            String priority = gift.getPriority();
            if (priority != null && (priority.equals("عالية") || priority.equals("عالي"))) {
                holder.reserveButton.setText("احجز - أولوية عالية");
                holder.reserveButton.setBackgroundTintList(
                        holder.itemView.getContext().getColorStateList(R.color.pink)
                );
            } else if (priority != null && priority.equals("متوسطة")) {
                holder.reserveButton.setText("احجز - أولوية متوسطة");
                holder.reserveButton.setBackgroundTintList(
                        holder.itemView.getContext().getColorStateList(R.color.pink2)
                );
            } else if (priority != null && priority.equals("منخفضة")) {
                holder.reserveButton.setText("احجز الهدية");
                holder.reserveButton.setBackgroundTintList(
                        holder.itemView.getContext().getColorStateList(R.color.babyPurple)
                );
            } else {
                holder.reserveButton.setText("احجز الهدية");
                holder.reserveButton.setBackgroundTintList(
                        holder.itemView.getContext().getColorStateList(R.color.pink2)
                );
            }

            holder.reserveButton.setEnabled(true);
            holder.reserveButton.setAlpha(1.0f);

            holder.reservedOverlay.setVisibility(View.GONE);
            holder.reservedBadge.setVisibility(View.GONE);
        }

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
        View reservedOverlay, divider;
        LinearLayout reservedBadge, storeLayout, linkLayout, detailsSection;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            giftImage = itemView.findViewById(R.id.giftImage);

            giftName = itemView.findViewById(R.id.giftName);
            giftPrice = itemView.findViewById(R.id.giftPrice);
            giftDescription = itemView.findViewById(R.id.giftDescription);
            giftStore = itemView.findViewById(R.id.giftStore);
            giftLink = itemView.findViewById(R.id.giftLink);
            priorityBadge = itemView.findViewById(R.id.priorityBadge);

            reserveButton = itemView.findViewById(R.id.reserveButton);
            reservedOverlay = itemView.findViewById(R.id.reservedOverlay);
            reservedBadge = itemView.findViewById(R.id.reservedBadge);
            divider = itemView.findViewById(R.id.divider);

            storeLayout = itemView.findViewById(R.id.storeLayout);
            linkLayout = itemView.findViewById(R.id.linkLayout);
            detailsSection = itemView.findViewById(R.id.detailsSection);
        }
    }
}