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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.buddies.Model.Model;
import com.example.buddies.R;
import com.example.buddies.ViewModel.ViewModel;
import com.example.buddies.adapters.CommentAdapter;
import com.example.buddies.common.AppUtils;
import com.example.buddies.common.Comment;
import com.example.buddies.common.Post;
import com.example.buddies.common.UserProfile;
import com.example.buddies.interfaces.CommentCreationEvent.ICommentCreationRequestEventHandler;
import com.example.buddies.interfaces.CommentCreationEvent.ICommentCreationResponseEventHandler;
import com.example.buddies.interfaces.LoadPostCommentsEvent.ILoadPostCommentsRequestEventHandler;
import com.example.buddies.interfaces.LoadPostCommentsEvent.ILoadPostCommentsResponsesEventHandler;
import com.example.buddies.interfaces.LoadUserProfileEvent.ILoadUserProfileRequestEventHandler;
import com.example.buddies.interfaces.LoadUserProfileEvent.ILoadUserProfileResponseEventHandler;
import com.example.buddies.interfaces.MVVM.IView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class ViewPostFragment extends    Fragment
                              implements IView,
                                         ILoadUserProfileRequestEventHandler,
                                         ILoadUserProfileResponseEventHandler,
                                         OnMapReadyCallback,
                                         ICommentCreationRequestEventHandler,
                                         ICommentCreationResponseEventHandler,
                                         ILoadPostCommentsRequestEventHandler,
                                         ILoadPostCommentsResponsesEventHandler
{
    public static final String VIEW_POST_FRAGMENT_TAG           = "view_post_fragment";

    private Context            m_Context                        = null;
    private ViewModel          m_ViewModel                      = ViewModel.getInstance();
    private Bundle             m_SavedInstanceState             = null;
    private Bundle             m_ViewPostFragmentArguments      = null;
    private Button             m_Button_ShowHideLocation        = null;

    private final float        m_DisabledElementsAlphaValue     = 0.15f;

    private RecyclerView       m_RecyclerView                   = null;
    private MapView            m_MapView_MeetingLocationViewer  = null;
    private ImageView          m_ImageView_PostCreatorImage     = null;
    private ImageView          m_ImageView_UserImage            = null;
    private ImageButton        m_Button_SendComment             = null;
    private ImageButton        m_Button_GoBack                  = null;

    private EditText           m_EditText_AddComment            = null;
    private TextView           m_TextView_PostCreatorFullName   = null;
    private TextView           m_TextView_PostCreatorDogsGender = null;
    private TextView           m_TextView_CityOfMeeting         = null;
    private TextView           m_TextView_StreetOfMeeting       = null;
    private TextView           m_TextView_TimeOfMeeting         = null;
    private TextView           m_TextView_DateOfMeeting         = null;
    private TextView           m_TextView_ContentOfPost         = null;

    private GoogleMap          m_MapObject                      = null;
    private Marker             m_CurrentMapMarker               = null;

    private Post               m_CurrentPost                    = null;
    private UserProfile        m_CurrentUserProfile             = null;
    private UserProfile        m_CurrentCreatorUserProfile      = null;

    private CommentAdapter     m_CommentAdapter                 = null;
    private List<Comment>      m_PostComments                   = new ArrayList<Comment>();

    private String             m_PostID                         = null;
    private String             m_UserProfileJsonString          = null;
    private String             m_CurrentPostJsonString          = null;

    // TODO: add CheckBox to subscribe/unsubscribe to FireBaseMessaging with postID.

    @Override
    public void onAttach(@NonNull Context context)
    {
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.m_SavedInstanceState = savedInstanceState;

        this.onRequestToLoadProfile();

        final String SHOW_LOCATION = m_Context.getString(R.string.show_location);
        final String HIDE_LOCATION = m_Context.getString(R.string.hide_location);

        this.m_Button_GoBack = view.findViewById(R.id.view_post_back_image_button);
        this.m_Button_GoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { getParentFragmentManager().popBackStack(); }
        });

        this.m_ImageView_PostCreatorImage      = view.findViewById(R.id.view_post_creator_image_view);
        this.m_TextView_PostCreatorFullName   = view.findViewById(R.id.view_post_creator_full_name_text_view);
        this.m_TextView_PostCreatorDogsGender = view.findViewById(R.id.view_post_creator_dog_gender_text_view);
        this.m_TextView_CityOfMeeting                  = view.findViewById(R.id.view_post_city_text_view);
        this.m_TextView_StreetOfMeeting                = view.findViewById(R.id.view_post_street_text_view);
        this.m_TextView_TimeOfMeeting                  = view.findViewById(R.id.view_post_time_text_view);
        this.m_TextView_DateOfMeeting                  = view.findViewById(R.id.view_post_date_text_view);
        this.m_TextView_ContentOfPost               = view.findViewById(R.id.view_post_content_text_view);
        this.m_Button_ShowHideLocation     = view.findViewById(R.id.view_post_location_button);
        this.m_MapView_MeetingLocationViewer  = view.findViewById(R.id.view_post_map_view);

        this.loadArgumentsFromBundle();

        this.m_PostID = m_CurrentPost.getPostID();

        AppUtils.loadImageUsingGlide(
                ViewPostFragment.this.m_Context,
                Uri.parse(m_CurrentCreatorUserProfile.getProfileImageUri()),
                null,
                null,
                true,
                null,
                m_ImageView_PostCreatorImage);

        this.m_TextView_PostCreatorFullName.setText(m_CurrentCreatorUserProfile.getFullName());
        this.m_TextView_PostCreatorDogsGender.setText(m_CurrentCreatorUserProfile.getDogGender().toString());
        this.m_TextView_CityOfMeeting.setText(m_CurrentPost.getMeetingCity());
        this.m_TextView_StreetOfMeeting.setText(m_CurrentPost.getMeetingStreet());
        this.m_TextView_DateOfMeeting.setText(m_CurrentPost.getMeetingDate().toString());
        this.m_TextView_TimeOfMeeting.setText(m_CurrentPost.getMeetingTime());
        this.m_TextView_ContentOfPost.setText(m_CurrentPost.getPostContent());
        this.m_MapView_MeetingLocationViewer.onCreate(this.m_SavedInstanceState);
        this.m_MapView_MeetingLocationViewer.onResume();
        this.m_MapView_MeetingLocationViewer.getMapAsync(ViewPostFragment.this);

        this.m_Button_ShowHideLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If the MapView is hidden - show it
                if (ViewPostFragment.this.m_MapView_MeetingLocationViewer.getVisibility() == View.GONE) {
                    ViewPostFragment.this.m_MapView_MeetingLocationViewer.setVisibility(View.VISIBLE);
                    ViewPostFragment.this.m_Button_ShowHideLocation.setText(HIDE_LOCATION);
                }
                // If the MapView is shown - hide it
                else {
                    ViewPostFragment.this.m_MapView_MeetingLocationViewer.setVisibility(View.GONE);
                    ViewPostFragment.this.m_Button_ShowHideLocation.setText(SHOW_LOCATION);
                }
            }
        });

        this.m_ImageView_UserImage    = view.findViewById(R.id.view_post_user_image_view);
        this.m_EditText_AddComment   = view.findViewById(R.id.view_post_add_comment_edit_text);
        this.m_Button_SendComment = view.findViewById(R.id.view_post_send_comment_image_button);

        if (!Model.getInstance().isCurrentUserAnonymous()) {
            String currentUserID = Model.getInstance().getCurrentUserUID();
            UserProfile currentLoggedOnUserProfile = Model.getInstance().resolveUserProfileFromUID(currentUserID);

            AppUtils.loadImageUsingGlide(
                    this.m_Context,
                    Uri.parse(currentLoggedOnUserProfile.getProfileImageUri()),
                    null,
                    null,
                    true,
                    null,
                    m_ImageView_UserImage);

            m_Button_SendComment.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View v) {
                    // If the user wants to send an empty comment
                    if (m_EditText_AddComment.getText().toString().isEmpty()) {
                        Snackbar.make(ViewPostFragment.this.m_Context, view, "Empty comment cannot be sent", 1);
                    } else {
                        // Save the new comment to FireBase
                        ViewPostFragment.this.onRequestToCreateComment(currentUserID, currentLoggedOnUserProfile.getProfileImageUri(), m_EditText_AddComment.getText().toString(), ViewPostFragment.this.m_PostID);
                    }
                }
            });
        } else {
            m_ImageView_UserImage.setEnabled(false);
            m_ImageView_UserImage.setAlpha(m_DisabledElementsAlphaValue);

            m_EditText_AddComment.setEnabled(false);
            m_EditText_AddComment.setAlpha(m_DisabledElementsAlphaValue);

            m_Button_SendComment.setEnabled(false);
            m_Button_SendComment.setAlpha(m_DisabledElementsAlphaValue);
        }

        this.m_RecyclerView = view.findViewById(R.id.view_post_recycler_view);
        this.initializeRecyclerView();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initializeRecyclerView() {
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this.m_Context);
        m_RecyclerView.setLayoutManager(manager);

        // Load comments from firebase
        this.onRequestToLoadPostComments(this.m_PostID);

        /*
        // Load comments from firebase
        this.m_PostComments = Model.getInstance().getAllPostComments(this.m_PostID);

        // If no comments found for this post, initialize an empty list with no comments.
        if (this.m_PostComments == null) {
            this.m_PostComments = new ArrayList<Comment>();
        }

        // Create the CommentAdapter which will be bound to the RecyclerView
        this.m_CommentAdapter = new CommentAdapter(this.m_Context, this.m_PostComments);

        this.m_RecyclerView.setAdapter(this.m_CommentAdapter);
        */
    }

    private void loadArgumentsFromBundle() {
        this.m_ViewPostFragmentArguments = getArguments();

        // Create custom objects from their json representation which exists in the bundle (Source: https://stackoverflow.com/a/46591617/2196301)
        this.m_UserProfileJsonString = m_ViewPostFragmentArguments.getString("userProfileJsonString");
        this.m_CurrentCreatorUserProfile = AppUtils.getGsonParser().fromJson(this.m_UserProfileJsonString, UserProfile.class);

        this.m_CurrentPostJsonString = m_ViewPostFragmentArguments.getString("currentPostJsonString");
        this.m_CurrentPost = AppUtils.getGsonParser().fromJson(this.m_CurrentPostJsonString, Post.class);
    }

    @Override
    public void onRequestToLoadProfile() { m_ViewModel.onRequestToLoadProfile(); }

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
                m_TextView_PostCreatorFullName.setText(i_UserProfile.getFullName());
                m_TextView_PostCreatorDogsGender.setText(i_UserProfile.getDogGender().toString());

                return false;
            }
        };

