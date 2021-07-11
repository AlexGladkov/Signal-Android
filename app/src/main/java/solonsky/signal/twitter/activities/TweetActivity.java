package solonsky.signal.twitter.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.databinding.ActivityTweetBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.viewmodels.TweetViewModel;

/**
 * Created by neura on 25.05.17.
 */

public class TweetActivity extends AppCompatActivity {

    private TweetViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.getInstance().isNightEnabled()) {
            setTheme(R.style.ActivityThemeDark);
        }

        ActivityTweetBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_tweet);

//        viewModel = new TweetViewModel();
//        binding.setModel(viewModel);
    }
}
