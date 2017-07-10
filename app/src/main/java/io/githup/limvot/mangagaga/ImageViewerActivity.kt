package io.githup.limvot.mangagaga

import org.jetbrains.anko.*

import android.os.Bundle
import android.app.Activity
import android.widget.ImageView
import android.view.GestureDetector
import android.view.MotionEvent;
import android.graphics.BitmapFactory;

class ImageViewerActivity : Activity(), GenericLogger, GestureDetector.OnGestureListener {
    var image : ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getActionBar().hide()
        frameLayout { image = imageView() }
        val detector = GestureDetector(this, this)
        image!!.setOnTouchListener({ view, motionEvent -> detector.onTouchEvent(motionEvent);true})
        updateImage()
    }
    fun updateImage() {
        doAsync {
            val bm = BitmapFactory.decodeFile(MangaManager.getCurrentPage())
            uiThread { image!!.setImageBitmap(bm) }
        }
    }
    fun move(forwards: Boolean): Boolean {
        val i = MangaManager.getCurrentPageNum()
        if(forwards) {
            if(i < MangaManager.getNumPages()-1) {
                MangaManager.setCurrentPageNum(i+1)
            } else {
                MangaManager.nextChapter()
                MangaManager.setCurrentPageNum(0)
            }
        } else {
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
    override fun onDown(event: MotionEvent) = true
    override fun onScroll(e1: MotionEvent, e2:MotionEvent, dx: Float, y: Float) = true
    override fun onSingleTapUp(event: MotionEvent) = move(event.getX() < 350)
    override fun onFling(m1: MotionEvent, m2: MotionEvent, vx: Float, vy: Float) = move(vx > 0)
}
