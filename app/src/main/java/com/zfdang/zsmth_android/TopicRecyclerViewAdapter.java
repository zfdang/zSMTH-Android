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
public class TopicRecyclerViewAdapter extends RecyclerView.Adapter<TopicRecyclerViewAdapter.ViewHolder> {

    private final List<Topic> mValues;
    private final OnTopicFragmentInteractionListener mListener;

    public TopicRecyclerViewAdapter(List<Topic> items, OnTopicFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.topic_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);


        if(holder.mItem.isCategory){
            holder.mSeperator.setVisibility(View.VISIBLE);
            holder.mTopicTitle.setVisibility(View.GONE);
            holder.mBoardNameLabel.setVisibility(View.GONE);
            holder.mBoardName.setVisibility(View.GONE);
            holder.mAuthorID.setVisibility(View.GONE);
            holder.mSeperator.setText(holder.mItem.getCategory());
        } else {
            holder.mSeperator.setVisibility(View.GONE);
            holder.mTopicTitle.setVisibility(View.VISIBLE);
            holder.mBoardNameLabel.setVisibility(View.VISIBLE);
            holder.mBoardName.setVisibility(View.VISIBLE);
            holder.mAuthorID.setVisibility(View.VISIBLE);

            holder.mTopicTitle.setText(holder.mItem.getTitle());
            holder.mBoardName.setText(holder.mItem.getBoardName());
            holder.mAuthorID.setText(holder.mItem.getAuthor());
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onTopicFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mSeperator;
        public final TextView mBoardName;
        public final TextView mTopicTitle;
        public final TextView mAuthorID;
        public final TextView mBoardNameLabel;
        public Topic mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mSeperator = (TextView) view.findViewById(R.id.category_name);
            mBoardNameLabel = (TextView) view.findViewById(R.id.board_name_label);
            mBoardName = (TextView) view.findViewById(R.id.board_name);
            mTopicTitle = (TextView) view.findViewById(R.id.topic_title);
            mAuthorID = (TextView) view.findViewById(R.id.author_id);
        }

        @Override
        public String toString() {
            return mTopicTitle.getText().toString();
        }
    }
}
