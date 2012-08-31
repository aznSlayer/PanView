PanView
=======

The PanView class provides a utility to pan a view
horizontally given the maximum pan bounds. It provides a smooth
animation handling user touches. The view automatically snaps to the
right or left depending on the point the user lifts his/her finger.

![1] -> ![2]

**Download:** [JAR library](https://github.com/downloads/herroWorld/PanView/panview_v1.0.jar)

## Usage
When using this library, simply add the JAR to your project:

```
Right click on project --> Properties --> Java Build Path --> Libraries --> Add External JARs
```

### Example
```
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

// Listen to when the pan starts and ends
mPanView.setOnPanListener(this);

// PanView implements GestureDetector.OnGestureListener
mGestureDetector = new GestureDetector(this, mPanView);
mGestureDetector.setIsLongpressEnabled(false);

// Calling class HAVE to Implement the OnMeasuredListener interface to
// provide the maximum pan
@Override
public int getMaxPan() {
    return (int) getResources().getDimension(R.dimen.max_pan);
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
```

## To Do
* Add functionality to support panning in all directions

## License
* [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

 [1]: https://github.com/downloads/herroWorld/PanView/panViewExample1.png
 [2]: https://github.com/downloads/herroWorld/PanView/panViewExample2.png
