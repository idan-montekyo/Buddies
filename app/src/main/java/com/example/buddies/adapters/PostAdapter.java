package com.example.buddies.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.buddies.R;
import com.example.buddies.ViewModel.ViewModel;
import com.example.buddies.common.Post;
import com.example.buddies.common.UserProfile;
import com.example.buddies.interfaces.LoadPostCardEvent.ILoadPostCardRequestEventHandler;
import com.example.buddies.interfaces.LoadPostCardEvent.ILoadPostCardResponseEventHandler;

import java.io.IOException;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder>
        implements ILoadPostCardRequestEventHandler,
                   ILoadPostCardResponseEventHandler {

    private final ViewModel m_ViewModel = ViewModel.getInstance();

    private final List<Post> m_Posts;
    private MyPostListener m_Listener;

    private PostViewHolder m_Holder;
    private Context m_Context;

    // Interface for listeners
    public interface MyPostListener {
        void onPostClicked(int index, View view) throws IOException;
    }

    public void setListener(MyPostListener listener) { this.m_Listener = listener; }

    // Constructor
    public PostAdapter(List<Post> m_Posts) { this.m_Posts = m_Posts; }

    public class PostViewHolder extends RecyclerView.ViewHolder {

        ImageView m_ImageIv;
        TextView m_NameTv, m_CityTv, m_DateTv, m_TimeTv, m_ContentTv;

        // Constructor
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            m_ImageIv = itemView.findViewById(R.id.card_post_image);
            m_NameTv = itemView.findViewById(R.id.card_post_creators_name);
            m_CityTv = itemView.findViewById(R.id.card_post_city);
            m_DateTv = itemView.findViewById(R.id.card_post_creation_date);
            m_TimeTv = itemView.findViewById(R.id.card_post_time);
            m_ContentTv = itemView.findViewById(R.id.card_post_content);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (m_Listener != null) {
                        try {
                            m_Listener.onPostClicked(getAdapterPosition(), v);
                        } catch (IOException exception) {
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
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_post, parent, false);
        return new PostViewHolder(view);
    }

    // Loading data into cards + Recycles card when scrolling
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {

        m_Holder = holder;
        m_Context = holder.m_DateTv.getContext();

        Post post = m_Posts.get(position);

        holder.m_CityTv.setText(post.getMeetingCity());
        holder.m_TimeTv.setText(post.getMeetingTime());
        holder.m_ContentTv.setText(post.getPostContent());

        String creationDate = post.getPostCreationDate().getPostCreationDay() +
                "/" + post.getPostCreationDate().getPostCreationMonth() +
                "/" + post.getPostCreationDate().getPostCreationYear();
        holder.m_DateTv.setText(m_Context.getString(R.string.created_on) +
                " " + creationDate);

        onLoadPostCard(post.getCreatorUserUID(), this);
    }

    @Override
    public int getItemCount() { return m_Posts.size(); }

    @Override
    public void onLoadPostCard(String i_CreatorUserUID, PostAdapter i_PostAdapterToUpdate) {
        m_ViewModel.onLoadPostCard(i_CreatorUserUID, i_PostAdapterToUpdate);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onSuccessToLoadPostCard(UserProfile i_UserProfile, PostAdapter i_PostAdapterToUpdate) {

        m_Holder.m_NameTv.setText(i_UserProfile.getFullName());

        if ((i_UserProfile.getProfileImageUri() != null) && (!i_UserProfile.getProfileImageUri().equals(""))) {
            Glide.with(m_Context).load(i_UserProfile.getProfileImageUri()).
                    circleCrop().into(m_Holder.m_ImageIv);
            System.out.println("TESTESTEST" + i_UserProfile.getProfileImageUri());
        } else {
            Glide.with(m_Context).load(m_Context.getDrawable(R.drawable.dog_default_profile_rounded)).
                    circleCrop().into(m_Holder.m_ImageIv);
        }
    }

    @Override
    public void onFailureToLoadPostCard(Exception i_Reason, PostAdapter i_PostAdapterToUpdate) {
        Toast.makeText(m_Context, i_Reason.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
