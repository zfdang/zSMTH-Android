package com.zfdang.zsmth_android.fresco;

import android.net.Uri;
import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;
import java.io.File;

/**
 * Created by zfdang on 2016-4-24.
 */
public class FrescoUtils {
  // return file or null
  // https://github.com/facebook/fresco/issues/80
  public static File getCachedImageOnDisk(Uri loadUri) {
    File localFile = null;
    if (loadUri != null) {
      CacheKey cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(ImageRequest.fromUri(loadUri), "FrescoUtils");
      if (ImagePipelineFactory.getInstance().getMainFileCache().hasKey(cacheKey)) {
        BinaryResource resource = ImagePipelineFactory.getInstance().getMainFileCache().getResource(cacheKey);
        localFile = ((FileBinaryResource) resource).getFile();
      } else if (ImagePipelineFactory.getInstance().getSmallImageFileCache().hasKey(cacheKey)) {
        BinaryResource resource = ImagePipelineFactory.getInstance().getSmallImageFileCache().getResource(cacheKey);
        localFile = ((FileBinaryResource) resource).getFile();
      }
    }
    return localFile;
  }
}
