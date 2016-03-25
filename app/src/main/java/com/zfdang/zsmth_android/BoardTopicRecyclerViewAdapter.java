package com.zfdang.zsmth_android;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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


        if(holder.mTopic.isCategory){
            holder.mSeperator.setVisibility(View.VISIBLE);
            holder.mTitle.setVisibility(View.GONE);
            holder.mAuthor.setVisibility(View.GONE);
            holder.mReplier.setVisibility(View.GONE);
            holder.mPublishDate.setVisibility(View.GONE);
            holder.mReplyDate.setVisibility(View.GONE);
            holder.mPublishLabel.setVisibility(View.GONE);
            holder.mReplyLabel.setVisibility(View.GONE);

            holder.mSeperator.setText(topic.getCategory());
        } else {
            holder.mSeperator.setVisibility(View.GONE);
            holder.mTitle.setVisibility(View.VISIBLE);
            holder.mAuthor.setVisibility(View.VISIBLE);
            holder.mReplier.setVisibility(View.VISIBLE);
            holder.mPublishDate.setVisibility(View.VISIBLE);
            holder.mReplyDate.setVisibility(View.VISIBLE);
            holder.mPublishLabel.setVisibility(View.VISIBLE);
            holder.mReplyLabel.setVisibility(View.VISIBLE);

            holder.mTitle.setText(topic.getTitle());
            holder.mAuthor.setText(topic.getAuthor());
            holder.mReplier.setText(topic.getReplier());
            holder.mPublishDate.setText(topic.getPublishDate());
            holder.mReplyDate.setText(topic.getReplyDate());
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
        public final TextView mReplyLabel;
        public final TextView mPublishLabel;
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
            mPublishLabel = (TextView) view.findViewById(R.id.topic_publish_label);
            mReplyLabel = (TextView) view.findViewById(R.id.topic_reply_label);
        }

        @Override
        public String toString() {
            return mTopic.toString();
        }
    }
}
