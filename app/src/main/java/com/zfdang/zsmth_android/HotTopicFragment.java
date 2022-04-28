package com.zfdang.zsmth_android;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.helpers.RecyclerViewUtil;
import com.zfdang.zsmth_android.listeners.OnTopicFragmentInteractionListener;
import com.zfdang.zsmth_android.listeners.OnVolumeUpDownListener;
import com.zfdang.zsmth_android.models.Topic;
import com.zfdang.zsmth_android.models.TopicListContent;
import com.zfdang.zsmth_android.newsmth.SMTHHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import okhttp3.ResponseBody;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnTopicFragmentInteractionListener}
 * interface.
 */
public class HotTopicFragment extends Fragment implements OnVolumeUpDownListener {

  private final String TAG = "HotTopicFragment";

  private OnTopicFragmentInteractionListener mListener;

  private RecyclerView mRecyclerView = null;
  private SmartRefreshLayout mRefreshLayout = null;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public HotTopicFragment() {
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // http://stackoverflow.com/questions/8308695/android-options-menu-in-fragment
    setHasOptionsMenu(true);
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_hot_topic, container, false);

    // http://sapandiwakar.in/pull-to-refresh-for-android-recyclerview-or-any-other-vertically-scrolling-view/
    // pull to refresh for android recyclerview
    mRefreshLayout = (SmartRefreshLayout) rootView;
    mRefreshLayout.setEnableLoadMore(false);
    mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
      @Override public void onRefresh(RefreshLayout refreshLayout) {
        RefreshGuidanceFromWWW();
      }
    });

    // http://blog.csdn.net/lmj623565791/article/details/45059587
    // 你想要控制Item间的间隔（可绘制），请通过ItemDecoration
    // 你想要控制Item增删的动画，请通过ItemAnimator
    // 你想要控制点击、长按事件，请自己写
    // item被按下的时候的highlight,这个是通过guidance item的backgroun属性来实现的 (android:background="@drawable/recyclerview_item_bg")
    mRecyclerView = (RecyclerView) rootView.findViewById(R.id.guidance_recycler_view);
    // Set the adapter
    if (mRecyclerView != null) {
      mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL, 0));
      Context context = rootView.getContext();
      mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(context));
      mRecyclerView.setItemAnimator(new DefaultItemAnimator());
      mRecyclerView.setAdapter(new HotTopicRecyclerViewAdapter(TopicListContent.HOT_TOPICS, mListener));
    }

    getActivity().setTitle(SMTHApplication.App_Title_Prefix + "首页导读");

    if (TopicListContent.HOT_TOPICS.size() == 0) {
      RefreshGuidance();
    }
    return rootView;
  }

  public void showLoadingHints() {
    MainActivity activity = (MainActivity) getActivity();
    if (activity != null) activity.showProgress("获取导读信息...");
  }

  public void clearLoadingHints() {
    // disable progress bar
    MainActivity activity = (MainActivity) getActivity();
    if (activity != null) {
      activity.dismissProgress();
    }

    // disable SmartFreshLayout
    if(mRefreshLayout.isRefreshing()) {
      mRefreshLayout.finishRefresh(100);
    }
  }

  public void RefreshGuidance() {
    // called by onCreate & refresh menu item
    showLoadingHints();
    RefreshGuidanceFromWWW();
  }

  public void RefreshGuidanceFromWWW() {
    final SMTHHelper helper = SMTHHelper.getInstance();

    helper.wService.getAllHotTopics().flatMap(new Function<ResponseBody, ObservableSource<Topic>>() {
      @Override public ObservableSource<Topic> apply(@NonNull ResponseBody responseBody) throws Exception {
        try {
          String response = responseBody.string();
          List<Topic> results = SMTHHelper.ParseHotTopicsFromWWW(response);
          return Observable.fromIterable(results);
        } catch (Exception e) {
          Log.d(TAG, Log.getStackTraceString(e));
        }
        return null;
      }
    }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Topic>() {
      @Override public void onSubscribe(@NonNull Disposable disposable) {
        // clearHotTopics current hot topics
        TopicListContent.clearHotTopics();
        mRecyclerView.getAdapter().notifyDataSetChanged();
      }

      @Override public void onNext(@NonNull Topic topic) {
        //                        Log.d(TAG, topic.toString());
        TopicListContent.addHotTopic(topic);
        mRecyclerView.getAdapter().notifyItemInserted(TopicListContent.HOT_TOPICS.size() - 1);
      }

      @Override public void onError(@NonNull Throwable e) {
        clearLoadingHints();
        Toast.makeText(SMTHApplication.getAppContext(), "获取热帖失败!\n" + e.toString(), Toast.LENGTH_LONG).show();
      }

      @Override public void onComplete() {
        Topic topic = new Topic("-- END --");
        TopicListContent.addHotTopic(topic);
        mRecyclerView.getAdapter().notifyItemInserted(TopicListContent.HOT_TOPICS.size() - 1);

        clearLoadingHints();
      }
    });
  }

  // http://stackoverflow.com/questions/32604552/onattach-not-called-in-fragment
  // If you run your application on a device with API 23 (marshmallow) then onAttach(Context) will be called.
  // On all previous Android Versions onAttach(Activity) will be called.
  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnTopicFragmentInteractionListener) {
      mListener = (OnTopicFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString() + " must implement OnTopicFragmentInteractionListener");
    }
  }

  @Override public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.main_action_refresh) {
      RefreshGuidance();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public boolean onVolumeUpDown(int keyCode) {
    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
      RecyclerViewUtil.ScrollRecyclerViewByKey(mRecyclerView, keyCode);
    } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
      RecyclerViewUtil.ScrollRecyclerViewByKey(mRecyclerView, keyCode);
    }
    return true;
  }
}
