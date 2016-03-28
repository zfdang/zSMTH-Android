package com.zfdang.zsmth_android;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zfdang.zsmth_android.helpers.StringUtils;
import com.zfdang.zsmth_android.models.Post;
import com.zfdang.zsmth_android.models.Topic;

import java.util.List;

/**
 * used by HotPostFragment & BoardPostFragment
 */
public class PostRecyclerViewAdapter extends RecyclerView.Adapter<PostRecyclerViewAdapter.ViewHolder> {

    public interface OnItemClickListener {
        public void onItemClicked(int position, View v);
    }

    public interface OnItemLongClickListener {
        public boolean onItemLongClicked(int position, View v);
    }

    private final List<Post> mPosts;
    private final Activity mListener;

    public PostRecyclerViewAdapter(List<Post> posts, Activity listener) {
        mPosts = posts;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mPost = mPosts.get(position);
        Post post = holder.mPost;

        holder.mPostAuthor.setText(post.getAuthor());
        holder.mPostPublishDate.setText(StringUtils.getFormattedString(post.getDate()));
        String formattedText = post.getContent();
        Spanned result = Html.fromHtml(formattedText);
        holder.mPostContent.setText(result);

        if(mListener != null) {
            int mCurrentPageNo = ((PostListActivity) mListener).mCurrentPageNo;
            holder.mPostIndex.setText(Topic.getPostIndex(mCurrentPageNo, position));
        }

        // http://stackoverflow.com/questions/4415528/how-to-pass-the-onclick-event-to-its-parent-on-android
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener && mListener instanceof OnItemClickListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    ((OnItemClickListener) mListener).onItemClicked(position, holder.mView);
                }
            }
        });
        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (null != mListener && mListener instanceof OnItemLongClickListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    return ((OnItemLongClickListener) mListener).onItemLongClicked(position, holder.mView);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mPostAuthor;
        public final TextView mPostIndex;
        public final TextView mPostPublishDate;
        public final TextView mPostContent;
        public Post mPost;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mPostAuthor = (TextView) view.findViewById(R.id.post_author);
            mPostIndex = (TextView) view.findViewById(R.id.post_index);
            mPostPublishDate = (TextView) view.findViewById(R.id.post_publish_date);
            mPostContent = (TextView) view.findViewById(R.id.post_content);
        }

        @Override
        public String toString() {
            return mPost.toString();
        }
    }
}
