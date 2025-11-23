package com.example.widdy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.widdy.model.WishlistModel;

import java.util.ArrayList;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {

    private final ArrayList<WishlistModel> list;
    private final OnWishlistClickListener listener;

    public interface OnWishlistClickListener {
        void onWishlistClick(WishlistModel wishlist);
        void onDeleteClick(WishlistModel wishlist, int position);
    }

    public WishlistAdapter(ArrayList<WishlistModel> list, OnWishlistClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_wishlist_card_item, parent, false);
        return new ViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WishlistModel wishlist = list.get(position);

        holder.tvTitle.setText(wishlist.getName());
        holder.tvItemsCount.setText(wishlist.getItemCount() + " عناصر");

        if (wishlist.getImageUrl() != null && !wishlist.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(wishlist.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.ic_gift)
                    .into(holder.ivWishlistImage);
        } else {
            holder.ivWishlistImage.setImageResource(R.drawable.ic_gift);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onWishlistClick(wishlist);
        });

        holder.btnView.setOnClickListener(v -> {
            if (listener != null) listener.onWishlistClick(wishlist);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(wishlist, position);
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvItemsCount;
        ImageView ivWishlistImage;
        Button btnView;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvItemsCount = itemView.findViewById(R.id.tvItemsCount);
            ivWishlistImage = itemView.findViewById(R.id.ivWishlistImage);
            btnView = itemView.findViewById(R.id.btnView);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
