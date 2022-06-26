package com.example.buddies.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.buddies.Model.Model;
import com.example.buddies.R;
import com.example.buddies.ViewModel.ViewModel;
import com.example.buddies.adapters.PostAdapter;
import com.example.buddies.common.AppUtils;
import com.example.buddies.common.Post;
import com.example.buddies.common.UserProfile;
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

    ViewModel m_ViewModel = ViewModel.getInstance();
    Context m_Context = null;
    Handler m_Handler = new Handler();

    DrawerLayout m_DrawerLayout;
    NavigationView m_NavigationView;
    CoordinatorLayout m_CoordinatorLayout;
    MaterialAutoCompleteTextView m_MaterialAutoCompleteTextView_SearchPostsByCity = null;
    RecyclerView m_RecyclerView;
    SwipeRefreshLayout m_SwipeRefreshLayout;

    public static List<Post> m_Posts = new ArrayList<>();
    @SuppressLint("StaticFieldLeak")
    public static PostAdapter m_PostAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        this.m_Context = context;
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);

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

        m_DrawerLayout = view.findViewById(R.id.home_drawer_layout);
        m_NavigationView = view.findViewById(R.id.home_navigation_view);

        m_CoordinatorLayout = view.findViewById(R.id.home_coordinator_layout);

        m_SwipeRefreshLayout = view.findViewById(R.id.home_swipe_refresh_layout);
        m_SwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String currentSearch = m_MaterialAutoCompleteTextView_SearchPostsByCity.getText().toString();

                m_Handler.postDelayed(new Runnable() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void run() {
                        if (currentSearch.equals("")) {
                            onRequestToLoadPosts(ePostType.ALL);
                        } else {
                            onRequestToLoadPostsByCity(currentSearch);
                        }
                        Objects.requireNonNull(m_RecyclerView.getAdapter()).notifyDataSetChanged();
                        m_SwipeRefreshLayout.setRefreshing(false);
                    }
                }, 1500);
            }
        });

        m_RecyclerView = view.findViewById(R.id.home_recycler_view);
        m_RecyclerView.setHasFixedSize(true);
        m_RecyclerView.setLayoutManager(new GridLayoutManager(m_Context, 1));

        onRequestToLoadPosts(ePostType.ALL);

        m_PostAdapter = new PostAdapter(m_Posts);

        m_PostAdapter.setListener(new PostAdapter.MyPostListener() {
            @Override
            public void onPostClicked(int index, View view) throws IOException {
                Fragment thisFragment = getParentFragmentManager().findFragmentByTag(HOME_FRAGMENT_TAG);
                assert thisFragment != null;

                Post        currentPost               = m_Posts.get(index);
                UserProfile currentCreatorUserProfile = Model.getInstance().resolveUserProfileFromUID(currentPost.getCreatorUserUID()); // m_PostAdapter.getCreatorUserProfile();

                // Convert the custom objects to json and pass them as json to the bundle (Source: https://stackoverflow.com/a/46591617/2196301)
                String userProfileJsonString = AppUtils.getGsonParser().toJson(currentCreatorUserProfile);
                String currentPostJsonString = AppUtils.getGsonParser().toJson(currentPost);

                Bundle viewPostFragmentArguments = new Bundle();
                viewPostFragmentArguments.putString("userProfileJsonString", userProfileJsonString);
                viewPostFragmentArguments.putString("currentPostJsonString", currentPostJsonString);

                ViewPostFragment postFragmentToLaunch = new ViewPostFragment();
                postFragmentToLaunch.setArguments(viewPostFragmentArguments);

                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in,  // enter
                                R.anim.fade_out,  // exit,
                                R.anim.fade_in,   // popEnter
                                R.anim.slide_out) // popExit
                        .hide(thisFragment)
                        .add(R.id.root_main_activity, postFragmentToLaunch, ViewPostFragment.VIEW_POST_FRAGMENT_TAG)
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

        // Change color for a menuItem (Source: https://stackoverflow.com/questions/3519277/how-to-change-the-text-color-of-menu-item-in-android)
        MenuItem item = m_NavigationView.getMenu().findItem(R.id.menu_posts_i_commented_on);
        SpannableString s = new SpannableString(getString(R.string.posts_i_commented_on) + " " + getString(R.string.soon));
        s.setSpan(new ForegroundColorSpan(Color.GRAY), 0, getString(R.string.posts_i_commented_on).length(), 0);
        s.setSpan(new ForegroundColorSpan(Color.RED), getString(R.string.posts_i_commented_on).length()+1, s.length(), 0);
        item.setTitle(s);

        if (Model.getInstance().isCurrentUserAnonymous()) {
            m_NavigationView.getMenu().removeItem(R.id.menu_my_posts);
            m_NavigationView.getMenu().removeItem(R.id.menu_posts_i_commented_on);
            m_NavigationView.getMenu().removeItem(R.id.menu_my_profile);

            createPostFAB.setVisibility(View.GONE);
        }

        m_NavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                m_DrawerLayout.closeDrawers();

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

                        Bundle bundleMyPosts = new Bundle();
                        bundleMyPosts.putString(RecyclerViewFragment.ACTION_KEY, RecyclerViewFragment.ACTION_MY_POSTS);

                        RecyclerViewFragment recyclerViewFragmentMyPosts = new RecyclerViewFragment();
                        recyclerViewFragmentMyPosts.setArguments(bundleMyPosts);

                        assert thisFragment != null;
                        getParentFragmentManager().beginTransaction()
                                .setCustomAnimations(
                                        R.anim.slide_in,  // enter
                                        R.anim.fade_out,  // exit
                                        R.anim.fade_in,   // popEnter
                                        R.anim.slide_out) // popExit
                                .hide(thisFragment)
                                .add(R.id.root_main_activity, recyclerViewFragmentMyPosts, RecyclerViewFragment.RECYCLER_VIEW_FRAGMENT_TAG)
                                .addToBackStack(null).commit();
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
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                String selectedCity = HomeFragment.this.m_MaterialAutoCompleteTextView_SearchPostsByCity.getText().toString();

                // Search bar is empty.
                if (selectedCity.equals("")) {
                    onRequestToLoadPosts(ePostType.ALL);
                } else {
                    onRequestToLoadPostsByCity(selectedCity);
                }
                Objects.requireNonNull(m_RecyclerView.getAdapter()).notifyDataSetChanged();
                m_SwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @SuppressLint("WrongConstant")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            m_DrawerLayout.openDrawer(Gravity.START);
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
    public void onUpload() {
        Snackbar.make(m_CoordinatorLayout, "post successfully uploaded", Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.BLACK).show();
    }

    @Override
    public void onSuccessToLogout() {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_out, R.anim.fade_out)
                .replace(R.id.root_main_activity, new LoginFragment(), LoginFragment.LOGIN_FRAGMENT_TAG)
                .commit();
    }

    @Override
    public void onFailureToLogout(Exception i_Reason) {
        Snackbar.make(m_CoordinatorLayout, "Logout Failed: " + i_Reason.getMessage(), Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.BLACK).show();
    }

    @Override
    public void onDestroy() {
        this.m_ViewModel.unregisterForEvents((IView) this);
        super.onDestroy();
    }

    @Override
    public void onRequestToUpdateListOfCities() { this.m_ViewModel.onRequestToUpdateListOfCities(); }

    @Override
    public void onSuccessToUpdateListOfCities(ArrayList<String> i_UpdatedListOfCities) {
        // Create the new adapter which will be linked to the AutocompleteTextView
        ArrayAdapter<String> autocompleteArrayAdapter = new ArrayAdapter<String>(this.m_Context, android.R.layout.simple_dropdown_item_1line, i_UpdatedListOfCities);

        // Order that the user have to insert at least 1 char in order to see the relevant suggestions.
        this.m_MaterialAutoCompleteTextView_SearchPostsByCity.setThreshold(1);

        // Link between the AutocompleteTextView and it's Strings adapter.
        this.m_MaterialAutoCompleteTextView_SearchPostsByCity.setAdapter(autocompleteArrayAdapter);

        autocompleteArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFailureToUpdateListOfCities(Exception i_Reason) {
        Snackbar.make(m_CoordinatorLayout, "Update of list of cities failed: " + i_Reason.getMessage(), Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.BLACK).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestToLoadPosts(ePostType type) { m_ViewModel.onRequestToLoadPosts(type); }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestToLoadPostsByCity(String i_SearchedCity) { m_ViewModel.onRequestToLoadPostsByCity(i_SearchedCity); }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onSuccessToLoadPosts(List<Post> i_PostsList) {
        m_Posts.clear();
        m_Posts.addAll(i_PostsList);
    }

    @Override
    public void onFailureToLoadPosts(Exception i_Reason) {
        Snackbar.make(m_CoordinatorLayout, Objects.requireNonNull(i_Reason.getMessage()), Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.BLACK).show();
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSuccessToCreatePost(Post i_Post)
    {
        m_Handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                HomeFragment.this.onRequestToLoadPosts(ePostType.ALL);
                Objects.requireNonNull(m_RecyclerView.getAdapter()).notifyDataSetChanged();
            }

        }, 100);
    }

    @Override
    public void onFailureToCreatePost(Exception i_Reason) { } // irrelevant.
}