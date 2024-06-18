package com.zfdang.zsmth_android;

import android.app.Activity;
import android.content.Intent;

import android.widget.GridLayout;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Point;
import android.view.Display;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.klinker.android.link_builder.LinkBuilder;
import com.klinker.android.link_builder.LinkConsumableTextView;
import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.fresco.WrapContentDraweeView;
import com.zfdang.zsmth_android.helpers.ActivityUtils;
import com.zfdang.zsmth_android.models.Attachment;
import com.zfdang.zsmth_android.models.ContentSegment;
import com.zfdang.zsmth_android.models.Post;
import java.util.ArrayList;
import java.util.List;
import com.bumptech.glide.Glide;

/**
 * used by HotPostFragment & BoardPostFragment
 */
public class PostRecyclerViewAdapter extends RecyclerView.Adapter<PostRecyclerViewAdapter.ViewHolder> {

  private final List<Post> mPosts;
  private final Activity mListener;

  private GridLayout gridLayout;
  private List<ImageView> imgList;

  public PostRecyclerViewAdapter(List<Post> posts, Activity listener) {
    mPosts = posts;
    mListener = listener;
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
    return new ViewHolder(view);
  }

  //    MailContentActivity.inflateContentViewGroup is using the same logic, please make sure they are consistent
  public void inflateContentViewGroup(ViewGroup viewGroup, TextView contentView, final Post post) {
    // remove all child view in viewgroup
    viewGroup.removeAllViews();

    List<ContentSegment> contents = post.getContentSegments();
    if (contents == null) return;

    if (contents.size() > 0) {
      // there are multiple segments, add the first contentView first
      // contentView is always available, we don't have to inflate it again
      ContentSegment content = contents.get(0);
      contentView.setText(content.getSpanned());
      LinkBuilder.on(contentView).addLinks(ActivityUtils.getPostSupportedLinks(mListener)).build();

      viewGroup.addView(contentView);
    }

    imgList = new ArrayList<>();

    // http://stackoverflow.com/questions/13438473/clicking-html-link-in-textview-fires-weird-androidruntimeexception
    final LayoutInflater inflater = mListener.getLayoutInflater();
    for (int i = 1; i < contents.size(); i++) {
      ContentSegment content = contents.get(i);

      if (content.getType() == ContentSegment.SEGMENT_IMAGE) {
        // Log.d("CreateView", "Image: " + content.getUrl());

        if(Settings.getInstance().isImageGridMode()){
          // use grid view to replace original imageview
          ImageView image = (ImageView) inflater.inflate(R.layout.post_item_image, viewGroup, false);
          image.setScaleType(ImageView.ScaleType.CENTER_CROP);
          image.setPadding(5, 5, 5, 5);
          Glide.with(mListener).load(content.getUrl()).into(image);
          image.setTag(R.id.image_tag, content.getImgIndex());

          // set onclicklistener
          image.setTag(R.id.image_tag, content.getImgIndex());
          image.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
              int position = (int) v.getTag(R.id.image_tag);

              Intent intent = new Intent(mListener, FSImageViewerActivity.class);

              ArrayList<String> urls = new ArrayList<>();
              List<Attachment> attaches = post.getAttachFiles();
              for (Attachment attach : attaches) {
                // load original image in FS image viewer
                urls.add(attach.getOriginalImageSource());
              }

              intent.putStringArrayListExtra(SMTHApplication.ATTACHMENT_URLS, urls);
              intent.putExtra(SMTHApplication.ATTACHMENT_CURRENT_POS, position);
              mListener.startActivity(intent);
            }
          });

          // add image to list, instead of adding to viewgroup directly
          imgList.add(image);
        } else {
          // Add the text layout to the parent layout
          // 采用混排模式，文字和图片混在一起
          WrapContentDraweeView image = (WrapContentDraweeView) inflater.inflate(R.layout.post_item_imageview, viewGroup, false);
          image.setImageFromStringURL(content.getUrl());

          // set onclicklistener
          image.setTag(R.id.image_tag, content.getImgIndex());
          image.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
              int position = (int) v.getTag(R.id.image_tag);

              Intent intent = new Intent(mListener, FSImageViewerActivity.class);

              ArrayList<String> urls = new ArrayList<>();
              List<Attachment> attaches = post.getAttachFiles();
              for (Attachment attach : attaches) {
                // load original image in FS image viewer
                urls.add(attach.getOriginalImageSource());
              }

              intent.putStringArrayListExtra(SMTHApplication.ATTACHMENT_URLS, urls);
              intent.putExtra(SMTHApplication.ATTACHMENT_CURRENT_POS, position);
              mListener.startActivity(intent);
            }
          });

          // Add the text view to the parent layout
          viewGroup.addView(image);
        }

      } else if (content.getType() == ContentSegment.SEGMENT_TEXT) {
        // Log.d("CreateView", "Text: " + content.getSpanned().toString());

        // Add the links and make the links clickable
        LinkConsumableTextView tv = (LinkConsumableTextView) inflater.inflate(R.layout.post_item_content, viewGroup, false);
        tv.setText(content.getSpanned());
        LinkBuilder.on(tv).addLinks(ActivityUtils.getPostSupportedLinks(mListener)).build();

        // Add the text view to the parent layout
        viewGroup.addView(tv);
      }
    }

    if(Settings.getInstance().isImageGridMode()) {
      // 将图片统一以网格形式添加进去
      int index = imgList.size() - imgList.size() > 1 ? 2 : 1;
      if (imgList.size() > 0) {
        int size, width;

        Display display = mListener.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        width = point.x;
        size = imgList.size();

        gridLayout = new GridLayout(mListener);
        GridLayout.Spec rowSpec, columnSpec;

        GridLayout.LayoutParams gridParams = null;
        int remainder, i;
        remainder = i = size % 3;
        for (int k = 0; k < remainder; ++k) {
          rowSpec = GridLayout.spec(0);
          columnSpec = GridLayout.spec(k);
          gridParams = new GridLayout.LayoutParams(rowSpec, columnSpec);
          gridParams.setGravity(Gravity.LEFT);
          gridParams.width = width / i;
          gridParams.height = width / 2;
          gridLayout.addView(imgList.get(k), gridParams);
        }
        viewGroup.addView(gridLayout, index++);

        gridLayout = new GridLayout(mListener);
        for (; i < size; ++i) {
          rowSpec = GridLayout.spec((i - remainder) / 3);
          columnSpec = GridLayout.spec((i - remainder) % 3);
          gridParams = new GridLayout.LayoutParams(rowSpec, columnSpec);
          gridParams.width = width / 3;
          gridParams.height = width / 3;
          gridLayout.addView(imgList.get(i), gridParams);
        }
        viewGroup.addView(gridLayout, index);
      }
    }
  }

  @Override public void onBindViewHolder(final ViewHolder holder, final int position) {
    holder.mPost = mPosts.get(position);
    Post post = holder.mPost;

    holder.mPostAuthor.setText(post.getAuthor());
    holder.mPostPublishDate.setText(post.getFormatedDate());
    holder.mPostIndex.setText(post.getPosition());

    inflateContentViewGroup(holder.mViewGroup, holder.mPostContent, post);

    // http://stackoverflow.com/questions/4415528/how-to-pass-the-onclick-event-to-its-parent-on-android
    // http://stackoverflow.com/questions/24885223/why-doesnt-recyclerview-have-onitemclicklistener-and-how-recyclerview-is-dif
    holder.mView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
      }
    });
    holder.mView.setOnTouchListener(new View.OnTouchListener() {
      @Override public boolean onTouch(View v, MotionEvent event) {
        if (mListener != null && mListener instanceof View.OnTouchListener) {
          return ((View.OnTouchListener) mListener).onTouch(v, event);
        }
        return false;
      }
    });
  }

  @Override public int getItemCount() {
    return mPosts.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final TextView mPostAuthor;
    public final TextView mPostIndex;
    public final TextView mPostPublishDate;
    private final LinearLayout mViewGroup;
    public final LinkConsumableTextView mPostContent;
    public Post mPost;

    public ViewHolder(View view) {
      super(view);
      mView = view;
      mPostAuthor = (TextView) view.findViewById(R.id.post_author);
      mPostIndex = (TextView) view.findViewById(R.id.post_index);
      mPostPublishDate = (TextView) view.findViewById(R.id.post_publish_date);
      mViewGroup = (LinearLayout) view.findViewById(R.id.post_content_holder);
      mPostContent = (LinkConsumableTextView) view.findViewById(R.id.post_content);
    }

    @Override public String toString() {
      return mPost.toString();
    }
  }
}
