package ticwear.design.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;

import com.mobvoi.ticwear.view.SidePanelEventDispatcher;

import ticwear.design.R;

@TargetApi(20)
@CoordinatorLayout.DefaultBehavior(TicklableListViewBehavior.class)
public class TicklableListView extends RecyclerView implements SidePanelEventDispatcher {

    static final String TAG = "TicklableLV";

    /**
     * {@link LayoutManager} for focus state.
     */
    @Nullable
    private TicklableLayoutManager mTicklableLayoutManager;

    private boolean mSkipNestedScroll;

    private ScrollBarHelper mScrollBarHelper;

    public TicklableListView(Context context) {
        this(context, null);
    }

    public TicklableListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TicklableListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TicklableListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        setHasFixedSize(true);
        setOverScrollMode(OVER_SCROLL_NEVER);

        mSkipNestedScroll = false;

        if (!isInEditMode() && getItemAnimator() != null) {
            long defaultAnimDuration = context.getResources()
                    .getInteger(R.integer.design_anim_list_item_state_change);
            long itemAnimDuration = defaultAnimDuration / 4;
            getItemAnimator().setMoveDuration(itemAnimDuration);
        }

        mScrollBarHelper = new ScrollBarHelper(context);
    }

    @Override
    public LinearLayoutManager getLayoutManager() {
        return (LinearLayoutManager) super.getLayoutManager();
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        mScrollBarHelper.setIsRound(insets.isRound());
        return super.onApplyWindowInsets(insets);
    }

    //@hide api @Override
    //use this hide api to draw scrollbar
    protected void onDrawVerticalScrollBar(Canvas canvas, Drawable scrollBar, int l, int t, int r, int b) {
        int range = computeVerticalScrollRange();
        int offset = computeVerticalScrollOffset();
        int extent = computeVerticalScrollExtent();
        mScrollBarHelper.onDrawScrollBar(canvas, range, offset, extent, scrollBar.getAlpha());
    }

    /**
     * Set a new adapter to provide child views on demand.
     *
     * @param adapter new adapter that should be instance of {@link TicklableListView.Adapter}
     */
    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        if (mTicklableLayoutManager == null || mTicklableLayoutManager.validAdapter(adapter)) {
            super.setAdapter(adapter);
        } else {
            throw new IllegalArgumentException("Adapter is invalid for current TicklableLayoutManager.");
        }
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);

        if (mTicklableLayoutManager == layout) {
            return;
        }

        if (mTicklableLayoutManager != null) {
            mTicklableLayoutManager.setTicklableListView(null);
        }
        if (!(layout instanceof TicklableLayoutManager)) {
            Log.w(TAG, "To let TicklableListView support complex tickle events," +
                    " let LayoutManager implements TicklableLayoutManager.");
            mTicklableLayoutManager = null;
            return;
        }

        TicklableLayoutManager ticklableLayoutManager = (TicklableLayoutManager) layout;
        if (ticklableLayoutManager.validAdapter(getAdapter())) {
            mTicklableLayoutManager = (TicklableLayoutManager) layout;
            mTicklableLayoutManager.setTicklableListView(this);
        } else {
            Log.w(TAG, "To let TicklableListView support complex tickle events," +
                    " make sure your Adapter is compat with TicklableLayoutManager.");
            mTicklableLayoutManager = null;
        }
    }

    public boolean interceptPreScroll() {
        return mTicklableLayoutManager != null && mTicklableLayoutManager.interceptPreScroll();
    }

    public boolean useScrollAsOffset() {
        return mTicklableLayoutManager != null && mTicklableLayoutManager.useScrollAsOffset();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return (mTicklableLayoutManager != null && mTicklableLayoutManager.dispatchTouchEvent(ev)) ||
                super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchSidePanelEvent(MotionEvent ev, @NonNull SuperCallback superCallback) {
        if (mTicklableLayoutManager == null) {
            return dispatchTouchEvent(ev) || superCallback.superDispatchTouchSidePanelEvent(ev);
        }

        return mTicklableLayoutManager.dispatchTouchSidePanelEvent(ev) ||
                superCallback.superDispatchTouchSidePanelEvent(ev);

    }

    public int getScrollOffset() {
        return mTicklableLayoutManager != null ? mTicklableLayoutManager.getScrollOffset() : 0;
    }

    /**
     * Update offset to scroll.
     *
     * This will calculate the delta of previous offset and new offset, then apply it to scroll.
     *
     * @param scrollOffset new offset to scroll.
     *
     * @return the unconsumed offset.
     */
    public int updateScrollOffset(int scrollOffset) {
        return mTicklableLayoutManager != null ?
                mTicklableLayoutManager.updateScrollOffset(scrollOffset) : scrollOffset;
    }

    public void scrollBySkipNestedScroll(int x, int y) {
        mSkipNestedScroll = true;
        scrollBy(x, y);
        mSkipNestedScroll = false;
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return !mSkipNestedScroll && super.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return !mSkipNestedScroll && super.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return !mSkipNestedScroll && super.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return !mSkipNestedScroll && super.dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean dispatchNestedPrePerformAccessibilityAction(int action, Bundle arguments) {
        return !mSkipNestedScroll && super.dispatchNestedPrePerformAccessibilityAction(action, arguments);
    }
}
