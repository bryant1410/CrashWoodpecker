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

package me.drakeet.library;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import me.drakeet.library.ui.CatchActivity;

/**
 * Created by drakeet(http://drakeet.me)
 * Date: 8/31/15 22:35
 */
public class CrashWoodpecker implements UncaughtExceptionHandler {

    private final static String TAG = "CrashWoodpecker";

    // Default log out time, 7days.
    private final static long LOG_OUT_TIME = TimeUnit.DAYS.toMillis(7);

    // get DateFormatter for current locale
    private final static DateFormat mFormatter = DateFormat.getDateInstance();

    private volatile UncaughtExceptionHandler mOriginHandler;
    private volatile UncaughtExceptionInterceptor mInterceptor;
    private volatile boolean mCrashing = false;

    private boolean mForceHandleByOrigin = false;
    private final Context mContext;
    private final String mVersion;

    /**
     * Install CrashWoodpecker.
     *
     * @param application to capture exceptions for.
     * @param forceHandleByOrigin whether to force original
     * UncaughtExceptionHandler handle again,
     * by default false.
     * @return CrashWoodpecker instance.
     */
    public static CrashWoodpecker init(Application application, boolean forceHandleByOrigin) {
        return new CrashWoodpecker(application, forceHandleByOrigin);
    }

    /**
     * Install CrashWoodpecker.
     *
     * @param application to capture exceptions for.
     * @return CrashWoodpecker instance.
     */
    public static CrashWoodpecker init(Application application) {
        return new CrashWoodpecker(application, false);
    }

    private CrashWoodpecker(Context context, boolean forceHandleByOrigin) {
        mContext = context;
        mForceHandleByOrigin = forceHandleByOrigin;


        try {
            PackageInfo info = context.getPackageManager()
                .getPackageInfo(context.getPackageName(), 0);
            mVersion = info.versionName + "(" + info.versionCode + ")";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        UncaughtExceptionHandler originHandler = Thread.currentThread().getUncaughtExceptionHandler();
        // check to prevent set again
        if (this != originHandler) {
            mOriginHandler = originHandler;
            Thread.currentThread().setUncaughtExceptionHandler(this);
            Thread.setDefaultUncaughtExceptionHandler(this);
        }
    }

    private boolean handleException(Throwable throwable) {
        boolean success = saveToFile(throwable);
        try {
            startCatchActivity(throwable);
            byeByeLittleWood();
        } catch (Exception e) {
            success = false;
        }
        return success;
    }


    private void byeByeLittleWood() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }


    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        // Don't re-enter,  avoid infinite loops if crash-handler crashes.
        if (mCrashing) {
            return;
        }
        mCrashing = true;

        // pass it to interceptor's before method
        UncaughtExceptionInterceptor interceptor = mInterceptor;
        if (interceptor != null &&
                interceptor.onInterceptExceptionBefore(thread, throwable)) {
            return;
        }

        boolean isHandle = handleException(throwable);

        // pass it to interceptor's after method
        if (interceptor != null &&
                interceptor.onInterceptExceptionAfter(thread, throwable)) {
            return;
        }

        if ((mForceHandleByOrigin || !isHandle) && mOriginHandler != null) {
            mOriginHandler.uncaughtException(thread, throwable);
        }
    }


    /**
     * Set uncaught exception interceptor.
     *
     * @param interceptor uncaught exception interceptor.
     */
    public void setInterceptor(UncaughtExceptionInterceptor interceptor) {
        mInterceptor = interceptor;
    }


    /**
     * Delete outmoded logs.
     */
    public void deleteLogs() {
        deleteLogs(LOG_OUT_TIME);
    }


    /**
     * Delete outmoded logs.
     *
     * @param timeout outmoded timeout.
     */
    public void deleteLogs(final long timeout) {
        final File logDir = new File(getCrashDir());
        try {
            final long currTime = System.currentTimeMillis();
            File[] files = logDir.listFiles(new FilenameFilter() {
                @Override public boolean accept(File dir, String filename) {
                    File f = new File(dir, filename);
                    return currTime - f.lastModified() > timeout;
                }
            });
            if (files != null) {
                for (File f : files) {
                    FileUtils.delete(f);
                }
            }
        } catch (Exception e) {
            Log.v(TAG, "exception occurs when deleting outmoded logs", e);
        }
    }


    private String getCrashDir() {
        String rootPath = Environment.getExternalStorageDirectory().getPath();
        return rootPath + "/CrashWoodpecker/";
    }


    private void startCatchActivity(Throwable throwable) {
        String traces = getStackTrace(throwable);
        Intent intent = new Intent();
        intent.setClass(mContext, CatchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String[] strings = traces.split("\n");
        String[] logs = new String[strings.length];
        for (int i = 0; i < strings.length; i++) {
            logs[i] = strings[i].trim();
        }
        intent.putExtra(CatchActivity.EXTRA_PACKAGE, mContext.getPackageName());
        intent.putExtra(CatchActivity.EXTRA_CRASH_LOGS, logs);
        intent.putExtra(CatchActivity.EXTRA_CRASH_4_LOGCAT, Log.getStackTraceString(throwable));
        mContext.startActivity(intent);
    }


    private String getStackTrace(Throwable throwable) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        printWriter.close();
        return writer.toString();
    }


    private boolean saveToFile(Throwable throwable) {
        String time = mFormatter.format(new Date());
        String fileName = "Crash-" + time + ".log";
        String crashDir = getCrashDir();
        String crashPath = crashDir + fileName;

        String androidVersion = Build.VERSION.RELEASE;
        String deviceModel = Build.MODEL;
        String manufacturer = Build.MANUFACTURER;

        File file = new File(crashPath);
        if (file.exists()) {
            file.delete();
        }
        else {
            try {
                new File(crashDir).mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                return false;
            }
        }

        PrintWriter writer;
        try {
            writer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            return false;
        }
        writer.write("Device: " + manufacturer + ", " + deviceModel + "\n");
        writer.write("Android Version: " + androidVersion + "\n");
        if (mVersion != null) writer.write("App Version: " + mVersion + "\n");
        writer.write("---------------------\n\n");
        throwable.printStackTrace(writer);
        writer.close();

        return true;
    }


    public interface UncaughtExceptionInterceptor {
        /**
         * Called before this uncaught exception be handled by {@link
         * CrashWoodpecker}.
         *
         * @return true if intercepted, which means this event won't be handled
         * by {@link CrashWoodpecker}.
         */
        boolean onInterceptExceptionBefore(Thread t, Throwable ex);

        /**
         * Called after this uncaught exception be handled by
         * {@link CrashWoodpecker} (but before {@link CrashWoodpecker}'s
         * parent).
         *
         * @return true if intercepted, which means this event won't be handled
         * by {@link CrashWoodpecker}'s parent.
         */
        boolean onInterceptExceptionAfter(Thread t, Throwable ex);
    }
}
