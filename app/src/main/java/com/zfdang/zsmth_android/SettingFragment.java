package com.zfdang.zsmth_android;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.helpers.FileLess;
import com.zfdang.zsmth_android.helpers.FileSizeUtil;

import java.io.File;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class SettingFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "SettingFragment";

    private OnFragmentInteractionListener mListener;
    private TextView setting_okhttp3_cache;
    private Button setting_okhttp3_cache_button;
    private TextView setting_fresco_cache;
    private Button setting_fresco_cache_button;
    private EditText setting_signature;
    private Button setting_signature_button;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        setting_okhttp3_cache = (TextView) view.findViewById(R.id.setting_okhttp3_cache);
        setting_fresco_cache = (TextView) view.findViewById(R.id.setting_fresco_cache);

        setting_okhttp3_cache_button = (Button) view.findViewById(R.id.setting_okhttp3_cache_button);
        setting_okhttp3_cache_button.setOnClickListener(this);

        setting_fresco_cache_button = (Button) view.findViewById(R.id.setting_fresco_cache_button);
        setting_fresco_cache_button.setOnClickListener(this);

        setting_signature= (EditText) view.findViewById(R.id.setting_device_signature);
        setting_signature.setText(Settings.getInstance().getSignature());
        setting_signature_button = (Button) view.findViewById(R.id.setting_device_signature_confirm);
        setting_signature_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String signature = setting_signature.getText().toString();
                Settings.getInstance().setSignature(signature);
                Toast.makeText(getActivity(), "设备签名已保存: " + signature, Toast.LENGTH_SHORT).show();
            }
        });

        updateOkHttp3Cache();
        updateFrescoCache();
        return view;
    }

    public void updateOkHttp3Cache() {
        File httpCacheDirectory = new File(SMTHApplication.getAppContext().getCacheDir(), "Responses");
        updateCacheSize(httpCacheDirectory.getAbsolutePath(), setting_okhttp3_cache);
    }

    public void updateFrescoCache() {
        File frescoCacheDirectory = new File(SMTHApplication.getAppContext().getCacheDir(), "image_cache");
        // Log.d(TAG, "updateFrescoCache: " + frescoCacheDirectory.getAbsolutePath());
        updateCacheSize(frescoCacheDirectory.getAbsolutePath(), setting_fresco_cache);
    }


    public void updateCacheSize(final String folder, final TextView tv) {
        Observable.just(folder)
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        return FileSizeUtil.getAutoFileOrFolderSize(s);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: " + Log.getStackTraceString(e));
                    }

                    @Override
                    public void onNext(String s) {
                        Log.d(TAG, "onNext: Folder size = " + s);
                        tv.setText(s);
                    }
                });

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {

        if(v == setting_fresco_cache_button) {
            ImagePipeline imagePipeline = Fresco.getImagePipeline();
            imagePipeline.clearDiskCaches();

            updateFrescoCache();
        } else if (v == setting_okhttp3_cache_button) {
            File cache = new File(SMTHApplication.getAppContext().getCacheDir(), "Responses");
            FileLess.$del(cache);
            if (!cache.exists()) {
                cache.mkdir();
            }
            updateOkHttp3Cache();
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
