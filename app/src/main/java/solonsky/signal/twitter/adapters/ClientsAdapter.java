package solonsky.signal.twitter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import solonsky.signal.twitter.databinding.CellClientBinding;
import solonsky.signal.twitter.models.SimpleModel;

/**
 * Created by neura on 26.05.17.
 */

public class ClientsAdapter extends RecyclerView.Adapter<ClientsAdapter.ViewHolder> {
    private final String TAG = ClientsAdapter.class.getSimpleName();
    private final ArrayList<SimpleModel> models;
    private final Context mContext;
    private final ClientsClickListener clickListener;

    public interface ClientsClickListener {
        void onItemClick(SimpleModel model, View v);
    }

    public ClientsAdapter(ArrayList<SimpleModel> models, Context mContext, ClientsClickListener clickListener) {
        this.models = models;
        this.mContext = mContext;
        this.clickListener = clickListener;
    }

    @Override
    public ClientsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CellClientBinding binding = CellClientBinding.inflate(inflater, parent, false);
        return new ClientsAdapter.ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(ClientsAdapter.ViewHolder holder, int position) {
        final SimpleModel model = models.get(position);
        holder.mBinding.setModel(model);
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
        CellClientBinding mBinding;

        public ViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }
    }
}
