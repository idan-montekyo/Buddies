package com.example.buddies.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.buddies.Model.Model;
import com.example.buddies.R;
import com.example.buddies.ViewModel.ViewModel;
import com.example.buddies.adapters.PostAdapter;
import com.example.buddies.common.AppUtils;
import com.example.buddies.common.Comment;
import com.example.buddies.common.Post;
import com.example.buddies.common.UserProfile;
import com.example.buddies.enums.eDogGender;
import com.example.buddies.interfaces.CommentCreationEvent.ICommentCreationRequestEventHandler;
import com.example.buddies.interfaces.CommentCreationEvent.ICommentCreationResponseEventHandler;
import com.example.buddies.interfaces.LoadUserProfileEvent.ILoadUserProfileRequestEventHandler;
import com.example.buddies.interfaces.LoadUserProfileEvent.ILoadUserProfileResponseEventHandler;
import com.example.buddies.interfaces.MVVM.IView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ViewPostFragment extends    Fragment
                              implements IView,
                                         ILoadUserProfileRequestEventHandler,
                                         ILoadUserProfileResponseEventHandler,
                                         OnMapReadyCallback,
                                         ICommentCreationRequestEventHandler,
                                         ICommentCreationResponseEventHandler {

    public static final String VIEW_POST_FRAGMENT_TAG = "view_post_fragment";

    private UserProfile m_CurrentUserProfile = null;
    private Context m_Context = null;
    ViewModel m_ViewModel = ViewModel.getInstance();

    // TODO: continue same as done with posts:
//    public static List<Comment> m_Comments = new ArrayList<>();
//    @SuppressLint("StaticFieldLeak")
//    public static CommentAdapter m_commentAdapter;

    RecyclerView m_RecyclerView;

    ImageView postCreatorImageIv;
    TextView postCreatorFullNameTv;
    TextView postCreatorDogsGenderTv;
    TextView cityTv;
    TextView streetTv;
    TextView timeTv;
    TextView contentTv;
    Button showHideLocationBtn;
    MapView meetingLocationMapView;
    ImageView userImageIv;
    EditText addCommentEt;
    ImageButton sendCommentBtn;

    GoogleMap m_GoogleMap = null;
    Marker m_CurrMarker = null;
    UserProfile currentCreatorUserProfile = null;
    Post currentPost = null;

    @Override
    public void onAttach(@NonNull Context context) {
        this.m_Context = context;
        super.onAttach(this.m_Context);
    }

    // Register for events in ViewModel.
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        m_ViewModel.registerForEvents((IView)this);
        super.onCreate(savedInstanceState);
    }

    // Inflate fragment_view_post
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final String SHOW_LOCATION = m_Context.getString(R.string.show_location);
        final String HIDE_LOCATION = m_Context.getString(R.string.hide_location);

        ImageButton backBtn = view.findViewById(R.id.view_post_back_image_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { getParentFragmentManager().popBackStack(); }
        });

        Bundle viewPostFragmentArguments = getArguments();

        String userProfileJsonString = viewPostFragmentArguments.getString("userProfileJsonString");
        currentCreatorUserProfile = AppUtils.getGsonParser().fromJson(userProfileJsonString, UserProfile.class);

        String currentPostJsonString = viewPostFragmentArguments.getString("currentPostJsonString");
        currentPost = AppUtils.getGsonParser().fromJson(currentPostJsonString, Post.class);

        postCreatorImageIv = view.findViewById(R.id.view_post_creator_image_view);
        postCreatorFullNameTv = view.findViewById(R.id.view_post_creator_full_name_text_view);
        postCreatorDogsGenderTv = view.findViewById(R.id.view_post_creator_dog_gender_text_view);
        cityTv = view.findViewById(R.id.view_post_city_text_view);
        streetTv = view.findViewById(R.id.view_post_street_text_view);
        timeTv = view.findViewById(R.id.view_post_time_text_view);
        contentTv = view.findViewById(R.id.view_post_content_text_view);
        showHideLocationBtn = view.findViewById(R.id.view_post_location_button);
        meetingLocationMapView = view.findViewById(R.id.view_post_map_view);

        AppUtils.loadImageUsingGlide(
                ViewPostFragment.this.m_Context,
                Uri.parse(currentCreatorUserProfile.getProfileImageUri()),
                null,
                null,
                true,
                null,
                postCreatorImageIv);

        postCreatorFullNameTv.setText(currentCreatorUserProfile.getFullName());
        postCreatorDogsGenderTv.setText(currentCreatorUserProfile.getDogGender().toString());
        cityTv.setText(currentPost.getMeetingCity());
        streetTv.setText(currentPost.getMeetingStreet());
        timeTv.setText(currentPost.getMeetingTime());
        contentTv.setText(currentPost.getPostContent());
        meetingLocationMapView.onCreate(savedInstanceState);
        meetingLocationMapView.onResume();
        meetingLocationMapView.getMapAsync(ViewPostFragment.this);

        showHideLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (meetingLocationMapView.getVisibility() == View.GONE) {
                    meetingLocationMapView.setVisibility(View.VISIBLE);
                    showHideLocationBtn.setText(HIDE_LOCATION);
                } else {
                    meetingLocationMapView.setVisibility(View.GONE);
                    showHideLocationBtn.setText(SHOW_LOCATION);
                }
            }
        });

        m_RecyclerView = view.findViewById(R.id.view_post_recycler_view);
        // TODO: add adapter mechanism
        //  + create "CommentCard" layout (instagram-like)
        //  + load comments from FireBase.

        userImageIv = view.findViewById(R.id.view_post_user_image_view);
        // TODO: insert currentUser's image.
        m_CurrentUserProfile = Model.getInstance().resolveUserProfileFromUID(Model.getInstance().getCurrentUserUID());
        AppUtils.loadImageUsingGlide(
                this.m_Context,
                Uri.parse(m_CurrentUserProfile.getProfileImageUri()),
                null,
                null,
                true,
                null,
                userImageIv);

        addCommentEt = view.findViewById(R.id.view_post_add_comment_edit_text);
        sendCommentBtn = view.findViewById(R.id.view_post_send_comment_image_button);
        sendCommentBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                // TODO: if addCommentEt is not-empty:
                //  1. save comment to FireBase
                //  2. notify (refresh) m_RecyclerView
                //  3. addCommentEt.settext("")

                String commentContent = addCommentEt.getText().toString();
                if (!commentContent.equals("")) {

                    String userUID = Model.getInstance().getCurrentUserUID();
                    String userImageUri = m_CurrentUserProfile.getProfileImageUri();

                    ZoneId zoneId = ZoneId.of("Israel");
                    int year = (Year.now(zoneId)).getValue();
                    int month = (YearMonth.now(zoneId)).getMonthValue();
                    int day = (MonthDay.now(zoneId)).getDayOfMonth();

                    Comment newComment = new Comment(userUID, userImageUri,
                                                     currentPost.getPostCreationDateTimeAsLong(),
                                                     commentContent, year, month, day);
                    onRequestToCreateComment(newComment);
                }
            }
        });
    }

    @Override
    public void onLoadProfile() { m_ViewModel.onLoadProfile(); }

    @Override
    public void onSuccessToLoadProfile(UserProfile i_UserProfile) {
        this.m_CurrentUserProfile = i_UserProfile;
        Uri uriToLoad;

        if  (!this.m_CurrentUserProfile.getProfileImageUri().equals("")) {
            uriToLoad = Uri.parse(i_UserProfile.getProfileImageUri());
        } else {
            uriToLoad = AppUtils.getUriOfDrawable("dog_default_profile_rounded", this.m_Context);
        }

        RequestListener<Drawable> glideListener = new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                postCreatorFullNameTv.setText(i_UserProfile.getFullName());
                postCreatorDogsGenderTv.setText(i_UserProfile.getDogGender().toString());
                return false;
            }
        };

        AppUtils.loadImageUsingGlide(
                ViewPostFragment.this.m_Context,
                uriToLoad,
                null,
                null,
                true,
                glideListener,
                postCreatorImageIv);
    }

    @Override
    public void onFailureToLoadProfile(Exception i_Reason) {
        Toast.makeText(m_Context, i_Reason.getMessage(), Toast.LENGTH_SHORT).show();
    }

    // Unregister for events in ViewModel.
    @Override
    public void onDestroy() {
        m_ViewModel.unregisterForEvents((IView)this);
        super.onDestroy();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        m_GoogleMap = googleMap;

        // If we want to disable moving the map, uncomment this. (Source: https://stackoverflow.com/a/28452115/2196301)
        // m_Map.getUiSettings().setAllGesturesEnabled(false);

        m_GoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (m_CurrMarker == null) {
            m_CurrMarker = m_GoogleMap.addMarker(new MarkerOptions().position(currentPost.getMeetingLocation())
                    .title(currentPost.getMeetingStreet()).snippet(currentPost.getMeetingCity()));
        } else {
            m_CurrMarker.setPosition(currentPost.getMeetingLocation());
        }

        // Center the map according to the chosen coordinates (Source: https://stackoverflow.com/a/16342378/2196301)
        m_GoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPost.getMeetingLocation(), 14f));
    }

    @Override
    public void onRequestToCreateComment(Comment i_Comment) { m_ViewModel.onRequestToCreateComment(i_Comment); }

    @Override
    public void onSuccessToCreateComment() { addCommentEt.setText(""); }

    @Override
    public void onFailureToCreateComment(Exception i_Reason) {
        AppUtils.printDebugToLogcat("ViewPostFragment", "onFailureToCreateComment", i_Reason.toString());
        Toast.makeText(m_Context, "Comment failed - " + i_Reason.getMessage(), Toast.LENGTH_LONG).show();
    }
}
