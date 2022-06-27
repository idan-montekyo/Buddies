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

import com.example.buddies.Model.Model;
import com.example.buddies.R;
import com.example.buddies.common.AppUtils;
import com.example.buddies.common.Post;
import com.example.buddies.common.UserProfile;
import com.example.buddies.interfaces.MVVM.IView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder>
                         implements IView {

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
        TextView m_NameTv, m_MeetingDate, m_CityTv, m_CreationDateTv, m_TimeTv, m_ContentTv;

        // Constructor
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            m_ImageIv = itemView.findViewById(R.id.card_post_image);
            m_MeetingDate = itemView.findViewById(R.id.card_post_date);
            m_NameTv = itemView.findViewById(R.id.card_post_creators_name);
            m_CityTv = itemView.findViewById(R.id.card_post_city);
            m_CreationDateTv = itemView.findViewById(R.id.card_post_creation_date);
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
        m_Context = holder.m_CreationDateTv.getContext();

        Post m_CurrentPost = m_Posts.get(position);

        holder.m_CityTv.setText(m_CurrentPost.getMeetingCity());
        holder.m_MeetingDate.setText(m_CurrentPost.getMeetingDate().getCreationDay() + "/" +
                m_CurrentPost.getMeetingDate().getCreationMonth());
        holder.m_TimeTv.setText(m_CurrentPost.getMeetingTime());
        holder.m_ContentTv.setText(m_CurrentPost.getPostContent());

        String creationDate = m_CurrentPost.getPostCreationDate().getCreationDay() +
                "/" + m_CurrentPost.getPostCreationDate().getCreationMonth() +
                "/" + m_CurrentPost.getPostCreationDate().getCreationYear();
        holder.m_CreationDateTv.setText(m_Context.getString(R.string.created_on) +
                " " + creationDate);

        // this.i_PostAdapterToUpdate = this;
        UserProfile currentUserProfile = Model.getInstance().resolveUserProfileFromUID(m_CurrentPost.getCreatorUserUID());

        if (currentUserProfile != null) {
            m_Holder.m_NameTv.setText(currentUserProfile.getFullName());

            if ((currentUserProfile.getProfileImageUri() != null) && (!currentUserProfile.getProfileImageUri().equals(""))) {
                AppUtils.loadImageUsingGlide(m_Context, Uri.parse(currentUserProfile.getProfileImageUri()), null, null, true, null, m_Holder.m_ImageIv);
            } else {
                AppUtils.loadImageUsingGlide(m_Context, AppUtils.getUriOfDrawable("dog_default_profile_rounded", m_Context), null, null, true, null, m_Holder.m_ImageIv);
            }
        } else {
            Toast.makeText(m_Context, "Model.m_UserProfile is null", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() { return m_Posts.size(); }

    public void updateAdapter(List<Post> i_NewPostsList) {
        this.m_Posts.addAll(i_NewPostsList);
    }

    public void updateAdapter(Post i_NewPost) { this.updateAdapter(Arrays.asList(i_NewPost)); }
}