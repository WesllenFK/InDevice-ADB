package com.indevice.adb;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends ComponentActivity {

    private TextView notifStatus;
    private TextView storageStatus;
    private Button settingsButton;
    private LinearLayout permissionPanel;
    private String[] neededPermissions;
    private boolean permissionsRequested = false;

    private final ActivityResultLauncher<String[]> permissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::onPermissionResult);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotifyReceiver.ensureChannel(this);
        buildUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePermissionUI();
        if (!permissionsRequested) {
            requestNeededPermissions();
        }
    }

    private void buildUI() {
        SharedPreferences prefs = getSharedPreferences(NotifyReceiver.PREF_NAME, MODE_PRIVATE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);

        // ── Header ──
        String lang = NotifyReceiver.getAppLang();
        String headerText = "InDevice ADB";
        String subtitleText = "pt".equals(lang) ? "App companheiro para pareamento ADB sem fio.\nRespostas salvas em /sdcard/Documents/adb-notify/."
            : "es".equals(lang) ? "App complementaria para emparejamiento ADB inalámbrico.\nRespuestas guardadas en /sdcard/Documents/adb-notify/."
            : "zh".equals(lang) ? "ADB无线配对配套应用。\n回复保存至 /sdcard/Documents/adb-notify/。"
            : "ru".equals(lang) ? "Вспомогательное приложение для беспроводного сопряжения ADB.\nОтветы сохраняются в /sdcard/Documents/adb-notify/."
            : "Companion app for ADB wireless pairing.\nReplies saved to /sdcard/Documents/adb-notify/.";

        TextView header = new TextView(this);
        header.setText(headerText);
        header.setTextSize(20);
        header.setPadding(0, 0, 0, pad);
        root.addView(header, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView subtitle = new TextView(this);
        subtitle.setText(subtitleText);
        subtitle.setTextSize(14);
        root.addView(subtitle, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ── Permission status panel ──
        String permTitle = "pt".equals(lang) ? "Permissões" : "es".equals(lang) ? "Permisos" : "zh".equals(lang) ? "权限" : "ru".equals(lang) ? "Разрешения" : "Permissions";
        String permBtn = "pt".equals(lang) ? "Abrir Permissões nas Configurações" : "es".equals(lang) ? "Abrir Permisos en Configuración" : "zh".equals(lang) ? "在设置中打开应用权限" : "ru".equals(lang) ? "Открыть разрешения в настройках" : "Open App Permissions in Settings";
        String notifLabel = "pt".equals(lang) ? "Notificações: " : "es".equals(lang) ? "Notificaciones: " : "zh".equals(lang) ? "通知: " : "ru".equals(lang) ? "Уведомления: " : "Notifications: ";
        String storageLabel = "pt".equals(lang) ? "Arquivos: " : "es".equals(lang) ? "Archivos: " : "zh".equals(lang) ? "文件访问: " : "ru".equals(lang) ? "Файлы: " : "File access: ";

        permissionPanel = new LinearLayout(this);
        permissionPanel.setOrientation(LinearLayout.VERTICAL);
        permissionPanel.setPadding(0, pad, 0, pad);

        TextView permHeader = new TextView(this);
        permHeader.setText(permTitle);
        permHeader.setTextSize(16);
        permissionPanel.addView(permHeader, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        notifStatus = new TextView(this);
        notifStatus.setTextSize(14);
        notifStatus.setPadding(pad / 2, pad / 2, pad / 2, pad / 2);
        permissionPanel.addView(notifStatus, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        storageStatus = new TextView(this);
        storageStatus.setTextSize(14);
        storageStatus.setPadding(pad / 2, pad / 2, pad / 2, pad / 2);
        permissionPanel.addView(storageStatus, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        settingsButton = new Button(this);
        settingsButton.setText(permBtn);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        });
        permissionPanel.addView(settingsButton, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        root.addView(permissionPanel, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ── Divider ──
        TextView divider = new TextView(this);
        divider.setText("──────────────────");
        divider.setGravity(Gravity.CENTER);
        root.addView(divider, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ── Settings ──
        String settingsTitle = "pt".equals(lang) ? "Configurações" : "es".equals(lang) ? "Configuración" : "zh".equals(lang) ? "设置" : "ru".equals(lang) ? "Настройки" : "Settings";
        String timeoutLbl = "pt".equals(lang) ? "Tempo limite de descoberta (segundos):" : "es".equals(lang) ? "Tiempo de descubrimiento (segundos):" : "zh".equals(lang) ? "发现超时（秒）:" : "ru".equals(lang) ? "Таймаут обнаружения (сек):" : "Discovery timeout (seconds):";
        String saveLbl = "pt".equals(lang) ? "Salvar" : "es".equals(lang) ? "Guardar" : "zh".equals(lang) ? "保存" : "ru".equals(lang) ? "Сохранить" : "Save";

        TextView settingsHeader = new TextView(this);
        settingsHeader.setText(settingsTitle);
        settingsHeader.setTextSize(16);
        settingsHeader.setPadding(0, pad, 0, pad / 2);
        root.addView(settingsHeader, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView timeoutLabel = new TextView(this);
        timeoutLabel.setText(timeoutLbl);
        root.addView(timeoutLabel, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        EditText timeoutInput = new EditText(this);
        timeoutInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        timeoutInput.setText(String.valueOf(prefs.getInt("timeout", 30)));
        root.addView(timeoutInput, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Button saveButton = new Button(this);
        saveButton.setText(saveLbl);
        saveButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            try {
                editor.putInt("timeout", Integer.parseInt(timeoutInput.getText().toString()));
            } catch (NumberFormatException e) {
                editor.putInt("timeout", 30);
            }
            editor.apply();
            String savedMsg = "pt".equals(lang) ? "Salvo" : "es".equals(lang) ? "Guardado" : "zh".equals(lang) ? "已保存" : "ru".equals(lang) ? "Сохранено" : "Saved";
            Toast.makeText(this, savedMsg, Toast.LENGTH_SHORT).show();
        });
        root.addView(saveButton, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        setContentView(root);
    }

    private void requestNeededPermissions() {
        ArrayList<String> needed = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 29) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        if (needed.isEmpty()) {
            permissionsRequested = true;
            return;
        }

        neededPermissions = needed.toArray(new String[0]);
        permissionLauncher.launch(neededPermissions);
    }

    private void onPermissionResult(Map<String, Boolean> results) {
        permissionsRequested = true;
        String lang = NotifyReceiver.getAppLang();
        String msg = "pt".equals(lang) ? "Permissão negada permanentemente. Ative nas Configurações."
            : "es".equals(lang) ? "Permiso denegado permanentemente. Actívalo en Configuración."
            : "zh".equals(lang) ? "权限已被永久拒绝。请在设置中启用。"
            : "ru".equals(lang) ? "Разрешение навсегда отклонено. Включите в настройках."
            : "Permission denied permanently. Enable in Settings.";
        for (Map.Entry<String, Boolean> entry : results.entrySet()) {
            if (!entry.getValue()) {
                boolean permanentlyDenied = !shouldShowRequestPermissionRationale(entry.getKey());
                if (permanentlyDenied) {
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
            }
        }
        updatePermissionUI();
    }

    private void updatePermissionUI() {
        boolean notifOk = true;
        boolean storageOk = true;
        String lang = NotifyReceiver.getAppLang();
        String notifLabel = "pt".equals(lang) ? "Notificações: " : "es".equals(lang) ? "Notificaciones: " : "zh".equals(lang) ? "通知: " : "ru".equals(lang) ? "Уведомления: " : "Notifications: ";
        String storageLabel = "pt".equals(lang) ? "Arquivos: " : "es".equals(lang) ? "Archivos: " : "zh".equals(lang) ? "文件访问: " : "ru".equals(lang) ? "Файлы: " : "File access: ";
        String granted = "pt".equals(lang) ? "✅ Concedida" : "es".equals(lang) ? "✅ Concedido" : "zh".equals(lang) ? "✅ 已授予" : "ru".equals(lang) ? "✅ Предоставлено" : "✅ Granted";
        String denied = "pt".equals(lang) ? "❌ Negada" : "es".equals(lang) ? "❌ Denegado" : "zh".equals(lang) ? "❌ 已拒绝" : "ru".equals(lang) ? "❌ Отказано" : "❌ Denied";
        String msLabel = "pt".equals(lang) ? "✅ MediaStore (sem permissão necessária)" : "es".equals(lang) ? "✅ MediaStore (sin permiso necesario)" : "zh".equals(lang) ? "✅ MediaStore（无需权限）" : "ru".equals(lang) ? "✅ MediaStore (разрешение не требуется)" : "✅ MediaStore (no permission needed)";

        if (Build.VERSION.SDK_INT >= 33) {
            notifOk = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
        }
        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 29) {
            storageOk = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
        }

        notifStatus.setText(notifLabel + (notifOk ? granted : denied));
        storageStatus.setText(storageLabel + (storageOk ? granted : denied));

        if (Build.VERSION.SDK_INT >= 29) {
            storageStatus.setText(msLabel);
        }

        boolean anyDenied = !notifOk || !storageOk;
        settingsButton.setVisibility(anyDenied ? android.view.View.VISIBLE : android.view.View.GONE);
    }
}
