package com.zfdang.zsmth_android;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.listeners.OnTopicFragmentInteractionListener;
import com.zfdang.zsmth_android.models.Topic;

import java.util.List;

/**
 * used by HotTopicFragment & BoardTopicFragment
 */
public class BoardTopicRecyclerViewAdapter extends RecyclerView.Adapter<BoardTopicRecyclerViewAdapter.ViewHolder> {

    private final List<Topic> mTopics;
    private final OnTopicFragmentInteractionListener mListener;

    public BoardTopicRecyclerViewAdapter(List<Topic> items, OnTopicFragmentInteractionListener listener) {
        mTopics = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.board_topic_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mTopic = mTopics.get(position);
        Topic topic = holder.mTopic;


        if(topic.isCategory){
            holder.mSeperator.setVisibility(View.VISIBLE);
            holder.mSeperator.setText(topic.getCategory());

            holder.mTitleRow.setVisibility(View.GONE);
            holder.mAuthorRow.setVisibility(View.GONE);
            holder.mReplierRow.setVisibility(View.GONE);
            holder.mStatusRow.setVisibility(View.GONE);
        } else {
            holder.mSeperator.setVisibility(View.GONE);
            holder.mTitleRow.setVisibility(View.VISIBLE);
            holder.mAuthorRow.setVisibility(View.VISIBLE);
            holder.mReplierRow.setVisibility(View.VISIBLE);
            holder.mStatusRow.setVisibility(View.VISIBLE);

            holder.mTitle.setText(topic.getTitle());
            holder.mAuthor.setText(topic.getAuthor());
            holder.mReplier.setText(topic.getReplier());
            holder.mPublishDate.setText(topic.getPublishDate());
            holder.mReplyDate.setText(topic.getReplyDate());
            holder.mStatusSummary.setText(topic.getStatusSummary());

            if(topic.hasAttach()) {
                holder.mAttach.setVisibility(View.VISIBLE);
            } else {
                holder.mAttach.setVisibility(View.GONE);
            }

            if(topic.isSticky) {
                holder.mView.setBackgroundDrawable(ContextCompat.getDrawable(SMTHApplication.getAppContext(), R.drawable.recyclerview_sticky_item_bg));
            } else {
                holder.mView.setBackgroundDrawable(ContextCompat.getDrawable(SMTHApplication.getAppContext(), R.drawable.recyclerview_item_bg));
            }
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onTopicFragmentInteraction(holder.mTopic);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTopics.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mSeperator;
        public final TextView mTitle;
        public final TextView mAuthor;
        public final TextView mReplier;
        public final TextView mPublishDate;
        public final TextView mReplyDate;
        public final TextView mStatusSummary;
        public final ImageView mAttach;

        public final LinearLayout mTitleRow;
        public final LinearLayout mAuthorRow;
        public final LinearLayout mReplierRow;
        public final LinearLayout mStatusRow;

        public Topic mTopic;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mSeperator = (TextView) view.findViewById(R.id.category_name);
            mTitle = (TextView) view.findViewById(R.id.topic_title);
            mAuthor = (TextView) view.findViewById(R.id.topic_author);
            mReplier = (TextView) view.findViewById(R.id.topic_replier);
            mPublishDate = (TextView) view.findViewById(R.id.topic_public_date);
            mReplyDate = (TextView) view.findViewById(R.id.topic_reply_date);
            mStatusSummary = (TextView) view.findViewById(R.id.topic_status_summary);
            mAttach = (ImageView) view.findViewById(R.id.topic_status_attach);

            mTitleRow = (LinearLayout) view.findViewById(R.id.topic_title_row);
            mAuthorRow = (LinearLayout) view.findViewById(R.id.topic_author_row);
            mReplierRow = (LinearLayout) view.findViewById(R.id.topic_replier_row);
            mStatusRow = (LinearLayout) view.findViewById(R.id.topic_status_row);
        }

        @Override
        public String toString() {
            return mTopic.toString();
        }
    }
}
