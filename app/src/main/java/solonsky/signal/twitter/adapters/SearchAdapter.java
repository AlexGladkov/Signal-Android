package solonsky.signal.twitter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.databinding.CellSearchBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.models.SearchModel;

/**
 * Created by neura on 26.05.17.
 */

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private final String TAG = "FEEDADAPTER";
    private final ArrayList<SearchModel> models;
    private final Context mContext;
    private final SearchClickListener clickListener;

    public interface SearchClickListener {
        void onClick(SearchModel model, View v);
        void onDelete(SearchModel model, View v);
    }

    public SearchAdapter(ArrayList<SearchModel> models, Context mContext, SearchClickListener clickListener) {
        this.models = models;
        this.mContext = mContext;
        this.clickListener = clickListener;
    }

    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CellSearchBinding binding = CellSearchBinding.inflate(inflater, parent, false);
        return new SearchAdapter.ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(SearchAdapter.ViewHolder holder, int position) {
        final SearchModel model = models.get(position);
        final boolean isNight = App.getInstance().isNightEnabled();

        holder.mBinding.setModel(model);

        holder.mBinding.searchViewDivider.setBackgroundColor(mContext.getResources().getColor(model.isHighlighted() ? isNight ?
                R.color.dark_divider_highlight_color : R.color.light_divider_highlight_color :
                isNight ? R.color.dark_divider_color : R.color.light_divider_color));

        holder.mBinding.searchViewDivider.setVisibility(position == models.size() - 1 ? View.GONE : View.VISIBLE);
        holder.mBinding.setClick(new SearchModel.SearchClickHandler() {
            @Override public void onItemClick(View v) {
                clickListener.onClick(model, v);
            }
            @Override public void onCloseClick(View v) {
                clickListener.onDelete(model, v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    @Override
    public long getItemId(int position) {
        return models.get(position).hashCode();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CellSearchBinding mBinding;

        public ViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }
    }
}
