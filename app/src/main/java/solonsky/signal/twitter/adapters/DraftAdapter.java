package solonsky.signal.twitter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import solonsky.signal.twitter.databinding.CellDraftBinding;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.helpers.drafts.DraftModel;

/**
 * Created by neura on 26.05.17.
 */

public class DraftAdapter extends RecyclerView.Adapter<DraftAdapter.ViewHolder> {
    private final String TAG = "FEEDADAPTER";
    private final ArrayList<DraftModel> models;
    private final Context mContext;
    private final DraftClickListener mClickHandler;

    public interface DraftClickListener {
        void onDeleteClick(View view, DraftModel removeModel);
        void onItemClick(View view, DraftModel removeModel);
    }

    public DraftAdapter(ArrayList<DraftModel> models, Context mContext, DraftClickListener clickHandler) {
        this.models = models;
        this.mContext = mContext;
        this.mClickHandler = clickHandler;
    }

    @Override
    public DraftAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CellDraftBinding binding = CellDraftBinding.inflate(inflater, parent, false);
        return new DraftAdapter.ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(DraftAdapter.ViewHolder holder, int position) {
        final DraftModel model = models.get(position);
        holder.mBinding.setModel(model);
        holder.mBinding.setClick(new DraftModel.DraftClickHandler() {
            @Override
            public void onItemClick(View view) {
                mClickHandler.onItemClick(view, model);
            }

            @Override
            public void onMinusClick(View view) {
                mClickHandler.onDeleteClick(view, model);
            }
        });

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.mBinding.draftViewDivider.getLayoutParams();
        params.leftMargin = position == models.size() - 1 ? 0 : (int) Utilities.convertDpToPixel(16, mContext);
        holder.mBinding.draftViewDivider.setLayoutParams(params);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CellDraftBinding mBinding;

        public ViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }
    }
}
