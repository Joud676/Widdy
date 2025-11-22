package com.example.widdy;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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

public class HomeFragment extends Fragment {

    RecyclerView rvWishlists;
    WishlistAdapter adapter;
    ArrayList<WishlistModel> list = new ArrayList<>();
    LinearLayout emptyState;
    Button btnViewAll, btnCreateWishlist, btnCreateNow;
    FirebaseFirestore db;
    FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvWishlists = view.findViewById(R.id.rvWishlists);
        emptyState = view.findViewById(R.id.emptyState);
        btnViewAll = view.findViewById(R.id.btnViewAll);
        btnCreateWishlist = view.findViewById(R.id.btnCreateWishlist);
        btnCreateNow = view.findViewById(R.id.btnCreateNow);

        rvWishlists.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new WishlistAdapter(list);
        rvWishlists.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadWishlists();

        btnViewAll.setOnClickListener(v -> {
            if (getActivity() != null) {
                ((HomePageActivity) getActivity()).openWishlists(v);
            }
        });

        btnCreateWishlist.setOnClickListener(v -> {
            if (getActivity() != null) {
                ((HomePageActivity) getActivity()).openCreateWishlists(v);
            }
        });

        btnCreateNow.setOnClickListener(v -> {
            if (getActivity() != null) {
                ((HomePageActivity) getActivity()).openCreateWishlists(v);
            }
        });

        return view;
    }

    private void loadWishlists() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        String uid = user.getUid();

        db.collection("users")
                .document(uid)
                .collection("wishlists")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(3)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(requireContext(), "خطأ في تحميل القوائم", Toast.LENGTH_SHORT).show();
                        Log.e("HomeFragment", "Error: " + e.getMessage());
                        return;
                    }

                    list.clear();

                    if (snapshots == null || snapshots.isEmpty()) {
                        showEmptyState();
                    } else {
                        hideEmptyState();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            WishlistModel model = doc.toObject(WishlistModel.class);
                            list.add(model);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        rvWishlists.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyState.setVisibility(View.GONE);
        rvWishlists.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadWishlists();
    }
}
