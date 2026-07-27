package com.miniadbnotify;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int REQUEST_PERMISSIONS = 1;

    private EditText timeoutInput;
    private RadioGroup langGroup;
    private RadioButton langPt;
    private RadioButton langEn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(NotifyReceiver.PREF_NAME, MODE_PRIVATE);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        TextView text = new TextView(this);
        text.setText("ADB Notify\n\nUse /system/bin/am broadcast to show notifications.\nReplies are saved to /sdcard/adb-notify/reply.txt.");
        layout.addView(text, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Button permissionButton = new Button(this);
        permissionButton.setText("Open Notification Settings");
        permissionButton.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivity(intent);
        });
        layout.addView(permissionButton, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Button appInfoButton = new Button(this);
        appInfoButton.setText("Open App Info");
        appInfoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        });
        layout.addView(appInfoButton, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView settingsHeader = new TextView(this);
        settingsHeader.setText("Settings");
        settingsHeader.setTextSize(18);
        settingsHeader.setPadding(0, pad, 0, pad / 2);
        layout.addView(settingsHeader, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView timeoutLabel = new TextView(this);
        timeoutLabel.setText("Timeout (seconds):");
        layout.addView(timeoutLabel, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        timeoutInput = new EditText(this);
        timeoutInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        timeoutInput.setText(String.valueOf(prefs.getInt("timeout", 30)));
        layout.addView(timeoutInput, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView langLabel = new TextView(this);
        langLabel.setText("Language:");
        layout.addView(langLabel, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        langGroup = new RadioGroup(this);
        langGroup.setOrientation(LinearLayout.HORIZONTAL);
        langPt = new RadioButton(this);
        langPt.setText("pt");
        langEn = new RadioButton(this);
        langEn.setText("en");
        langGroup.addView(langPt);
        langGroup.addView(langEn);
        String currentLang = prefs.getString("lang", "pt");
        if ("en".equals(currentLang)) {
            langEn.setChecked(true);
        } else {
            langPt.setChecked(true);
        }
        layout.addView(langGroup, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Button saveButton = new Button(this);
        saveButton.setText("Save Settings");
        saveButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            int timeout;
            try {
                timeout = Integer.parseInt(timeoutInput.getText().toString());
            } catch (NumberFormatException e) {
                timeout = 30;
            }
            editor.putInt("timeout", timeout);
            String lang = langPt.isChecked() ? "pt" : "en";
            editor.putString("lang", lang);
            editor.apply();
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
        });
        layout.addView(saveButton, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        setContentView(layout);

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_PERMISSIONS);
        } else if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
        }
    }
}
