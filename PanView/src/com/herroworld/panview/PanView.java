
package com.herroworld.panview;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

/**
 * This class provides a utility to pan a view horizontally given the maximum
 * pan bounds. It provides a smooth animation handling user touches. The view
 * automatically snaps to the right or left depending on the point the user
 * lifts his/her finger.
 */
public class PanView implements GestureDetector.OnGestureListener, Runnable {
    private static final String TAG = PanView.class.getSimpleName();
    private static final boolean DEBUG = false;

    private boolean mFocused = false;
    private int mPreviousX = 0;

    private final View mView;
    private final OnMeasuredListener mOnMeasuredListener;
    private OnPanListener mOnPanListener;
    private final Scroller mScroller;

    /**
     * Interface to get the maximum pan.
     */
    public interface OnMeasuredListener {
        public int getMaxPan();
    }

    /**
     * Interface to listen to pan events.
     */
    public interface OnPanListener {
        public void onPanStart();

        public void onPanEnd();
    }

    /**
     * Initialize providing the view to be panned and a listener to obtain the
     * maximum pan value.
     * 
     * @param context
     * @param view The view to be panned.
     */
    public PanView(Context context, View view) {
        mView = view;
        mScroller = new Scroller(context);

        // User of this class has to implement OnMeasuredListener interface
        mOnMeasuredListener = (OnMeasuredListener) context;
    }

    /**
     * Setting the pan listener.
     * 
     * @param listener Pan listener.
     */
    public void setOnPanListener(OnPanListener listener) {
        mOnPanListener = listener;
    }

    /**
     * Returns true is the view has focus.
     * 
     * @return True if the view has focus, false otherwise.
     */
    public boolean isFocused() {
        return mFocused;
    }

    /**
     * Returns true is the view is panned.
     * 
     * @return True if the view is panned, false otherwise.
     */
    public boolean isPanned() {
        if (!mScroller.isFinished()) {
            // If scroller isn't done scrolling, check for the finalX value
            return (mScroller.getFinalX() != 0);
        } else {
            return (mView.getScrollX() != 0);
        }
    }

    /**
     * Hides/Pans the view based on the maximum pan, as provided by {@Link
     *  OnMeasuredListener#getMaxPan()}.
     * 
     * @return True if the view was panned/hidden, false otherwise.
     */
    public boolean hide() {
        // Returns immediately if view isn't ready
        if (!isViewMeasured()) {
            return false;
        }

        // Done if viewed is not panned
        if (!isPanned()) {
            return true;
        }

        // Starting point of the pan
        final int startX = mView.getScrollX();

        // Since right scroll returns a negative value, the horizontal distance
        // required to un-pan the view is the exact same value as the current
        // scrollX
        final int dX = startX;

        fling(startX, dX);

        return true;
    }

    /**
     * Shows/Un-pans the full view.
     * 
     * @return True if the view was fully shown, false otherwise.
     */
    public boolean pan() {
        if (!isViewMeasured()) {
            return false;
        }

        if (isPanned()) {
            return true;
        }

        // Horizontal distance to travel
        final int startX = mView.getScrollX();

        final int dX = startX + mOnMeasuredListener.getMaxPan();
        fling(startX, dX);

        return true;
    }

    /**
     * Returns true if the view is measured.
     * 
     * @return True if the view is measured, false otherwise.
     */
    private boolean isViewMeasured() {
        return !(mView.getMeasuredWidth() == 0
        || mView.getMeasuredHeight() == 0);
    }

    /**
     * Interrupt the panning.
     */
    public void stop() {
        if (!mScroller.isFinished()) {
            mScroller.forceFinished(true);
        }
    }

    /**
     * Handles the {@link MotionEvent#ACTION_DOWN} event.
     */
    @Override
    public boolean onDown(MotionEvent e) {
        // Interrupt any ongoing panning
        stop();

        if (mOnPanListener != null) {
            mOnPanListener.onPanStart();
        }

        return false;
    }

