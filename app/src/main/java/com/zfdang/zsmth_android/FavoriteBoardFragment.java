package com.zfdang.zsmth_android;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
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
  private String mDefaultTitle = "收藏夹";

  // list of favorite paths
  private List<Board> mFavoritePaths = null;

  public void pushPath(Board board) {
    mFavoritePaths.add(board);
  }

  public void popPath() {
    if (mFavoritePaths.size() > 0) {
      this.mFavoritePaths.remove(this.mFavoritePaths.size() - 1);
    }
  }

  public Board getCurrentPath() {
    if (mFavoritePaths.size() > 0) {
      return this.mFavoritePaths.get(this.mFavoritePaths.size() - 1);
    } else {
      return null;
    }
  }

  public String getCurrentPathInString() {
    Board board = getCurrentPath();
    if (board == null) {
      return "";
    }
    if(board.isSection()) {
      return board.getSectionID();
    }
    if(board.isFolder()) {
      return board.getFolderID();
    }
    return "";
  }

  public boolean isAtRoot() {
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
      mFavoritePaths = new ArrayList<Board>();
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
    SMTHHelper.ClearBoardListCache(SMTHHelper.BOARD_TYPE_FAVORITE, getCurrentPathInString());
    RefreshFavoriteBoards();
  }

  public void RefreshFavoriteBoards() {
    showLoadingHints();
    LoadFavoriteBoardsByPath();
  }

  protected void LoadFavoriteBoardsByPath() {
    SMTHHelper helper = SMTHHelper.getInstance();

    Board board = getCurrentPath();
    final String finalCurrentPath = getCurrentPathInString();

    // all boards loaded in cached file
    final Observable<List<Board>> cache = Observable.create(new ObservableOnSubscribe<List<Board>>() {
      @Override public void subscribe(@NonNull ObservableEmitter<List<Board>> observableEmitter) throws Exception {
        List<Board> boards = SMTHHelper.LoadBoardListFromCache(SMTHHelper.BOARD_TYPE_FAVORITE, finalCurrentPath);
        if (boards != null && boards.size() > 0) {
          observableEmitter.onNext(boards);
        } else {
          observableEmitter.onComplete();
        }
      }
    });

    // all boards loaded from network
    Observable<List<Board>> network = null;
    if (board == null || board.isFolder()) {
      // 用户在收藏夹里创建的目录
      network = Observable.create(new ObservableOnSubscribe<List<Board>>() {
        @Override public void subscribe(@NonNull ObservableEmitter<List<Board>> observableEmitter) throws Exception {
          List<Board> boards = SMTHHelper.LoadFavoriteBoardsInFolder(finalCurrentPath);
          if (boards != null && boards.size() > 0) {
            observableEmitter.onNext(boards);
          } else {
            observableEmitter.onComplete();
          }
        }
      });
    } else if(board != null && board.isSection()){
      // 用户在收藏夹里收藏的系统的二级目录
      network = Observable.create(new ObservableOnSubscribe<List<Board>>() {
        @Override public void subscribe(@NonNull ObservableEmitter<List<Board>> observableEmitter) throws Exception {
          List<Board> boards = SMTHHelper.LoadFavoriteBoardsInSection(finalCurrentPath);
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
    if(mFavoritePaths.size() == 0) {
      title = SMTHApplication.App_Title_Prefix + mDefaultTitle;
    } else {
      title = mDefaultTitle;
      for (int i = 0; i < mFavoritePaths.size(); i++) {
        Board board = mFavoritePaths.get(i);
        if(board.isFolder()) {
          title += " | " + mFavoritePaths.get(i).getFolderName();
        } else if(board.isSection()) {
          title += " | " + mFavoritePaths.get(i).getSectionName();
        }
      }
    }
    activity.setTitle(title);
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
