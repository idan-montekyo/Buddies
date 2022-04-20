package com.example.buddies.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.buddies.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

public class HomeFragment extends Fragment implements CreatePostFragment.IOnUploadListener,
                                                        ProfileFragment.IOnSaveListener {

    public final String LOGIN_FRAGMENT_TAG = "login_fragment";
    public final String REGISTER_FRAGMENT_TAG = "register_fragment";
    public final String PROFILE_FRAGMENT_TAG = "profile_fragment";
    public final String HOME_FRAGMENT_TAG = "home_fragment";
    public final String CREATE_POST_FRAGMENT_TAG = "create_post_fragment";

    DrawerLayout m_drawerLayout;
    NavigationView m_navigationView;
    CoordinatorLayout m_coordinatorLayout;

    // Inflate fragment_home.
    @SuppressLint("WrongConstant")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Toolbar toolbar = view.findViewById(R.id.home_toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        // Source (Tash Pemhiwa) - https://stackoverflow.com/questions/18133443/fragments-onoptionsitemselected-doesnt-get-called
        setHasOptionsMenu(true);
        actionBar.setTitle("");

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        m_drawerLayout = view.findViewById(R.id.home_drawer_layout);
        m_navigationView = view.findViewById(R.id.home_navigation_view);
        m_coordinatorLayout = view.findViewById(R.id.home_coordinator_layout);

        m_navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                m_drawerLayout.closeDrawers();

                Fragment thisFragment = getParentFragmentManager().findFragmentByTag(HOME_FRAGMENT_TAG);

                switch (item.getItemId()) {
                    case R.id.menu_my_profile:
                        assert thisFragment != null;
                        getParentFragmentManager().beginTransaction()
                                .setCustomAnimations(
                                        R.anim.slide_in,  // enter
                                        R.anim.fade_out,  // exit
                                        R.anim.fade_in,   // popEnter
                                        R.anim.slide_out) // popExit
                                .hide(thisFragment)
                                .add(R.id.root_main_activity, new ProfileFragment(), PROFILE_FRAGMENT_TAG)
                                .addToBackStack(null).commit();
                        break;
                    case R.id.menu_my_posts:
                        break;
                    case R.id.menu_posts_i_commented_on:
                        break;
                    case R.id.menu_settings:
                        break;
                    case R.id.menu_log_out:
                        getParentFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.slide_out, R.anim.fade_out)
                                .replace(R.id.root_main_activity, new LoginFragment(), LOGIN_FRAGMENT_TAG)
                                .commit();
                        break;
                }

                return false;
            }
        });

        EditText searchEt = view.findViewById(R.id.home_search_edit_text);
        ImageButton searchBtn = view.findViewById(R.id.home_search_image_button);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchEt.getText().toString().equals("")) { // Search bar is empty.
                    Toast.makeText(requireContext(), "No city was searched for", Toast.LENGTH_SHORT).show();
                } else { // Some string in SearchEt
                    Toast.makeText(requireContext(), searchEt.getText().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        FloatingActionButton createPostFAB = view.findViewById(R.id.home_fab);
        createPostFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment thisFragment = getParentFragmentManager().findFragmentByTag(HOME_FRAGMENT_TAG);
                assert thisFragment != null;
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in,  // enter
                                R.anim.fade_out,  // exit
                                R.anim.fade_in,   // popEnter
                                R.anim.slide_out) // popExit
                        .hide(thisFragment)
                        .add(R.id.root_main_activity, new CreatePostFragment(), CREATE_POST_FRAGMENT_TAG)
                        .addToBackStack(null).commit();
            }
        });


    }

    @SuppressLint("WrongConstant")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home) {
            m_drawerLayout.openDrawer(Gravity.START);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSave() {
        Snackbar.make(m_coordinatorLayout, "profile info successfully saved", Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.BLACK).show();
    }

    @Override
    public void onUpload() {
        Snackbar.make(m_coordinatorLayout, "post successfully uploaded", Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.BLACK).show();
    }
}
