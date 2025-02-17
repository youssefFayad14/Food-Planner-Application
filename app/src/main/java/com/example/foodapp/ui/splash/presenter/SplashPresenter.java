package com.example.foodapp.ui.splash.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.foodapp.ui.splash.SplashContract;
import com.example.foodapp.utils.UserPreferences;
import com.google.firebase.auth.FirebaseAuth;

public class SplashPresenter implements SplashContract.Presenter {
    private final SplashContract.View view;
    private final UserPreferences userPreferences;

    private final Context context;

    public SplashPresenter(SplashContract.View view, Context context) {
        this.view = view;
        this.context = context;
        userPreferences = new UserPreferences(context);
    }

    @Override
    public void checkUserLogin() {
        if (userPreferences.isLoggedIn() && FirebaseAuth.getInstance().getCurrentUser() != null) {
            view.navigateToMain();
        } else if (userPreferences.isGuest()) {
            view.navigateToMain();
        } else {
            view.navigateToLogin();
        }
    }
}
