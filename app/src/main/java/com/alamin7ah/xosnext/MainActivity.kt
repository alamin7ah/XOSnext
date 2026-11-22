package com.alamin7ah.xosnext

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var homeGrid: RecyclerView
    private lateinit var drawerGrid: RecyclerView
    private lateinit var drawerContainer: XosDrawerLayout
    private lateinit var searchBar: EditText
    private var allApps: List<AppModel> = emptyList()
    private lateinit var drawerAdapter: AppAdapter
    private lateinit var homeAdapter: AppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        homeGrid = findViewById(R.id.homeGrid)
        drawerGrid = findViewById(R.id.drawerGrid)
        drawerContainer = findViewById(R.id.drawerContainer)
        searchBar = findViewById(R.id.searchBar)

        homeGrid.layoutManager = GridLayoutManager(this, 4)
        drawerGrid.layoutManager = GridLayoutManager(this, 4)

        homeAdapter = AppAdapter(emptyList(), ::launchApp, ::showXosPopup)
        drawerAdapter = AppAdapter(emptyList(), ::launchApp, ::showXosPopup)

        homeGrid.adapter = homeAdapter
        drawerGrid.adapter = drawerAdapter

        drawerContainer.recyclerView = drawerGrid
        drawerContainer.onDrawerDismiss = { closeDrawer() }

        setupGestures()
        setupSearch()
        loadApps()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerContainer.visibility == View.VISIBLE) closeDrawer()
            }
        })
    }

    private fun loadApps() {
        lifecycleScope.launch {
            allApps = AppRepository.getInstalledApps(this@MainActivity)
            homeAdapter.updateList(allApps.take(16))
            drawerAdapter.updateList(allApps)
        }
    }

    private fun setupGestures() {
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (e1 != null && (e1.y - e2.y) > 120 && velocityY < -500) {
                    openDrawer()
                    return true
                }
                return false
            }
        })
        homeGrid.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }
    }

    private fun openDrawer() {
        drawerContainer.translationY = drawerContainer.height.toFloat().takeIf { it > 0 } ?: 1500f
        drawerContainer.visibility = View.VISIBLE
        drawerContainer.animate().translationY(0f).setDuration(250).start()
    }

    private fun closeDrawer() {
        drawerContainer.animate().translationY(drawerContainer.height.toFloat()).setDuration(220).withEndAction {
            drawerContainer.visibility = View.GONE
            searchBar.text.clear()
        }.start()
    }

    private fun setupSearch() {
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim().lowercase()
                val filtered = if (query.isEmpty()) allApps else allApps.filter { it.name.lowercase().contains(query) }
                drawerAdapter.updateList(filtered)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun launchApp(app: AppModel) {
        packageManager.getLaunchIntentForPackage(app.packageName)?.let { startActivity(it) }
    }

    private fun showXosPopup(app: AppModel, anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add("App Info")
        popup.menu.add("Uninstall")
        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "App Info" -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.packageName}"))
                    startActivity(intent)
                }
                "Uninstall" -> {
                    val intent = Intent(Intent.ACTION_DELETE, Uri.parse("package:${app.packageName}"))
                    startActivity(intent)
                }
            }
            true
        }
        popup.show()
    }
}
