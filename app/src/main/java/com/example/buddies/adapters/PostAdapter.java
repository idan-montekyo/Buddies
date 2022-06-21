package com.example.buddies.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.buddies.Model.Model;
import com.example.buddies.R;
import com.example.buddies.ViewModel.ViewModel;
import com.example.buddies.common.AppUtils;
import com.example.buddies.common.Post;
import com.example.buddies.common.UserProfile;
import com.example.buddies.interfaces.MVVM.IView;

import java.io.IOException;
import java.util.List;

public class PostAdapter extends    RecyclerView.Adapter<PostAdapter.PostViewHolder>
                         implements IView// ,
                                    // ILoadPostCardRequestEventHandler,
                                    // ILoadPostCardResponseEventHandler,
                                    // IResolveUIDToUserProfileRequestEventHandler,
                                    // IResolveUIDToUserProfileResponsesEventHandler
{

    private final ViewModel m_ViewModel = ViewModel.getInstance();

    private final List<Post> m_Posts;
    private MyPostListener m_Listener;

    private PostViewHolder m_Holder;
    private Context m_Context;
    PostAdapter i_PostAdapterToUpdate = null;

    // Interface for listeners
    public interface MyPostListener
    {
        void onPostClicked(int index, View view) throws IOException;
    }

    public void setListener(MyPostListener listener) { this.m_Listener = listener; }

    // Constructor
    public PostAdapter(List<Post> m_Posts) { this.m_Posts = m_Posts; }

    public class PostViewHolder extends RecyclerView.ViewHolder
    {
        ImageView m_ImageIv;
        TextView m_NameTv, m_CityTv, m_DateTv, m_TimeTv, m_ContentTv;

        // Constructor
        public PostViewHolder(@NonNull View itemView)
        {
            super(itemView);

            m_ImageIv = itemView.findViewById(R.id.card_post_image);
            m_NameTv = itemView.findViewById(R.id.card_post_creators_name);
            m_CityTv = itemView.findViewById(R.id.card_post_city);
            m_DateTv = itemView.findViewById(R.id.card_post_creation_date);
            m_TimeTv = itemView.findViewById(R.id.card_post_time);
            m_ContentTv = itemView.findViewById(R.id.card_post_content);

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (m_Listener != null)
                    {
                        try
                        {
                            m_Listener.onPostClicked(getAdapterPosition(), v);
                        }
                        catch (IOException exception)
                        {
                            exception.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    // Add the first cards to fill up the screen
    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_post, parent, false);
        return new PostViewHolder(view);
    }

    // Loading data into cards + Recycles card when scrolling
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position)
    {
        m_Holder = holder;
        m_Context = holder.m_DateTv.getContext();

        Post m_CurrentPost = m_Posts.get(position);

        holder.m_CityTv.setText(m_CurrentPost.getMeetingCity());
        holder.m_TimeTv.setText(m_CurrentPost.getMeetingTime());
        holder.m_ContentTv.setText(m_CurrentPost.getPostContent());

        String creationDate = m_CurrentPost.getPostCreationDate().getCreationDay() +
                "/" + m_CurrentPost.getPostCreationDate().getCreationMonth() +
                "/" + m_CurrentPost.getPostCreationDate().getCreationYear();
        holder.m_DateTv.setText(m_Context.getString(R.string.created_on) +
                " " + creationDate);

        // this.i_PostAdapterToUpdate = this;
        UserProfile currentUserProfile = Model.getInstance().resolveUserProfileFromUID(m_CurrentPost.getCreatorUserUID());

        if (currentUserProfile != null)
        {
            m_Holder.m_NameTv.setText(currentUserProfile.getFullName());

            if ((currentUserProfile.getProfileImageUri() != null) && (!currentUserProfile.getProfileImageUri().equals("")))
            {
                AppUtils.loadImageUsingGlide(m_Context, Uri.parse(currentUserProfile.getProfileImageUri()), null, null, true, null, m_Holder.m_ImageIv);
            }
            else
            {
                AppUtils.loadImageUsingGlide(m_Context, AppUtils.getUriOfDrawable("dog_default_profile_rounded", m_Context), null, null, true, null, m_Holder.m_ImageIv);
            }
        }
        else
        {
            Toast.makeText(m_Context, "Model.m_UserProfile is null", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() { return m_Posts.size(); }

    /*
    @Override
    public void onRequestToLoadPostCard(String i_CreatorUserUID, PostAdapter i_PostAdapterToUpdate)
    {
        m_ViewModel.onRequestToLoadPostCard(i_CreatorUserUID, i_PostAdapterToUpdate);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onSuccessToLoadPostCard(UserProfile i_UserProfile, PostAdapter i_PostAdapterToUpdate)
    {
        // this.m_CreatorUserProfile = i_UserProfile;

        m_Holder.m_NameTv.setText(i_UserProfile.getFullName());

        if ((i_UserProfile.getProfileImageUri() != null) && (!i_UserProfile.getProfileImageUri().equals("")))
        {
            Glide.with(m_Context).load(i_UserProfile.getProfileImageUri()).
                    circleCrop().into(m_Holder.m_ImageIv);
            System.out.println("TESTESTEST" + i_UserProfile.getProfileImageUri());
        }
        else
        {
            Glide.with(m_Context).load(m_Context.getDrawable(R.drawable.dog_default_profile_rounded)).
                    circleCrop().into(m_Holder.m_ImageIv);
        }
    }

    @Override
    public void onFailureToLoadPostCard(Exception i_Reason, PostAdapter i_PostAdapterToUpdate)
    {
        Toast.makeText(m_Context, i_Reason.getMessage(), Toast.LENGTH_SHORT).show();
    }
    */

    /*
    @Override
    public void onRequestToResolveUIDToUserProfile(String i_UserIDToResolve, IView i_Caller)
    {
        m_ViewModel.onRequestToResolveUIDToUserProfile(i_UserIDToResolve, i_Caller);
    }

    @Override
    public void onSuccessToResolveUIDToUserProfile(UserProfile i_ResolvedUserProfile, IView i_Caller)
    {
        m_Holder.m_NameTv.setText(i_ResolvedUserProfile.getFullName());

        if ((i_ResolvedUserProfile.getProfileImageUri() != null) && (!i_ResolvedUserProfile.getProfileImageUri().equals("")))
        {
            Glide.with(m_Context).load(i_ResolvedUserProfile.getProfileImageUri()).
                    circleCrop().into(m_Holder.m_ImageIv);
            System.out.println("TESTESTEST" + i_ResolvedUserProfile.getProfileImageUri());
        }
        else
        {
            Glide.with(m_Context).load(m_Context.getDrawable(R.drawable.dog_default_profile_rounded)).
                    circleCrop().into(m_Holder.m_ImageIv);
        }
    }

    @Override
    public void onFailureToResolveUIDToUserProfile(Exception i_Reason, IView i_Caller)
    {
        Toast.makeText(m_Context, i_Reason.getMessage(), Toast.LENGTH_SHORT).show();
    }
    */
}