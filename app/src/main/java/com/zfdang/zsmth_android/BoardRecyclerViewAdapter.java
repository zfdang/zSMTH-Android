package com.zfdang.zsmth_android;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import com.zfdang.zsmth_android.listeners.OnBoardFragmentInteractionListener;
import com.zfdang.zsmth_android.models.Board;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Board} and makes a call to the
 * specified {@link OnBoardFragmentInteractionListener}.
 */
public class BoardRecyclerViewAdapter extends RecyclerView.Adapter<BoardRecyclerViewAdapter.ViewHolder> implements Filterable {

  private final List<Board> mBoards;
  private final OnBoardFragmentInteractionListener mListener;
  private BoardFilter mFilter = null;

  public BoardRecyclerViewAdapter(List<Board> items, OnBoardFragmentInteractionListener listener) {
    mBoards = items;
    mListener = listener;
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.board_item, parent, false);
    return new ViewHolder(view);
  }

  @Override public void onBindViewHolder(final ViewHolder holder, int position) {
    holder.mItem = mBoards.get(position);
    Board board = holder.mItem;
    if (board.isFolder()) {
      holder.mCategoryView.setText("[" + board.getCategoryName() + "]");
      holder.mModeratorView.setVisibility(View.GONE);
      holder.mNameView.setText(board.getFolderName());
    } else {
      holder.mCategoryView.setText("[" + board.getCategoryName() + "]");
      holder.mModeratorView.setVisibility(View.VISIBLE);
      holder.mModeratorView.setText(board.getModerator());
      holder.mNameView.setText("[" + board.getBoardEngName() + "]" + board.getBoardChsName());
    }

    holder.mView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (null != mListener) {
          // Notify the active callbacks interface (the activity, if the
          // fragment is attached to one) that an item has been selected.
          mListener.onBoardFragmentInteraction(holder.mItem);
        }
      }
    });

    holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override public boolean onLongClick(View v) {
        if (null != mListener) {
          // Notify the active callbacks interface (the activity, if the
          // fragment is attached to one) that an item has been selected.
          mListener.onBoardLongClick(holder.mItem);
        }
        return false;
      }
    });
  }

  @Override public int getItemCount() {
    return mBoards.size();
  }

  @Override public Filter getFilter() {
    if (mFilter == null) {
      mFilter = new BoardFilter(this, mBoards);
    }
    return mFilter;
  }

  // https://stackoverflow.com/questions/29792187/add-a-search-filter-on-recyclerview-with-cards/29792313#29792313
  private static class BoardFilter extends Filter {
    private final BoardRecyclerViewAdapter adapter;
    private final List<Board> originalList;
    private final List<Board> filteredList;

    private BoardFilter(BoardRecyclerViewAdapter adapter, List<Board> originalList) {
      super();
      this.adapter = adapter;
      this.originalList = new LinkedList<>(originalList);
      this.filteredList = new ArrayList<>();
    }

    @Override protected FilterResults performFiltering(CharSequence constraint) {
      filteredList.clear();
      final FilterResults results = new FilterResults();

      if (constraint.length() == 0) {
        filteredList.addAll(originalList);
      } else {
        final String filterPattern = constraint.toString().trim();

        for (final Board board : originalList) {
          // this matching method can be further improved
          String boardLabel = String.format("%s-%s-%s", board.getBoardChsName(), board.getBoardEngName(), board.getCategoryName());
          if (Pattern.compile(Pattern.quote(filterPattern), Pattern.CASE_INSENSITIVE).matcher(boardLabel).find()) {
            filteredList.add(board);
          }
        }
      }
      results.values = filteredList;
      results.count = filteredList.size();
      return results;
    }

    @Override protected void publishResults(CharSequence constraint, FilterResults results) {
      adapter.mBoards.clear();
      adapter.mBoards.addAll((ArrayList<Board>) results.values);
      adapter.notifyDataSetChanged();
    }
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    public Board mItem;
    public final View mView;
    public final TextView mCategoryView;
    public final TextView mModeratorView;
    public final TextView mNameView;

    public ViewHolder(View view) {
      super(view);
      mView = view;
      mCategoryView = (TextView) view.findViewById(R.id.CategoryName);
      mModeratorView = (TextView) view.findViewById(R.id.ModeratorID);
      mNameView = (TextView) view.findViewById(R.id.BoardName);
    }

    @Override public String toString() {
      return mNameView.getText().toString();
    }
  }
}
