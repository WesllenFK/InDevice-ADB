package com.indevice.adb;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class NotifyReceiver extends BroadcastReceiver {

    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_ID = "id";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_CONTENT = "content";
    public static final String EXTRA_BUTTON = "button";
    public static final String EXTRA_OUTPUT = "output";
    public static final String EXTRA_TARGET = "target";
    public static final String EXTRA_SERVICE_TYPE = "service_type";
    public static final String EXTRA_REPLY_LABEL = "reply_label";
    public static final String EXTRA_MODE_TAG = "mode_tag";
    public static final String MODE_SHOW = "show";
    public static final String MODE_CLEAR = "clear";
    public static final String MODE_DISCOVER_ADB = "discover_adb";
    public static final String MODE_DISCOVER_AND_PAIR = "discover_and_pair";
    public static final String MODE_TAG_PAIR = "pair";
    public static final String CHANNEL_ID = "adb-notify";
    public static final String DEFAULT_OUTPUT = "/sdcard/Documents/adb-notify/reply.txt";
    public static final String DEFAULT_DISCOVERED_OUTPUT = "/sdcard/Documents/adb-notify/adb-discovered.txt";
    public static final String LAST_OUTPUT = "/sdcard/Documents/adb-notify/reply-last.txt";
    public static final String ERROR_FILE = "/sdcard/Documents/adb-notify/error.txt";
    public static final String KEY_TEXT_REPLY = "ADB_NOTIFY_REPLY";
    public static final String EXTRA_JSON = "json";
    public static final String PREF_NAME = "adb_notify_prefs";
    public static final String PAIR_CACHE_FILE = "/sdcard/Documents/adb-notify/pair-cache.txt";

    private static final int NOTIFY_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        ensureChannel(context);
        String mode = value(intent, EXTRA_MODE, MODE_SHOW);
        debug(context, "onReceive mode=" + mode + " id=" + value(intent, EXTRA_ID, "default"));
        if (MODE_CLEAR.equals(mode)) return;
        if (MODE_DISCOVER_ADB.equals(mode)) { discoverAdb(context, intent); return; }
        if (MODE_DISCOVER_AND_PAIR.equals(mode)) { discoverAndPair(context, intent); return; }
        if ("ping".equals(mode)) {
            if (!writeExternalFile(context, "ping.txt", "ok\n")) {
                updateNotification(context, "InDevice ADB", localized(context, "Sem permissao de escrita", "Write permission denied", "Permiso de escritura denegado", "无写入权限", "Нет разрешения на запись"), null, null, true);
            } else {
                updateNotification(context, "InDevice ADB", localized(context, "Online", "Online", "En línea", "在线", "Онлайн"), null, null, false);
            }
            return;
        }
        showReplyNotification(context,
            value(intent, EXTRA_ID, "default"),
            value(intent, EXTRA_TITLE, "InDevice ADB"),
            value(intent, EXTRA_CONTENT, ""),
            value(intent, EXTRA_BUTTON, "Reply"),
            value(intent, EXTRA_OUTPUT, DEFAULT_OUTPUT),
            null);
    }

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "InDevice ADB", NotificationManager.IMPORTANCE_HIGH));
        }
    }

    private void showReplyNotification(Context context, String id, String title, String content, String button, String output, String target) {
        Intent replyIntent = new Intent(context, ReplyReceiver.class)
            .putExtra(EXTRA_ID, id).putExtra(EXTRA_OUTPUT, output);
        if (target != null) replyIntent.putExtra(EXTRA_TARGET, target);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) flags |= PendingIntent.FLAG_MUTABLE;
        PendingIntent pi = PendingIntent.getBroadcast(context, id.hashCode(), replyIntent, flags);
        RemoteInput ri = new RemoteInput.Builder(KEY_TEXT_REPLY).setLabel(button).build();
        Notification.Action action = new Notification.Action.Builder(null, button, pi).addRemoteInput(ri).build();
        buildAndNotify(context, title, content, action);
        debug(context, "showReplyNotification id=" + id + " title=" + title);
    }

    private void showPairCodeNotification(Context context, String target, String output, boolean jsonMode) {
        String id = "pair-" + System.currentTimeMillis();
        Intent replyIntent = new Intent(context, ReplyReceiver.class)
            .putExtra(EXTRA_ID, id).putExtra(EXTRA_OUTPUT, output)
            .putExtra(EXTRA_TARGET, target).putExtra(EXTRA_MODE_TAG, MODE_TAG_PAIR)
            .putExtra(EXTRA_JSON, jsonMode ? "true" : "false");
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) flags |= PendingIntent.FLAG_MUTABLE;
        PendingIntent pi = PendingIntent.getBroadcast(context, id.hashCode(), replyIntent, flags);
        String inputLabel = localized(context, "Codigo de 6 digitos", "6-digit code", "Código de 6 dígitos", "6位代码", "6-значный код");
        RemoteInput ri = new RemoteInput.Builder(KEY_TEXT_REPLY).setLabel(inputLabel).build();
        String actionLabel = localized(context, "Enviar", "Send", "Enviar", "发送", "Отправить");
        Notification.Action action = new Notification.Action.Builder(null, actionLabel, pi).addRemoteInput(ri).build();
        String content = localized(context,
            "Alvo " + target + ". Digite o codigo de 6 digitos",
            "Target " + target + ". Enter the 6-digit code",
            "Objetivo " + target + ". Ingrese el código de 6 dígitos",
            "目标 " + target + "。输入6位代码",
            "Цель " + target + ". Введите 6-значный код");
        buildAndNotify(context, "InDevice ADB: Pair", content, action);
        debug(context, "showPairCodeNotification target=" + target + " output=" + output + " json=" + jsonMode);
    }

    private void buildAndNotify(Context context, String title, String content, Notification.Action action) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder b = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ? new Notification.Builder(context, CHANNEL_ID) : new Notification.Builder(context);
        b.setContentTitle(title).setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true).setShowWhen(true).addAction(action);
        manager.notify(NOTIFY_ID, b.build());
    }

    public static void updateNotification(Context context, String title, String content, String button, String output, boolean persist) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder b = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ? new Notification.Builder(context, CHANNEL_ID) : new Notification.Builder(context);
        b.setContentTitle(title).setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setShowWhen(true).setAutoCancel(!persist);
        if (button != null && output != null) {
            Intent replyIntent = new Intent(context, ReplyReceiver.class)
                .putExtra(EXTRA_ID, "update-" + System.currentTimeMillis())
                .putExtra(EXTRA_OUTPUT, output);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) flags |= PendingIntent.FLAG_MUTABLE;
            PendingIntent pi = PendingIntent.getBroadcast(context, 1002, replyIntent, flags);
            RemoteInput ri = new RemoteInput.Builder(KEY_TEXT_REPLY).setLabel(button).build();
            b.addAction(new Notification.Action.Builder(null, button, pi).addRemoteInput(ri).build());
        }
        manager.notify(NOTIFY_ID, b.build());
        debug(context, "updateNotification title=" + title + " content=" + content + " persist=" + persist);
    }

    private void discoverAndPair(Context context, Intent intent) {
        String output = value(intent, EXTRA_OUTPUT, DEFAULT_OUTPUT);
        debug(context, "discoverAndPair starting output=" + output);
        new Thread(() -> {
            try {
                NsdManager nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
                if (nsdManager == null) { debug(context, "discoverAndPair nsdManager is null"); return; }
                CountDownLatch latch = new CountDownLatch(1);
                AtomicReference<String> result = new AtomicReference<>();
                NsdManager.ResolveListener resolveListener = new NsdManager.ResolveListener() {
                    @Override public void onServiceResolved(NsdServiceInfo resolved) {
                        String host = resolved.getHost().getHostAddress();
                        int port = resolved.getPort();
                        result.set(host + ":" + port);
                        debug(context, "discoverAndPair resolved: " + host + ":" + port);
                        latch.countDown();
                    }
                    @Override public void onResolveFailed(NsdServiceInfo si, int ec) {
                        debug(context, "discoverAndPair resolve failed code=" + ec);
                        latch.countDown();
                    }
                };
                boolean[] resolving = {false};
                NsdManager.DiscoveryListener dl = new NsdManager.DiscoveryListener() {
                    @Override public void onDiscoveryStarted(String regType) { debug(context, "discoverAndPair discovery started"); }
                    @Override public void onServiceFound(NsdServiceInfo info) {
                        debug(context, "discoverAndPair service found: " + info.getServiceName());
                        if (!resolving[0]) { resolving[0] = true; nsdManager.resolveService(info, resolveListener); }
                    }
                    @Override public void onServiceLost(NsdServiceInfo info) { debug(context, "discoverAndPair service lost"); }
                    @Override public void onDiscoveryStopped(String regType) { debug(context, "discoverAndPair discovery stopped"); }
                    @Override public void onStartDiscoveryFailed(String regType, int ec) { debug(context, "discoverAndPair start failed code=" + ec); latch.countDown(); }
                    @Override public void onStopDiscoveryFailed(String regType, int ec) {}
                };
                nsdManager.discoverServices("_adb-tls-pairing._tcp", NsdManager.PROTOCOL_DNS_SD, dl);
                boolean found = latch.await(getTimeout(context), TimeUnit.SECONDS);
                nsdManager.stopServiceDiscovery(dl);
                if (found) {
                    String target = result.get();
                    if (target != null) showPairCodeNotification(context, target, output, isJsonMode(intent));
                } else {
                    debug(context, "discoverAndPair timed out");
                    updateNotification(context, "InDevice ADB",
                        localized(context, "Nenhum dispositivo encontrado", "No device found", "Ningún dispositivo encontrado", "未找到设备", "Устройство не найдено"),
                        null, null, false);
                }
            } catch (Exception e) { debug(context, "discoverAndPair error: " + e.getMessage()); }
        }).start();
    }

    private void discoverAdb(Context context, Intent intent) {
        String serviceType = value(intent, EXTRA_SERVICE_TYPE, "pairing");
        String outputPath = value(intent, EXTRA_OUTPUT, DEFAULT_DISCOVERED_OUTPUT);
        String mdnsService = "pairing".equals(serviceType) ? "_adb-tls-pairing._tcp" : "_adb-tls-connect._tcp";
        debug(context, "discoverAdb starting type=" + mdnsService + " output=" + outputPath);
        new Thread(() -> {
            try {
                NsdManager nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
                if (nsdManager == null) { debug(context, "discoverAdb nsdManager is null"); return; }
                CountDownLatch latch = new CountDownLatch(1);
                AtomicReference<String> result = new AtomicReference<>();
                NsdManager.ResolveListener resolveListener = new NsdManager.ResolveListener() {
                    @Override public void onServiceResolved(NsdServiceInfo resolved) {
                        String host = resolved.getHost().getHostAddress();
                        int port = resolved.getPort();
                        result.set(host + ":" + port);
                        debug(context, "discoverAdb resolved: " + host + ":" + port);
                        latch.countDown();
                    }
                    @Override public void onResolveFailed(NsdServiceInfo si, int ec) {
                        debug(context, "discoverAdb resolve failed code=" + ec);
                        latch.countDown();
                    }
                };
                boolean[] resolving = {false};
                NsdManager.DiscoveryListener dl = new NsdManager.DiscoveryListener() {
                    @Override public void onDiscoveryStarted(String regType) { debug(context, "discoverAdb discovery started"); }
                    @Override public void onServiceFound(NsdServiceInfo info) {
                        debug(context, "discoverAdb service found: " + info.getServiceName());
                        if (!resolving[0]) { resolving[0] = true; nsdManager.resolveService(info, resolveListener); }
                    }
                    @Override public void onServiceLost(NsdServiceInfo info) { debug(context, "discoverAdb service lost"); }
                    @Override public void onDiscoveryStopped(String regType) { debug(context, "discoverAdb discovery stopped"); }
                    @Override public void onStartDiscoveryFailed(String regType, int ec) { debug(context, "discoverAdb start failed code=" + ec); latch.countDown(); }
                    @Override public void onStopDiscoveryFailed(String regType, int ec) {}
                };
                nsdManager.discoverServices(mdnsService, NsdManager.PROTOCOL_DNS_SD, dl);
                boolean found = latch.await(getTimeout(context), TimeUnit.SECONDS);
                nsdManager.stopServiceDiscovery(dl);
                if (found) {
                    String r = result.get();
                    if (r != null) {
                        writeExternalFile(context, "adb-discovered.txt",
                            isJsonMode(intent) ? "{\"target\":\"" + r + "\",\"service_type\":\"" + serviceType + "\"}\n" : r + "\n");
                        debug(context, "discoverAdb wrote result: " + r);
                    }
                } else { debug(context, "discoverAdb timed out"); }
            } catch (Exception e) { debug(context, "discoverAdb error: " + e.getMessage()); }
        }).start();
    }

    static void debug(Context context, String message) {
        if (context == null) return;
        try {
            String text = "[" + System.currentTimeMillis() + "] " + message + "\n";
            File f = new File(context.getFilesDir(), "adb-notify/debug/notify.log");
            if (f.getParentFile() != null) f.getParentFile().mkdirs();
            if (f.exists() && f.length() > 65536) {
                try (FileOutputStream fos = new FileOutputStream(f, false)) { /* truncate */ }
            }
            try (FileOutputStream fos = new FileOutputStream(f, true)) {
                fos.write(text.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception ignored) {}
    }

    static boolean writeExternalFile(Context context, String fileName, String data) {
        return writeExternalFileInDir(context, "Documents/adb-notify", fileName, data);
    }

    static boolean writeExternalFileInDir(Context context, String relativePath, String fileName, String data) {
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                Uri collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL);
                context.getContentResolver().delete(collection,
                    MediaStore.Files.FileColumns.DISPLAY_NAME + " LIKE ? AND " +
                    MediaStore.Files.FileColumns.RELATIVE_PATH + "=?",
                    new String[]{fileName + "%", relativePath});
                ContentValues values = new ContentValues();
                values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.Files.FileColumns.MIME_TYPE, "text/plain");
                values.put(MediaStore.Files.FileColumns.RELATIVE_PATH, relativePath);
                Uri uri = context.getContentResolver().insert(collection, values);
                if (uri != null) {
                    try (OutputStream os = context.getContentResolver().openOutputStream(uri)) {
                        if (os != null) { os.write(data.getBytes(StandardCharsets.UTF_8)); return true; }
                    }
                }
            } else {
                File file = new File("/sdcard/" + relativePath + "/" + fileName);
                File dir = file.getParentFile();
                if (dir != null) {
                    if (dir.isDirectory()) {
                        File[] stale = dir.listFiles((d, name) -> name.startsWith(fileName));
                        if (stale != null) for (File f : stale) f.delete();
                    }
                    dir.mkdirs();
                }
                try (FileOutputStream fos = new FileOutputStream(file, false)) {
                    fos.write(data.getBytes(StandardCharsets.UTF_8));
                    return true;
                }
            }
        } catch (Exception e) {
            debug(null, "writeExternalFileInDir failed: " + relativePath + "/" + fileName + " " + e.getMessage());
        }
        writeErrorFile(context, "write_failed: " + relativePath + "/" + fileName);
        return false;
    }

    static void writeErrorFile(Context context, String message) {
        try {
            Uri collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL);
            ContentValues values = new ContentValues();
            values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, "error.txt");
            values.put(MediaStore.Files.FileColumns.MIME_TYPE, "text/plain");
            values.put(MediaStore.Files.FileColumns.RELATIVE_PATH, "Documents/adb-notify");
            Uri uri = context.getContentResolver().insert(collection, values);
            if (uri != null) {
                try (OutputStream os = context.getContentResolver().openOutputStream(uri)) {
                    if (os != null) os.write(message.getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (Exception e) {
            try {
                File fb = new File(context.getFilesDir(), "adb-notify-error.txt");
                try (FileOutputStream fos = new FileOutputStream(fb, false)) {
                    fos.write(message.getBytes(StandardCharsets.UTF_8));
                }
            } catch (Exception ignored) {}
        }
    }

    static String getAppLang() {
        String lang = java.util.Locale.getDefault().getLanguage();
        if ("pt".equals(lang) || "en".equals(lang) || "es".equals(lang) || "zh".equals(lang) || "ru".equals(lang))
            return lang;
        return "en";
    }

    static String localized(Context context, String pt, String en, String es, String zh, String ru) {
        switch (getAppLang()) {
            case "pt": return pt;
            case "es": return es;
            case "zh": return zh;
            case "ru": return ru;
            default: return en;
        }
    }

    static String localized(Context context, String pt, String en) {
        return localized(context, pt, en, en, en, en);
    }

    private static String value(Intent intent, String key, String fallback) {
        String v = intent.getStringExtra(key);
        return v == null ? fallback : v;
    }

    private static boolean isJsonMode(Intent intent) {
        return "true".equals(value(intent, EXTRA_JSON, "false"));
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static int getTimeout(Context context) {
        return getPrefs(context).getInt("timeout", 30);
    }
}
