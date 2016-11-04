package me.drakeet.library.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import me.drakeet.library.R;

import static me.drakeet.library.FileUtils.clearApplicationData;

public class DialogActivity extends Activity {

    private static final String EXTRA_TITLE = "extra_title";
    private static final String EXTRA_ULTIMATE_MESSAGE = "extra_ultimate_message";
    private static final String EXTRA_URL = "extra_url";

    private String title, ultimateMessage, url;
    private static final String KEY_REBOUND = "key_rebound";


    public static Intent newIntent(
        Context context, String title, String ultimateMessage, String url) {

        Intent intent = new Intent();
        intent.setClass(context, DialogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_ULTIMATE_MESSAGE, ultimateMessage);
        intent.putExtra(EXTRA_URL, url);
        return intent;
    }


    private void parseIntent(Intent intent) {
        title = intent.getStringExtra(EXTRA_TITLE);
        ultimateMessage = intent.getStringExtra(EXTRA_ULTIMATE_MESSAGE);
        url = intent.getStringExtra(EXTRA_URL);
    }


    @Override protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        parseIntent(getIntent());
        if (ultimateMessage == null) {
            ultimateMessage = getString(R.string.cw_error_message);
        }
        ultimateSolution();
    }


    private void ultimateSolution() {
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(ultimateMessage)
            .setCancelable(false)
            .setIcon(R.drawable.cw_ic_error)
            .setNegativeButton(R.string.cw_action_download, new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    dialog.dismiss();
                    finish();
                }
            })
            .setPositiveButton(R.string.cw_action_restart, new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                    clearApplicationData(getApplicationContext());
                    dialog.dismiss();
                    finish();
                    restart();
                }
            }).show();
    }


    private void restart() {
        Intent intent = getBaseContext().getPackageManager()
            .getLaunchIntentForPackage(getBaseContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    @Override public void finish() {
        overridePendingTransition(0, 0);
        super.finish();
    }
}
