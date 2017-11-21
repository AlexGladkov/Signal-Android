package solonsky.signal.twitter.libs.bottomBar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.helpers.App;

/**
 * Created by neura on 21.05.17.
 */

public class AGBottomBar extends LinearLayout {
    private final String TAG = "AGBOTTOMBAR";
    private final int VIEW_SIZE = 4; // size in dp
    private final int ICON_SIZE = 24; // size in dp
    private int CURRENT_TAB_ID = 0;
    private int CURRENT_TAB_VIEW_ID = 0;
    private int LONG_CLICK_POSITION = 0;
    private AGHandler agHandler;
    private ArrayList<AGBottomBarItem> barItems;
    private ArrayList<AGBottomBarItem> mItems;
    private ArrayList<ImageView> mImages;
    private ArrayList<View> mCaptions;
    private AppCompatActivity activity;
    private PopupWindow popup;
    private ArrayList<AGBottomBarMultipleItem> popupItems;
    private String path;
    private String filename;

    private AGPopup agPopup;

    private ImageView agItemMultiple1, agItemMultiple2;
    private boolean flag = true;
    private boolean checkLongClickItem = false;

    private boolean isNightEnabled = App.getInstance().isNightEnabled();

    public interface AGHandler {
        void itemClick(View view, int id);
    }

    public interface AGPopup {
        void changeItem(int position, int id);
    }

    public AGBottomBar(Context context) {
        super(context);
        initControl(context, null);
    }

