package com.zfdang.zsmth_android.fresco;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * Created by zfdang on 2016-6-3.
 */
public class ImageUtils {
  private static final int DEFAULT_MAX_BITMAP_DIMENSION = 2048;
  private static int maxHeight = 0;

  // http://stackoverflow.com/questions/15313807/android-maximum-allowed-width-height-of-bitmap
  static {
    // Get EGL Display
    EGL10 egl = (EGL10) EGLContext.getEGL();
    EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

    // Initialise
    int[] version = new int[2];
    egl.eglInitialize(display, version);

    // Query total number of configurations
    int[] totalConfigurations = new int[1];
    egl.eglGetConfigs(display, null, 0, totalConfigurations);

    // Query actual list configurations
    EGLConfig[] configurationsList = new EGLConfig[totalConfigurations[0]];
    egl.eglGetConfigs(display, configurationsList, totalConfigurations[0], totalConfigurations);

    int[] textureSize = new int[1];
    int maximumTextureSize = 0;

    // Iterate through all the configurations to located the maximum texture size
    for (int i = 0; i < totalConfigurations[0]; i++) {
      // Only need to check for width since opengl textures are always squared
      egl.eglGetConfigAttrib(display, configurationsList[i], EGL10.EGL_MAX_PBUFFER_WIDTH, textureSize);

      // Keep track of the maximum texture size
      if (maximumTextureSize < textureSize[0]) maximumTextureSize = textureSize[0];
    }

    // Release
    egl.eglTerminate(display);

    // Return largest texture size found, or default
    maxHeight = Math.max(maximumTextureSize, DEFAULT_MAX_BITMAP_DIMENSION);
  }

  public static int getMaxHeight() {
    return maxHeight - 6;
  }
}