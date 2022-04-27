package com.zfdang.zsmth_android;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.OnOutsidePhotoTapListener;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.jude.swipbackhelper.SwipeBackHelper;
import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.fresco.FrescoUtils;
import com.zfdang.zsmth_android.fresco.MyPhotoView;
import com.zfdang.zsmth_android.helpers.FileSizeUtil;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import me.relex.circleindicator.CircleIndicator;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class FSImageViewerActivity extends AppCompatActivity implements OnPhotoTapListener, OnOutsidePhotoTapListener {

  private static final String TAG = "FullViewer";

  private HackyViewPager mViewPager;

  private FSImagePagerAdapter mPagerAdapter;
  private CircleIndicator mIndicator;
  private ArrayList<String> mURLs;

  private LinearLayout layoutToolbar;
  private ImageView btBack;
  private ImageView btInfo;
  private ImageView btSave;

  private static final int RC_READ_WRITE_STORAGE = 123;
  String mTargetImagePath;
  boolean mTargetImageAnimation;

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    // Forward results to EasyPermissions
    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // 延伸显示区域到刘海
    Window window = this.getWindow();
    WindowManager.LayoutParams lp = this.getWindow().getAttributes();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
    }
    window.setAttributes(lp);

    setContentView(R.layout.activity_fs_image_viewer);

    mViewPager = (HackyViewPager) findViewById(R.id.fullscreen_image_pager);

    // find parameters from parent
    mURLs = getIntent().getStringArrayListExtra(SMTHApplication.ATTACHMENT_URLS);
    assert mURLs != null;
    int pos = getIntent().getIntExtra(SMTHApplication.ATTACHMENT_CURRENT_POS, 0);
    if (pos < 0 || pos >= mURLs.size()) {
      pos = 0;
    }

    mPagerAdapter = new FSImagePagerAdapter(mURLs, this);
    mViewPager.setAdapter(mPagerAdapter);
    mViewPager.setCurrentItem(pos);

    mIndicator = (CircleIndicator) findViewById(R.id.fullscreen_image_indicator);
    mIndicator.setViewPager(mViewPager);

    // initialize toolbar and its child buttons
    layoutToolbar = (LinearLayout) findViewById(R.id.fullscreen_toolbar);

    btBack = (ImageView) findViewById(R.id.fullscreen_button_back);
    btBack.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        finish();
      }
    });

    btInfo = (ImageView) findViewById(R.id.fullscreen_button_info);
    btInfo.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        int position = mViewPager.getCurrentItem();
        final String imagePath = mURLs.get(position);

        showExifDialog(imagePath);
      }
    });

    btSave = (ImageView) findViewById(R.id.fullscreen_button_save);
    btSave.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        int position = mViewPager.getCurrentItem();
        mTargetImagePath = mURLs.get(position);

        View currentView = mPagerAdapter.mCurrentView;
        mTargetImageAnimation = false;
        if (currentView instanceof MyPhotoView) {
          MyPhotoView photoView = (MyPhotoView) currentView;
          mTargetImageAnimation = photoView.isAnimation();
        }
        saveImageToFile();
      }
    });


    SwipeBackHelper.onCreate(this);
    SwipeBackHelper.getCurrentPage(this).setSwipeEdgePercent(0.2f);
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    if (hasFocus) {
      hideSystemUI();
      layoutToolbar.setVisibility(LinearLayout.GONE);
    }
  }

  private void hideSystemUI() {
    // Enables regular immersive mode.
    // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
    // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    View decorView = getWindow().getDecorView();
    decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE
                    // Hide the nav bar and status bar
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    );
  }

  // Shows the system bars by removing all the flags
  // except for the ones that make the content appear under the system bars.
  private void showSystemUI() {
    View decorView = getWindow().getDecorView();
    decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
  }


  @Override protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    SwipeBackHelper.onPostCreate(this);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    SwipeBackHelper.onDestroy(this);
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
      mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
      return true;
    } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
      mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  // http://stackoverflow.com/questions/4500354/control-volume-keys
  @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
    // disable the beep sound when volume up/down is pressed
    if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP) || (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
      return true;
    }
    return super.onKeyUp(keyCode, event);
  }

  private String getURLHashCode(String imagePath) {
    return Integer.toHexString(imagePath.hashCode());
  }

  @AfterPermissionGranted(RC_READ_WRITE_STORAGE)
  void saveImageToFile() {
    String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    if (EasyPermissions.hasPermissions(this, perms)) {
      // Already have permission, do the thing
      File imageFile = FrescoUtils.getCachedImageOnDisk(Uri.parse(mTargetImagePath));
      if (imageFile == null) {
        Toast.makeText(FSImageViewerActivity.this, "无法读取缓存文件！", Toast.LENGTH_LONG).show();
        return;
      }
      // Log.d(TAG, "saveImageToFile: " + imageFile.getAbsolutePath());

      // save image to sdcard
      try {
        if (TextUtils.equals(Environment.getExternalStorageState(), Environment.MEDIA_MOUNTED)) {
          String path = Environment.getExternalStorageDirectory().getPath() + "/zSMTH/";
          File dir = new File(path);
          if (!dir.exists()) {
            dir.mkdirs();
          }

          String IMAGE_FILE_PREFIX = "zSMTH-";
          String IMAGE_FILE_SUFFIX = ".jpg";
          if (mTargetImageAnimation) {
            IMAGE_FILE_SUFFIX = ".gif";
          }
          File outFile = new File(dir, IMAGE_FILE_PREFIX + getURLHashCode(mTargetImagePath) + IMAGE_FILE_SUFFIX);

          BufferedInputStream bufr = new BufferedInputStream(new FileInputStream(imageFile));
          BufferedOutputStream bufw = new BufferedOutputStream(new FileOutputStream(outFile));

          int len = 0;
          byte[] buf = new byte[1024];
          while ((len = bufr.read(buf)) != -1) {
            bufw.write(buf, 0, len);
            bufw.flush();
          }
          bufw.close();
          bufr.close();

          // make sure the new file can be recognized soon
          sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(outFile)));

          Toast.makeText(FSImageViewerActivity.this, "图片已存为: /zSMTH/" + outFile.getName(), Toast.LENGTH_SHORT).show();
        }
      } catch (Exception e) {
        Log.e(TAG, "saveImageToFile: " + Log.getStackTraceString(e));
        Toast.makeText(FSImageViewerActivity.this, "保存图片失败:\n请授予应用存储权限！\n" + e.toString(), Toast.LENGTH_LONG).show();
      }
    } else {
      // Do not have permissions, request them now
      EasyPermissions.requestPermissions(this, getString(R.string.read_write_storage_rationale),
              RC_READ_WRITE_STORAGE, perms);
    }
  }

  // get image attribute from exif
  private void setImageAttributeFromExif(View layout, int tv_id, ExifInterface exif, String attr) {
    if (layout == null || exif == null) return;
    TextView tv = (TextView) layout.findViewById(tv_id);
    if (tv == null) {
      Log.d(TAG, "setImageAttributeFromExif: " + "Invalid resource ID: " + tv_id);
      return;
    }

    String attribute = exif.getAttribute(attr);
    if (attribute != null) {
      // there are some special treatment
      // http://www.sno.phy.queensu.ca/~phil/exiftool/TagNames/EXIF.html
      if (attr.equals(ExifInterface.TAG_APERTURE)) {
        attribute = "F/" + attribute;
      } else if (attr.equals(ExifInterface.TAG_EXPOSURE_TIME)) {
        try {
          float f = Float.parseFloat(attribute);
          if (f >= 1.0) {
            attribute = attribute + " s";
          } else if (f >= 0.1) {
            f = 1 / f;
            BigDecimal exposure = new BigDecimal(f).setScale(0, BigDecimal.ROUND_HALF_UP);
            attribute = "1/" + exposure.toString() + " s";
          } else {
            f = 1 / f / 10;
            BigDecimal exposure = new BigDecimal(f).setScale(0, BigDecimal.ROUND_HALF_UP);
            exposure = exposure.multiply(new BigDecimal(10));
            attribute = "1/" + exposure.toString() + " s";
          }
        } catch (NumberFormatException e) {
          Log.d("Can't convert exposure:", attribute);
        }
      } else if (attr.equals(ExifInterface.TAG_FLASH)) {
        int flash = Integer.parseInt(attribute);
        switch (flash) {
          case 0x0:
            attribute += " (No Flash)";
            break;
          case 0x1:
            attribute += " (Fired)";
            break;
          case 0x5:
            attribute += " (Fired, Return not detected)";
            break;
          case 0x7:
            attribute += " (Fired, Return detected)";
            break;
          case 0x8:
            attribute += " (On, Did not fire)";
            break;
          case 0x9:
            attribute += " (On, Fired)";
            break;
          case 0xd:
            attribute += " (On, Return not detected)";
            break;
          case 0xf:
            attribute += " (On, Return detected)";
            break;
          case 0x10:
            attribute += " (Off, Did not fire)";
            break;
          case 0x14:
            attribute += " (Off, Did not fire, Return not detected)";
            break;
          case 0x18:
            attribute += " (Auto, Did not fire)";
            break;
          case 0x19:
            attribute += " (Auto, Fired)";
            break;
          case 0x1d:
            attribute += " (Auto, Fired, Return not detected)";
            break;
          case 0x1f:
            attribute += " (Auto, Fired, Return detected)";
            break;
          case 0x20:
            attribute += " (No flash function)";
            break;
          case 0x30:
            attribute += " (Off, No flash function)";
            break;
          case 0x41:
            attribute += " (Fired, Red-eye reduction)";
            break;
          case 0x45:
            attribute += " (Fired, Red-eye reduction, Return not detected)";
            break;
          case 0x47:
            attribute += " (Fired, Red-eye reduction, Return detected)";
            break;
          case 0x49:
            attribute += " (On, Red-eye reduction)";
            break;
          case 0x4d:
            attribute += " (On, Red-eye reduction, Return not detected)";
            break;
          case 0x4f:
            attribute += " (On, Red-eye reduction, Return detected)";
            break;
          case 0x50:
            attribute += " (Off, Red-eye reduction)";
            break;
          case 0x58:
            attribute += " (Auto, Did not fire, Red-eye reduction)";
            break;
          case 0x59:
            attribute += " (Auto, Fired, Red-eye reduction)";
            break;
          case 0x5d:
            attribute += " (Auto, Fired, Red-eye reduction, Return not detected)";
            break;
          case 0x5f:
            attribute += " (Auto, Fired, Red-eye reduction, Return detected)";
            break;
          default:
            break;
        }
      } else if (attr.equals(ExifInterface.TAG_WHITE_BALANCE)) {
        int wb = Integer.parseInt(attribute);
        switch (wb) {
          case 0:
            attribute += " (Auto)";
            break;
          case 1:
            attribute += " (Manual)";
            break;
        }
      }
      tv.setText(attribute);
    }
  }

  public void showExifDialog(String imagePath) {
    File imageFile = FrescoUtils.getCachedImageOnDisk(Uri.parse(imagePath));
    if (imageFile == null) {
      Toast.makeText(FSImageViewerActivity.this, "无法读取缓存文件！", Toast.LENGTH_SHORT).show();
      return;
    }

    // show exif information dialog
    LayoutInflater inflater = getLayoutInflater();
    View layout = inflater.inflate(R.layout.image_exif_info, null);
    try {
      String sFileName = imageFile.getAbsolutePath();

      ExifInterface exif = new ExifInterface(sFileName);
      // basic information
      TextView tvFilename = (TextView) layout.findViewById(R.id.ii_filename);
      tvFilename.setText(imagePath);
      setImageAttributeFromExif(layout, R.id.ii_datetime, exif, ExifInterface.TAG_DATETIME);
      setImageAttributeFromExif(layout, R.id.ii_width, exif, ExifInterface.TAG_IMAGE_WIDTH);
      setImageAttributeFromExif(layout, R.id.ii_height, exif, ExifInterface.TAG_IMAGE_LENGTH);

      // get filesize
      try {
        long filesize = FileSizeUtil.getFileSize(imageFile);
        TextView tv = (TextView) layout.findViewById(R.id.ii_size);
        if (tv != null) {
          tv.setText(FileSizeUtil.FormatFileSize(filesize));
        }
      } catch (Exception e) {
        Log.e(TAG, "showExifDialog: " + Log.getStackTraceString(e));
      }

      // capture information
      setImageAttributeFromExif(layout, R.id.ii_make, exif, ExifInterface.TAG_MAKE);
      setImageAttributeFromExif(layout, R.id.ii_model, exif, ExifInterface.TAG_MODEL);
      setImageAttributeFromExif(layout, R.id.ii_focal_length, exif, ExifInterface.TAG_FOCAL_LENGTH);
      setImageAttributeFromExif(layout, R.id.ii_aperture, exif, ExifInterface.TAG_APERTURE);
      setImageAttributeFromExif(layout, R.id.ii_exposure_time, exif, ExifInterface.TAG_EXPOSURE_TIME);
      setImageAttributeFromExif(layout, R.id.ii_flash, exif, ExifInterface.TAG_FLASH);
      setImageAttributeFromExif(layout, R.id.ii_iso, exif, ExifInterface.TAG_ISO);
      setImageAttributeFromExif(layout, R.id.ii_white_balance, exif, ExifInterface.TAG_WHITE_BALANCE);
    } catch (IOException e) {
      Log.d("read ExifInfo", "can't read Exif information");
    }

    new AlertDialog.Builder(FSImageViewerActivity.this).setView(layout).setOnDismissListener(new DialogInterface.OnDismissListener() {
      @Override public void onDismiss(DialogInterface dialog) {
      }
    }).show();
  }

  private void toggleToobarVisibility() {
    int visibility = layoutToolbar.getVisibility();
    if (visibility == LinearLayout.GONE) {
      showSystemUI();
      layoutToolbar.setVisibility(LinearLayout.VISIBLE);
    } else {
      hideSystemUI();
      layoutToolbar.setVisibility(LinearLayout.GONE);
    }
  }

  @Override
  public void onOutsidePhotoTap(ImageView imageView) {
    toggleToobarVisibility();
  }

  @Override
  public void onPhotoTap(ImageView view, float x, float y) {
    toggleToobarVisibility();
  }
}
