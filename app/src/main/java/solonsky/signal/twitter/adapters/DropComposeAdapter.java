package solonsky.signal.twitter.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.DropDownComposeItems;

/**
 * Created by neura on 30.05.17.
 */

public class DropComposeAdapter extends BaseAdapter {
    private final MenuClickListener menuClickListener;
    private Context context;
    private ArrayList<DropDownComposeItems> items;
    private int layout;

    public interface MenuClickListener {
        void onMenuClick(long id, String imageUrl, View v, String clientSecret, String clientToken);
    }

    public DropComposeAdapter(Context context, int layout, ArrayList<DropDownComposeItems> items,
                              MenuClickListener menuClickListener) {
        this.context = context;
        this.items = items;
        this.menuClickListener = menuClickListener;
        this.layout = layout;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public DropDownComposeItems getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final DropDownComposeItems item = getItem(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout, null);
        }

        convertView.setPadding(0, position == 0 ? (int) Utilities.convertDpToPixel(8, context) : 0,
                0, position == items.size() - 1 ? (int) Utilities.convertDpToPixel(8, context) : 0);
        final ImageView imgIcon = (ImageView) convertView.findViewById(R.id.menu_compose_icon);
        final TextView txtTitle = (TextView) convertView.findViewById(R.id.menu_compose_text);

        Picasso.get().load(item.getImageUrl()).into(imgIcon);
        txtTitle.setText(item.getText());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuClickListener.onMenuClick(item.getId(), item.getImageUrl(), v, item.getClientSecret(),
                        item.getClientToken());
            }
        });

        return convertView;
    }
}
