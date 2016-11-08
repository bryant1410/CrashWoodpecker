package me.drakeet.library;

import android.app.Activity;

/**
 * @author drakeet
 */
public class WoodpeckerBaseActivity extends Activity {

    @Override final protected void onDestroy() {
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
