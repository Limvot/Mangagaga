package io.githup.limvot.mangagaga

import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

import android.os.Bundle
import android.os.Environment;
import android.app.Activity

import android.widget.ImageView
import android.view.GestureDetector
import android.view.MotionEvent;

class ImageViewerActivity : Activity(), AnkoLogger, GestureDetector.OnGestureListener {
    var image : ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getActionBar().hide()
        frameLayout {
            image = imageView()
        }
        val detector = GestureDetector(this, this)
        image!!.setOnTouchListener({ view, motionEvent -> detector.onTouchEvent(motionEvent); true})
        updateImage()
    }
    fun updateImage() {
        /*val dialog = indeterminateProgressDialog(title = "Loading Page", message = "(may take a little bit if script sets up pages)")*/
        doAsync {
            val imagePath = MangaManager.getCurrentPage()
            val bm = ImageManager.getNext(imagePath)
            uiThread {
                image!!.setImageBitmap(bm)
                /*dialog.dismiss()*/
            }
        }
    }
    // Gesturedetector.OnGestureListener implmentation
    override fun onSingleTapUp(event: MotionEvent): Boolean {
        var x: Float = 0f
        x = event.getX()
        if(x < 350) {
            var total = MangaManager.getNumPages()
            var i = MangaManager.getCurrentPageNum()
            if(i < total-1) {
                MangaManager.setCurrentPageNum(i+1)
            } else {
                MangaManager.nextChapter()
                MangaManager.setCurrentPageNum(0)
            }
        } else {
            var i = MangaManager.getCurrentPageNum()
            if(i > 0) {
                MangaManager.setCurrentPageNum(i-1)
            } else {
                MangaManager.previousChapter()
                MangaManager.setCurrentPageNum(MangaManager.getNumPages() - 1)
            }
        }
        updateImage()
        return true
    }

    override fun onShowPress(event: MotionEvent) {}
    override fun onLongPress(event: MotionEvent) {}
    override fun onScroll(e1: MotionEvent, e2:MotionEvent, distancex: Float, distancey: Float) = true

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velx: Float, vely: Float): Boolean {
        if(velx > 0) {
            //swiped from left to right
            var total = MangaManager.getNumPages()
            var i = MangaManager.getCurrentPageNum()
            if(i < total-1) {
                MangaManager.setCurrentPageNum(i+1)
            } else {
                // Returns true if successful (has a next chapter)
                if (MangaManager.nextChapter())
                    MangaManager.setCurrentPageNum(0)
            }
        } else {
            //swiped from right to left
            var i = MangaManager.getCurrentPageNum()
            if(i > 0) {
                MangaManager.setCurrentPageNum(i-1)
            } else {
                // Returns true if successful (has a previous chapter)
                if (MangaManager.previousChapter())
                    MangaManager.setCurrentPageNum(MangaManager.getNumPages() - 1)
            }
        }
        updateImage()
        return true
    }

    override fun onDown(event: MotionEvent) = true
}
