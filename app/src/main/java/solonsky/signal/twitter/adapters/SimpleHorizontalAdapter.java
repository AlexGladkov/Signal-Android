package solonsky.signal.twitter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import solonsky.signal.twitter.databinding.CellSimpleHorizontalBinding;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.SimpleModel;

/**
 * Created by neura on 26.05.17.
 */

public class SimpleHorizontalAdapter extends RecyclerView.Adapter<SimpleHorizontalAdapter.ViewHolder> {
    private final String TAG = "FEEDADAPTER";
    private final ArrayList<SimpleModel> models;
    private final Context mContext;
    private final SimpleClickListener clickListener;

    public interface SimpleClickListener {
        void onItemClick(View v, SimpleModel simpleModel);
    }

    public SimpleHorizontalAdapter(ArrayList<SimpleModel> models, Context mContext, SimpleClickListener clickListener) {
        this.models = models;
        this.mContext = mContext;
        this.clickListener = clickListener;
    }

    @Override
    public SimpleHorizontalAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CellSimpleHorizontalBinding binding = CellSimpleHorizontalBinding.inflate(inflater, parent, false);
        return new SimpleHorizontalAdapter.ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(SimpleHorizontalAdapter.ViewHolder holder, int position) {
        final SimpleModel model = models.get(position);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.mBinding.tagTxtMain.getLayoutParams();
        params.rightMargin = position == models.size() - 1 ? (int) Utilities.convertDpToPixel(24, mContext) : 0;
        holder.mBinding.tagTxtMain.setLayoutParams(params);

        holder.mBinding.setModel(model);
        holder.mBinding.setClick(new SimpleModel.SimpleClickHandler() {
            @Override
            public void onItemClick(View v) {
                clickListener.onItemClick(v, model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CellSimpleHorizontalBinding mBinding;

        public ViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }
    }
}
