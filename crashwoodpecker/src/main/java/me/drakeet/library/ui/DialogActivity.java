package me.drakeet.library.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class DialogActivity extends Activity {

    private static final String EXTRA_TITLE = "extra_title";
    private static final String EXTRA_MESSAGE = "extra_message";
    private static final String EXTRA_URL = "extra_url";

    private String title, message, url;


    public static Intent newIntent(Context context, String title, String message, String url) {
        Intent intent = new Intent();
        intent.setClass(context, DialogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_MESSAGE, message);
        intent.putExtra(EXTRA_URL, url);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title = getIntent().getStringExtra(EXTRA_TITLE);
        message = getIntent().getStringExtra(EXTRA_MESSAGE);
        url = getIntent().getStringExtra(EXTRA_URL);
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    dialog.dismiss();
                    finish();
                }
            }).show();
    }
}
