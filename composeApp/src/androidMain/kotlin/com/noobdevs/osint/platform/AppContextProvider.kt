package com.noobdevs.osint.platform

import android.content.Context

object AppContextProvider {
    lateinit var context: Context

    fun init(appContext: Context) {
        context = appContext.applicationContext
    }
}