    public AGBottomBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initControl(context, attrs);
    }

    public AGBottomBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initControl(context, attrs);
    }

    private void initControl(Context context, @Nullable AttributeSet attrs) {
        mItems = new ArrayList<>();
        mImages = new ArrayList<>();
        mCaptions = new ArrayList<>();
        barItems = new ArrayList<>();
    }

    /**
     * Init tab main layout
     *
     * @return created layout
     */
    private LinearLayout initTabMainView() {
        LayoutParams agItemParams = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        LinearLayout agItemView = new LinearLayout(getContext());
        agItemView.setBackground(getResources().getDrawable(isNightEnabled ?
                R.drawable.dark_bar_pressed : R.drawable.light_bar_pressed));
        agItemView.setOrientation(LinearLayout.VERTICAL);
        agItemView.setWeightSum(1);
        agItemView.setLayoutParams(agItemParams);
        return agItemView;
    }

    /**
     * Init multiple tab icon
     *
     * @return tab icon
     */
    private LinearLayout initMultipleImageView(AGBottomBarMultipleItem agBottomBarMultipleItem) {
        LayoutParams agItemParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout agItemIcon = new LinearLayout(getContext());
        agItemParams.topMargin = (int) convertDpToPixel(12, getContext());
        agItemParams.gravity = Gravity.CENTER;
        agItemIcon.setOrientation(LinearLayout.HORIZONTAL);
        agItemIcon.setLayoutParams(agItemParams);

        LayoutParams agImageParams = new LayoutParams(
                (int) convertDpToPixel(ICON_SIZE, getContext()),
                (int) convertDpToPixel(ICON_SIZE, getContext()));

        LayoutParams agMoreParams = new LayoutParams(
                (int) convertDpToPixel(3, getContext()),
                (int) convertDpToPixel(13, getContext()));

        agImageParams.setMarginStart((int) convertDpToPixel(8, getContext()));
        agMoreParams.gravity = Gravity.CENTER_VERTICAL;
        agMoreParams.setMarginStart((int) convertDpToPixel(5, getContext()));

        ImageView agItemImage = new ImageView(getContext());
        ImageView agItemMultiple = new ImageView(getContext());

        agItemImage.setImageDrawable(getResources().getDrawable(agBottomBarMultipleItem.getIconUrl()));
        agItemMultiple.setImageDrawable(getResources().getDrawable(agBottomBarMultipleItem.getMultipleUrl()));
        agItemMultiple.setScaleType(ImageView.ScaleType.FIT_XY);

        agItemImage.setColorFilter(CURRENT_TAB_ID == agBottomBarMultipleItem.getId() ?
                isNightEnabled ?
                        getResources().getColor(R.color.dark_bar_active_color) :
                        getResources().getColor(R.color.light_bar_active_color) :
                isNightEnabled ?
                        getResources().getColor(R.color.dark_bar_inactive_color) :
                        getResources().getColor(R.color.light_bar_inactive_color));

        agItemMultiple.setColorFilter(isNightEnabled ?
                        getResources().getColor(R.color.dark_bar_inactive_color) :
                        getResources().getColor(R.color.light_bar_inactive_color));

        agItemImage.setLayoutParams(agImageParams);
        agItemImage.setTag(agBottomBarMultipleItem.getId());
        agItemMultiple.setLayoutParams(agMoreParams);

        if (flag) {
            agItemMultiple1 = agItemMultiple;
            flag = false;
        } else {
            agItemMultiple2 = agItemMultiple;
            flag = true;
        }

        agItemIcon.addView(agItemImage);
        agItemIcon.addView(agItemMultiple);

        return agItemIcon;
    }

    /**
     * Init imageView for single tab
     *
     * @return imageView
     */
    private ImageView initSingleTabImage(AGBottomBarSingleItem agBottomBarSingleItem) {
        LayoutParams agIconParams = new LayoutParams((int) convertDpToPixel(ICON_SIZE, getContext()),
                (int) convertDpToPixel(ICON_SIZE, getContext()));
        agIconParams.gravity = Gravity.CENTER;
        agIconParams.topMargin = (int) convertDpToPixel(12, getContext());
        ImageView agItemIcon = new ImageView(getContext());

        agItemIcon.setTag(agBottomBarSingleItem.getId());
        agItemIcon.setLayoutParams(agIconParams);
        agItemIcon.setImageResource(agBottomBarSingleItem.getIconUrl());
        agItemIcon.setColorFilter(CURRENT_TAB_ID == agBottomBarSingleItem.getId() ?
                isNightEnabled ?
                        getResources().getColor(R.color.dark_bar_active_color) :
                        getResources().getColor(R.color.light_bar_active_color) :
                isNightEnabled ?
                        getResources().getColor(R.color.dark_bar_inactive_color) :
                        getResources().getColor(R.color.light_bar_inactive_color));
        return agItemIcon;
    }

    /**
     * Init tab indicator view
     *
     * @return tab indicator
     */
    private View initCaption(boolean isNew) {
        View agItemCaption = new View(getContext());

        LayoutParams agCaptionParams = new LayoutParams((int) convertDpToPixel(VIEW_SIZE, getContext()),
                (int) convertDpToPixel(VIEW_SIZE, getContext()));
        agCaptionParams.topMargin = (int) convertDpToPixel(4, getContext());
        agCaptionParams.gravity = Gravity.CENTER_HORIZONTAL;

        agItemCaption.setLayoutParams(agCaptionParams);
        agItemCaption.setBackgroundResource(isNightEnabled ? R.drawable.dark_shape_4dp : R.drawable.light_shape_4dp);
        agItemCaption.setVisibility(isNew ? View.VISIBLE : View.GONE);

        return agItemCaption;
    }

    /**
     * Build bottom bar and display items
     */
    public void build() {
        int i = 0;
        this.removeAllViews();
        barItems = new ArrayList<>();

        //initBarItems();

        for (AGBottomBarItem item : mItems) {
            if (item.isStart()) barItems.add(item);
        }

        for (AGBottomBarItem item : barItems) {
            item.setPosition(i);
            if (item instanceof AGBottomBarSingleItem) {
                initSingleItem((AGBottomBarSingleItem) item);
            } else {
                initMultipleItem((AGBottomBarMultipleItem) item);
            }

            i++;
        }

        this.requestLayout();
    }

    /**
     * Draw single item
     *
     * @param agBottomBarSingleItem - params container
     */
    private void initSingleItem(AGBottomBarSingleItem agBottomBarSingleItem) {
        LinearLayout agItemView = initTabMainView();
        ImageView agItemIcon = initSingleTabImage(agBottomBarSingleItem);
        View agItemCaption = initCaption(agBottomBarSingleItem.isHasNew());

        agItemView.setTag(agBottomBarSingleItem.getPosition());
        agItemView.setOnClickListener(itemClickListener);
        agItemView.addView(agItemIcon);
        agItemView.addView(agItemCaption);

        mImages.add(agItemIcon);
        mCaptions.add(agItemCaption);

        this.addView(agItemView);
        this.invalidate();
    }

    /**
     * Draw multiple item
     *
     * @param agBottomBarMultipleItem - params container
     */
    private void initMultipleItem(AGBottomBarMultipleItem agBottomBarMultipleItem) {
        LinearLayout agItemView = initTabMainView();
        LinearLayout agItemIcon = initMultipleImageView(agBottomBarMultipleItem);
        View agItemCaption = initCaption(agBottomBarMultipleItem.isHasNew());

        agItemView.setTag(agBottomBarMultipleItem.getPosition());
        agItemView.setOnClickListener(itemClickListener);
        agItemView.setOnLongClickListener(longClickListener);
        agItemView.addView(agItemIcon);
        agItemView.addView(agItemCaption);

        mImages.add((ImageView) agItemIcon.getChildAt(0));
        mCaptions.add(agItemCaption);

        this.addView(agItemView);
        this.invalidate();
    }

    public void addItem(AGBottomBarMultipleItem agBottomBarMultipleItem) {
        mItems.add(agBottomBarMultipleItem);
    }

    public void addItem(AGBottomBarSingleItem agBottomBarSingleItem) {
        mItems.add(agBottomBarSingleItem);
    }

    public void setCaptionVisibility(int visibility, int position) {
        mCaptions.get(position).setVisibility(visibility);
    }

    private void paintTab(int id, int color) {
        int i = 0;
        int defaultColor = getResources().getColor(isNightEnabled ? R.color.dark_bar_inactive_color : R.color.light_bar_inactive_color);
        for (ImageView imageView : mImages) {
            int a = (int) imageView.getTag();
            int b = id;
            //if (id > 4) Toast.makeText(activity.getBaseContext(), "Error!", Toast.LENGTH_SHORT).show();
            //Toast.makeText(activity.getBaseContext(), "id: " + String.valueOf(id) + "\nb: " + String.valueOf(b), Toast.LENGTH_SHORT).show();
            if (a == b) {
                imageView.setColorFilter(getResources().getColor(color));
                switch (i) {
                    case 3:
//                        agItemMultiple2.setColorFilter(defaultColor);
//                        agItemMultiple1.setColorFilter(getResources().getColor(color));
                        break;

                    case 4:
//                        agItemMultiple1.setColorFilter(defaultColor);
//                        agItemMultiple2.setColorFilter(getResources().getColor(color));
                        break;

                    default:
                        agItemMultiple1.setColorFilter(defaultColor);
                        agItemMultiple2.setColorFilter(defaultColor);
                        break;
                }
                break;
            }
            i++;
        }
    }

    private void displayPopupWindow(View anchorView, int id) {
        if (activity == null) return;
        if (popup != null) popup.dismiss();
        popup = new PopupWindow(activity);
        View layout = activity.getLayoutInflater().inflate(R.layout.popup_tab_bar, null);
        popup.setContentView(layout);

        ImageView item_1 = (ImageView) layout.findViewById(R.id.popup_menu_item_1);
        ImageView item_2 = (ImageView) layout.findViewById(R.id.popup_menu_item_2);
        ImageView item_3 = (ImageView) layout.findViewById(R.id.popup_menu_item_3);

        popupItems = new ArrayList<>();

        for (AGBottomBarItem agItem : mItems) {
            if (!agItem.isStart()) popupItems.add((AGBottomBarMultipleItem) agItem);
        }

        /*item_1.setImageDrawable(getResources().getDrawable(popupItems.get(0).getIconUrl()));
        item_2.setImageDrawable(getResources().getDrawable(popupItems.get(1).getIconUrl()));
        item_3.setImageDrawable(getResources().getDrawable(popupItems.get(2).getIconUrl()));*/

        item_1.setImageDrawable(getResources().getDrawable(popupItems.get(0).getIconUrl()));
        item_2.setImageDrawable(getResources().getDrawable(popupItems.get(1).getIconUrl()));
        item_3.setImageDrawable(getResources().getDrawable(popupItems.get(2).getIconUrl()));

        item_1.setOnClickListener(popupClickListener);
        item_2.setOnClickListener(popupClickListener);
        item_3.setOnClickListener(popupClickListener);

        item_1.setTag(0);
        item_2.setTag(1);
        item_3.setTag(2);

        // Set content width and height
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setBackgroundDrawable(new BitmapDrawable());
        // Show anchored to button

        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();

        display.getSize(size);

        int layoutheight = 154;
        int barHeight = this.getHeight();

        int x = (int) anchorView.getX() - size.x / 2 + anchorView.getWidth() / 2;
        int y = (int) (this.getY() - convertDpToPixel(154, getContext()));

        popup.setAnimationStyle(android.R.style.Animation_Toast);
        popup.showAtLocation(anchorView, Gravity.TOP, x, y);

        //paintTab(id, isNightEnabled ? R.color.dark_bar_active_color : R.color.light_bar_active_color);
        //paintTab(CURRENT_TAB_ID, isNightEnabled ? R.color.dark_bar_inactive_color : R.color.light_bar_inactive_color);

        /*int width = size.x;

        int sampleWidth = width / 5;
        int realStart = -(width / 2);

        int position = 0;
        for (int i = 0; i < mImages.size(); i++) {
            if ((int) mImages.get(i).getTag() == id) {
                position = i;
                break;
            }
        }

        int x = (realStart + (sampleWidth * (position + 1))) - (sampleWidth / 2);
        popup.setAnimationStyle(android.R.style.Animation_Toast);
        popup.showAtLocation(anchorView, Gravity.TOP, x, (int) (this.getY() - convertDpToPixel(154, getContext())));*/
    }

    private OnClickListener popupClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int temp = barItems.get(LONG_CLICK_POSITION).getId();
            AGBottomBarMultipleItem popupItem = popupItems.get((Integer) v.getTag());
            AGBottomBarMultipleItem barItem = (AGBottomBarMultipleItem) barItems.get(LONG_CLICK_POSITION);
            barItems.set(LONG_CLICK_POSITION, popupItem);
            mImages.get(LONG_CLICK_POSITION).setImageDrawable(getResources().getDrawable(popupItem.getIconUrl()));

            int itemId = 0;
            for (AGBottomBarItem item : mItems) {
                if (item.getId() == popupItem.getId()) {
                    item.setStart(true);
                    itemId = item.getId();
                }

                if (item.getId() == barItem.getId()) {
                    item.setStart(false);
                }
            }

            //saveBarItemsPlaces();

            if (CURRENT_TAB_ID > 2 && CURRENT_TAB_ID == temp) {
                if (popup != null & agPopup != null) {
                    agPopup.changeItem(LONG_CLICK_POSITION, itemId);
                }

                agHandler.itemClick(v, itemId);

                int y = popupItem.getId();
                mImages.get(LONG_CLICK_POSITION).setTag(y);
                CURRENT_TAB_ID = y;
            }
            if (popup != null) popup.dismiss();
        }
    };

    private OnClickListener itemClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag();
            if (position > 2) {
                paintTab(CURRENT_TAB_ID, isNightEnabled ?
                        R.color.dark_bar_inactive_color : R.color.light_bar_inactive_color);
                int a = barItems.get(position).getId();
                paintTab(a, isNightEnabled ?
                        R.color.dark_bar_active_color : R.color.light_bar_active_color);
            } else {
                paintTab(CURRENT_TAB_ID, isNightEnabled ?
                        R.color.dark_bar_inactive_color : R.color.light_bar_inactive_color);
                paintTab(position, isNightEnabled ?
                        R.color.dark_bar_active_color : R.color.light_bar_active_color);
            }
            CURRENT_TAB_ID = barItems.get(position).getId();
            agHandler.itemClick(v, barItems.get(position).getId());
            if (popup != null) popup.dismiss();
        }
    };

    private OnLongClickListener longClickListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            int position = (int) v.getTag();
            LONG_CLICK_POSITION = position;
            displayPopupWindow(v, position);
            paintTab(CURRENT_TAB_ID, isNightEnabled ?
                    R.color.dark_bar_inactive_color : R.color.light_bar_inactive_color);
            int a = barItems.get(position).getId();
            paintTab(a/*(int) mImages.get(barItems.get(position).getId()).getTag()*/, isNightEnabled ?
                    R.color.dark_bar_active_color : R.color.light_bar_active_color);
            /*paintTab(CURRENT_TAB_ID, isNightEnabled ?
                    R.color.dark_bar_inactive_color : R.color.light_bar_inactive_color);
            //Toast.makeText(activity.getBaseContext(), "LONG_CLICK_POSITION: " + String.valueOf(LONG_CLICK_POSITION), Toast.LENGTH_SHORT).show();
            paintTab(LONG_CLICK_POSITION, isNightEnabled ?
                    R.color.dark_bar_active_color : R.color.light_bar_active_color);*/
            CURRENT_TAB_ID = barItems.get(position).getId();
            return true;
        }
    };

    public void updateIcon(int id, int drawable) {
        for (AGBottomBarItem agBottomBarItem : barItems) {
            if (agBottomBarItem.getId() == id) {
                mImages.get(barItems.indexOf(agBottomBarItem))
                        .setImageDrawable(getResources().getDrawable(drawable));
            }
        }
    }

    private int temp_CURRENT_TAB_ID;

    public void setCURRENT_TAB_ID (int CURRENT_TAB_ID) {
        //this.temp_CURRENT_TAB_ID = CURRENT_TAB_ID;
        this.CURRENT_TAB_ID = CURRENT_TAB_ID;
    }

    private void parseCURRENT_TAB_ID () {
        /*for (AGBottomBarItem agBottomBarItem : barItems) {
            if (agBottomBarItem.getId() == temp_CURRENT_TAB_ID) {
                CURRENT_TAB_ID = agBottomBarItem.getPosition();
                mImages.get(CURRENT_TAB_ID).setTag(CURRENT_TAB_ID);
                paintTab(0, isNightEnabled ?
                        R.color.dark_bar_inactive_color : R.color.light_bar_inactive_color);
                paintTab(CURRENT_TAB_ID, isNightEnabled ?
                        R.color.dark_bar_active_color : R.color.light_bar_active_color);
                break;
            } else {
                CURRENT_TAB_ID = 0;
            }
        }*/
    }

    public void setAgHandler(AGHandler agHandler) {
        this.agHandler = agHandler;
    }

    public void setActivity(AppCompatActivity activity) {
        this.activity = activity;
    }

    public ArrayList<AGBottomBarItem> getBarItems() {
        return barItems;
    }

    public ArrayList<ImageView> getmImages() {
        return mImages;
    }

    public PopupWindow getPopup() {
        return popup;
    }

    public void setFilename (String filename) {
        this.filename = filename;
    }

    public String getFilename () {
        return filename;
    }

    public void setPath (String path) {
        this.path = path;
    }

    public String getPath () {
        return path;
    }

    private float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    private void saveBarItemsPlaces () {
        JSONObject jsonObject = new JSONObject();
        try {
            int i = 0;
            for (int j = 0; j < mItems.size(); j++) {
                if (mItems.get(j).isStart()) {
                    jsonObject.put(String.valueOf(i), mItems.get(j).getId());
                    i++;
                }
            }
            for (int j = 0; j < mItems.size(); j++) {
                if (!mItems.get(j).isStart()) {
                    jsonObject.put(String.valueOf(i), mItems.get(j).getId());
                    i++;
                }
            }

            try {
                File file = new File(Environment.getExternalStorageDirectory(), path);
                if (!file.exists()) {
                    file.mkdirs();
                }

                FileWriter writer = new FileWriter(new File(file, filename + ".json"));
                writer.append(jsonObject.toString());
                writer.flush();
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONObject loadBarItemsPlaces () {
        File file = new File(Environment.getExternalStorageDirectory(), path);
        JSONObject jsonObject  = null;
        if (file.exists()) {
            String jsonStr = "";
            try {
                FileReader reader = new FileReader(new File(file, filename + ".json"));
                BufferedReader bufferedReader = new BufferedReader(reader);
                jsonStr = bufferedReader.readLine();

                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                jsonObject = new JSONObject(jsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return jsonObject;
    }

    public void initBarItems () {
        JSONObject jsonObject = loadBarItemsPlaces();
        if (jsonObject == null) return;

        try {
            int[] arrayItemsId = new int[mItems.size()];
            for (int i = 0; i < mItems.size(); i++) {
                arrayItemsId[i] = Integer.valueOf(jsonObject.get(String.valueOf(i)).toString());
            }

            ArrayList<AGBottomBarItem> mItemsSort = new ArrayList<>(mItems.size());
            for (int i = 0; i < mItems.size(); i++) {
                for (int j = 0; j < mItems.size(); j++) {
                    if (arrayItemsId[i] == mItems.get(j).getId()) {
                        mItemsSort.add(mItems.get(j));
                        break;
                    }
                }
                mItemsSort.get(i).setStart(i < 5);
            }

            mItems = mItemsSort;
            //mItems.trimToSize();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setAgPopup (AGPopup agPopup) {
        this.agPopup = agPopup;
    }
}