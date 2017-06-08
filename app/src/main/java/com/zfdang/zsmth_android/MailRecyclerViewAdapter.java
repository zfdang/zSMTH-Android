package com.zfdang.zsmth_android;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.zfdang.zsmth_android.listeners.OnMailInteractionListener;
import com.zfdang.zsmth_android.models.Mail;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Mail} and makes a call to the
 * specified {@link OnMailInteractionListener}.
 */
public class MailRecyclerViewAdapter extends RecyclerView.Adapter<MailRecyclerViewAdapter.ViewHolder> {

  private static final String TAG = "MailAdapter";
  private final List<Mail> mValues;
  private final OnMailInteractionListener mListener;

  public MailRecyclerViewAdapter(List<Mail> items, OnMailInteractionListener listener) {
    mValues = items;
    mListener = listener;
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mail_item, parent, false);
    return new ViewHolder(view);
  }

  @Override public void onBindViewHolder(final ViewHolder holder, int position) {
    Mail mail = mValues.get(position);
    holder.mItem = mail;

    if (mail.isNew) {
      // Log.d(TAG, "onBindViewHolder: " + "mail is new");
      holder.mView.setBackgroundResource(R.drawable.recyclerview_new_item_bg);
    } else {
      holder.mView.setBackgroundResource(R.drawable.recyclerview_item_bg);
    }

    if (mail.isCategory) {
      holder.mPage.setVisibility(View.VISIBLE);
      holder.mAuthorLabel.setVisibility(View.GONE);
      holder.mAuthor.setVisibility(View.GONE);
      holder.mTopic.setVisibility(View.GONE);
      holder.mDate.setVisibility(View.GONE);

      holder.mPage.setText(mail.category);
    } else {
      holder.mPage.setVisibility(View.GONE);
      holder.mAuthorLabel.setVisibility(View.VISIBLE);
      holder.mAuthor.setVisibility(View.VISIBLE);
      holder.mTopic.setVisibility(View.VISIBLE);
      holder.mDate.setVisibility(View.VISIBLE);

      holder.mAuthor.setText(mail.getFrom());
      holder.mTopic.setText(mail.title);
      holder.mDate.setText(mail.date);

      holder.mView.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onMailInteraction(holder.mItem, holder.getPosition());
          }
        }
      });
    }
  }

  @Override public int getItemCount() {
    return mValues.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final TextView mPage;
    public final TextView mTopic;
    public final TextView mAuthorLabel;
    public final TextView mAuthor;
    public final TextView mDate;
    public Mail mItem;

    public ViewHolder(View view) {
      super(view);
      mView = view;
      mPage = (TextView) view.findViewById(R.id.mail_item_page);
      mTopic = (TextView) view.findViewById(R.id.mail_item_topic);
      mAuthorLabel = (TextView) view.findViewById(R.id.mail_item_author_label);
      mAuthor = (TextView) view.findViewById(R.id.mail_item_author);
      mDate = (TextView) view.findViewById(R.id.mail_item_date);
    }

    @Override public String toString() {
      return super.toString() + " '" + mTopic.getText() + "'";
    }
  }
}
