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
import android.widget.SearchView;

import com.zfdang.zsmth_android.models.Board;
import com.zfdang.zsmth_android.models.ListBoardContent;
import com.zfdang.zsmth_android.newsmth.SMTHHelper;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnBoardFragmentInteractionListener}
 * interface.
 */
public class AllBoardFragment extends Fragment {

    final private String TAG = "AllBoardFragment";
    private RecyclerView mRecylerView = null;
    private SearchView mSearchView = null;
    private QueryTextListner mQueryListner = null;

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnBoardFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AllBoardFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static AllBoardFragment newInstance(int columnCount) {
        AllBoardFragment fragment = new AllBoardFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_board, container, false);

        mRecylerView = (RecyclerView) view.findViewById(R.id.all_board_list);
        // Set the adapter
        mRecylerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        Context context = view.getContext();
        if (mColumnCount <= 1) {
            mRecylerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            mRecylerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        mRecylerView.setAdapter(new BoardRecyclerViewAdapter(ListBoardContent.ALL_BOARDS, mListener));

        mSearchView = (SearchView) view.findViewById(R.id.all_board_search);
        mSearchView.setIconifiedByDefault(false);
//        mSearchView.setSubmitButtonEnabled(true);
        if( mQueryListner == null) {
            mQueryListner = new QueryTextListner((BoardRecyclerViewAdapter) mRecylerView.getAdapter());
            mSearchView.setOnQueryTextListener(mQueryListner);
        }

        if(ListBoardContent.ALL_BOARDS.size() == 0) {
            // only load boards on the first time
            LoadAllBoards();
        }
        return view;
    }

    public void showLoadingHints() {
        MainActivity activity = (MainActivity)getActivity();
        activity.showProgress("加载所有版面列表，请等待...", true);
    }

    public void clearLoadingHints () {
        // disable progress bar
        MainActivity activity = (MainActivity) getActivity();
        activity.showProgress("", false);
    }

    public void LoadAllBoards () {
        showLoadingHints();

        // http://stackoverflow.com/questions/26311513/convert-observable-to-list
//        List<Board> boards = SMTHHelper.LoadAllBoardsFromWWW()
//                .subscribeOn(Schedulers.io())
//                .toList().toBlocking().single();
//        Log.d(TAG, "All Boards" + boards.size());

        SMTHHelper.LoadAllBoardsFromWWW()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Board>() {
                    @Override
                    public void onStart() {
                        super.onStart();
                        ListBoardContent.clearAllBoards();
                    }

                    @Override
                    public void onCompleted() {
                        clearLoadingHints();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, e.toString());
                    }

                    @Override
                    public void onNext(Board board) {
                        Log.d(TAG, board.toString());
                        ListBoardContent.addAllBoardItem(board);
                        mRecylerView.getAdapter().notifyItemInserted(ListBoardContent.ALL_BOARDS.size() - 1);
                    }
                });

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

    public class QueryTextListner implements  SearchView.OnQueryTextListener {
        private BoardRecyclerViewAdapter mAdapter = null;

        public QueryTextListner(BoardRecyclerViewAdapter mAdapter) {
            this.mAdapter = mAdapter;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            Log.d(TAG, newText);
            mAdapter.getFilter().filter(newText);
            return true;
        }
    }


}
