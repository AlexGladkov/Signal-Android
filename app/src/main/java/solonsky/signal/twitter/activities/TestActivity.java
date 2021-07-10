package solonsky.signal.twitter.activities;

import android.os.Bundle;

import solonsky.signal.twitter.R;

/**
 * Created by neura on 04.09.17.
 */

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewDataBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_test);
    }
}
