package com.alamin7ah.xosnext

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class AppAdapter(
    private var apps: List<AppModel>,
    private val onAppClick: (AppModel) -> Unit,
    private val onAppLongHold: (AppModel, View) -> Unit
) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val label: TextView = view.findViewById(R.id.appLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.label.text = app.name
        holder.icon.setImageDrawable(app.icon)

        val handler = Handler(Looper.getMainLooper())
        var isLongTriggered = false
        var downX = 0f
        var downY = 0f

        val longPressRunnable = Runnable {
            isLongTriggered = true
            holder.itemView.animate().scaleX(1.12f).scaleY(1.12f).setDuration(150).start()
            onAppLongHold(app, holder.itemView)
        }

        holder.itemView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isLongTriggered = false
                    downX = event.x
                    downY = event.y
                    // ঠিক ১.২ সেকেন্ড (1200ms) চেপে ধরলে তবেই অ্যাক্টিভ হবে
                    handler.postDelayed(longPressRunnable, 1200L)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (abs(event.x - downX) > 20 || abs(event.y - downY) > 20) {
                        handler.removeCallbacks(longPressRunnable)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    handler.removeCallbacks(longPressRunnable)
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                    if (!isLongTriggered && abs(event.x - downX) < 20 && abs(event.y - downY) < 20) {
                        onAppClick(app)
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacks(longPressRunnable)
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
            true
        }
    }

    override fun getItemCount() = apps.size

    fun updateList(newList: List<AppModel>) {
        apps = newList
        notifyDataSetChanged()
    }
}
