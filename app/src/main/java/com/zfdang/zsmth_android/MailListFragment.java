package com.zfdang.zsmth_android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.listeners.EndlessRecyclerOnScrollListener;
import com.zfdang.zsmth_android.listeners.OnMailInteractionListener;
import com.zfdang.zsmth_android.models.ComposePostContext;
import com.zfdang.zsmth_android.models.Mail;
import com.zfdang.zsmth_android.models.MailListContent;
import com.zfdang.zsmth_android.newsmth.AjaxResponse;
import com.zfdang.zsmth_android.newsmth.SMTHHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnMailInteractionListener}
 * interface.
 */
public class MailListFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = "MailListFragment";
    private static final String INBOX_LABEL = "inbox";
    private static final String OUTBOX_LABEL = "outbox";
    private static final String DELETED_LABEL = "deleted";

    private OnMailInteractionListener mListener;
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

        // http://stackoverflow.com/questions/8308695/android-options-menu-in-fragment
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mail_list, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_mail_contents);
        Context context = view.getContext();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
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

        // enable swipe to delete mail
        initItemHelper();

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

    public void initItemHelper() {
        //0则不执行拖动或者滑动
        ItemTouchHelper.Callback mCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Mail mail = MailListContent.MAILS.get(position);
                if(!mail.isCategory) {
                    Map<String, String> mails = new HashMap<String, String>();
                    String mailKey = String.format("m_%s", mail.getMailIDFromURL());
                    Log.d(TAG, "onSwiped: " + mailKey);
                    mails.put(mailKey, "on");

                    SMTHHelper helper = SMTHHelper.getInstance();
                    helper.wService.deleteMail(currentFolder, mails)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<AjaxResponse>() {
                                @Override
                                public void onCompleted() {

                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.e(TAG, "onError: " + Log.getStackTraceString(e) );
                                    Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onNext(AjaxResponse ajaxResponse) {
                                    Log.d(TAG, "onNext: " + ajaxResponse.toString());

                                    if(ajaxResponse.getAjax_st() == AjaxResponse.AJAX_RESULT_OK) {
                                        MailListContent.MAILS.remove(viewHolder.getAdapterPosition());
                                        recyclerView.getAdapter().notifyItemRemoved(viewHolder.getAdapterPosition());
                                    }
                                    Toast.makeText(getActivity(), ajaxResponse.getAjax_msg(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(mCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public void LoadMoreMails() {
        // LoadMore will be re-enabled in clearLoadingHints.
        // if we return here, loadMore will not be triggered again

        ProgressDialog pdialog = ((MainActivity)getActivity()).pdialog;
        if(pdialog != null && pdialog.isShowing())
        {
            // loading in progress, do nothing
            return;
        }

        if(currentPage >= MailListContent.totalPages){
            // reach the last page, do nothing
            Mail mail = new Mail(".END.");
            MailListContent.addItem(mail);

            recyclerView.getAdapter().notifyItemChanged(MailListContent.MAILS.size() - 1);
            return;
        }

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
        Log.d(TAG, "LoadMails: " + currentPage);
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
                        recyclerView.smoothScrollToPosition(0);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: " + Log.getStackTraceString(e) );
                        Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(Mail mail) {
                        // Log.d(TAG, "onNext: " + mail.toString());

                        MailListContent.addItem(mail);
                        recyclerView.getAdapter().notifyItemChanged(MailListContent.MAILS.size() - 1);
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

        // re-enable endless load
        if(mScrollListener != null) {
            mScrollListener.setLoading(false);
        }
    }

    public void markMailAsReaded(int position) {
        if(position < MailListContent.MAILS.size()) {
            Mail mail = MailListContent.MAILS.get(position);
            mail.isNew = false;
            recyclerView.getAdapter().notifyItemChanged(position);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMailInteractionListener) {
            mListener = (OnMailInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMailInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.findItem(R.id.mail_list_fragment_newmail);
        item.setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.mail_list_fragment_newmail) {
            // write new mail
            ComposePostContext postContext = new ComposePostContext();
            postContext.setThroughMail(true);

            Intent intent = new Intent(getActivity(), ComposePostActivity.class);
            intent.putExtra(SMTHApplication.COMPOSE_POST_CONTEXT, postContext);
            startActivity(intent);
            return true;
        } else if (id == R.id.main_action_refresh) {
            LoadMailsFromBeginning();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(v == btInbox) {
            if(TextUtils.equals(currentFolder, INBOX_LABEL)) return;
            btInbox.setTextColor(getResources().getColor(R.color.blue_text_night));
            btOutbox.setTextColor(getResources().getColor(R.color.status_text_night));
            btTrashbox.setTextColor(getResources().getColor(R.color.status_text_night));
            currentFolder = INBOX_LABEL;
        } else if(v == btOutbox) {
            if(TextUtils.equals(currentFolder, OUTBOX_LABEL)) return;
            btInbox.setTextColor(getResources().getColor(R.color.status_text_night));
            btOutbox.setTextColor(getResources().getColor(R.color.blue_text_night));
            btTrashbox.setTextColor(getResources().getColor(R.color.status_text_night));
            currentFolder = OUTBOX_LABEL;
        } else if (v == btTrashbox) {
            if(TextUtils.equals(currentFolder, DELETED_LABEL)) return;
            btInbox.setTextColor(getResources().getColor(R.color.status_text_night));
            btOutbox.setTextColor(getResources().getColor(R.color.status_text_night));
            btTrashbox.setTextColor(getResources().getColor(R.color.blue_text_night));
            currentFolder = DELETED_LABEL;
        }

        LoadMailsFromBeginning();
    }
}
