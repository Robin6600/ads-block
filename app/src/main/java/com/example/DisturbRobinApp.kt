package com.example

import android.app.Application
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class DisturbRobinApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("DisturbRobinApp", "Uncaught exception in thread ${thread.name}", throwable)
            try {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                throwable.printStackTrace(pw)
                File(filesDir, "crash.log").writeText(sw.toString())
            } catch (e: Exception) {}
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
