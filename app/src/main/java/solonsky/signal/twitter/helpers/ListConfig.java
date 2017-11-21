package solonsky.signal.twitter.helpers;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.IntRange;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import solonsky.signal.twitter.draw.FancyLinearLayoutManager;
import solonsky.signal.twitter.libs.SpeedyLinearLayoutManager;

/**
 * Import library for RecyclerView Config
 * @link https://github.com/drstranges/DataBinding_For_RecyclerView
 */
public class ListConfig {
    private final RecyclerView.Adapter mAdapter;
    private final LayoutManagerProvider mLayoutManagerProvider;
    private final RecyclerView.ItemAnimator mItemAnimator;
    private final List<RecyclerView.ItemDecoration> mItemDecorations;
    private final List<RecyclerView.OnScrollListener> mScrollListeners;
    private final ItemTouchHelper mItemTouchHelper;
    private final boolean mHasFixedSize;
    private final boolean mHasNestedScroll;

    private ListConfig(final RecyclerView.Adapter adapter,
                       final LayoutManagerProvider layoutManagerProvider,
                       final RecyclerView.ItemAnimator itemAnimator,
                       final List<RecyclerView.ItemDecoration> itemDecorations,
                       final List<RecyclerView.OnScrollListener> scrollListeners,
                       final ItemTouchHelper itemTouchHelper,
                       final boolean hasFixedSize,
                       final boolean hasNestedScroll) {
        mAdapter = adapter;
        mLayoutManagerProvider = layoutManagerProvider;
        mItemAnimator = itemAnimator;
        mItemDecorations = itemDecorations != null ? itemDecorations : Collections.<RecyclerView.ItemDecoration>emptyList();
        mScrollListeners = scrollListeners != null ? scrollListeners : Collections.<RecyclerView.OnScrollListener>emptyList();
        mItemTouchHelper = itemTouchHelper;
        mHasFixedSize = hasFixedSize;
        mHasNestedScroll = hasNestedScroll;
    }

    /**
     * Applies defined configuration for RecyclerView
     *
     * @param context      the context
     * @param recyclerView the target recycler view for applying the configuration
     */
    public void applyConfig(final Context context, final RecyclerView recyclerView) {
        final RecyclerView.LayoutManager layoutManager;
        if (mAdapter == null || mLayoutManagerProvider == null || (layoutManager = mLayoutManagerProvider.get(context)) == null)
            return;
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(mHasFixedSize);
        recyclerView.setNestedScrollingEnabled(mHasNestedScroll);
        recyclerView.setAdapter(mAdapter);
        for (RecyclerView.ItemDecoration itemDecoration : mItemDecorations) {
            recyclerView.addItemDecoration(itemDecoration);
        }
        for (RecyclerView.OnScrollListener scrollListener : mScrollListeners) {
            recyclerView.addOnScrollListener(scrollListener);
        }
        if (mItemAnimator != null) {
            recyclerView.setItemAnimator(mItemAnimator);
        }
        if (mItemTouchHelper != null) {
            mItemTouchHelper.attachToRecyclerView(recyclerView);
        }
    }

    /**
     * Builder for setting ListConfig
     * Sample:
     * <pre>
     * {@code
     *      ListConfig listConfig = new ListConfig.Builder(mAdapter)
     *          .setLayoutManagerProvider(new SimpleGridLayoutManagerProvider(mSpanCount, getSpanSizeLookup()))
     *          .addItemDecoration(new ColorDividerItemDecoration(color, spacing, SPACE_LEFT|SPACE_TOP, false))
     *          .setDefaultDividerEnabled(true)
     *          .addOnScrollListener(new OnLoadMoreScrollListener(mCallback))
     *          .setItemAnimator(getItemAnimator())
     *          .setHasFixedSize(true)
     *          .setItemTouchHelper(getItemTouchHelper())
     *          .build(context);
     * }
     * </pre>
     * If LinearLayoutManager will be used by default
     */
    public static class Builder {
        private static final String TAG = Builder.class.getSimpleName();
        private final RecyclerView.Adapter mAdapter;
        private LayoutManagerProvider mLayoutManagerProvider;
        private RecyclerView.ItemAnimator mItemAnimator;
        private List<RecyclerView.ItemDecoration> mItemDecorations;
        private List<RecyclerView.OnScrollListener> mOnScrollListeners;
        private ItemTouchHelper mItemTouchHelper;
        private boolean mHasNestedScroll;
        private boolean mHasFixedSize;
        private int mDefaultDividerSize = -1;

        /**
         * Creates new Builder for config RecyclerView with the adapter
         *
         * @param adapter the adapter, which will be set to the RecyclerView
         */
        public Builder(RecyclerView.Adapter adapter) {
            mAdapter = adapter;
        }

        /**
         * Set Layout manager provider. If not set default {@link LinearLayoutManager} will be applied
         *
         * @param layoutManagerProvider the layout manager provider. Can be custom or one of
         *                              simple: {@link SimpleLinearLayoutManagerProvider},
         *                              {@link SimpleGridLayoutManagerProvider} or
         *                              {@link SimpleStaggeredGridLayoutManagerProvider}.
         * @return the builder
         */
        public Builder setLayoutManagerProvider(LayoutManagerProvider layoutManagerProvider) {
            Log.e(TAG, "layout manager is set - " + layoutManagerProvider);
            mLayoutManagerProvider = layoutManagerProvider;
            return this;
        }

