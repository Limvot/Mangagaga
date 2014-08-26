package io.githup.limvot.mangaapp;

import io.githup.limvot.mangaapp.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class ImageViewerActivity extends Activity implements GestureDetector.OnGestureListener {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = false;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    private GestureDetectorCompat detector;
    private View.OnTouchListener gestureListen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_viewer);

        // Hide the action bar.
        final ActionBar actionBar = getActionBar();
        Drawable d = new ColorDrawable(0);
        d.setAlpha(1);
        actionBar.setBackgroundDrawable(d);
        //actionBar.hide();

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        detector = new GestureDetectorCompat(this,this);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        contentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                detector.onTouchEvent(motionEvent);
                return true;
            }
        });

        this.displayImage();
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.back_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    /**
     * Gesture Listener method implementations.
     */
    @Override
    public boolean onSingleTapUp(MotionEvent event)
    {
        float x;
        x = event.getX();
        if(x < 350)
        {
            Script source = ScriptManager.getScriptManager().getCurrentSource();
            int total = source.getNumPages();
            int i = source.getCurrentPage();
            if(i < total-1) {
                source.setCurrentPage(i+1);
            } else {
                source.nextChapter();
                source.setCurrentPage(0);
            }
        }
        else
        {
            Script source = ScriptManager.getScriptManager().getCurrentSource();
            int i = source.getCurrentPage();
            if(i > 0) {
                source.setCurrentPage(i-1);
            } else {
                source.previousChapter();
                source.setCurrentPage(source.getNumPages() - 1);
            }
        }
        displayImage();
        Log.d("onSingleTapUp","X: "+Float.toString(x));
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event)
    {}

    @Override
    public void onLongPress(MotionEvent event)
    {}

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distancex, float distancey)
    {
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velx, float vely)
    {
        Log.d("onFling", "Velx: "+Float.toString(velx));
        if(velx > 0)
        {
            //swiped from left to right
            Script source = ScriptManager.getScriptManager().getCurrentSource();
            int total = source.getNumPages();
            int i = source.getCurrentPage();
            Log.d("onClick", Integer.toString(total));
            if(i < total-1) {
                source.setCurrentPage(i+1);
            } else {
                source.nextChapter();
                source.setCurrentPage(0);
            }
        }
        else
        {
            //swiped from right to left
            Script source = ScriptManager.getScriptManager().getCurrentSource();
            int i = source.getCurrentPage();
            if(i > 0) {
                source.setCurrentPage(i-1);
            } else {
                source.previousChapter();
                source.setCurrentPage(source.getNumPages() - 1);
            }
        }
        displayImage();
        return true;
    }

    public boolean onDown(MotionEvent event)
    {
        return true;
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void displayImage()
    {
        Script source = ScriptManager.getScriptManager().getCurrentSource();
        Log.i("DISPLAY IMAGE", Integer.toString(source.getNumPages()));
        String imagepath = source.downloadPage();
        Log.i("Display image!", imagepath);
        ImageManager im = ImageManager.getImageManager();

        ImageView contentview = (ImageView) findViewById(R.id.fullscreen_content);
        Bitmap page = im.getNext(imagepath);
        contentview.setImageBitmap(page);
    }
}
