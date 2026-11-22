package com.alamin7ah.xosnext

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView

class XosDrawerLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    var recyclerView: RecyclerView? = null
    var onDrawerDismiss: (() -> Unit)? = null
    private var startY = 0f
    private var isDraggingDrawer = false

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                startY = ev.y
                isDraggingDrawer = false
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = ev.y - startY
                val canScrollUp = recyclerView?.canScrollVertically(-1) ?: false
                // একমাত্র যখন লিস্ট একদম উপরে আছে (!canScrollUp) এবং নিচে টান দেওয়া হচ্ছে (dy > 25)
                if (dy > 25 && !canScrollUp) {
                    isDraggingDrawer = true
                    return true
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isDraggingDrawer) return super.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                val dy = (event.y - startY).coerceAtLeast(0f)
                translationY = dy
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (translationY > height * 0.25f) {
                    onDrawerDismiss?.invoke()
                } else {
                    animate().translationY(0f).setDuration(200).start()
                }
                isDraggingDrawer = false
            }
        }
        return true
    }
}
