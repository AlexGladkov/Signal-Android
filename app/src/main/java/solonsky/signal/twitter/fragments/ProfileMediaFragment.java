package solonsky.signal.twitter.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.adapters.DetailStaggeredAdapter;
import solonsky.signal.twitter.adapters.ImageAdapter;
import solonsky.signal.twitter.adapters.MediaStaggeredAdapter;
import solonsky.signal.twitter.api.ProfileDataApi;
import solonsky.signal.twitter.databinding.FragmentProfileMediaBinding;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.ListConfig;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.interfaces.UpdateHandler;
import solonsky.signal.twitter.libs.DownloadFiles;
import solonsky.signal.twitter.models.ImageModel;
import solonsky.signal.twitter.overlays.ImageOverlay;
import solonsky.signal.twitter.viewmodels.ProfileMediaViewModel;

/**
 * Created by neura on 27.05.17.
 */

public class ProfileMediaFragment extends Fragment {

    private static final String TAG = ProfileMediaFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentProfileMediaBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_media, container, false);

        final MediaStaggeredAdapter imageAdapter = new MediaStaggeredAdapter(ProfileDataApi.getInstance().getImages(),
                (AppCompatActivity) getActivity(), new MediaStaggeredAdapter.ImageStaggeredListener() {
            @Override
            public void onClick(ImageModel imageModel, View v) {
                ArrayList<String> urls = new ArrayList<>();
                for (ImageModel imageModel1 : ProfileDataApi.getInstance().getImages()) {
                    if (imageModel1.getImageUrl() != null)
                        urls.add(imageModel1.getImageUrl());
                }

                final ImageOverlay imageOverlay = new ImageOverlay(urls, (AppCompatActivity) getActivity(),
                        ProfileDataApi.getInstance().getImages().indexOf(imageModel));
                imageOverlay.setImageOverlayClickHandler(new ImageOverlay.ImageOverlayClickHandler() {
                    @Override
                    public void onBackClick(View v) {
                        imageOverlay.getImageViewer().onDismiss();
                    }

                    @Override
                    public void onSaveClick(View v, String url) {
                        DownloadFiles downloadFiles = new DownloadFiles(getActivity());
                        downloadFiles.saveFile(url, getActivity().getString(R.string.download_url));
                    }
                });
            }
        });

        final ProfileMediaViewModel viewModel = new ProfileMediaViewModel(getContext());
        binding.setModel(viewModel);

        GridLayoutManager manager = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (position == 0 ? (ProfileDataApi.getInstance().getImages().size() % 2 == 0) ? 1 : 2 : 1);
            }
        });

        binding.recyclerProfileMedia.setHasFixedSize(true);
        binding.recyclerProfileMedia.setNestedScrollingEnabled(false);
        binding.recyclerProfileMedia.setLayoutManager(manager);
        binding.recyclerProfileMedia.setAdapter(imageAdapter);
        binding.recyclerProfileMedia.addItemDecoration(new ListConfig.SpacesItemDecoration((int)
                Utilities.convertDpToPixel(2, getContext())));

        ProfileDataApi.getInstance().setMediaHandler(new UpdateHandler() {
            @Override
            public void onUpdate() {
                viewModel.setState(ProfileDataApi.getInstance().getImages().size() == 0 ?
                        AppData.UI_STATE_NO_ITEMS : AppData.UI_STATE_VISIBLE);
            }
        });

        viewModel.setState(ProfileDataApi.getInstance().getImages().size() == 0 ?
                AppData.UI_STATE_NO_ITEMS : AppData.UI_STATE_VISIBLE);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
