package com.zfdang.zsmth_android;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.zfdang.zsmth_android.listeners.EndlessRecyclerOnScrollListener;
import com.zfdang.zsmth_android.models.Mail;
import com.zfdang.zsmth_android.models.MailListContent;
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
public class MailListFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = "MailListFragment";
    private static final String INBOX_LABEL = "inbox";
    private static final String OUTBOX_LABEL = "outbox";
    private static final String DELETED_LABEL = "deleted";

    private OnListFragmentInteractionListener mListener;
    private RecyclerView recyclerView;
    private EndlessRecyclerOnScrollListener mScrollListener = null;

    private Button btInbox;
    private Button btOutbox;
    private Button btTrashbox;


    private String currentFolder;
    private int currentPage;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MailListFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mail_list, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_mail_contents);
        Context context = view.getContext();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL, 0));
        recyclerView.setAdapter(new MailRecyclerViewAdapter(MailListContent.MAILS, mListener));

        // enable endless loading
        mScrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                // do something...
                LoadMoreMails();
            }
        };
        recyclerView.addOnScrollListener(mScrollListener);

        btInbox = (Button) view.findViewById(R.id.mail_button_inbox);
        btInbox.setOnClickListener(this);
        btOutbox = (Button) view.findViewById(R.id.mail_button_outbox);
        btOutbox.setOnClickListener(this);
        btTrashbox = (Button) view.findViewById(R.id.mail_button_trashbox);
        btTrashbox.setOnClickListener(this);

        currentFolder = INBOX_LABEL;
        LoadMailsFromBeginning();

        return view;
    }


    public void LoadMoreMails() {
        currentPage += 1;
        LoadMails();
    }

    public void LoadMailsFromBeginning() {
        currentPage = 1;
        MailListContent.clear();
        recyclerView.getAdapter().notifyDataSetChanged();

        showLoadingHints();
        LoadMails();
    }

    public void LoadMails() {
        SMTHHelper helper = SMTHHelper.getInstance();

        helper.wService.getUserMails(currentFolder, Integer.toString(currentPage))
                .flatMap(new Func1<ResponseBody, Observable<Mail>>() {
                    @Override
                    public Observable<Mail> call(ResponseBody responseBody) {
                        try {
                            String response = responseBody.string();
                            List<Mail> results = SMTHHelper.ParseMailsFromWWW(response);
                            return Observable.from(results);
                        } catch (Exception e) {
                            Log.d(TAG, Log.getStackTraceString(e));
                        }
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Mail>() {
                    @Override
                    public void onCompleted() {
                        clearLoadingHints();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: " + Log.getStackTraceString(e) );
                        Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(Mail mail) {
                        Log.d(TAG, "onNext: " + mail.toString());

                        MailListContent.addItem(mail);
                        recyclerView.getAdapter().notifyItemChanged(MailListContent.MAILS.size() -1 );
                    }
                });

    }


    public void showLoadingHints() {
        MainActivity activity = (MainActivity)getActivity();
        activity.showProgress("加载信件中...", true);
    }

    public void clearLoadingHints () {
        // disable progress bar
        MainActivity activity = (MainActivity) getActivity();
        if(activity != null) {
            activity.showProgress("", false);
        }

        // disable SwipeFreshLayout
//        mSwipeRefreshLayout.setRefreshing(false);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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

    @Override
    public void onClick(View v) {
        if(v == btInbox) {
            btInbox.setTextColor(getResources().getColor(R.color.blue_text_night));
            btOutbox.setTextColor(getResources().getColor(R.color.status_text_night));
            btTrashbox.setTextColor(getResources().getColor(R.color.status_text_night));
            currentFolder = INBOX_LABEL;
        } else if(v == btOutbox) {
            btInbox.setTextColor(getResources().getColor(R.color.status_text_night));
            btOutbox.setTextColor(getResources().getColor(R.color.blue_text_night));
            btTrashbox.setTextColor(getResources().getColor(R.color.status_text_night));
            currentFolder = OUTBOX_LABEL;
        } else if (v == btTrashbox) {
            btInbox.setTextColor(getResources().getColor(R.color.status_text_night));
            btOutbox.setTextColor(getResources().getColor(R.color.status_text_night));
            btTrashbox.setTextColor(getResources().getColor(R.color.blue_text_night));
            currentFolder = DELETED_LABEL;
        }

        LoadMailsFromBeginning();
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
        void onListFragmentInteraction(Mail item);
    }
}