    /**
     * Handles the {@link MotionEvent#ACTION_UP} event.
     * 
     * @param e The up motion event.
     * @return True if event was handled.
     */
    public boolean onUp(MotionEvent e) {
        // Nothing to handle is the view isn't in focus
        if (!isFocused())
            return false;

        mFocused = false;

        // Complete the panning since user is done with manual panning the
        // view
        completePanning();
        return true;
    }

    /**
     * Handles the scroll events if it is a horizontal pan and the view is in
     * focus.
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
            float distanceY) {

        if (mFocused) {
            panBy((int) distanceX);
        } else {
            // Handle the scroll if it is a horizontal pan, return false
            // otherwise
            if (Math.abs(distanceX) < Math.abs(distanceY)) {
                return mFocused;
            } else {
                mFocused = true;
                panBy((int) distanceX);
            }
        }

        return mFocused;

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
        return false;
    }

    /**
     * Move the pan position of the view.
     * 
     * @param x The amount of pixels to pan by horizontally.
     */
    public void panBy(int x) {
        if (!mFocused) {
            return;
        }

        final int startX = mView.getScrollX();
        final int dX;

        if (x < 0) { // Pan to the right
            // Negate maximum pan value because of negative scrollX value for
            // right pans
            final int maxPan = -mOnMeasuredListener.getMaxPan();

            // Making sure view does not pan more than maximum pan
            if ((startX + x) < maxPan) {
                // Pan as much as needed to get to maximum pan value
                dX = maxPan - startX;
            } else {
                dX = x;
            }
        } else { // Pan to the left
            // View is already at the left most edge of the screen
            if (startX == 0) {
                return;
            }

            if ((startX + x) > 0) {
                dX = -startX;
            } else {
                dX = x;
            }
        }

        if (DEBUG) {
            Log.i(TAG, "pan to the" + ((x < 0) ? " right" : " left") + " by: " + dX
                    + " current x: " + startX);
        }

        mView.scrollBy(dX, 0);
    }

    /**
     * Fling the view.
     * 
     * @param startX
     * @param dX
     */
    public void fling(int startX, int dX) {
        if (DEBUG) {
            Log.i(TAG, "fling from: " + startX + " by: " + dX);
        }

        if (dX == 0)
            return;

        // Start panning the scroller
        mScroller.startScroll(startX, 0, dX, 0);

        mPreviousX = startX;

        // Post a runnable to handle panning the view using scroller as a
        // reference
        mView.post(this);
    }

    /**
     * Complete the view panning when {@link MotionEvent#ACTION_UP} event
     * happens.
     */
    private void completePanning() {
        final int currentX = mView.getScrollX();

        final int maxPan = mOnMeasuredListener.getMaxPan();
        final int middle = -maxPan / 2;
        final int dX;

        // Check if current position is above or below the middle point; pan
        // left if it is not, right otherwise
        if (currentX > middle) {
            dX = currentX;
        } else {
            dX = maxPan + currentX;
        }

        fling(currentX, dX);
    }

    /**
     * Runnable to handle the physical panning of the view using scroller as the
     * reference
     */
    @Override
    public void run() {
        if (mScroller.isFinished()) {
            if (mOnPanListener != null) {
                mOnPanListener.onPanEnd();
            }
            return;
        }

        // Check if the scroller animation is finished; continue panning if
        // it's not, return otherwise
        final boolean panMore = mScroller.computeScrollOffset();
        final int currentX = mScroller.getCurrX();

        // Calculate the difference between the current X position of the view
        // and the scroller
        final int diff = mPreviousX - currentX;

        if (DEBUG) {
            Log.i(TAG, "diff: " + diff + " current x: " + currentX + " previous x = " + mPreviousX
                    + " panMore:" + panMore);
        }

        // Pan by the difference between the view and the scroller
        if (diff != 0) {
            mView.scrollBy(diff, 0);
            mPreviousX = currentX;
        }

        if (panMore) {
            mView.post(this);
        }
    }
}
