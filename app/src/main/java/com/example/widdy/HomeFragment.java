package com.example.widdy;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;

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
    ArrayList<String> docIds = new ArrayList<>();
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

        adapter = new WishlistAdapter(list, new WishlistAdapter.OnWishlistClickListener() {
            @Override
            public void onWishlistClick(WishlistModel wishlist) {
                if (getActivity() != null) {
                    ((HomePageActivity) getActivity())
                            .openWishlistDetails(wishlist.getDocumentId());
                }
            }

            @Override
            public void onDeleteClick(WishlistModel wishlist, int position) {
                showDeleteDialog(wishlist, position);
            }
        });

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
                    docIds.clear();

                    if (snapshots == null || snapshots.isEmpty()) {
                        showEmptyState();
                    } else {
                        hideEmptyState();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            WishlistModel model = doc.toObject(WishlistModel.class);
                            model.setDocumentId(doc.getId());
                            list.add(model);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }
    private void deleteWishlist(WishlistModel wishlist, int position) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        String wishlistDocId = wishlist.getDocumentId();

        if (wishlistDocId == null || wishlistDocId.isEmpty()) {
            Toast.makeText(getContext(), "خطأ: لم يتم تحديد القائمة", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .document(uid)
                .collection("wishlists")
                .document(wishlistDocId)
                .collection("gifts")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (var doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete();
                    }

                    db.collection("users")
                            .document(uid)
                            .collection("wishlists")
                            .document(wishlistDocId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                list.remove(position);
                                adapter.notifyItemRemoved(position);
                                Toast.makeText(getContext(), "تم حذف القائمة وكل الهدايا", Toast.LENGTH_SHORT).show();
                                if (list.isEmpty()) showEmptyState();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Delete failed: " + e.getMessage());
                                Toast.makeText(getContext(), "فشل الحذف: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Delete gifts failed: " + e.getMessage());
                    Toast.makeText(getContext(), "فشل حذف الهدايا: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showDeleteDialog(WishlistModel wishlist, int position) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_confirm_delete, null);

        androidx.appcompat.app.AlertDialog dialog =
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setView(dialogView)
                        .setCancelable(true)
                        .create();

        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            deleteWishlist(wishlist, position);
        });

        dialog.show();
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