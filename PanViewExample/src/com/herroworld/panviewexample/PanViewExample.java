
package com.herroworld.panviewexample;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;

import com.herroworld.panview.PanView;
import com.herroworld.panview.PanView.OnMeasuredListener;
import com.herroworld.panview.PanView.OnPanListener;

public class PanViewExample extends Activity implements OnMeasuredListener, OnPanListener {
    private static final String TAG = PanViewExample.class.getSimpleName();
    private GestureDetector mGestureDetector;
    private PanView mPanView;
    private Button mPanButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // View to enable touch based panning
        final View view = findViewById(R.id.panView);
        view.setOnTouchListener(mTouchListener);

        // Button to quickly pan/unpan view
        mPanButton = (Button) findViewById(R.id.panButton);
        mPanButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mPanView.isPanned()) {
                    mPanView.hide();
                } else {
                    mPanView.pan();
                }
            }
        });

        mPanView = new PanView(this, view);

        // Listen to when the pan starts/ends measure
        mPanView.setOnPanListener(this);
        mPanView.setOnMeasuredListener(this);

        // PanView implements GestureDetector.OnGestureListener
        mGestureDetector = new GestureDetector(this, mPanView);
        mGestureDetector.setIsLongpressEnabled(false);
    }

    OnTouchListener mTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final int action = event.getAction();

            // Handle the up event
            if (mPanView.isFocused() && action == MotionEvent.ACTION_UP) {
                mPanView.onUp(event);
                return false;
            }

            // Let ViewController handle all touch events first
            if (mGestureDetector.onTouchEvent(event) || mPanView.isFocused()) {
                return false;
            }

            return true;
        }
    };

    /**
     * Interface to provide the maximum pan.
     */
    @Override
    public int getMaxPan() {
        return (int) getResources().getDimension(R.dimen.max_pan);
    }

    @Override
    public void onPanStart() {
    }

    /**
     * Change the button text depending on whether or not the view was panned
     */
    @Override
    public void onPanEnd() {
        if (mPanView.isPanned()) {
            mPanButton.setText(getResources().getString(R.string.unpan));
        } else {
            mPanButton.setText(getResources().getString(R.string.pan));
        }
    }
}
