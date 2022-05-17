package com.example.buddies.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.buddies.Model.Model;
import com.example.buddies.R;
import com.example.buddies.ViewModel.ViewModel;
import com.example.buddies.fragments.HomeFragment;
import com.example.buddies.fragments.LoginFragment;
import com.example.buddies.interfaces.MVVM.IView;
import com.example.buddies.interfaces.UpdateProfileEvent.IUpdateProfileResponsesEventHandler;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity implements IView,
                                                               IUpdateProfileResponsesEventHandler
{
    boolean m_doubleBackToExitPressedOnce = false;
    Toast m_backToast;
    View m_RootMainActivity;
    ViewModel m_ViewModel = ViewModel.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        this.m_ViewModel.registerForEvents((IView) this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_RootMainActivity = this.findViewById(R.id.root_main_activity);

        if (Model.getInstance().isUserLoggedIn() == true)
        {
            // Inflate Home-Fragment.
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.root_main_activity, new HomeFragment(), HomeFragment.HOME_FRAGMENT_TAG)
                    .commit();
        }
        else
        {
            // Inflate Login-Fragment.
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.root_main_activity, new LoginFragment(), LoginFragment.LOGIN_FRAGMENT_TAG)
                    .commit();
        }
    }

    // Requires double-back-press to exit while in Login/Home fragments.
    @Override
    public void onBackPressed()
    {
        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();

        // Not on Login / Home fragment.
        if(backStackCount > 0)
        {
            super.onBackPressed();
        }
        // On Login / Home fragment.
        else
        {
            // In case back button has already been pressed.
            if (m_doubleBackToExitPressedOnce)
            {
                m_backToast.cancel();
                super.onBackPressed();
                return;
            }

            this.m_doubleBackToExitPressedOnce = true;
            m_backToast = Toast.makeText(this, "Tap again to exit", Toast.LENGTH_SHORT);
            m_backToast.show();

            new Handler(Looper.getMainLooper())
                    .postDelayed(() -> m_doubleBackToExitPressedOnce = false, 2000);
        }
    }

    @Override
    public void onSuccessToUpdateProfile()
    {
        Snackbar.make(this.m_RootMainActivity, "Profile has been successfully updated.", Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.BLACK).show();
    }

    @Override
    public void onFailureToUpdateProfile(Exception i_Reason)
    {
        Snackbar.make(this.m_RootMainActivity, i_Reason.getMessage(), Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.BLACK).show();
    }
}