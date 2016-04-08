package com.zfdang.zsmth_android.fresco;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;

import java.io.File;


/**
 * Created by zfdang on 2016-4-8.
 */
// http://stackoverflow.com/questions/33955510/facebook-fresco-using-wrap-conent/34075281#34075281
/**
 * Works when either height or width is set to wrap_content
 * The view is resized based on the image fetched
 */

public class WrapContentDraweeView extends SimpleDraweeView {
    private static final String TAG = "DraweeView";

    // we set a listener and update the view's aspect ratio depending on the loaded image
    private final ControllerListener listener = new BaseControllerListener<ImageInfo>() {
        @Override
        public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
            updateViewSize(imageInfo);
        }

        @Override
        public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
            updateViewSize(imageInfo);
        }
    };

    public WrapContentDraweeView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
    }

    public WrapContentDraweeView(Context context) {
        super(context);
    }

    public WrapContentDraweeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WrapContentDraweeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public WrapContentDraweeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setImageURI(Uri uri, Object callerContext) {
        DraweeController controller = ((PipelineDraweeControllerBuilder)getControllerBuilder())
                .setControllerListener(listener)
                .setCallerContext(callerContext)
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .setOldController(getController())
                .build();
        setController(controller);
        Log.d(TAG, "setImageURI: " + "With Context");
    }

    void updateViewSize(@Nullable ImageInfo imageInfo) {
        if (imageInfo != null) {
            setAspectRatio((float) imageInfo.getWidth() / imageInfo.getHeight());
        }
    }

    // load image from string URL
    public void setImageFromStringURL(final String url) {
        this.setImageURI(Uri.parse(url));
    }

    // load image from local file
    public void setImageFromLocalFilename(final String filename) {
        this.setImageURI(Uri.fromFile(new File(filename)));
    }



}