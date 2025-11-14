package com.example.widdy;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.widdy.model.WishlistModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AllWishlistsFragment extends Fragment {

    RecyclerView rvAll;
    WishlistAdapter adapter;
    ArrayList<WishlistModel> list = new ArrayList<>();
    FirebaseFirestore firestore;

    public AllWishlistsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_all_wishlists, container, false);

        rvAll = view.findViewById(R.id.rvAllWishlists);
        rvAll.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new WishlistAdapter(list);
        rvAll.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();

        loadWishlists();

        return view;
    }

    private void loadWishlists() {
        firestore.collection("wishlists")
                .get()
                .addOnSuccessListener(snapshots -> {
                    list.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        list.add(doc.toObject(WishlistModel.class));
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
