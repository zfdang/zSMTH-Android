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

import java.util.ArrayList;
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
public class FavoriteBoardFragment extends Fragment {

    private final String TAG = "FavoriteBoardFragment";
    private OnBoardFragmentInteractionListener mListener;

    private RecyclerView mRecyclerView = null;
    private String mOriginalTitle = null;

    // list of favorite paths
    private List<String> mFavoritePaths = null;
    private List<String> mFavoritePathNames = null;

    public void pushFavoritePath(String path, String name) {
        if(mFavoritePaths == null) {
            mFavoritePaths = new ArrayList<String>();
        }
        if(mFavoritePathNames == null) {
            mFavoritePathNames = new ArrayList<String>();
        }
        mFavoritePaths.add(path);
        mFavoritePathNames.add(name.trim());
    }

    public void popFavoritePath() {
        if(mFavoritePaths != null & mFavoritePaths.size() > 1){
            this.mFavoritePaths.remove(this.mFavoritePaths.size()-1);
            this.mFavoritePathNames.remove(this.mFavoritePathNames.size()-1);
        }
    }

    public String getCurrentFavoritePath(){
        if(mFavoritePaths != null & mFavoritePaths.size() > 0){
            return this.mFavoritePaths.get(this.mFavoritePaths.size() - 1);
        } else {
            return "";
        }
    }

    public String getCurrentFavoritePathName(){
        if(mFavoritePathNames != null & mFavoritePathNames.size() > 0){
            return this.mFavoritePathNames.get(this.mFavoritePathNames.size() - 1);
        } else {
            return "";
        }
    }

    public boolean atFavoriteRoot() {
        return !(mFavoritePaths != null && mFavoritePaths.size() > 1);
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FavoriteBoardFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // set the initial favorite path chain
        mFavoritePaths = new ArrayList<String>();
        mFavoritePathNames = new ArrayList<String>();
        pushFavoritePath("", "根目录");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_favorite_board, container, false);


        // Set the adapter
        if (mRecyclerView != null) {
//            http://stackoverflow.com/questions/28713231/recyclerview-item-separator
            mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL, 0));
            Context context = mRecyclerView.getContext();
            mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(context));
            mRecyclerView.setAdapter(new BoardRecyclerViewAdapter(BoardListContent.FAVORITE_BOARDS, mListener));
        }

        if(BoardListContent.FAVORITE_BOARDS.size() == 0) {
            // only load boards on the first time
            RefreshFavoriteBoards();
        }

        return mRecyclerView;
    }

    public void showLoadingHints() {
        MainActivity activity = (MainActivity)getActivity();
        if(activity != null)
            activity.showProgress("加载收藏版面，请稍候...");
    }

    public void clearLoadingHints () {
        // disable progress bar
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null){
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
        final Observable<List<Board>> cache = Observable.create(new Observable.OnSubscribe<List<Board>>() {
            @Override
            public void call(Subscriber<? super List<Board>> subscriber) {
                List<Board> boards = SMTHHelper.LoadBoardListFromCache(SMTHHelper.BOARD_TYPE_FAVORITE, path);
                if(boards != null && boards.size() > 0) {
                    subscriber.onNext(boards);
                } else {
                    subscriber.onCompleted();
                }
            }
        });

        // all boards loaded from network
        Observable<List<Board>> network = null;
        String pathName = getCurrentFavoritePathName();
        if(pathName.endsWith("[二级目录]")) {
            // if user create cusomized folder like xxxx.[二级目录], this implementation will fail
            // but let's assume that noboday will do this
            network = Observable.create(new Observable.OnSubscribe<List<Board>>() {
                @Override
                public void call(Subscriber<? super List<Board>> subscriber) {
                    List<Board> boards = SMTHHelper.LoadFavoriteBoardsInGroupFromWWW(path);
                    if (boards != null && boards.size() > 0) {
                        subscriber.onNext(boards);
                    } else {
                        subscriber.onCompleted();
                    }
                }
            });
        } else {
            network = Observable.create(new Observable.OnSubscribe<List<Board>>() {
                @Override
                public void call(Subscriber<? super List<Board>> subscriber) {
                    List<Board> boards = SMTHHelper.LoadFavoriteBoardsByFolderFromWWW(path);
                    if (boards != null && boards.size() > 0) {
                        subscriber.onNext(boards);
                    } else {
                        subscriber.onCompleted();
                    }
                }
            });
        }

        Observable.concat(cache, network)
                .first()
                .flatMap(new Func1<List<Board>, Observable<Board>>() {
                    @Override
                    public Observable<Board> call(List<Board> boards) {
                        return Observable.from(boards);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Board>() {
                    @Override
                    public void onStart() {

                        super.onStart();
                        BoardListContent.clearFavorites();
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                    }

                    @Override
                    public void onCompleted() {
                        clearLoadingHints();
                        updateFavoriteTitle();
                    }

                    @Override
                    public void onError(Throwable e) {
                        clearLoadingHints();
                        Toast.makeText(SMTHApplication.getAppContext(), "加载收藏夹失败!\n" + e.toString(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(Board board) {
                        BoardListContent.addFavoriteItem(board);
                        mRecyclerView.getAdapter().notifyItemInserted(BoardListContent.FAVORITE_BOARDS.size());
//                        Log.d(TAG, board.toString());
                    }
                });
    }

    private void updateFavoriteTitle(){
        Activity activity = getActivity();
        if(activity == null){
            return;
        }

        if(mOriginalTitle == null) {
            mOriginalTitle = activity.getTitle().toString();
        }
        if( mFavoritePathNames != null && mFavoritePathNames.size() > 1) {
            String path = "";
            for(int i = 1; i < mFavoritePathNames.size(); i ++) {
                path += ">" + mFavoritePathNames.get(i);
            }
            activity.setTitle(mOriginalTitle + path);
        } else {
            activity.setTitle(mOriginalTitle);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnBoardFragmentInteractionListener) {
            mListener = (OnBoardFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnBoardFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.main_action_refresh) {
            RefreshFavoriteBoardsWithCache();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
