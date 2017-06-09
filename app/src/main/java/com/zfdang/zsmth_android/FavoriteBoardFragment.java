package com.zfdang.zsmth_android;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.listeners.OnBoardFragmentInteractionListener;
import com.zfdang.zsmth_android.models.Board;
import com.zfdang.zsmth_android.models.BoardListContent;
import com.zfdang.zsmth_android.newsmth.SMTHHelper;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Subscriber;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnBoardFragmentInteractionListener}
 * interface.
 */
public class FavoriteBoardFragment extends Fragment {

  private final String TAG = "FavoriteBoardFragment";
  private OnBoardFragmentInteractionListener mListener;

  private RecyclerView mRecyclerView = null;
  private String mDefaultTitle = null;

  // list of favorite paths
  private List<String> mFavoritePaths = null;
  private List<String> mFavoritePathNames = null;

  public void pushFavoritePathAndName(String path, String name) {
    mFavoritePaths.add(path);
    mFavoritePathNames.add(name.trim());
  }

  public void popFavoritePathAndName() {
    if (mFavoritePaths.size() > 0 && mFavoritePathNames.size() > 0) {
      this.mFavoritePaths.remove(this.mFavoritePaths.size() - 1);
      this.mFavoritePathNames.remove(this.mFavoritePathNames.size() - 1);
    }
  }

  public String getCurrentFavoritePath() {
    if (mFavoritePaths.size() > 0) {
      return this.mFavoritePaths.get(this.mFavoritePaths.size() - 1);
    } else {
      return "";
    }
  }

  public String getCurrentFavoritePathName() {
    if (mFavoritePathNames.size() > 0) {
      return this.mFavoritePathNames.get(this.mFavoritePathNames.size() - 1);
    } else {
      return "";
    }
  }

  public boolean atFavoriteRoot() {
    return mFavoritePaths.size() == 0;
  }

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public FavoriteBoardFragment() {
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);

    if (mFavoritePaths == null) {
      mFavoritePaths = new ArrayList<String>();
    }
    if (mFavoritePathNames == null) {
      mFavoritePathNames = new ArrayList<String>();
    }
    if (mDefaultTitle == null) {
      mDefaultTitle = SMTHApplication.App_Title_Prefix + "收藏夹";
    }
    updateFavoriteTitle();
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_favorite_board, container, false);

    // Set the adapter
    if (mRecyclerView != null) {
      //            http://stackoverflow.com/questions/28713231/recyclerview-item-separator
      mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL, 0));
      Context context = mRecyclerView.getContext();
      mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(context));
      mRecyclerView.setAdapter(new BoardRecyclerViewAdapter(BoardListContent.FAVORITE_BOARDS, mListener));
    }

    if (BoardListContent.FAVORITE_BOARDS.size() == 0) {
      // only load boards on the first time
      RefreshFavoriteBoards();
    }

    return mRecyclerView;
  }

  public void showLoadingHints() {
    MainActivity activity = (MainActivity) getActivity();
    if (activity != null) activity.showProgress("加载收藏版面，请稍候...");
  }

  public void clearLoadingHints() {
    // disable progress bar
    MainActivity activity = (MainActivity) getActivity();
    if (activity != null) {
      activity.dismissProgress();
    }
  }

  public void RefreshFavoriteBoardsWithCache() {
    SMTHHelper.ClearBoardListCache(SMTHHelper.BOARD_TYPE_FAVORITE, getCurrentFavoritePath());
    RefreshFavoriteBoards();
  }

  public void RefreshFavoriteBoards() {
    showLoadingHints();
    LoadFavoriteBoardsByPath(getCurrentFavoritePath());
  }

  protected void LoadFavoriteBoardsByPath(final String path) {
    SMTHHelper helper = SMTHHelper.getInstance();

    // all boards loaded in cached file
    final Observable<List<Board>> cache = Observable.create(new ObservableOnSubscribe<List<Board>>() {
      @Override public void subscribe(@NonNull ObservableEmitter<List<Board>> observableEmitter) throws Exception {
        List<Board> boards = SMTHHelper.LoadBoardListFromCache(SMTHHelper.BOARD_TYPE_FAVORITE, path);
        if (boards != null && boards.size() > 0) {
          observableEmitter.onNext(boards);
        } else {
          observableEmitter.onComplete();
        }
      }
    });

    // all boards loaded from network
    Observable<List<Board>> network = null;
    String pathName = getCurrentFavoritePathName();
    if (pathName.endsWith("[二级目录]")) {
      // if user create cusomized folder like xxxx.[二级目录], this implementation will fail
      // but let's assume that noboday will do this
      network = Observable.create(new ObservableOnSubscribe<List<Board>>() {
        @Override public void subscribe(@NonNull ObservableEmitter<List<Board>> observableEmitter) throws Exception {
          List<Board> boards = SMTHHelper.LoadFavoriteBoardsInGroupFromWWW(path);
          if (boards != null && boards.size() > 0) {
            observableEmitter.onNext(boards);
          } else {
            observableEmitter.onComplete();
          }
        }
      });
    } else {
      network = Observable.create(new ObservableOnSubscribe<List<Board>>() {
        @Override public void subscribe(@NonNull ObservableEmitter<List<Board>> observableEmitter) throws Exception {
          List<Board> boards = SMTHHelper.LoadFavoriteBoardsByFolderFromWWW(path);
          if (boards != null && boards.size() > 0) {
            observableEmitter.onNext(boards);
          } else {
            observableEmitter.onComplete();
          }
        }
      });
    }

    List<Board> boards = new ArrayList<>();
    Observable.concat(cache, network).first(boards).toObservable().flatMap(new Function<List<Board>, ObservableSource<Board>>() {
      @Override public ObservableSource<Board> apply(@NonNull List<Board> boards) throws Exception {
        return Observable.fromIterable(boards);
      }
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Board>() {
      @Override public void onSubscribe(@NonNull Disposable disposable) {
        BoardListContent.clearFavorites();
        mRecyclerView.getAdapter().notifyDataSetChanged();
      }

      @Override public void onNext(@NonNull Board board) {
        BoardListContent.addFavoriteItem(board);
        mRecyclerView.getAdapter().notifyItemInserted(BoardListContent.FAVORITE_BOARDS.size());
        // Log.d(TAG, board.toString());
      }

      @Override public void onError(@NonNull Throwable e) {
        clearLoadingHints();
        Toast.makeText(SMTHApplication.getAppContext(), "加载收藏夹失败!\n" + e.toString(), Toast.LENGTH_LONG).show();

      }

      @Override public void onComplete() {
        clearLoadingHints();
        updateFavoriteTitle();
      }
    });
  }

  private void updateFavoriteTitle() {
    Activity activity = getActivity();
    if (activity == null) {
      return;
    }

    String title = "";
    for (int i = 0; i < mFavoritePathNames.size(); i++) {
      title += ">" + mFavoritePathNames.get(i);
    }
    activity.setTitle(mDefaultTitle + title);
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnBoardFragmentInteractionListener) {
      mListener = (OnBoardFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString() + " must implement OnBoardFragmentInteractionListener");
    }
  }

  @Override public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.main_action_refresh) {
      RefreshFavoriteBoardsWithCache();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
