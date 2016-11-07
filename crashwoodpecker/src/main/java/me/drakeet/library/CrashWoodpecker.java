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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.StringRes;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import me.drakeet.library.ui.CatchActivity;
import me.drakeet.library.ui.DialogActivity;

/**
 * Created by drakeet(http://drakeet.me)
 * Date: 8/31/15 22:35
 */
public class CrashWoodpecker implements UncaughtExceptionHandler {

    private final static String TAG = "CrashWoodpecker";

    /* Default log out time, 7 days. */
    private final static long LOG_OUT_TIME = 7 * 24 * 60 * 60 * 1000;

    /* Get DateFormatter for current locale */
    private final static DateFormat FORMATTER = DateFormat.getDateInstance();
    private static final PatchMode DEFAULT_MODE = PatchMode.SHOW_LOG_PAGE;

    private volatile UncaughtExceptionHandler originDefaultHandler;
    private volatile UncaughtExceptionInterceptor interceptor;
    private volatile boolean crashing;

    private boolean passToOriginalDefaultHandler;
    private boolean forcePassToOriginalDefaultHandler;
    private Context applicationContext;
    private String version;
    /* For highlight */
    private ArrayList<String> keys;
    private PatchMode mode;
    private String patchDialogTitle;
    private String patchDialogMessage;
    private String patchDialogUrlToOpen;
    @SuppressLint("StaticFieldLeak")
    private static CrashWoodpecker instance;


    private CrashWoodpecker() {
        this.passToOriginalDefaultHandler = false;
        this.crashing = false;
        this.keys = new ArrayList<>();
        this.mode = DEFAULT_MODE;
    }


    public static CrashWoodpecker instance() {
        if (instance == null) {
            instance = new CrashWoodpecker();
        }
        return instance;
    }


    public void flyTo(Context context) {
        this.applicationContext = context.getApplicationContext();
        initContextResources();
        turnOnHandler();
    }


    public UncaughtExceptionHandler getHandler(Context context) {
        this.applicationContext = context.getApplicationContext();
        initContextResources();
        return this;
    }


    private void initContextResources() {
        this.keys.add(this.applicationContext.getPackageName());
        try {
            PackageInfo info = applicationContext.getPackageManager()
                .getPackageInfo(applicationContext.getPackageName(), 0);
            version = info.versionName + "(" + info.versionCode + ")";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void turnOnHandler() {
        UncaughtExceptionHandler originDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        if (this != originDefaultHandler) {
            this.originDefaultHandler = originDefaultHandler;
            Thread.setDefaultUncaughtExceptionHandler(this);
        }
    }


    public CrashWoodpecker setPassToOriginalDefaultHandler(boolean passToOriginalDefaultHandler) {
        this.passToOriginalDefaultHandler = passToOriginalDefaultHandler;
        return this;
    }


    private boolean handleException(Throwable throwable) {
        boolean success = saveToFile(throwable);
        try {
            if (mode == PatchMode.SHOW_LOG_PAGE) {
                startCatchActivity(throwable);
            } else if (mode == PatchMode.SHOW_DIALOG_TO_OPEN_URL) {
                showPatchDialog();
            }
        } catch (Exception e) {
            success = false;
        }
        return success;
    }


    private void showPatchDialog() {
        Intent intent = DialogActivity.newIntent(
            applicationContext,
            getApplicationName(applicationContext),
            patchDialogMessage,
            patchDialogUrlToOpen);
        applicationContext.startActivity(intent);
    }


    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        // Don't re-enter, avoid infinite loops if crash-handler crashes.
        if (crashing) {
            return;
        }
        crashing = true;

        final UncaughtExceptionInterceptor interceptor = this.interceptor;
        // Pass it to interceptor's before method.
        if (interceptor != null && interceptor.onBeforeHandlingException(thread, throwable)) {
            return;
        }

        boolean success = handleException(throwable);

        // Pass it to interceptor's after method.
        if (interceptor != null && interceptor.onAfterHandlingException(thread, throwable)) {
            return;
        }

        if (passToOriginalDefaultHandler || !success) {
            if (originDefaultHandler != null) {
                originDefaultHandler.uncaughtException(thread, throwable);
            }
        }
        byeByeLittleWood();
    }


    /**
     * For setting more highlight keys except package name
     *
     * @param keys highlight keys except package name
     * @return itself
     */
    public CrashWoodpecker withKeys(final String... keys) {
        this.keys.addAll(Arrays.asList(keys));
        return this;
    }


    /**
     * Set uncaught exception interceptor.
     *
     * @param interceptor uncaught exception interceptor.
     * @return itself
     */
    public CrashWoodpecker setInterceptor(UncaughtExceptionInterceptor interceptor) {
        this.interceptor = interceptor;
        return this;
    }


    private void byeByeLittleWood() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }


