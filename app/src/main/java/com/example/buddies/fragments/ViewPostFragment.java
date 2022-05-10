package com.example.buddies.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.buddies.R;
import com.example.buddies.ViewModel.ViewModel;
import com.example.buddies.interfaces.MVVM.IView;

public class ViewPostFragment extends Fragment implements IView {

    public static final String VIEW_POST_FRAGMENT_TAG = "view_post_fragment";

    ViewModel m_ViewModel = ViewModel.getInstance();

    RecyclerView m_RecyclerView;

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
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backBtn = view.findViewById(R.id.view_post_back_image_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { getParentFragmentManager().popBackStack(); }
        });

        ImageView postCreatorImageIv = view.findViewById(R.id.view_post_creator_image_view);
        // TODO: insert creator image.
        TextView postCreatorFullNameTv = view.findViewById(R.id.view_post_creator_full_name_text_view);
        TextView postCreatorDogsGenderTv = view.findViewById(R.id.view_post_creator_dog_gender_text_view);
        // TODO: insert creator full name, and dog's gender.

        TextView cityTv = view.findViewById(R.id.view_post_city_text_view);
        TextView streetTv = view.findViewById(R.id.view_post_street_text_view);
        TextView timeTv = view.findViewById(R.id.view_post_time_text_view);
        TextView contentTv = view.findViewById(R.id.view_post_content_text_view);
        // TODO: insert data of post.

        Button showHideLocationBtn = view.findViewById(R.id.view_post_location_button);
        // TODO: add map mechanism (googlemap, latlng, ...) + load coordinates.


        m_RecyclerView = view.findViewById(R.id.view_post_recycler_view);
        // TODO: add adapter mechanism
        //  + create "CommentCard" layout (instagram-like)
        //  + load comments from FireBase.

        ImageView userImageIv = view.findViewById(R.id.view_post_user_image_view);
        // TODO: insert currentUser's image.

        EditText addCommentEt = view.findViewById(R.id.view_post_add_comment_edit_text);
        ImageButton sendCommentBtn = view.findViewById(R.id.view_post_send_comment_image_button);
        sendCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: if addCommentEt is not-empty:
                //  1. save comment to FireBase
                //  2. notify (refresh) m_RecyclerView
                //  3. addCommentEt.settext("")
            }
        });
    }

    // Unregister for events in ViewModel.
    @Override
    public void onDestroy() {
        m_ViewModel.unregisterForEvents((IView)this);
        super.onDestroy();
    }
}