//        AppUtils.loadImageUsingGlide(
//                ViewPostFragment.this.m_Context,
//                uriToLoad,
//                null,
//                null,
//                true,
//                glideListener,
//                m_ImageView_PostCreatorImage);
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
        m_MapObject = googleMap;

        // If we want to disable moving the map, uncomment this. (Source: https://stackoverflow.com/a/28452115/2196301)
        // m_Map.getUiSettings().setAllGesturesEnabled(false);

        m_MapObject.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (m_CurrentMapMarker == null) {
            m_CurrentMapMarker = m_MapObject.addMarker(new MarkerOptions().position(m_CurrentPost.getMeetingLocation())
                    .title(m_CurrentPost.getMeetingStreet()).snippet(m_CurrentPost.getMeetingCity()));
        } else {
            m_CurrentMapMarker.setPosition(m_CurrentPost.getMeetingLocation());
        }

        // Center the map according to the chosen coordinates (Source: https://stackoverflow.com/a/16342378/2196301)
        m_MapObject.moveCamera(CameraUpdateFactory.newLatLngZoom(m_CurrentPost.getMeetingLocation(), 14f));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestToCreateComment(String i_CreatorUserUID, String i_UserProfileImageUri, String i_CommentContent, String i_PostID) {
        m_ViewModel.onRequestToCreateComment(i_CreatorUserUID, i_UserProfileImageUri, i_CommentContent, i_PostID);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onSuccessToCreateComment(Comment i_Comment) {
        // Refresh the RecyclerView
        ((CommentAdapter)this.m_RecyclerView.getAdapter()).updateAdapter(i_Comment);

        // Scroll the RecyclerView to the end automatically (Source: https://stackoverflow.com/a/27063152/2196301)
        this.m_RecyclerView.scrollToPosition(this.m_PostComments.size() - 1);

        //  Clear the messages edit text
        m_EditText_AddComment.setText("");

        // TODO: add this post to PostsICommentedOn list, for future use.

        // Send notification only when you reply on someone else's posts.
        if (!m_CurrentPost.getCreatorUserUID().equals(i_Comment.getCreatorUserUID())) {
            AppUtils.initializeDataMessageAndSendToServer(m_Context, m_CurrentPost, i_Comment,
                                                          m_CurrentCreatorUserProfile, m_CurrentUserProfile);
        }
    }

    @Override
    public void onFailureToCreateComment(Exception i_Reason) {
        AppUtils.printDebugToLogcat("ViewPostFragment", "onFailureToCreateComment", i_Reason.toString());
        Toast.makeText(m_Context, "Comment failed - " + i_Reason.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestToLoadPostComments(String i_PostID)
    {
        this.m_ViewModel.onRequestToLoadPostComments(i_PostID);
    }

    @Override
    public void onSuccessToLoadPostComments(List<Comment> i_Comments)
    {
        this.m_PostComments = i_Comments;

        // If no comments found for this post, initialize an empty list with no comments.
        if (this.m_PostComments == null)
        {
            this.m_PostComments = new ArrayList<Comment>();
        }

        // Create the CommentAdapter which will be bound to the RecyclerView
        this.m_CommentAdapter = new CommentAdapter(this.m_Context, this.m_PostComments);

        this.m_RecyclerView.setAdapter(this.m_CommentAdapter);
    }

    @Override
    public void onFailureToLoadPostComments(Exception i_Reason)
    {
        Toast.makeText(this.m_Context, i_Reason.getMessage(), Toast.LENGTH_LONG).show();
    }
}
