package com.example.buddies.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.buddies.Model.Model;
import com.example.buddies.R;
import com.example.buddies.common.AppUtils;
import com.example.buddies.common.Comment;
import com.example.buddies.common.UserProfile;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder>
{
    private Context m_OwnerContext = null;
    List<Comment> m_CommentsOfPost = new ArrayList<Comment>();

    /**
     * Constructor of the class "CommentAdapter"
     * @param i_Context      - The context which has created the adapter
     * @param commentsOfPost - The list which contains all the comments that should be displayed
     */
    public CommentAdapter(Context i_Context, List<Comment> commentsOfPost)
    {
        super();
        this.m_OwnerContext = i_Context;
        m_CommentsOfPost = commentsOfPost;
    }

    /**
     * This class is in use in order to represent a cell in the RecyclerView object.
     * The ViewHolder receives an inflated view and reference it's visual components for handling the current item
     * Note that there's need for only 1 instance of this class.
     */
    public class CommentViewHolder extends RecyclerView.ViewHolder
    {
        ImageView m_ImageView_ProfileImageOfCommenter = null;
        TextView m_TextView_NameOfCommenter = null;
        TextView m_TextView_TextOfComment = null;

        public CommentViewHolder(@NonNull View itemView)
        {
            super(itemView);

            // Get reference to visual components here and define a behaviour for them with event handlers and listeners
            m_ImageView_ProfileImageOfCommenter = (ImageView) itemView.findViewById(R.id.ImageView_ProfileImageOfCommenter);
            m_TextView_NameOfCommenter          = (TextView)  itemView.findViewById(R.id.TextView_NameOfCommenter);
            m_TextView_TextOfComment            = (TextView)  itemView.findViewById(R.id.TextView_TextOfComment);
        }
    }

    /**
     * onCreateViewHolder()
     * Purpose:
     *     This method allows to create the suitable ViewHolder for the current view.
     *     Note that this method is being called only as the amount of the first cells in the RecyclerView.
     * @param parent   - The parent of the given view
     * @param viewType - The type of the given view
     * @return The ViewHolder of the current given view
     */
    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        // Inflate the suitable view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_comment_cell, parent, false);

        // Create a new ViewHolder for the current inflated view
        CommentViewHolder commentViewHolder = new CommentViewHolder(view);

        return commentViewHolder;
    }

    /**
     * onBindViewHolder()
     * Purpose:
     *     This method is responsible to bind the current element inside the given ViewHolder,
     *     in order to represent it in the RecyclerView
     * @param holder - The ViewHolder object which the current element should be bound to
     * @param position - The index of the current element to bind to the given ViewHolder
     */
    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position)
    {
        AppUtils.printDebugToLogcat("CommentAdapter", "onBindViewHolder", "just got into here.");
        Comment comment = m_CommentsOfPost.get(position);

        UserProfile commenterUserProfile = Model.getInstance().resolveUserProfileFromUID(comment.getCreatorUserUID());

        // load to holder's visual components (holder.xxx) the data related to the current comment that needs to be shown.
        AppUtils.loadImageUsingGlide(m_OwnerContext, Uri.parse(commenterUserProfile.getProfileImageUri()), null, null, true, null, holder.m_ImageView_ProfileImageOfCommenter);
        holder.m_TextView_NameOfCommenter.setText(commenterUserProfile.getFullName());
        holder.m_TextView_TextOfComment.setText(comment.getCommentContent());
    }

    @Override
    public int getItemCount()
    {
        return m_CommentsOfPost.size();
    }

    /**
     * getItemViewType()
     * Purpose:
     *     This method is responsible to retrieve the type of the given view, which passed as it's index
     *     in the RecyclerView.
     *     If there's only one type of layout - it is not necessary to implement this method
     * @param position   - The position of the view in the RecyclerView
     * @return <unnamed> - The type of the given view
     */
    @Override
    public int getItemViewType(int position)
    {
        return super.getItemViewType(position);
    }

    public void updateAdapter(List<Comment> i_NewCommentsList)
    {
        this.m_CommentsOfPost.addAll(i_NewCommentsList);
        this.notifyDataSetChanged();
    }

    public void updateAdapter(Comment i_NewComment)
    {
        this.updateAdapter(Arrays.asList(i_NewComment));
    }
}