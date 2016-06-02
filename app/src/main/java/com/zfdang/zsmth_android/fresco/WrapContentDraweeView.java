package com.zfdang.zsmth_android.fresco;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.zfdang.SMTHApplication;

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
            if (imageInfo == null) {
                return;
            }
            updateViewSize(imageInfo);
        }
    };

    public WrapContentDraweeView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
        InitProgressBar();
    }

    public WrapContentDraweeView(Context context) {
        super(context);
        InitProgressBar();
    }

    public WrapContentDraweeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        InitProgressBar();
    }

    public WrapContentDraweeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        InitProgressBar();
    }

    public WrapContentDraweeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        InitProgressBar();
    }

    public void InitProgressBar() {
        getHierarchy().setProgressBarImage(new LoadingProgressDrawable(SMTHApplication.getAppContext()));
    }

    @Override
    public void setImageURI(Uri uri, Object callerContext) {
        // http://stackoverflow.com/questions/7428996/hw-accelerated-activity-how-to-get-opengl-texture-size-limit
        // larger images are resized to 2048*2048
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setAutoRotateEnabled(true)
                .setResizeOptions(new ResizeOptions(2048, 2048))
                .build();

        DraweeController controller = ((PipelineDraweeControllerBuilder) getControllerBuilder())
                .setImageRequest(request)
                .setControllerListener(listener)
                .setCallerContext(callerContext)
                .setAutoPlayAnimations(true)
                .setOldController(getController())
                .build();
        setController(controller);

//        Log.d(TAG, "setImageURI: " + "With Context");
    }

    void updateViewSize(@Nullable ImageInfo imageInfo) {
        // since we have placeholder to show loading status, the height is 68dp, we need to reset height to WRAP_CONTENT
        getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

        // set ratio
        setAspectRatio((float) imageInfo.getWidth() / imageInfo.getHeight());
    }

    // load image from string URL
    public void setImageFromStringURL(final String url) {
        if(url == null || url.length() == 0)
            return;
        this.setImageURI(Uri.parse(url));
    }

    // load image from local file
    public void setImageFromLocalFilename(final String filename) {
        this.setImageURI(Uri.fromFile(new File(filename)));
    }
}