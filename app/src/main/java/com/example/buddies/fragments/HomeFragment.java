package com.example.buddies.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.buddies.Model.Model;
import com.example.buddies.R;
import com.example.buddies.ViewModel.ViewModel;
import com.example.buddies.adapters.PostAdapter;
import com.example.buddies.common.Post;
import com.example.buddies.enums.ePostType;
import com.example.buddies.interfaces.LoadPostsEvent.ILoadPostsRequestEventHandler;
import com.example.buddies.interfaces.LoadPostsEvent.ILoadPostsResponseEventHandler;
import com.example.buddies.interfaces.LogoutEvent.ILogoutResponsesEventHandler;
import com.example.buddies.interfaces.MVVM.IView;
import com.example.buddies.interfaces.PostCreationEvent.IPostCreationResponseEventHandler;
import com.example.buddies.interfaces.UpdateCitiesAutocompleteListEvent.IUpdateCitiesAutocompleteListRequestEventHandler;
import com.example.buddies.interfaces.UpdateCitiesAutocompleteListEvent.IUpdateCitiesAutocompleteListResponsesEventHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment implements IView,
                                                      CreatePostFragment.IOnUploadListener,
                                                      // ProfileFragment.IOnSaveListener,
                                                      ILogoutResponsesEventHandler,
                                                      IUpdateCitiesAutocompleteListRequestEventHandler,
                                                      IUpdateCitiesAutocompleteListResponsesEventHandler,
                                                      ILoadPostsRequestEventHandler,
                                                      ILoadPostsResponseEventHandler,
                                                      IPostCreationResponseEventHandler
{
    public static final String HOME_FRAGMENT_TAG = "home_fragment";

    DrawerLayout m_drawerLayout;
    NavigationView m_navigationView;
    CoordinatorLayout m_coordinatorLayout;
    ViewModel m_ViewModel = ViewModel.getInstance();
    MaterialAutoCompleteTextView m_MaterialAutoCompleteTextView_SearchPostsByCity = null;
    Context m_Context = null;
    RecyclerView m_RecyclerView;

    public static List<Post> m_Posts = new ArrayList<>();
    public static PostAdapter m_PostAdapter;

    @Override
    public void onAttach(@NonNull Context context)
    {
        this.m_Context = context;
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        this.m_ViewModel.registerForEvents((IView) this);
        super.onCreate(savedInstanceState);
    }

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

        // (Source: https://stackoverflow.com/questions/18133443/fragments-onoptionsitemselected-doesnt-get-called)
        setHasOptionsMenu(true);
        actionBar.setTitle("");

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        m_drawerLayout = view.findViewById(R.id.home_drawer_layout);
        m_navigationView = view.findViewById(R.id.home_navigation_view);

        m_coordinatorLayout = view.findViewById(R.id.home_coordinator_layout);

        m_RecyclerView = view.findViewById(R.id.home_recycler_view);
        m_RecyclerView.setHasFixedSize(true);
        m_RecyclerView.setLayoutManager(new GridLayoutManager(m_Context, 1));

        onLoadPosts(ePostType.ALL);

        m_PostAdapter = new PostAdapter(m_Posts);

        m_PostAdapter.setListener(new PostAdapter.MyPostListener() {
            @Override
            public void onPostClicked(int index, View view) throws IOException {

                Fragment thisFragment = getParentFragmentManager().findFragmentByTag(HOME_FRAGMENT_TAG);
                assert thisFragment != null;
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in,  // enter
                                R.anim.fade_out,  // exit
                                R.anim.fade_in,   // popEnter
                                R.anim.slide_out) // popExit
                        .hide(thisFragment)
                        .add(R.id.root_main_activity, new ViewPostFragment(), ViewPostFragment.VIEW_POST_FRAGMENT_TAG)
                        .addToBackStack(null).commit();
            }
        });

        m_RecyclerView.setAdapter(m_PostAdapter);

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
                        .add(R.id.root_main_activity, new CreatePostFragment(), CreatePostFragment.CREATE_POST_FRAGMENT_TAG)
                        .addToBackStack(null).commit();
            }
        });

        if (Model.getInstance().isCurrentUserAnonymous() == true)
        {
            m_navigationView.getMenu().removeItem(R.id.menu_my_posts);
            m_navigationView.getMenu().removeItem(R.id.menu_posts_i_commented_on);
            m_navigationView.getMenu().removeItem(R.id.menu_my_profile);

            createPostFAB.setVisibility(View.GONE);
        }

        m_navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
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
                                .add(R.id.root_main_activity, new ProfileFragment(), ProfileFragment.PROFILE_FRAGMENT_TAG)
                                .addToBackStack(null).commit();
                        break;
                    case R.id.menu_my_posts:
                        break;
                    case R.id.menu_posts_i_commented_on:
                        break;
                    case R.id.menu_settings:
                        break;
                    case R.id.menu_log_out:
                        HomeFragment.this.m_ViewModel.onRequestToLogout(m_Context);
                        break;
                }

                return false;
            }
        });

        // EditText searchEt = view.findViewById(R.id.home_search_edit_text);
        this.m_MaterialAutoCompleteTextView_SearchPostsByCity = (MaterialAutoCompleteTextView) view.findViewById(R.id.home_search_edit_text);

        // Make the dropdown to be white with "setDropDownBackgroundDrawable" / "setDropDownBackgroundResource"
        // (Source: https://stackoverflow.com/a/62764533/2196301)
        this.m_MaterialAutoCompleteTextView_SearchPostsByCity.setDropDownBackgroundResource(R.color.white);
        this.m_ViewModel.onRequestToUpdateListOfCities();

        ImageButton searchBtn = view.findViewById(R.id.home_search_image_button);
        searchBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String selectedCity = HomeFragment.this.m_MaterialAutoCompleteTextView_SearchPostsByCity.getText().toString();

                // Search bar is empty.
                if (selectedCity.equals(""))
                {
                    Toast.makeText(HomeFragment.this.m_Context, "No city was searched for", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    // TODO: retrieve from the firebase all the posts in that city
                    Toast.makeText(HomeFragment.this.m_Context, selectedCity, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @SuppressLint("WrongConstant")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if(item.getItemId() == android.R.id.home) {
            m_drawerLayout.openDrawer(Gravity.START);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    @Override
    public void onSave()
    {
        Snackbar.make(m_coordinatorLayout, "profile info successfully saved", Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.BLACK).show();
    }
    */

    @Override
    public void onUpload()
    {
        Snackbar.make(m_coordinatorLayout, "post successfully uploaded", Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.BLACK).show();
    }

    @Override
    public void onSuccessToLogout()
    {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_out, R.anim.fade_out)
                .replace(R.id.root_main_activity, new LoginFragment(), LoginFragment.LOGIN_FRAGMENT_TAG)
                .commit();
    }

    @Override
    public void onFailureToLogout(Exception i_Reason)
    {
        Snackbar.make(m_coordinatorLayout, "Logout Failed: " + i_Reason.getMessage(), Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.BLACK).show();
    }

    @Override
    public void onDestroy()
    {
        this.m_ViewModel.unregisterForEvents((IView) this);
        super.onDestroy();
    }

    @Override
    public void onRequestToUpdateListOfCities()
    {
        this.m_ViewModel.onRequestToUpdateListOfCities();
    }

    @Override
    public void onSuccessToUpdateListOfCities(ArrayList<String> i_UpdatedListOfCities)
    {
        // Create the new adapter which will be linked to the AutocompleteTextView
        ArrayAdapter<String> autocompleteArrayAdapter = new ArrayAdapter<String>(this.m_Context, android.R.layout.simple_dropdown_item_1line, i_UpdatedListOfCities);

        // Order that the user have to insert at least 1 char in order to see the relevant suggestions.
        this.m_MaterialAutoCompleteTextView_SearchPostsByCity.setThreshold(1);

        // Link between the AutocompleteTextView and it's Strings adapter.
        this.m_MaterialAutoCompleteTextView_SearchPostsByCity.setAdapter(autocompleteArrayAdapter);

        autocompleteArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFailureToUpdateListOfCities(Exception i_Reason)
    {
        Snackbar.make(m_coordinatorLayout, "Update of list of cities failed: " + i_Reason.getMessage(), Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.BLACK).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onLoadPosts(ePostType type) {
        m_ViewModel.onLoadPosts(type);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onSuccessToLoadPosts(List<Post> i_PostsList) {
        m_Posts.clear();
        m_Posts.addAll(i_PostsList);
    }

    @Override
    public void onFailureToLoadPosts(Exception i_Reason) {
        Snackbar.make(m_coordinatorLayout, Objects.requireNonNull(i_Reason.getMessage()), Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.BLACK).show();
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSuccessToCreatePost() {
        onLoadPosts(ePostType.ALL);
        // TODO: needs to change to notifyItemInserted with pos 0 only when relevant!
        //       for example, if we search Haifa and Ashdod was added, it's not relevant to us.
        Objects.requireNonNull(this.m_RecyclerView.getAdapter()).notifyDataSetChanged();
        this.m_RecyclerView.invalidate();

    }

    @Override
    public void onFailureToCreatePost(Exception i_Reason) {
        // irrelevant.
    }
}