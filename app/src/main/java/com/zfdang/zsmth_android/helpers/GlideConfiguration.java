package com.zfdang.zsmth_android.helpers;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.module.GlideModule;

/**
 * Glide options
 * Created by zfdang on 2016-3-30.
 */
public class GlideConfiguration  implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // Apply options to the builder here.
//        builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);
        // 把你的磁盘缓存放到应用程序私有的内部存储目录中; 250M
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, "Glide", 1024*1024*250));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }
}
