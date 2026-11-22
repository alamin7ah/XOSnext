package com.alamin7ah.xosnext

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppRepository {
    suspend fun getInstalledApps(context: Context): List<AppModel> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        resolveInfos.mapNotNull { info ->
            val pkg = info.activityInfo.packageName
            if (pkg == context.packageName) return@mapNotNull null
            AppModel(
                name = info.loadLabel(pm).toString(),
                packageName = pkg,
                icon = info.loadIcon(pm)
            )
        }.sortedBy { it.name.lowercase() }
    }
}
