package com.zfdang.zsmth_android;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zfdang.zsmth_android.models.Board;
import com.zfdang.zsmth_android.models.ListBoardContent;
import com.zfdang.zsmth_android.newsmth.SMTHHelper;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
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
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnBoardFragmentInteractionListener mListener;

    private RecyclerView mRecylerView = null;
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

    public boolean atFavoriteRoot() {
        return !(mFavoritePaths != null && mFavoritePaths.size() > 1);
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FavoriteBoardFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static FavoriteBoardFragment newInstance(int columnCount) {
        FavoriteBoardFragment fragment = new FavoriteBoardFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        // set the initial favorite path chain
        mFavoritePaths = new ArrayList<String>();
        mFavoritePathNames = new ArrayList<String>();
        pushFavoritePath("", "根目录");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRecylerView = (RecyclerView) inflater.inflate(R.layout.fragment_favorite_board, container, false);


        // Set the adapter
        if (mRecylerView != null) {
//            http://stackoverflow.com/questions/28713231/recyclerview-item-separator
            mRecylerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
            Context context = mRecylerView.getContext();
            if (mColumnCount <= 1) {
                mRecylerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                mRecylerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mRecylerView.setAdapter(new BoardRecyclerViewAdapter(ListBoardContent.FAVORITE_BOARDS, mListener));
        }

        if(ListBoardContent.FAVORITE_BOARDS.size() == 0) {
            // only load boards on the first time
            RefreshFavoriteBoards();
        }

        return mRecylerView;
    }

    public void showLoadingHints() {
        MainActivity activity = (MainActivity)getActivity();
        activity.showProgress("加载收藏版面，请稍候...", true);
    }

    public void clearLoadingHints () {
        // disable progress bar
        MainActivity activity = (MainActivity) getActivity();
        activity.showProgress("", false);
    }

    public void RefreshFavoriteBoards() {
        showLoadingHints();
        LoadFavoriteBoardsByPath(getCurrentFavoritePath());
    }

    protected void LoadFavoriteBoardsByPath(final String path) {
        SMTHHelper helper = SMTHHelper.getInstance();
        helper.wService.getFavoriteByPath(path)
                .flatMap(new Func1<ResponseBody, Observable<Board>>() {
                    @Override
                    public Observable<Board> call(ResponseBody resp) {
                        try {
                            String response = SMTHHelper.DecodeResponseFromWWW(resp.bytes());
                            Log.d(TAG, response);
                            List<Board> boards = SMTHHelper.ParseFavoriteBoardsFromWWW(response);
                            return Observable.from(boards);
                        } catch (Exception e) {
                            Log.d(TAG, "Failed to load favorite {" + path + "}");
                            Log.d(TAG, e.toString());
                            return null;
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Board>() {
                    @Override
                    public void onStart() {

                        super.onStart();
                        ListBoardContent.clearFavorites();
                        mRecylerView.getAdapter().notifyDataSetChanged();
                    }

                    @Override
                    public void onCompleted() {
                        clearLoadingHints();
                        updateFavoriteTitle();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, e.toString());

                    }

                    @Override
                    public void onNext(Board board) {
                        ListBoardContent.addFavoriteItem(board);
                        mRecylerView.getAdapter().notifyItemInserted(ListBoardContent.FAVORITE_BOARDS.size());
                        Log.d(TAG, board.toString());
                    }
                });
    }

    private void updateFavoriteTitle(){
        if(mOriginalTitle == null) {
            mOriginalTitle = getActivity().getTitle().toString();
        }
        if( mFavoritePathNames != null && mFavoritePathNames.size() > 1) {
            String path = "";
            for(int i = 1; i < mFavoritePathNames.size(); i ++) {
                path += ">" + mFavoritePathNames.get(i);
            }
            getActivity().setTitle(mOriginalTitle + path);

        } else {
            getActivity().setTitle(mOriginalTitle);
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
    
}
