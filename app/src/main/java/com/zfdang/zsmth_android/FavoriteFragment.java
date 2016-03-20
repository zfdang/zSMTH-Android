package com.zfdang.zsmth_android;

import android.app.Activity;
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
import com.zfdang.zsmth_android.models.FavoriteBoardContent;
import com.zfdang.zsmth_android.newsmth.SMTHHelper;

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
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class FavoriteFragment extends Fragment {

    private final String TAG = "FavoriteFragment";
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    // the current path of favorite
    private String mFavoritePath = "";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FavoriteFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static FavoriteFragment newInstance(int columnCount) {
        FavoriteFragment fragment = new FavoriteFragment();
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
        View view = inflater.inflate(R.layout.fragment_favorite_board, container, false);


        // Set the adapter
        if (view instanceof RecyclerView) {
//            http://stackoverflow.com/questions/28713231/recyclerview-item-separator
            ((RecyclerView) view).addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new FavoriteRecyclerViewAdapter(FavoriteBoardContent.ITEMS, mListener));
        }

//        LoadFavorites(mFavoritePath);
        LoadFavorites("1");
        return view;
    }


    public void LoadFavorites(final String path) {
        SMTHHelper helper = SMTHHelper.getInstance();
        helper.mService.getFavoriteBoards(mFavoritePath)
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .flatMap(new Func1<ResponseBody, Observable<Board>>() {
                    @Override
                    public Observable<Board> call(ResponseBody resp) {
                        try{
                            String response = resp.string();
                            Log.d(TAG, response);
                            List<Board> boards = SMTHHelper.ParseFavoriteBoardsFromMobile(response);
                            return Observable.from(boards);
                        } catch (Exception e) {
                            Log.d(TAG, "Failed to load favorite {" + path + "}");
                            Log.d(TAG, e.toString());
                        }
                        return null;
                    }
                })
                .subscribe(new Subscriber<Board>() {
                    @Override
                    public void onStart() {
                        super.onStart();
                    }

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Board board) {
                        Log.d(TAG, board.toString());
                    }
                });
        return;
    }

    @Override
    public void onAttach(Activity activity) {
        this.onAttach((Context)activity);
    }

    //    @Override
    public void onAttach(Context context) {
        super.onAttach((Activity) context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Board item);
    }
}
