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

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Switch
import me.drakeet.library.CrashWoodpecker
import me.drakeet.library.PatchMode
import java.io.IOException

/**
 * Created by drakeet on 8/31/15.
 */
class MainActivity : AppCompatActivity() {

    val KEY_IS_DIALOG_MODE = "key_is_log_mode";

    override fun onCreate(savedInstanceState: Bundle?) {
        throw RuntimeException()

        super.onCreate(savedInstanceState)

        val preferences = getSharedPreferences("CrashWoodpecker-Sample", Context.MODE_PRIVATE)
        val isDialogMode = preferences.getBoolean(KEY_IS_DIALOG_MODE, false)

        setContentView(R.layout.activity_main)

        findViewById(R.id.button_crash_1)!!.setOnClickListener { view ->
            throw IOException("=.=")
        }

        findViewById(R.id.button_crash_2)!!.setOnClickListener { view ->
            Thread({ throw Exception("from a thread ~.~") }).start()
        }

        val patchModeSwitch = findViewById(R.id.patch_mode) as Switch
        switchPatchMode(patchModeSwitch, isDialogMode);
        patchModeSwitch.setOnCheckedChangeListener { compoundButton, isChecked ->
            switchPatchMode(patchModeSwitch, isChecked)
            preferences.edit().putBoolean(KEY_IS_DIALOG_MODE, isChecked).apply()
        }
    }

    private fun switchPatchMode(patchModeSwitch: Switch, checked: Boolean) {
        if (checked != patchModeSwitch.isChecked) {
            patchModeSwitch.isChecked = checked;
        }
        if (checked) {
            CrashWoodpecker.instance().setPatchMode(PatchMode.SHOW_DIALOG_TO_OPEN_URL);
            patchModeSwitch.text = "Patch Mode: dialog mode"
        } else {
            CrashWoodpecker.instance().setPatchMode(PatchMode.SHOW_LOG_PAGE);
            patchModeSwitch.text = "Patch Mode: log mode"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }
}