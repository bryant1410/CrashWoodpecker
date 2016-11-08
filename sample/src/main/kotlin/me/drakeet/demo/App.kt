/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 drakeet (drakeet.me@gmail.com)
 * http://drakeet.me
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.drakeet.demo

import android.app.Application
import android.util.Log
import me.drakeet.library.CrashWoodpecker
import me.drakeet.library.PatchMode

/**
 * Created by drakeet on 8/31/15.
 */
class App : Application() {

    public var patchMode = PatchMode.SHOW_LOG_PAGE

    override fun onCreate() {
        super.onCreate()
        val patchMode = if (BuildConfig.DEBUG) {
            PatchMode.SHOW_LOG_PAGE
        } else {
            PatchMode.SHOW_DIALOG_TO_OPEN_URL
        }
        setupOriginalHandler()
        CrashWoodpecker.instance()
            .withKeys("widget", "me.drakeet")
            .setPatchMode(PatchMode.SHOW_DIALOG_TO_OPEN_URL)    // TODO
            .setPatchDialogUrlToOpen("https://drakeet.me")
            .setPassToOriginalDefaultHandler(true)
            .flyTo(this)
    }

    private fun setupOriginalHandler() {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("CrashSample", "Default-UncaughtExceptionHandler", throwable)
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(0)
        }
    }
}