    /**
     * Delete outmoded logs.
     */
    public void deleteLogs() {
        deleteLogs(LOG_OUT_TIME);
    }


    private void startCatchActivity(Throwable throwable) {
        String traces = getStackTrace(throwable);
        Intent intent = new Intent();
        intent.setClass(applicationContext, CatchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String[] strings = traces.split("\n");
        String[] logs = new String[strings.length];
        for (int i = 0; i < strings.length; i++) {
            logs[i] = strings[i].trim();
        }
        intent.putStringArrayListExtra(CatchActivity.EXTRA_HIGHLIGHT_KEYS, keys);
        intent.putExtra(CatchActivity.EXTRA_APPLICATION_NAME,
            getApplicationName(applicationContext));
        intent.putExtra(CatchActivity.EXTRA_CRASH_LOGS, logs);
        intent.putExtra(CatchActivity.EXTRA_CRASH_4_LOGCAT, Log.getStackTraceString(throwable));
        applicationContext.startActivity(intent);
    }


    private String getStackTrace(Throwable throwable) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        printWriter.close();
        return writer.toString();
    }


    public CrashWoodpecker setPatchMode(PatchMode mode) {
        this.mode = mode;
        return this;
    }


    public CrashWoodpecker setPatchDialogMessage(String message) {
        this.patchDialogMessage = message;
        return this;
    }


    public CrashWoodpecker setPatchDialogMessage(@StringRes int messageResId) {
        this.patchDialogMessage = applicationContext.getString(messageResId);
        return this;
    }


    public CrashWoodpecker setPatchDialogUrlToOpen(String url) {
        this.patchDialogUrlToOpen = url;
        return this;
    }


    private boolean saveToFile(Throwable throwable) {
        String time = FORMATTER.format(new Date());
        String fileName = "Crash-" + time + ".log";
        String crashDir = getCrashDir();
        String crashPath = crashDir + fileName;

        String androidVersion = Build.VERSION.RELEASE;
        String deviceModel = Build.MODEL;
        String manufacturer = Build.MANUFACTURER;

        File file = new File(crashPath);
        if (file.exists()) {
            file.delete();
        } else {
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
        if (version != null) writer.write("App Version: " + version + "\n");
        writer.write("---------------------\n\n");
        throwable.printStackTrace(writer);
        writer.close();

        return true;
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
                    File file = new File(dir, filename);
                    return currTime - file.lastModified() > timeout;
                }
            });
            if (files != null) {
                for (File file : files) {
                    FileUtils.delete(file);
                }
            }
        } catch (Exception e) {
            Log.v(TAG, "Exception occurs when deleting outmoded logs", e);
        }
    }


    private String getCrashDir() {
        String rootPath = Environment.getExternalStorageDirectory().getPath();
        return rootPath + "/CrashWoodpecker/";
    }


    public String getApplicationName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        String name = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(
                context.getApplicationInfo().packageName, 0);
            name = (String) packageManager.getApplicationLabel(applicationInfo);
        } catch (final PackageManager.NameNotFoundException e) {
            String[] packages = context.getPackageName().split(".");
            name = packages[packages.length - 1];
        }
        return name;
    }
}
