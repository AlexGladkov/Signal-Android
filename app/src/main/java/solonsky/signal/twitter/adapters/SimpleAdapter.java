package solonsky.signal.twitter.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import solonsky.signal.twitter.databinding.CellSimpleBinding;
import solonsky.signal.twitter.models.SimpleModel;

/**
 * Created by neura on 26.05.17.
 */

public class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.ViewHolder> {
    private final String TAG = "FEEDADAPTER";
    private final ArrayList<SimpleModel> models;
    private final Context mContext;
    private final SimpleClickListener clickListener;

    public interface SimpleClickListener {
        void onItemClick(SimpleModel model, View v);
    }

    public SimpleAdapter(ArrayList<SimpleModel> models, Context mContext, SimpleClickListener clickListener) {
        this.models = models;
        this.mContext = mContext;
        this.clickListener = clickListener;
    }

    @Override
    public SimpleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CellSimpleBinding binding = CellSimpleBinding.inflate(inflater, parent, false);
        return new SimpleAdapter.ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(SimpleAdapter.ViewHolder holder, int position) {
        final SimpleModel model = models.get(position);

        holder.mBinding.setModel(model);
        holder.mBinding.simpleViewDivider.setVisibility(position == models.size() - 1 ? View.GONE : View.VISIBLE);
        holder.mBinding.setClick(new SimpleModel.SimpleClickHandler() {
            @Override
            public void onItemClick(View v) {
                clickListener.onItemClick(model, v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CellSimpleBinding mBinding;

        public ViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }
    }
}
