package com.example.widdy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.widdy.model.WishlistModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    RecyclerView rvWishlists;
    WishlistAdapter adapter;
    ArrayList<WishlistModel> list = new ArrayList<>();

    FirebaseFirestore firestore;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvWishlists = view.findViewById(R.id.rvWishlists);
        rvWishlists.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new WishlistAdapter(list);
        rvWishlists.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();

        loadWishlists();

        return view;
    }

    private void loadWishlists() {
        firestore.collection("wishlists")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    list.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        WishlistModel model = doc.toObject(WishlistModel.class);
                        list.add(model);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e("FirestoreError", "Error: " + e.getMessage()));
    }





}
