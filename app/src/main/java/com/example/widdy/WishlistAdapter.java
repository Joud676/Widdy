package com.example.widdy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.widdy.model.WishlistModel;

import java.util.ArrayList;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {

    private ArrayList<WishlistModel> list;

    public WishlistAdapter(ArrayList<WishlistModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wishlist_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WishlistModel wishlist = list.get(position);

        holder.tvTitle.setText(wishlist.getName());
        holder.tvItems.setText(wishlist.getItemCount() + " عناصر");

        if (wishlist.getImageUrl() != null && !wishlist.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(wishlist.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.ic_gift)
                    .into(holder.ivWishlistImage);
        }

        holder.itemView.setOnClickListener(v -> {
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvItems;
        ImageView ivWishlistImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvItems = itemView.findViewById(R.id.tvItemsCount);
            ivWishlistImage = itemView.findViewById(R.id.ivWishlistImage);
        }
    }
}