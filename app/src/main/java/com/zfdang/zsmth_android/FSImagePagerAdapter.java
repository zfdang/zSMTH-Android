package com.zfdang.zsmth_android;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zfdang.SMTHApplication;

import java.util.List;

/**
 * Created by zfdang on 2016-3-31.
 */
public class FSImagePagerAdapter extends PagerAdapter {

    private List<String> mURLs;
    private View.OnClickListener mListener;

    public FSImagePagerAdapter(List<String> URLs, View.OnClickListener listener) {
        mURLs = URLs;
        mListener = listener;
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final LayoutInflater inflater = (LayoutInflater) SMTHApplication.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Add the text layout to the parent layout
        ImageView image = (ImageView) inflater.inflate(R.layout.image_viewer_pager, container, false);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null) {
                    mListener.onClick(v);
                }
            }
        });

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
