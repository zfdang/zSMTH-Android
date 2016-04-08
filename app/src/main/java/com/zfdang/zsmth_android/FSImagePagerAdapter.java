package com.zfdang.zsmth_android;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zfdang.zsmth_android.fresco.MyPhotoView;
import com.zfdang.SMTHApplication;

import java.util.List;
import java.util.Map;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by zfdang on 2016-3-31.
 */
public class FSImagePagerAdapter extends PagerAdapter {

    private List<String> mURLs;
    private Activity mListener;
    private Map<Integer, PhotoViewAttacher> mAttachers;

    public FSImagePagerAdapter(List<String> URLs, Activity listener) {
        mURLs = URLs;
        mListener = listener;
    }

    @Override
    public int getCount() {
        return mURLs.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final LayoutInflater inflater = (LayoutInflater) SMTHApplication.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Add the text layout to the parent layout
        MyPhotoView image = (MyPhotoView) inflater.inflate(R.layout.image_viewer_pager, container, false);
        assert image != null;
        image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mListener != null && mListener instanceof View.OnLongClickListener) {
                    return ((View.OnLongClickListener) mListener).onLongClick(v);
                }
                return false;
            }
        });
        image.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                if (mListener != null && mListener instanceof PhotoViewAttacher.OnPhotoTapListener) {
                    ((PhotoViewAttacher.OnPhotoTapListener) mListener).onPhotoTap(view, x, y);
                }
            }

            @Override
            public void onOutsidePhotoTap() {
                if (mListener != null && mListener instanceof PhotoViewAttacher.OnPhotoTapListener) {
                    ((PhotoViewAttacher.OnPhotoTapListener) mListener).onOutsidePhotoTap();
                }

            }
        });

        image.setMaximumScale(12.0f);

        // use only this method to set image
        image.setImageUri(mURLs.get(position), null);

        container.addView(image, 0);

        return image;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        ImageView iv = (ImageView) object;
        container.removeView(iv);
        object = null;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (object == view);
    }
}
