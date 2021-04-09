/* Copyright (C) 2017 M. Steve Todd mstevetodd@gmail.com

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package jmri.enginedriver;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

import java.util.HashMap;

public class about_page extends AppCompatActivity {

    private threaded_application mainapp; // hold pointer to mainapp
    private Menu AMenu;

    private Toolbar toolbar;

    /**
     * Called when the activity is first created.
     */
    @SuppressLint("DefaultLocale")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainapp = (threaded_application) this.getApplication();

        mainapp.applyTheme(this);

        setContentView(R.layout.about_page);

        // format and show version info
        TextView v = findViewById(R.id.about_info);
        String s;
        // ED version info
        s = "Engine Driver: " + mainapp.appVersion;
        if (mainapp.host_ip != null) {
            // WiT info
            if (mainapp.withrottle_version != 0.0) {
                s += "\nWiThrottle: v" + mainapp.withrottle_version;
                s += String.format("    Heartbeat: %dms", mainapp.heartbeatInterval);
            }
            s += String.format("\nHost: %s", mainapp.host_ip);
            //show server type and description if set
            String sServer;
            if (mainapp.getServerDescription().contains(mainapp.getServerType())) {
                sServer = mainapp.getServerDescription();
            } else {
                sServer = mainapp.getServerType() + " " + mainapp.getServerDescription();
            }
            if (!sServer.isEmpty()) {
                s += String.format("\nServer: %s", sServer);
            } else {
                // otherwise show JMRI version info from web if populated
                HashMap<String, String> JmriMetadata = threaded_application.jmriMetadata;
                if (JmriMetadata != null && JmriMetadata.size() > 0) {
                    s += "\nJMRI v" + JmriMetadata.get("JMRIVERCANON") + "    build: " + JmriMetadata.get("JMRIVERSION");
                    if (JmriMetadata.get("activeProfile") != null) {
                        s += "\nActive Profile: " + JmriMetadata.get("activeProfile");
                    }
                }
            }
        }
        s += String.format("\nSSID: %s Net: %s ", mainapp.client_ssid, mainapp.client_type);
        if (mainapp.client_address_inet4 != null) {
            s += String.format("IP: %s", mainapp.client_address_inet4.toString().replaceAll("/", ""));
        }
        s += String.format("\nOS: %s, SDK: %s ", android.os.Build.VERSION.RELEASE, Build.VERSION.SDK_INT);

        // show info
        v.setText(s);

        // show ED webpage
        WebView webview = findViewById(R.id.about_webview);
        webview.loadUrl(getApplicationContext().getResources().getString(R.string.about_page_url));

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar.showOverflowMenu();
            setToolbarTitle(getApplicationContext().getResources().getString(R.string.app_name_about)
                    + "\n" + getApplicationContext().getResources().getString(R.string.app_name)
                    , "");
        }

    } //end onCreate

    @Override
    public void onResume() {
        super.onResume();
        if (mainapp.isForcingFinish()) {        //expedite
            this.finish();
            return;
        }
        mainapp.setActivityOrientation(this);  //set screen orientation based on prefs

        if (AMenu != null) {
            mainapp.displayEStop(AMenu);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.about_menu, menu);
        AMenu = menu;
        mainapp.displayEStop(menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle all of the possible menu actions.
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case R.id.EmerStop:
                mainapp.sendEStopMsg();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Handle pressing of the back button to end this activity
    @Override
    public boolean onKeyDown(int key, KeyEvent event) {
        if (key == KeyEvent.KEYCODE_BACK) {
            this.finish();  //end this activity
            connection_activity.overridePendingTransition(this, R.anim.fade_in, R.anim.fade_out);
            return true;
        }
        return (super.onKeyDown(key, event));
    }

    private void setToolbarTitle(String title, String clockText) {
        if (toolbar != null) {
            toolbar.setTitle("");
            TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
            mTitle.setText(title);
            TextView mClock = (TextView) toolbar.findViewById(R.id.toolbar_clock);
            mClock.setText(clockText);
        }
    }
}
