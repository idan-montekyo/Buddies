package com.example.buddies.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.example.buddies.R;
import com.example.buddies.ViewModel.ViewModel;
import com.example.buddies.interfaces.LoginEvent.ILoginResponsesEventHandler;
import com.example.buddies.interfaces.MVVM.IView;
import com.google.android.material.snackbar.Snackbar;

public class LoginFragment extends Fragment implements IView,
                                                       RegisterFragment.IOnRegisteredListener,
                                                       ILoginResponsesEventHandler
{
    public static final String LOGIN_FRAGMENT_TAG = "login_fragment";

    CoordinatorLayout loginCoordinatorLayout;
    ViewModel m_ViewModel = ViewModel.getInstance();

    Context m_Context;

    @Override
    public void onAttach(@NonNull Context context) {
        m_Context = context;
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        this.m_ViewModel.registerForEvents((IView) this);
        super.onCreate(savedInstanceState);
    }

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
                        .add(R.id.root_main_activity, new RegisterFragment(), RegisterFragment.REGISTER_FRAGMENT_TAG)
                        .addToBackStack(null).commit();
            }
        });

        Button loginBtn = view.findViewById(R.id.login_login_button);
        loginBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String i_UserName = usernameEt.getText().toString();
                String i_Password = passwordEt.getText().toString();

                LoginFragment.this.m_ViewModel.onRequestToLogin(m_Context, i_UserName, i_Password);
            }
        });

        Button guestBtn = view.findViewById(R.id.login_login_guest_button);
        guestBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                LoginFragment.this.m_ViewModel.onRequestToAnonymousLogin(m_Context);
            }
        });
    }

    // Show snackbar message after returning from RegisterFragment and successfully creating a new user.
    @Override
    public void onRegistered()
    {
        Snackbar.make(loginCoordinatorLayout, "successfully registered", Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.BLACK).show();
    }

    @Override
    public void onSuccessToLogin()
    {
        this.moveToMainProgramFragment();
    }

    @Override
    public void onFailureToLogin(Exception i_Reason)
    {
        // TODO: check again
        Toast.makeText(requireContext(), "An error occured while trying to login, please retry or contact support.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSuccessToAnonymousLogin()
    {
        this.moveToMainProgramFragment();
    }

    @Override
    public void onFailureToAnonymousLogin(Exception i_Reason)
    {
        // TODO: check again
        Toast.makeText(requireContext(), "An error occured while trying to login, please retry or contact support.", Toast.LENGTH_LONG).show();
    }

    private void moveToMainProgramFragment()
    {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out) // popExit
                .replace(R.id.root_main_activity, new HomeFragment(), HomeFragment.HOME_FRAGMENT_TAG)
                .commit();
    }

    @Override
    public void onDestroy()
    {
        this.m_ViewModel.unregisterForEvents((IView) this);
        super.onDestroy();
    }
}