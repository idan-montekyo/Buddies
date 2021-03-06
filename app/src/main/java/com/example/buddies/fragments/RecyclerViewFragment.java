package com.example.buddies.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.buddies.interfaces.MVVM.IView;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RecyclerViewFragment extends Fragment implements IView,
                                                              ILoadPostsRequestEventHandler,
                                                              ILoadPostsResponseEventHandler {

    public static final String RECYCLER_VIEW_FRAGMENT_TAG = "recycler_view_fragment";

    public static final String ACTION_KEY = "action";
    public static final String ACTION_MY_POSTS = "my_posts";
    public static final String ACTION_POSTS_I_COMMENTED_ON = "posts_i_commented_on";

    private Context m_Context = null;
    private final ViewModel m_ViewModel = ViewModel.getInstance();

    private CoordinatorLayout m_CoordinatorLayout;

    private final List<Post> m_Posts = new ArrayList<>();
    private PostAdapter m_PostAdapter;
    private String m_Action = "";

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Updates the list of all posts in HomeFragment when back pressed, to avoid crashing.
        FragmentManager fragmentManager = getParentFragmentManager();
        HomeFragment homeFragment = (HomeFragment)fragmentManager.findFragmentByTag(HomeFragment.HOME_FRAGMENT_TAG);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                fragmentManager.popBackStack();
                homeFragment.onRequestToLoadPosts(ePostType.ALL);
                homeFragment.onSuccessToCreatePost(null);
            }
        });

        Bundle bundle = this.getArguments();

        m_CoordinatorLayout = view.findViewById(R.id.recycler_view_coordinator_layout);
        TextView titleTv = view.findViewById(R.id.recycler_view_title);

        ImageView backBtn = view.findViewById(R.id.recycler_view_back_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(m_Context, 1));

        if (bundle != null) {
            m_Action = bundle.getString(RecyclerViewFragment.ACTION_KEY);
        }

        switch (m_Action) {
            case RecyclerViewFragment.ACTION_MY_POSTS:
                titleTv.setText(m_Context.getString(R.string.my_posts));
                onRequestToLoadPosts(ePostType.MY_POSTS);
                break;
            case RecyclerViewFragment.ACTION_POSTS_I_COMMENTED_ON:
                titleTv.setText(m_Context.getString(R.string.posts_i_commented_on));
                break;
        }

        m_PostAdapter = new PostAdapter(m_Posts);

        m_PostAdapter.setListener(new PostAdapter.MyPostListener() {
            @Override
            public void onPostClicked(int index, View view) throws IOException {
                Fragment thisFragment = getParentFragmentManager().findFragmentByTag(RECYCLER_VIEW_FRAGMENT_TAG);
                assert thisFragment != null;

                Post currentPost = m_Posts.get(index);
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
                                R.anim.fade_out,  // exit
                                R.anim.fade_in,   // popEnter
                                R.anim.slide_out) // popExit
                        .hide(thisFragment)
                        .add(R.id.root_main_activity, postFragmentToLaunch, ViewPostFragment.VIEW_POST_FRAGMENT_TAG)
                        .addToBackStack(null).commit();
            }
        });

        recyclerView.setAdapter(m_PostAdapter);
    }

    @Override
    public void onDestroy() {
        this.m_ViewModel.unregisterForEvents((IView) this);
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestToLoadPosts(ePostType type) {
        this.m_ViewModel.onRequestToLoadPosts(type);
    }

    @Override
    public void onRequestToLoadPostsByCity(String i_SearchedCity) { } // Irrelevant.

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
}