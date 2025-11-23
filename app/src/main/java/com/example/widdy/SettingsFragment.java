package com.example.widdy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";
    private static final String PREFS_NAME = "AppPrefs";

    private LinearLayout logoutLayout, changePasswordLayout, deleteAccountLayout, termsLayout;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews(view);
    }

    private void initViews(View view) {
        logoutLayout = view.findViewById(R.id.logoutLayout);
        changePasswordLayout = view.findViewById(R.id.changePasswordLayout);
        deleteAccountLayout = view.findViewById(R.id.deleteAccountLayout);
        termsLayout = view.findViewById(R.id.termsLayout);

        logoutLayout.setOnClickListener(v -> showLogoutDialog());

        changePasswordLayout.setOnClickListener(v -> showChangePasswordDialog());

        deleteAccountLayout.setOnClickListener(v -> showDeleteAccountDialog());

        termsLayout.setOnClickListener(v -> {
            Toast.makeText(getContext(), "الشروط والأحكام - قريباً", Toast.LENGTH_SHORT).show();
        });
    }

    private void showLogoutDialog() {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_confirm_delete, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        ((android.widget.TextView) dialogView.findViewById(R.id.tvDialogTitle))
                .setText("تسجيل الخروج");
        ((android.widget.TextView) dialogView.findViewById(R.id.tvDialogMessage))
                .setText("هل أنت متأكد من تسجيل الخروج؟");

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            logout();
        });

        dialog.show();
    }

    private void logout() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, requireContext().MODE_PRIVATE);
        prefs.edit().clear().apply();

        auth.signOut();

        Intent intent = new Intent(getActivity(), Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_change_password, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        EditText oldPassword = dialogView.findViewById(R.id.oldPassword);
        EditText newPassword = dialogView.findViewById(R.id.newPassword);
        EditText confirmPassword = dialogView.findViewById(R.id.confirmPassword);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String oldPass = oldPassword.getText().toString().trim();
            String newPass = newPassword.getText().toString().trim();
            String confirmPass = confirmPassword.getText().toString().trim();

            if (oldPass.isEmpty()) {
                oldPassword.setError("أدخل كلمة المرور القديمة");
                return;
            }

            if (newPass.isEmpty() || newPass.length() < 6) {
                newPassword.setError("كلمة المرور يجب أن تكون 6 أحرف على الأقل");
                return;
            }

            if (!newPass.equals(confirmPass)) {
                confirmPassword.setError("كلمات المرور غير متطابقة");
                return;
            }

            dialog.dismiss();
            changePassword(oldPass, newPass);
        });

        dialog.show();
    }

    private void changePassword(String oldPassword, String newPassword) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(getContext(), "خطأ في المصادقة", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);

        user.reauthenticate(credential)
                .addOnSuccessListener(unused -> {
                    user.updatePassword(newPassword)
                            .addOnSuccessListener(unused2 -> {
                                Toast.makeText(getContext(), "تم تغيير كلمة المرور بنجاح ✓", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error updating password: " + e.getMessage());
                                Toast.makeText(getContext(), "حدث خطأ أثناء تغيير كلمة المرور", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Reauthentication failed: " + e.getMessage());
                    Toast.makeText(getContext(), "كلمة المرور القديمة غير صحيحة", Toast.LENGTH_LONG).show();
                });
    }

    private void showDeleteAccountDialog() {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_delete_account, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        EditText passwordInput = dialogView.findViewById(R.id.passwordInput);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String password = passwordInput.getText().toString().trim();

            if (password.isEmpty()) {
                passwordInput.setError("أدخل كلمة المرور للتأكيد");
                return;
            }

            dialog.dismiss();
            deleteAccount(password);
        });

        dialog.show();
    }

    private void deleteAccount(String password) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(getContext(), "خطأ في المصادقة", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

        user.reauthenticate(credential)
                .addOnSuccessListener(unused -> {
                    deleteUserData(userId, () -> {
                        user.delete()
                                .addOnSuccessListener(unused2 -> {
                                    Toast.makeText(getContext(), "تم حذف الحساب بنجاح", Toast.LENGTH_SHORT).show();

                                    SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, requireContext().MODE_PRIVATE);
                                    prefs.edit().clear().apply();

                                    Intent intent = new Intent(getActivity(), Login.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    requireActivity().finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error deleting account: " + e.getMessage());
                                    Toast.makeText(getContext(), "حدث خطأ أثناء حذف الحساب", Toast.LENGTH_SHORT).show();
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Reauthentication failed: " + e.getMessage());
                    Toast.makeText(getContext(), "كلمة المرور غير صحيحة", Toast.LENGTH_LONG).show();
                });
    }

    private void deleteUserData(String userId, Runnable onComplete) {
        db.collection("users")
                .document(userId)
                .collection("wishlists")
                .get()
                .addOnSuccessListener(wishlists -> {
                    for (var wishlist : wishlists) {
                        db.collection("users")
                                .document(userId)
                                .collection("wishlists")
                                .document(wishlist.getId())
                                .collection("gifts")
                                .get()
                                .addOnSuccessListener(gifts -> {
                                    for (var gift : gifts) {
                                        gift.getReference().delete();
                                    }
                                });

                        wishlist.getReference().delete();
                    }

                    db.collection("users")
                            .document(userId)
                            .delete()
                            .addOnSuccessListener(unused -> {
                                onComplete.run();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error deleting user data: " + e.getMessage());
                                onComplete.run();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading wishlists: " + e.getMessage());
                    onComplete.run();
                });
    }
}