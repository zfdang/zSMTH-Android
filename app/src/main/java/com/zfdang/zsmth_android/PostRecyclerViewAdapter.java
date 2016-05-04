package com.zfdang.zsmth_android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.fresco.WrapContentDraweeView;
import com.zfdang.zsmth_android.models.Attachment;
import com.zfdang.zsmth_android.models.ContentSegment;
import com.zfdang.zsmth_android.models.Post;
import com.zfdang.zsmth_android.view.LinkTextView;

import java.util.ArrayList;
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


    public void inflateContentViewGroup(ViewGroup viewGroup, TextView contentView, final Post post) {
        // remove all child view in viewgroup
        viewGroup.removeAllViews();

        List<ContentSegment> contents = post.getContentSegments();
        if(contents == null) return;

        // the simple case, without any attachment
        if(contents.size() == 1) {
            viewGroup.addView(contentView);
            contentView.setText(contents.get(0).getSpanned());
            Linkify.addLinks(contentView, Linkify.WEB_URLS);

            return;
        }

        // there are multiple segments, add the first contentView first
        // contentView is always available, we don't have to inflate it again
        viewGroup.addView(contentView);
        contentView.setText(contents.get(0).getSpanned());

        final LayoutInflater inflater = (LayoutInflater) SMTHApplication.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(int i = 1; i < contents.size(); i++) {
            ContentSegment content = contents.get(i);

            if(content.getType() == ContentSegment.SEGMENT_IMAGE) {
                // Log.d("CreateView", "Image: " + content.getUrl());

                // Add the text layout to the parent layout
                WrapContentDraweeView image = (WrapContentDraweeView) inflater.inflate(R.layout.post_item_imageview, viewGroup, false);
                image.setImageFromStringURL(content.getUrl());


                // set onclicklistener
                image.setTag(R.id.image_tag, content.getImgIndex());
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = (int) v.getTag(R.id.image_tag);

                        Intent intent = new Intent(mListener, FSImageViewerActivity.class);

                        ArrayList<String> urls = new ArrayList<>();
                        List<Attachment> attaches = post.getAttachFiles();
                        for (Attachment attach: attaches) {
                            urls.add(attach.getImageSrc());
                        }

                        intent.putStringArrayListExtra(SMTHApplication.ATTACHMENT_URLS, urls);
                        intent.putExtra(SMTHApplication.ATTACHMENT_CURRENT_POS, position);
                        mListener.startActivity(intent);
                    }
                });

                // Add the text view to the parent layout
                viewGroup.addView(image);
            } else if (content.getType() == ContentSegment.SEGMENT_TEXT) {
                // Log.d("CreateView", "Text: " + content.getSpanned().toString());

                // Add the text layout to the parent layout
                LinkTextView tv = (LinkTextView) inflater.inflate(R.layout.post_item_content, viewGroup, false);
                tv.setText(content.getSpanned());
                Linkify.addLinks(tv, Linkify.WEB_URLS);

                // Add the text view to the parent layout
                viewGroup.addView(tv);
            }
        }

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mPost = mPosts.get(position);
        Post post = holder.mPost;

        holder.mPostAuthor.setText(post.getAuthor());
        holder.mPostPublishDate.setText(post.getFormatedDate());
        holder.mPostIndex.setText(post.getPosition());

        inflateContentViewGroup(holder.mViewGroup, holder.mPostContent, post);

        // http://stackoverflow.com/questions/4415528/how-to-pass-the-onclick-event-to-its-parent-on-android
        // http://stackoverflow.com/questions/24885223/why-doesnt-recyclerview-have-onitemclicklistener-and-how-recyclerview-is-dif
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
        holder.mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(mListener != null && mListener instanceof View.OnTouchListener) {
                    return ((View.OnTouchListener) mListener).onTouch(v, event);
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
        private final LinearLayout mViewGroup;
        public final LinkTextView mPostContent;
        public Post mPost;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mPostAuthor = (TextView) view.findViewById(R.id.post_author);
            mPostIndex = (TextView) view.findViewById(R.id.post_index);
            mPostPublishDate = (TextView) view.findViewById(R.id.post_publish_date);
            mViewGroup = (LinearLayout) view.findViewById(R.id.post_content_holder);
            mPostContent = (LinkTextView) view.findViewById(R.id.post_content);
        }

        @Override
        public String toString() {
            return mPost.toString();
        }
    }
}
