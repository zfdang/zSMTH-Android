package com.zfdang.zsmth_android;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.listeners.OnBoardFragmentInteractionListener;
import com.zfdang.zsmth_android.listeners.OnVolumeUpDownListener;
import com.zfdang.zsmth_android.models.Board;
import com.zfdang.zsmth_android.models.BoardListContent;
import com.zfdang.zsmth_android.newsmth.SMTHHelper;
import java.util.List;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnBoardFragmentInteractionListener}
 * interface.
 */
public class AllBoardFragment extends Fragment implements OnVolumeUpDownListener {

  final private String TAG = "AllBoardFragment";
  private RecyclerView mRecyclerView = null;
  private SearchView mSearchView = null;
  private QueryTextListener mQueryListener = null;

  private OnBoardFragmentInteractionListener mListener = null;
  private BoardRecyclerViewAdapter mAdapter = null;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public AllBoardFragment() {
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_all_board, container, false);

    mRecyclerView = (RecyclerView) view.findViewById(R.id.all_board_list);
    // Set the adapter
    mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL, 0));
    Context context = view.getContext();
    mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(context));
    if (mAdapter == null) {
      // this is very important, we only create adapter on the first time.
      // otherwise, BoardListContent.ALL_BOARDS might be filtered result already
      mAdapter = new BoardRecyclerViewAdapter(BoardListContent.ALL_BOARDS, mListener);
    }
    mRecyclerView.setAdapter(mAdapter);

    mSearchView = (SearchView) view.findViewById(R.id.all_board_search);
    mSearchView.setIconifiedByDefault(false);

    // http://stackoverflow.com/questions/11321129/is-it-possible-to-change-the-textcolor-on-an-android-searchview
    int id = mSearchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
    TextView textView = (TextView) mSearchView.findViewById(id);
    textView.setTextColor(getResources().getColor(R.color.status_text_night));
    textView.setHintTextColor(getResources().getColor(R.color.status_text_night));

    if (mQueryListener == null) {
      mQueryListener = new QueryTextListener((BoardRecyclerViewAdapter) mRecyclerView.getAdapter());
    }
    mSearchView.setOnQueryTextListener(mQueryListener);

    // set focus to recyclerview
    mRecyclerView.requestFocus();

    if (BoardListContent.ALL_BOARDS.size() == 0) {
      // only load boards on the first timer
      LoadAllBoards();
    }

    return view;
  }

  public void showLoadingHints() {
    MainActivity activity = (MainActivity) getActivity();
    if (activity != null) activity.showProgress("从缓存或网络加载所有版面，请耐心等待...");
  }

  public void clearLoadingHints() {
    // disable progress bar
    MainActivity activity = (MainActivity) getActivity();
    if (activity != null) activity.dismissProgress();
  }

  public void LoadAllBoardsWithoutCache() {
    SMTHHelper.ClearBoardListCache(SMTHHelper.BOARD_TYPE_ALL, null);
    LoadAllBoards();
  }

  public void LoadAllBoards() {
    showLoadingHints();

    // all boards loaded in cached file
    final Observable<List<Board>> cache = Observable.create(new Observable.OnSubscribe<List<Board>>() {
      @Override public void call(Subscriber<? super List<Board>> subscriber) {
        List<Board> boards = SMTHHelper.LoadBoardListFromCache(SMTHHelper.BOARD_TYPE_ALL, null);
        if (boards != null && boards.size() > 0) {
          subscriber.onNext(boards);
        } else {
          subscriber.onCompleted();
        }
      }
    });

    // all boards loaded from network
    final Observable<List<Board>> network = Observable.create(new Observable.OnSubscribe<List<Board>>() {
      @Override public void call(Subscriber<? super List<Board>> subscriber) {
        List<Board> boards = SMTHHelper.LoadAllBoardsFromWWW();
        if (boards != null && boards.size() > 0) {
          subscriber.onNext(boards);
        } else {
          subscriber.onCompleted();
        }
      }
    });

    // use the first available source to load all boards
    Observable.concat(cache, network).first().flatMap(new Func1<List<Board>, Observable<Board>>() {
      @Override public Observable<Board> call(List<Board> boards) {
        return Observable.from(boards);
      }
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Board>() {
      @Override public void onStart() {
        super.onStart();
        BoardListContent.clearAllBoards();
        mRecyclerView.getAdapter().notifyDataSetChanged();
      }

      @Override public void onCompleted() {
        clearLoadingHints();
      }

      @Override public void onError(Throwable e) {
        clearLoadingHints();
        Toast.makeText(SMTHApplication.getAppContext(), "加载所有版面失败!\n" + e.toString(), Toast.LENGTH_LONG).show();
      }

      @Override public void onNext(Board board) {
        //                        Log.d(TAG, board.toString());
        BoardListContent.addAllBoardItem(board);
        int size = BoardListContent.ALL_BOARDS.size();
        mRecyclerView.getAdapter().notifyItemInserted(size - 1);
        if (size == 50) {
          // if 50 items have been shown already, stop the loading hints
          clearLoadingHints();
        }
      }
    });
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
      LoadAllBoardsWithoutCache();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  public class QueryTextListener implements SearchView.OnQueryTextListener {
    private BoardRecyclerViewAdapter mAdapter = null;

    public QueryTextListener(BoardRecyclerViewAdapter mAdapter) {
      this.mAdapter = mAdapter;
    }

    @Override public boolean onQueryTextSubmit(String query) {
      return false;
    }

    @Override public boolean onQueryTextChange(String newText) {
      Log.d(TAG, newText);
      mAdapter.getFilter().filter(newText);
      return true;
    }
  }

  @Override public boolean onVolumeUpDown(int keyCode) {
    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
      Toast.makeText(SMTHApplication.getAppContext(), "Volume UP, to scroll up", Toast.LENGTH_SHORT).show();
    } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
      Toast.makeText(SMTHApplication.getAppContext(), "Volume down, to scroll down", Toast.LENGTH_SHORT).show();
    }
    return true;
  }
}
