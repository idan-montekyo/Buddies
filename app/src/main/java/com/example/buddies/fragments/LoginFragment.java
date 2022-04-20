package com.example.buddies.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.transition.TransitionInflater;

import com.example.buddies.R;
import com.google.android.material.snackbar.Snackbar;

public class LoginFragment extends Fragment implements RegisterFragment.IOnRegisteredListener {

//    // void newInstance
//    // used to get args from calling fragment, working with newInstance method
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }

    public final String LOGIN_FRAGMENT_TAG = "login_fragment";
    public final String REGISTER_FRAGMENT_TAG = "register_fragment";
    public final String HOME_FRAGMENT_TAG = "home_fragment";

    CoordinatorLayout loginCoordinatorLayout;

    // inflate fragment_login
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);
        return view;
    }

    // Handle register, login, and guest buttons. In login, confirm user exists in DataBase.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loginCoordinatorLayout = view.findViewById(R.id.login_coordinator_layout);
        EditText usernameEt = view.findViewById(R.id.login_username_input);
        EditText passwordEt = view.findViewById(R.id.login_password_input);

        Button registerBtn = view.findViewById(R.id.login_register_button);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Fragment thisFragment = getParentFragmentManager().findFragmentByTag(LOGIN_FRAGMENT_TAG);
                assert thisFragment != null;
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in,  // enter
                                R.anim.fade_out,  // exit
                                R.anim.fade_in,   // popEnter
                                R.anim.slide_out) // popExit
                        .hide(thisFragment)
                        .add(R.id.root_main_activity, new RegisterFragment(), REGISTER_FRAGMENT_TAG)
                        .addToBackStack(null).commit();
            }
        });

        Button loginBtn = view.findViewById(R.id.login_login_button);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in,  // enter
                                R.anim.fade_out,  // exit
                                R.anim.fade_in,   // popEnter
                                R.anim.slide_out) // popExit
                        .replace(R.id.root_main_activity, new HomeFragment(), HOME_FRAGMENT_TAG)
                        .commit();
            }
        });

        Button guestBtn = view.findViewById(R.id.login_login_guest_button);
        guestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    // Show snackbar message after returning from RegisterFragment and successfully creating a new user.
    @Override
    public void onRegistered() {
        Snackbar.make(loginCoordinatorLayout, "successfully registered", Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.BLACK).show();
    }
}
