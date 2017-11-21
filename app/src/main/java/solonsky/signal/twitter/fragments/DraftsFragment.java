package solonsky.signal.twitter.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anupcowkur.reservoir.ReservoirGetCallback;

import java.util.ArrayList;
import java.util.List;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.ComposeActivity;
import solonsky.signal.twitter.adapters.DraftAdapter;
import solonsky.signal.twitter.databinding.FragmentDraftsBinding;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.drafts.DraftModel;
import solonsky.signal.twitter.helpers.drafts.Drafts;
import solonsky.signal.twitter.viewmodels.DraftsViewModel;

/**
 * Created by neura on 22.05.17.
 */

public class DraftsFragment extends Fragment {
    private final String TAG = DraftsFragment.class.getSimpleName();
    private DraftsViewModel viewModel;
    private DraftAdapter draftAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentDraftsBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_drafts, container, false);
        viewModel = new DraftsViewModel(AppData.UI_STATE_NO_ITEMS);
        final ComposeActivity mActivity = (ComposeActivity) getActivity();

        final Drafts drafts = new Drafts(getContext(), AppData.ME.getId());
        final ArrayList<DraftModel> draftModels = new ArrayList<>();
        draftAdapter = new DraftAdapter(draftModels, getContext(), new DraftAdapter.DraftClickListener() {
            @Override
            public void onDeleteClick(View view, DraftModel removeModel) {
                int position = draftModels.indexOf(removeModel);
                draftModels.remove(removeModel);
                draftAdapter.notifyItemRemoved(position);
                drafts.deleteDraft(removeModel.getMessage());
                mActivity.binding.btnComposeDrafts.setImageDrawable(getResources().getDrawable(
                        draftModels.size() > 0 ? R.drawable.ic_icons_compose_drafts :
                                R.drawable.ic_icons_compose_drafts_empty));
            }

            @Override
            public void onItemClick(View view, DraftModel removeModel) {
                ((ComposeActivity) getActivity()).setupDraft(removeModel.getMessage());
            }
        });

        binding.recyclerDrafts.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.recyclerDrafts.setAdapter(draftAdapter);
        binding.recyclerDrafts.setHasFixedSize(true);

        drafts.loadAll(new ReservoirGetCallback<List<DraftModel>>() {
            @Override public void onFailure(Exception e) {}
            @Override public void onSuccess(List<DraftModel> drafts) {
                for (DraftModel draft : drafts) {
                    draftModels.add(draft);
                }

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        draftAdapter.notifyDataSetChanged();
                        viewModel.setState(draftModels.size() == 0 ? AppData.UI_STATE_NO_ITEMS : AppData.UI_STATE_VISIBLE);
                    }
                });
            }
        });

        binding.setDrafts(viewModel);
        return binding.getRoot();
    }
}
