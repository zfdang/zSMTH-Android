package com.zfdang.zsmth_android;

import android.app.Activity;
import android.content.Context;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.OnOutsidePhotoTapListener;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.fresco.MyPhotoView;
import java.util.List;
import java.util.Map;

/**
 * Created by zfdang on 2016-3-31.
 */
public class FSImagePagerAdapter extends PagerAdapter {

  private List<String> mURLs;
  private Activity mListener;
  private Map<Integer, PhotoViewAttacher> mAttachers;

  // http://stackoverflow.com/questions/6807262/get-focused-view-from-viewpager
  public View mCurrentView;

  public FSImagePagerAdapter(List<String> URLs, Activity listener) {
    mURLs = URLs;
    mListener = listener;
  }

  @Override public int getCount() {
    return mURLs.size();
  }

  @Override public Object instantiateItem(ViewGroup container, int position) {
    final LayoutInflater inflater = (LayoutInflater) SMTHApplication.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    // Add the text layout to the parent layout
    MyPhotoView image = (MyPhotoView) inflater.inflate(R.layout.image_viewer_pager, container, false);
    assert image != null;

    image.setTag(R.id.fsview_image_index, position);
    image.setMaximumScale(12.0f);
    // use only this method to set image
    image.setImageUri(mURLs.get(position));

    //        image.setOnLongClickListener(new View.OnLongClickListener() {
    //            @Override
    //            public boolean onLongClick(View v) {
    //                if (mListener != null && mListener instanceof View.OnLongClickListener) {
    //                    return ((View.OnLongClickListener) mListener).onLongClick(v);
    //                }
    //                return false;
    //            }
    //        });
    image.setOnPhotoTapListener(new OnPhotoTapListener() {
      @Override
      public void onPhotoTap(ImageView view, float x, float y) {
        if (mListener != null && mListener instanceof OnPhotoTapListener) {
          ((OnPhotoTapListener) mListener).onPhotoTap(view, x, y);
        }
      }
    });

    image.setOnOutsidePhotoTapListener(new OnOutsidePhotoTapListener() {
      @Override
      public void onOutsidePhotoTap(ImageView imageView) {
        if (mListener != null && mListener instanceof OnOutsidePhotoTapListener) {
          ((OnOutsidePhotoTapListener) mListener).onOutsidePhotoTap(imageView);
        }
      }
    });

    container.addView(image, 0);
    return image;
  }

  @Override public void destroyItem(ViewGroup container, int position, Object object) {

    ImageView iv = (ImageView) object;
    container.removeView(iv);
    object = null;
  }

  @Override public void setPrimaryItem(ViewGroup container, int position, Object object) {
    mCurrentView = (View) object;
    super.setPrimaryItem(container, position, object);
  }

  @Override public boolean isViewFromObject(View view, Object object) {
    return (object == view);
  }
}
