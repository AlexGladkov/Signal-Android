package solonsky.signal.twitter.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

import solonsky.signal.twitter.models.DropDownItems;

/**
 * Created by neura on 30.05.17.
 */

public class DropDownAdapter extends BaseAdapter {
    private final MenuClickListener menuClickListener;
    private Context context;
    private ArrayList<DropDownItems> items;
    private int layout;

    public interface MenuClickListener {
        void onMenuClick(int id, String title, String subtitle, View v);
    }

    public DropDownAdapter(Context context, int layout, ArrayList<DropDownItems> items,
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
    public DropDownItems getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final DropDownItems item = getItem(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout, null);
        }

//        if (layout == R.layout.menu_font) {
//            final TextView textSize = (TextView) convertView.findViewById(R.id.font_txt_size);
//            final TextView textTitle = (TextView) convertView.findViewById(R.id.font_txt_title);
//
//            textSize.setText(item.getSize());
//            textTitle.setText(item.getTitle());
//        } else if (layout == R.layout.menu_simple) {
//            final TextView textTitle = (TextView) convertView.findViewById(R.id.menu_txt_title);
//            textTitle.setText(item.getTitle());
//        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuClickListener.onMenuClick(item.getId(), item.getTitle(), item.getSize(), v);
            }
        });

        return convertView;
    }
}