        /**
         * Set {@link android.support.v7.widget.RecyclerView.ItemAnimator}
         *
         * @param itemAnimator the item animator
         * @return the builder
         */
        public Builder setItemAnimator(RecyclerView.ItemAnimator itemAnimator) {
            mItemAnimator = itemAnimator;
            return this;
        }

        /**
         * Set {@link android.support.v7.widget.RecyclerView.ItemDecoration}
         *
         * @return the builder
         */
        public Builder addItemDecoration(RecyclerView.ItemDecoration itemDecoration) {
            if (mItemDecorations == null) {
                mItemDecorations = new ArrayList<>();
            }
            mItemDecorations.add(itemDecoration);
            return this;
        }

        /**
         * Set {@link android.support.v7.widget.RecyclerView.OnScrollListener}
         *
         * @param onScrollListener the scroll listener.
         * @return the builder
         */
        public Builder addOnScrollListener(RecyclerView.OnScrollListener onScrollListener) {
            if (mOnScrollListeners == null) {
                mOnScrollListeners = new ArrayList<>();
            }
            mOnScrollListeners.add(onScrollListener);
            return this;
        }

        /**
         * Set true if adapter changes cannot affect the size of the RecyclerView.
         * Applied to {@link RecyclerView#setHasFixedSize(boolean)}
         *
         * @param isFixedSize true if RecyclerView items have fixed size
         * @return the builder
         */
        public Builder setHasFixedSize(boolean isFixedSize) {
            mHasFixedSize = isFixedSize;
            return this;
        }

        /**
         * Set true if adapter changes cannot affect the size of the RecyclerView.
         * Applied to {@link RecyclerView#setNestedScrollingEnabled(boolean)}
         *
         * @param isNestedScroll true if RecyclerView items have nested scroll
         * @return the builder
         */
        public Builder setHasNestedScroll(boolean isNestedScroll) {
            mHasNestedScroll = isNestedScroll;
            return this;
        }

        /**
         * Set true to apply default divider with default size of 4dp.
         *
         * @param isEnabled set true to apply default divider.
         * @return the builder
         */
        public Builder setDefaultDividerEnabled(boolean isEnabled) {
            mDefaultDividerSize = isEnabled ? 0 : -1;
            return this;
        }

        /**
         * Enables defoult divider with custom size
         *
         * @param size
         * @return the builder
         */
        public Builder setDefaultDividerSize(int size) {
            mDefaultDividerSize = size;
            return this;
        }

        /**
         * Set {@link ItemTouchHelper}
         *
         * @param itemTouchHelper the ItemTouchHelper to apply for RecyclerView
         * @return the builder
         */
        public Builder setItemTouchHelper(ItemTouchHelper itemTouchHelper) {
            mItemTouchHelper = itemTouchHelper;
            return this;
        }

        /**
         * Creates new {@link ListConfig} with defined configuration
         * If LayoutManagerProvider is not set, the {@link SimpleLinearLayoutManagerProvider}
         * will be used.
         *
         * @param context the context
         * @return the new ListConfig
         */
        public ListConfig build(Context context) {
            if (mLayoutManagerProvider == null)
                mLayoutManagerProvider = new SimpleLinearLayoutManagerProvider();

            if (mDefaultDividerSize >= 0) {
                /*if (mDefaultDividerSize == 0) mDefaultDividerSize = context.getResources()
                        .getDimensionPixelSize(R.dimen.rvdb_list_divider_size_default);
                addItemDecoration(new DividerItemDecoration(mDefaultDividerSize));*/
            }

            return new ListConfig(
                    mAdapter,
                    mLayoutManagerProvider,
                    mItemAnimator, mItemDecorations,
                    mOnScrollListeners,
                    mItemTouchHelper,
                    mHasFixedSize,
                    mHasNestedScroll);
        }
    }

    /**
     * The provider of LayoutManager for RecyclerView
     */
    public interface LayoutManagerProvider {
        RecyclerView.LayoutManager get(Context context);
    }

    /**
     * The simple LayoutManager provider for {@link LinearLayoutManager}
     */
    public static class SimpleLinearLayoutManagerProvider implements LayoutManagerProvider {
        @Override
        public RecyclerView.LayoutManager get(Context context) {
            return new LinearLayoutManager(context);
        }
    }

    public static class SpeedyLinearLayoutManagerProvider implements LayoutManagerProvider {
        private static final String TAG = SpeedyLinearLayoutManagerProvider.class.getSimpleName();

        @Override
        public RecyclerView.LayoutManager get(Context context) {
            Log.e(TAG, "Speedy manager created");
            return new SpeedyLinearLayoutManager(context);
        }
    }

    public static class RefreshLinearLayoutManagerProvider implements LayoutManagerProvider {
        @Override
        public RecyclerView.LayoutManager get(Context context) {
            return new LinearLayoutManager(context);
        }
    }

