package solonsky.signal.twitter.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import solonsky.signal.twitter.databinding.CellRemoveBinding;
import solonsky.signal.twitter.models.RemoveModel;

/**
 * Created by neura on 26.05.17.
 */

public class RemoveAdapter extends RecyclerView.Adapter<RemoveAdapter.ViewHolder> {
    private final String TAG = "FEEDADAPTER";
    private final ArrayList<RemoveModel> models;
    private final Context mContext;
    private final RemoveClickListener mClickHandler;

    public interface RemoveClickListener {
        void onDeleteClick(View view, RemoveModel removeModel);
    }

    public RemoveAdapter(ArrayList<RemoveModel> models, Context mContext, RemoveClickListener clickHandler) {
        this.models = models;
        this.mContext = mContext;
        this.mClickHandler = clickHandler;
    }

    @Override
    public RemoveAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CellRemoveBinding binding = CellRemoveBinding.inflate(inflater, parent, false);
        return new RemoveAdapter.ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(RemoveAdapter.ViewHolder holder, int position) {
        final RemoveModel model = models.get(position);
        holder.mBinding.setModel(model);
        holder.mBinding.setClick(new RemoveModel.RemoveClickHandler() {
            @Override
            public void onItemClick(View view) {

            }

            @Override
            public void onMinusClick(View view) {
                mClickHandler.onDeleteClick(view, model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CellRemoveBinding mBinding;

        public ViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }
    }
}
