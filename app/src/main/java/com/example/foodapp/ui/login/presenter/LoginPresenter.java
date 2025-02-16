package com.example.foodapp.ui.login.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Patterns;

import com.example.foodapp.data.repository.FavoriteMealRepository;
import com.example.foodapp.data.repository.MealPlanRepository;
import com.example.foodapp.ui.login.LoginContract;
import com.example.foodapp.utils.DataSyncUtil;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LoginPresenter implements LoginContract.Presenter {
    private final LoginContract.View view;
    private final FirebaseAuth auth;
    private final SharedPreferences sharedPreferences;
    private DataSyncUtil dataSyncUtil;

    public LoginPresenter(LoginContract.View view, Context context, FavoriteMealRepository favoriteMealRepository, MealPlanRepository mealPlanRepository) {
        this.view = view;
        this.auth = FirebaseAuth.getInstance();
        this.sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        this.dataSyncUtil = new DataSyncUtil(favoriteMealRepository, mealPlanRepository);
    }

    @Override
    public void loginWithEmail(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            view.showErrorMessage("Enter email and password.");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view.showErrorMessage("Invalid email format.");
            return;
        }
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null && user.isEmailVerified()) {
                        saveUserLoginState(user.getUid());
                        dataSyncUtil.syncUserData();
                        view.navigateToMain();
                    } else {
                        view.showErrorMessage("Email not verified. Please check your email.");
                    }
                })
                .addOnFailureListener(e -> view.showErrorMessage("Login failed: " + e.getMessage()));
    }

    @Override
    public void loginWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential).addOnCompleteListener(authTask -> {
            if (authTask.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    saveUserLoginState(user.getUid());
                    dataSyncUtil.syncUserData();
                    view.navigateToMain();
                }
            } else {
                view.showErrorMessage("Google login failed: " + authTask.getException().getMessage());
            }
        });
    }

    @Override
    public void resetPassword(String email) {
        if (email.isEmpty()) {
            view.showErrorMessage("Enter your email to reset password");
            return;
        }
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    view.showErrorMessage("Reset link sent to your email");
                    view.clearFields();
                })
                .addOnFailureListener(e -> view.showErrorMessage("Failed to send reset email: " + e.getMessage()));
    }

    private void saveUserLoginState(String uid) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("USER_UID", uid);
        editor.putBoolean("IS_LOGGED_IN", true);
        editor.apply();
    }
}