    public static class SimpleHorizontalLayoutManagerProvider implements LayoutManagerProvider {
        @Override
        public RecyclerView.LayoutManager get(Context context) {
            return new FancyLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        }
    }

    /**
     * The simple LayoutManager provider for {@link LinearLayoutManager}
     */
    public static class ReversedLinearLayoutManagerProvider implements LayoutManagerProvider {
        @Override
        public RecyclerView.LayoutManager get(Context context) {
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
            mLayoutManager.setReverseLayout(true);
            mLayoutManager.setStackFromEnd(true);
            return mLayoutManager;
        }
    }

    /**
     * The simple LayoutManager provider for {@link GridLayoutManager}
     */
    public static class SimpleGridLayoutManagerProvider implements LayoutManagerProvider {
        private final int mSpanCount;
        private GridLayoutManager.SpanSizeLookup mSpanSizeLookup;

        public SimpleGridLayoutManagerProvider(@IntRange(from = 1) int mSpanCount) {
            this.mSpanCount = mSpanCount;
        }

        public SimpleGridLayoutManagerProvider(int spanCount, GridLayoutManager.SpanSizeLookup spanSizeLookup) {
            mSpanCount = spanCount;
            mSpanSizeLookup = spanSizeLookup;
        }

        @Override
        public RecyclerView.LayoutManager get(Context context) {
            GridLayoutManager layoutManager = new GridLayoutManager(context, mSpanCount);
            if (mSpanSizeLookup != null) layoutManager.setSpanSizeLookup(mSpanSizeLookup);
            return layoutManager;
        }
    }

    /**
     * The simple LayoutManager provider for {@link StaggeredGridLayoutManager}
     */
    public static class SimpleStaggeredGridLayoutManagerProvider implements LayoutManagerProvider {
        private final int mSpanCount;
        private final int mOrientation;

        public SimpleStaggeredGridLayoutManagerProvider(@IntRange(from = 1) int spanCount) {
            this(spanCount, StaggeredGridLayoutManager.VERTICAL);
        }

        public SimpleStaggeredGridLayoutManagerProvider(@IntRange(from = 1) int spanCount, final int orientation) {
            this.mSpanCount = spanCount;
            this.mOrientation = orientation;
        }

        @Override
        public RecyclerView.LayoutManager get(Context context) {
            return new StaggeredGridLayoutManager(mSpanCount, mOrientation);
        }
    }

    public static class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {

            // Add top margin only for the first item to avoid double space between
            if (parent.getLayoutManager().getItemCount() == 4) {
                switch (parent.getChildLayoutPosition(view)) {
                    case 0:
                        outRect.top = 0;
                        outRect.right = space;
                        outRect.left = 0;
                        outRect.bottom = space;
                        break;

                    case 1:
                        outRect.top = 0;
                        outRect.right = 0;
                        outRect.left = space;
                        outRect.bottom = space;
                        break;

                    case 2:
                        outRect.top = space;
                        outRect.right = space;
                        outRect.left = 0;
                        outRect.bottom = 0;
                        break;

                    case 3:
                        outRect.top = space;
                        outRect.right = 0;
                        outRect.left = space;
                        outRect.bottom = 0;
                        break;
                }
            } else if (parent.getLayoutManager().getItemCount() == 3) {
                switch (parent.getChildLayoutPosition(view)) {
                    case 0:
                        outRect.top = 0;
                        outRect.right = 0;
                        outRect.left = 0;
                        outRect.bottom = 0;
                        break;

                    case 1:
                        outRect.top = space * 2;
                        outRect.right = space;
                        outRect.left = 0;
                        outRect.bottom = 0;
                        break;

                    case 2:
                        outRect.top = space * 2;
                        outRect.right = 0;
                        outRect.left = space;
                        outRect.bottom = 0;
                        break;
                }
            } else if (parent.getLayoutManager().getItemCount() == 2) {
                switch (parent.getChildLayoutPosition(view)) {
                    case 0:
                        outRect.top = 0;
                        outRect.right = space;
                        outRect.left = 0;
                        outRect.bottom = 0;
                        break;

                    case 1:
                        outRect.top = 0;
                        outRect.right = 0;
                        outRect.left = space;
                        outRect.bottom = 0;
                        break;
                }
            } else if (parent.getLayoutManager().getItemCount() == 3) {
                outRect.top = 0;
                outRect.right = 0;
                outRect.left = 0;
                outRect.bottom = 0;
            } else {
                int position = parent.getChildLayoutPosition(view);
                if (position > 1) {
                    if (position % 2 == 0) {
                        outRect.top = 0;
                        outRect.right = space;
                        outRect.left = 0;
                        outRect.bottom = space * 2;
                    } else {
                        outRect.top = 0;
                        outRect.right = 0;
                        outRect.left = space;
                        outRect.bottom = space * 2;
                    }
                } else {
                    outRect.top = 0;
                    outRect.right = position == 0 ? space : 0;
                    outRect.left = position == 0 ? 0 : space;
                    outRect.bottom = space * 2;
                }
            }
        }
    }
}
