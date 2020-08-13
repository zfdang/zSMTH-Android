package com.zfdang.zsmth_android.fresco;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import com.facebook.common.references.CloseableReference;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;
import com.zfdang.SMTHApplication;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by zfdang on 2016-4-8.
 */

// http://stackoverflow.com/questions/33955510/facebook-fresco-using-wrap-conent/34075281#34075281
// http://blog.csdn.net/yuanhejie/article/details/49868131

/**
 * Works when either height or width is set to wrap_content
 * The imageview will be resized after image was fetched;
 *
 * this view is also capable of handling very long image:
 * it will split the long images into multiple bitmaps, and draw them one by one in OnDraw
 */

public class WrapContentDraweeView extends SimpleDraweeView {
  private static final String TAG = "DraweeView";

  private int WindowWidth;
  private Rect src;
  private Rect dst;
  private Paint paint;
  private ArrayList<Bitmap> bmps;

  // we set a listener and update the view's aspect ratio depending on the loaded image
  private final ControllerListener listener = new BaseControllerListener<ImageInfo>() {
    @Override public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
      updateViewSize(imageInfo);
    }

    @Override public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
      if (imageInfo == null) {
        return;
      }
      updateViewSize(imageInfo);
    }
  };

  // update view's width & height after loading is done
  void updateViewSize(@Nullable ImageInfo imageInfo) {
    // since we have placeholder to show loading status, the height is 68dp, we need to reset height to WRAP_CONTENT
    getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

    // set ratio, so that image view's height will be updated by Fresco (width = match_parent)
    setAspectRatio((float) imageInfo.getWidth() / imageInfo.getHeight());
  }

  public WrapContentDraweeView(Context context, GenericDraweeHierarchy hierarchy) {
    super(context, hierarchy);
    initDraweeView();
  }

  public WrapContentDraweeView(Context context) {
    super(context);
    initDraweeView();
  }

  public WrapContentDraweeView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initDraweeView();
  }

  public WrapContentDraweeView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initDraweeView();
  }

  public WrapContentDraweeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initDraweeView();
  }

  public void initDraweeView() {
    getHierarchy().setProgressBarImage(new LoadingProgressDrawable(SMTHApplication.getAppContext()));

    WindowWidth = getResources().getDisplayMetrics().widthPixels;

    paint = new Paint();
    paint.setAntiAlias(true);
    paint.setFilterBitmap(true);

    src = new Rect();
    dst = new Rect();
  }

  // getTimes(3, 4) == 0
  // getTimes(4, 4) == 1
  // getTimes(8, 4) == 2
  // getTimes(9, 4) == 3
  int getTimes(int actualNumber, int allowedMaxNumber) {
    if (actualNumber < allowedMaxNumber) return 0;
    int result = actualNumber / allowedMaxNumber;
    if (result * allowedMaxNumber < actualNumber) {
      result += 1;
    }
    return result;
  }

  @Override public void setImageURI(Uri uri, Object callerContext) {
    // http://frescolib.org/docs/modifying-image.html
    // this post process will do two things: 1. resize if image width is too large; 2. split if image height is too large
    Postprocessor postProcessor = new BasePostprocessor() {
      @Override public String getName() {
        return "SplitLongImagePostProcessor";
      }

      @Override public CloseableReference<Bitmap> process(Bitmap sourceBitmap, PlatformBitmapFactory bitmapFactory) {
        CloseableReference<Bitmap> bitmapRef = null;

        try {
          // resize image if its width is too large: > windowWidth * 1.5
          double ratio = 1.0;
          if (sourceBitmap.getWidth() >= WindowWidth * 1.5) {
            ratio = (double) WindowWidth / sourceBitmap.getWidth();
          }
          bitmapRef = bitmapFactory.createBitmap((int) (sourceBitmap.getWidth() * ratio), (int) (sourceBitmap.getHeight() * ratio));

          Bitmap destBitmap = bitmapRef.get();
          Canvas canvas = new Canvas(destBitmap);
          Rect destRect = new Rect(0, 0, destBitmap.getWidth(), destBitmap.getHeight());
          canvas.drawBitmap(sourceBitmap, null, destRect, paint);

          // split images if its height is too large: > OpenGL max Height
          try {
            int imageTotalHeight = destBitmap.getHeight();
            double imageAspectRatio = destBitmap.getWidth() / (double) WindowWidth;
            int imageMaxAllowedHeight;
            if (imageAspectRatio < 1) {
              imageMaxAllowedHeight = (int) (ImageUtils.getMaxHeight() * imageAspectRatio) - 5;
            } else {
              imageMaxAllowedHeight = ImageUtils.getMaxHeight();
            }
            int imageCount = getTimes(imageTotalHeight, imageMaxAllowedHeight);
            // Log.d(TAG, "process: h = " + imageTotalHeight + " w = " + destBitmap.getWidth() + " allowed: " + imageMaxAllowedHeight + " count: " + imageCount);
            if (imageCount > 1) {
              bmps = new ArrayList<Bitmap>();
              Rect bsrc = new Rect();

              ByteArrayOutputStream baos = new ByteArrayOutputStream();
              destBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
              InputStream isBm = new ByteArrayInputStream(baos.toByteArray());
              BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(isBm, true);
              for (int i = 0; i < imageCount; i++) {
                bsrc.left = 0;
                bsrc.top = i * imageMaxAllowedHeight;
                bsrc.right = destBitmap.getWidth();
                bsrc.bottom = Math.min(bsrc.top + imageMaxAllowedHeight, imageTotalHeight);
                Bitmap bmp = decoder.decodeRegion(bsrc, null);
                bmps.add(bmp);
              }
            }
          } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
          }
          return CloseableReference.cloneOrNull(bitmapRef);
        } finally {
          CloseableReference.closeSafely(bitmapRef);
        }
      }
    };

    ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri).setAutoRotateEnabled(true)
        // this will reduce image's size if it's wider than screen width
                        .setResizeOptions(new ResizeOptions(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE))
        .setPostprocessor(postProcessor).build();

    DraweeController controller = ((PipelineDraweeControllerBuilder) getControllerBuilder()).setImageRequest(request)
        .setControllerListener(listener)
        .setCallerContext(callerContext)
        .setAutoPlayAnimations(true)
        .setOldController(getController())
        .build();
    setController(controller);
  }

  @Override protected void onDraw(Canvas canvas) {
    if (bmps == null || bmps.size() <= 1) {
      // use super.onDraw
      super.onDraw(canvas);
    } else {
      // this is a very large image, and it has been splitted into several small bitmaps
      int accumulatedHeight = 0;
      for (int i = 0; i < bmps.size(); i++) {
        Bitmap bmp = bmps.get(i);
        src.left = 0;
        src.top = 0;
        src.right = bmp.getWidth();
        src.bottom = bmp.getHeight();

        dst.left = 0;
        dst.top = accumulatedHeight;
        dst.right = getWidth();
        dst.bottom = accumulatedHeight + (int) ((double) src.bottom / (double) src.right * getWidth());
        canvas.drawBitmap(bmp, src, dst, paint);

        accumulatedHeight = dst.bottom;
      }
    }
  }

  // load image from string URL
  public void setImageFromStringURL(final String url) {
    if (url == null || url.length() == 0) return;
    this.setImageURI(Uri.parse(url));
  }

  // load image from local file
  public void setImageFromLocalFilename(final String filename) {
    this.setImageURI(Uri.fromFile(new File(filename)));
  }
}