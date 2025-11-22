package com.example.widdy;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.widdy.model.WishlistModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AllWishlistsFragment extends Fragment {

    private static final String TAG = "AllWishlists";

    RecyclerView rvAll;
    WishlistAdapter adapter;
    ArrayList<WishlistModel> list = new ArrayList<>();
    FirebaseFirestore db;
    FirebaseAuth auth;
    ImageView backButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout for the fragment
        View view = inflater.inflate(R.layout.fragment_all_wishlists, container, false);

        rvAll = view.findViewById(R.id.rvAllWishlists);
        backButton = view.findViewById(R.id.backButton);

        rvAll.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new WishlistAdapter(list);
        rvAll.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        loadWishlists();

        return view;
    }

    private void loadWishlists() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "يجب تسجيل الدخول أولاً", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "User not logged in");
            return;
        }

        String uid = currentUser.getUid();
        Log.d(TAG, "Loading wishlists for user: " + uid);

        db.collection("users")
                .document(uid)
                .collection("wishlists")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    list.clear();

                    if (snapshots.isEmpty()) {
                        Log.d(TAG, "No wishlists found");
                        Toast.makeText(getContext(), "لا توجد قوائم أمنيات", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "Found " + snapshots.size() + " wishlists");

                        for (QueryDocumentSnapshot doc : snapshots) {
                            WishlistModel wishlist = doc.toObject(WishlistModel.class);
                            list.add(wishlist);
                            Log.d(TAG, "Loaded wishlist: " + wishlist.getName());
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load wishlists: " + e.getMessage());
                    Toast.makeText(getContext(), "خطأ في تحميل القوائم: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
