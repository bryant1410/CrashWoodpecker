package me.drakeet.library;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import java.util.List;

/**
 * @author drakeet
 */
class Checks {

    private static final String PROCESS = ":woodpecker";


    static boolean isWoodpeckerRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
            Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infoList = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : infoList) {
            Log.e("~~~processName", info.processName);
            if (info.processName.endsWith(PROCESS)) {
                Log.e("~~~isWoodpeckerRunning", "true");
                return true;
            } else {
                Log.e("~~~isWoodpeckerRunning", "false");
            }
        }
        return false;
    }
}
