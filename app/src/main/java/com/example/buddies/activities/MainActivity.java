package com.example.buddies.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.buddies.Model.Model;
import com.example.buddies.R;
import com.example.buddies.ViewModel.ViewModel;
import com.example.buddies.fragments.HomeFragment;
import com.example.buddies.fragments.LoginFragment;

public class MainActivity extends AppCompatActivity {

    boolean m_doubleBackToExitPressedOnce = false;
    Toast m_backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    public void onBackPressed() {

        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
        if(backStackCount > 0) { // Not on Login / Home fragment.
            super.onBackPressed();
        } else { // On Login / Home fragment.
            if (m_doubleBackToExitPressedOnce) { // In case back button has already been pressed.
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

}