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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.InputDevice;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;

import scala.concurrent.Future;
import scala.concurrent.ExecutionContext;
import org.scaloid.common._
import scala.collection.JavaConversions._


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
class ImageViewerActivity extends Activity with GestureDetector.OnGestureListener {
    implicit val exec = ExecutionContext.fromExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private val AUTO_HIDE: Boolean = false;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private val AUTO_HIDE_DELAY_MILLIS: Int = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private val TOGGLE_ON_CLICK: Boolean = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private val HIDER_FLAGS: Int = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private var mSystemUiHider:SystemUiHider = null;

    private var detector:GestureDetectorCompat = null;
    private var gestureListen:View.OnTouchListener = null;

    private var contentview:ImageView = null;


    override protected def onCreate(savedInstanceState: Bundle) = {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_viewer);
        contentview = findViewById(R.id.fullscreen_content).asInstanceOf[ImageView];


        // Hide the action bar.
        var actionBar: ActionBar = getActionBar();
        var d = new ColorDrawable(0);
        d.setAlpha(1);
        if (actionBar != null) {
          actionBar.setBackgroundDrawable(d);
          actionBar.hide();
        }



        val controlsView: View = findViewById(R.id.fullscreen_content_controls);
        val contentView: View = findViewById(R.id.fullscreen_content);

        detector = new GestureDetectorCompat(this,this);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    var mControlsHeight: Int = 0;
                    var mShortAnimTime: Int = 0;

                    
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    override def onVisibilityChange(visible: Boolean) =  {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate().translationY((if (visible) 0 else mControlsHeight)).setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility((if (visible) View.VISIBLE else 8));
                            // replaced GONE with value 8. Looked up value in dev documentation. 
                            // Value is listed as 0x0000000008
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            override def onClick(view: View) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        contentView.setOnTouchListener(new View.OnTouchListener() {
            override def onTouch(view: View, motionEvent: MotionEvent): Boolean =  {
                detector.onTouchEvent(motionEvent);
                return true;
            }
        });

        this.updateImage();
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.back_button).setOnTouchListener(mDelayHideTouchListener);
    }

    override protected def onPostCreate(savedInstanceState: Bundle) = {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        //delayedHide(100);
    }

    /**
     * Gesture Listener method implementations.
     */
    override def onSingleTapUp(event: MotionEvent): Boolean = {
        var x: Float = 0;
        x = event.getX();
        if(x < 350)
        {
            var total: Int = MangaManager.getNumPages();
            var i: Int = MangaManager.getCurrentPageNum();
            if(i < total-1) {
                MangaManager.setCurrentPageNum(i+1);
            } else {
                MangaManager.nextChapter();
                MangaManager.setCurrentPageNum(0);
            }
        }
        else
        {
            var i: Int = MangaManager.getCurrentPageNum();
            if(i > 0) {
                MangaManager.setCurrentPageNum(i-1);
            } else {
                MangaManager.previousChapter();
                MangaManager.setCurrentPageNum(MangaManager.getNumPages() - 1);
            }
        }
        updateImage();
        Log.d("onSingleTapUp","X: "+x.toFloat.toString);
        return true;
    }

    override def onCreateOptionsMenu(menu: Menu): Boolean = {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.image_viewer, menu);
        return true;
    }

    override def onShowPress(event: MotionEvent) = {}

    override def onLongPress(event: MotionEvent) = {}

    override def onScroll(e1: MotionEvent, e2:MotionEvent, distancex: Float, distancey: Float): Boolean = {
        return true;
    }

    override def onFling(e1: MotionEvent, e2: MotionEvent, velx: Float, vely: Float): Boolean = {
        Log.d("onFling", "Velx: "+velx.toFloat.toString);
        if(velx > 0)
        {
            //swiped from left to right
            var total: Int = MangaManager.getNumPages();
            var i: Int = MangaManager.getCurrentPageNum();
            Log.d("onClick", Integer.toString(total));
            if(i < total-1) {
                MangaManager.setCurrentPageNum(i+1);
            } else {
                // Returns true if successful (has a next chapter)
                if (MangaManager.nextChapter())
                    MangaManager.setCurrentPageNum(0);
            }
        }
        else
        {
            //swiped from right to left
            var i: Int = MangaManager.getCurrentPageNum();
            if(i > 0) {
                MangaManager.setCurrentPageNum(i-1);
            } else {
                // Returns true if successful (has a previous chapter)
                if (MangaManager.previousChapter())
                    MangaManager.setCurrentPageNum(MangaManager.getNumPages() - 1);
            }
        }
        updateImage();
        return true;
    }

    def onDown(event: MotionEvent): Boolean = {
        return true;
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    var mDelayHideTouchListener = new View.OnTouchListener() {
        override def onTouch(view: View, motionEvent: MotionEvent): Boolean = {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    var mHideHandler = new Handler();
    var mHideRunnable = new Runnable() {
        override def run() = {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private def delayedHide(delayMillis: Int) = {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    // Do image updates async to maintain responsiveness


    def updateImage() = {
        // new UpdateImage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        // THE FUTURE IS NOW
        Future {
            Log.i("ASYNC DO IN BACKGROUND", "STARTING");
            var imagepath = MangaManager.getCurrentPage();
            Log.i("Display image!", imagepath);
            Log.i("ASYNC DO IN BACKGROUND", "ENDING");
            var bm: Bitmap = ImageManager.getNext(imagepath);
            Log.i("ASYNC DO IN BACKGROUND", "Post Execute!");
            runOnUiThread(contentview.setImageBitmap(bm));
        }
        // NOW IT'S THE PRESENT

    }
}
