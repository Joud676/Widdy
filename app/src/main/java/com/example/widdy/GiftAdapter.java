package com.example.widdy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.widdy.model.GiftModel;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;

public class GiftAdapter extends RecyclerView.Adapter<GiftAdapter.ViewHolder> {

    private ArrayList<GiftModel> giftsList;
    private OnGiftActionListener onEditListener;
    private OnGiftActionListener onDeleteListener;

    public interface OnGiftActionListener {
        void onAction(GiftModel gift);
    }

    public GiftAdapter(ArrayList<GiftModel> giftsList, OnGiftActionListener onEditListener, OnGiftActionListener onDeleteListener) {
        this.giftsList = giftsList;
        this.onEditListener = onEditListener;
        this.onDeleteListener = onDeleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gift_item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GiftModel gift = giftsList.get(position);

        holder.giftName.setText(gift.getName());
        holder.giftPrice.setText(gift.getExpectedPrice() + " ريال");
        holder.giftDescription.setText(gift.getDescription());

        if (gift.getImageUrl() != null && !gift.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(gift.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.ic_gift) //
                    .into(holder.giftImage);
        }

        setPriorityChip(holder.priorityChip, gift.getPriority());

        holder.btnEdit.setOnClickListener(v -> onEditListener.onAction(gift));
        holder.btnDelete.setOnClickListener(v -> onDeleteListener.onAction(gift));
    }

    private void setPriorityChip(Chip chip, String priority) {
        chip.setText(priority);

        switch (priority) {
            case "عالية":
                chip.setChipBackgroundColorResource(R.color.pink);
                break;
            case "متوسطة":
                chip.setChipBackgroundColorResource(R.color.mustard);
                break;
            case "منخفضة":
                chip.setChipBackgroundColorResource(R.color.aqua);
                break;
            default:
                chip.setChipBackgroundColorResource(R.color.grey);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return giftsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView giftImage, btnEdit, btnDelete;
        TextView giftName, giftPrice, giftDescription;
        Chip priorityChip;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            giftImage = itemView.findViewById(R.id.giftImage);
            giftName = itemView.findViewById(R.id.giftName);
            giftPrice = itemView.findViewById(R.id.giftPrice);
            giftDescription = itemView.findViewById(R.id.giftDescription);
            priorityChip = itemView.findViewById(R.id.priorityChip);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}