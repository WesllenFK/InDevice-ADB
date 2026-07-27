package com.miniadbnotify;

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
import android.widget.Toast;

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
    public static final String DEBUG_DIR = "/sdcard/Documents/adb-notify/debug";
    public static final String DEBUG_LOG = DEBUG_DIR + "/notify.log";
    public static final String KEY_TEXT_REPLY = "ADB_NOTIFY_REPLY";
    public static final String EXTRA_JSON = "json";
    public static final String PREF_NAME = "adb_notify_prefs";
    public static final String PAIR_CACHE_FILE = "/sdcard/Documents/adb-notify/pair-cache.txt";

    @Override
    public void onReceive(Context context, Intent intent) {
        String mode = value(intent, EXTRA_MODE, MODE_SHOW);
        debug("onReceive mode=" + mode + " id=" + value(intent, EXTRA_ID, "default"));
        if (MODE_CLEAR.equals(mode)) {
            clearNotification(context, value(intent, EXTRA_ID, "default"));
            return;
        }
        if (MODE_DISCOVER_ADB.equals(mode)) {
            discoverAdb(context, intent);
            return;
        }
        if (MODE_DISCOVER_AND_PAIR.equals(mode)) {
            discoverAndPair(context, intent);
            return;
        }
        if ("ping".equals(mode)) {
            writeExternalFile(context, "ping.txt", "ok\n");
            return;
        }
        showNotification(context, intent);
    }

    private void discoverAndPair(Context context, Intent intent) {
        String output = value(intent, EXTRA_OUTPUT, DEFAULT_OUTPUT);
        debug("discoverAndPair starting output=" + output);

        new Thread(() -> {
            try {
                NsdManager nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
                if (nsdManager == null) {
                    debug("discoverAndPair nsdManager is null");
                    return;
                }
                CountDownLatch latch = new CountDownLatch(1);
                AtomicReference<String> result = new AtomicReference<>();

                NsdManager.ResolveListener resolveListener = new NsdManager.ResolveListener() {
                    @Override
                    public void onServiceResolved(NsdServiceInfo resolved) {
                        String host = resolved.getHost().getHostAddress();
                        int port = resolved.getPort();
                        String target = host + ":" + port;
                        result.set(target);
                        debug("discoverAndPair resolved: " + target);
                        latch.countDown();
                    }

                    @Override
                    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                        debug("discoverAndPair resolve failed code=" + errorCode);
                        latch.countDown();
                    }
                };

                boolean[] resolving = {false};

                NsdManager.DiscoveryListener discoveryListener = new NsdManager.DiscoveryListener() {
                    @Override
                    public void onDiscoveryStarted(String regType) {
                        debug("discoverAndPair discovery started");
                    }

                    @Override
                    public void onServiceFound(NsdServiceInfo info) {
                        debug("discoverAndPair service found: " + info.getServiceName());
                        if (!resolving[0]) {
                            resolving[0] = true;
                            nsdManager.resolveService(info, resolveListener);
                        }
                    }

                    @Override
                    public void onServiceLost(NsdServiceInfo info) {
                        debug("discoverAndPair service lost: " + info.getServiceName());
                    }

                    @Override
                    public void onDiscoveryStopped(String regType) {
                        debug("discoverAndPair discovery stopped");
                    }

                    @Override
                    public void onStartDiscoveryFailed(String regType, int errorCode) {
                        debug("discoverAndPair start failed code=" + errorCode);
                        latch.countDown();
                    }

                    @Override
                    public void onStopDiscoveryFailed(String regType, int errorCode) {
                    }
                };

                nsdManager.discoverServices("_adb-tls-pairing._tcp", NsdManager.PROTOCOL_DNS_SD, discoveryListener);

                boolean found = latch.await(getTimeout(context), TimeUnit.SECONDS);
                nsdManager.stopServiceDiscovery(discoveryListener);

                if (found) {
                    String target = result.get();
                    if (target != null) {
                        debug("discoverAndPair showing code notification for " + target);
                        showPairCodeNotification(context, target, output, isJsonMode(intent));
                    }
                } else {
                    debug("discoverAndPair timed out");
                    String id = "discover-fail-" + System.currentTimeMillis();
                    showNotification(context,
                        localized(context, "ADB Pair: falha", "ADB Pair: failed"),
                        localized(context, "Nenhum dispositivo encontrado. Ative a Depuracao sem Fio.", "No device found. Enable Wireless Debugging."),
                        localized(context, "OK", "OK"), output, id, null);
                }
            } catch (Exception e) {
                debug("discoverAndPair error: " + e.getMessage());
            }
        }).start();
    }

    private void showPairCodeNotification(Context context, String target, String output, boolean jsonMode) {
        String id = "pair-code-" + System.currentTimeMillis();
        Intent replyIntent = new Intent(context, ReplyReceiver.class)
            .putExtra(EXTRA_ID, id)
            .putExtra(EXTRA_OUTPUT, output)
            .putExtra(EXTRA_TARGET, target)
            .putExtra(EXTRA_MODE_TAG, MODE_TAG_PAIR)
            .putExtra(EXTRA_JSON, jsonMode ? "true" : "false");
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) flags |= PendingIntent.FLAG_MUTABLE;
        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(context, id.hashCode(), replyIntent, flags);

        RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel(localized(context, "Codigo de 6 digitos", "6-digit code")).build();
        Notification.Action action = new Notification.Action.Builder(null, localized(context, "Enviar", "Send"), replyPendingIntent)
            .addRemoteInput(remoteInput).build();

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "ADB Notify", NotificationManager.IMPORTANCE_HIGH));
        }

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ? new Notification.Builder(context, CHANNEL_ID)
            : new Notification.Builder(context);

        String contentFormat = localized(context, "Alvo %s. Digite o codigo de 6 digitos", "Target %s. Enter the 6-digit code");
        builder.setContentTitle(localized(context, "ADB Pair: codigo", "ADB Pair: code"))
            .setContentText(String.format(contentFormat, target))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setShowWhen(true)
            .addAction(action);

        manager.notify(id.hashCode(), builder.build());
        Toast.makeText(context, "pair code requested", Toast.LENGTH_SHORT).show();
        debug("showPairCodeNotification target=" + target + " output=" + output + " json=" + jsonMode);
    }

    private void discoverAdb(Context context, Intent intent) {
        String serviceType = value(intent, EXTRA_SERVICE_TYPE, "pairing");
        String outputPath = value(intent, EXTRA_OUTPUT, DEFAULT_DISCOVERED_OUTPUT);
        String mdnsService = "pairing".equals(serviceType)
            ? "_adb-tls-pairing._tcp"
            : "_adb-tls-connect._tcp";

        debug("discoverAdb starting type=" + mdnsService + " output=" + outputPath);

        new Thread(() -> {
            try {
                NsdManager nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
                if (nsdManager == null) {
                    debug("discoverAdb nsdManager is null");
                    return;
                }
                CountDownLatch latch = new CountDownLatch(1);
                AtomicReference<String> result = new AtomicReference<>();

                NsdManager.ResolveListener resolveListener = new NsdManager.ResolveListener() {
                    @Override
                    public void onServiceResolved(NsdServiceInfo resolved) {
                        String host = resolved.getHost().getHostAddress();
                        int port = resolved.getPort();
                        result.set(host + ":" + port);
                        debug("discoverAdb resolved: " + host + ":" + port);
                        latch.countDown();
                    }

                    @Override
                    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                        debug("discoverAdb resolve failed code=" + errorCode);
                        latch.countDown();
                    }
                };

                boolean[] resolving = {false};

                NsdManager.DiscoveryListener discoveryListener = new NsdManager.DiscoveryListener() {
                    @Override
                    public void onDiscoveryStarted(String regType) {
                        debug("discoverAdb discovery started");
                    }

                    @Override
                    public void onServiceFound(NsdServiceInfo info) {
                        debug("discoverAdb service found: " + info.getServiceName());
                        if (!resolving[0]) {
                            resolving[0] = true;
                            nsdManager.resolveService(info, resolveListener);
                        }
                    }

                    @Override
                    public void onServiceLost(NsdServiceInfo info) {
                        debug("discoverAdb service lost: " + info.getServiceName());
                    }

                    @Override
                    public void onDiscoveryStopped(String regType) {
                        debug("discoverAdb discovery stopped");
                    }

                    @Override
                    public void onStartDiscoveryFailed(String regType, int errorCode) {
                        debug("discoverAdb start failed code=" + errorCode);
                        latch.countDown();
                    }

                    @Override
                    public void onStopDiscoveryFailed(String regType, int errorCode) {
                    }
                };

                nsdManager.discoverServices(mdnsService, NsdManager.PROTOCOL_DNS_SD, discoveryListener);

                boolean found = latch.await(getTimeout(context), TimeUnit.SECONDS);
                nsdManager.stopServiceDiscovery(discoveryListener);

                if (found) {
                    String r = result.get();
                    if (r != null) {
                        if (isJsonMode(intent)) {
                            writeExternalFile(context, "adb-discovered.txt", "{\"target\":\"" + r + "\",\"service_type\":\"" + serviceType + "\"}\n");
                        } else {
                            writeExternalFile(context, "adb-discovered.txt", r + "\n");
                        }
                        debug("discoverAdb wrote result: " + r);
                    }
                } else {
                    debug("discoverAdb timed out, no service found");
                }
            } catch (Exception e) {
                debug("discoverAdb error: " + e.getMessage());
            }
        }).start();
    }

    private void showNotification(Context context, Intent intent) {
        String title = value(intent, EXTRA_TITLE, "ADB Notify");
        String content = value(intent, EXTRA_CONTENT, "");
        String button = value(intent, EXTRA_BUTTON, "Reply");
        String output = value(intent, EXTRA_OUTPUT, DEFAULT_OUTPUT);
        String id = value(intent, EXTRA_ID, "default");

        Intent replyIntent = new Intent(context, ReplyReceiver.class)
            .putExtra(EXTRA_ID, id)
            .putExtra(EXTRA_OUTPUT, output);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) flags |= PendingIntent.FLAG_MUTABLE;
        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(context, id.hashCode(), replyIntent, flags);

        RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY).setLabel(button).build();
        Notification.Action action = new Notification.Action.Builder(null, button, replyPendingIntent).addRemoteInput(remoteInput).build();
        buildAndNotify(context, id, title, content, action);
        debug("showNotification id=" + id + " output=" + output + " title=" + title);
    }

    private void showNotification(Context context, String title, String content, String button, String output, String id, String target) {
        Intent replyIntent = new Intent(context, ReplyReceiver.class)
            .putExtra(EXTRA_ID, id)
            .putExtra(EXTRA_OUTPUT, output);
        if (target != null) {
            replyIntent.putExtra(EXTRA_TARGET, target);
        }
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) flags |= PendingIntent.FLAG_MUTABLE;
        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(context, id.hashCode(), replyIntent, flags);

        RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY).setLabel(button).build();
        Notification.Action action = new Notification.Action.Builder(null, button, replyPendingIntent).addRemoteInput(remoteInput).build();
        buildAndNotify(context, id, title, content, action);
    }

    private void buildAndNotify(Context context, String id, String title, String content, Notification.Action action) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "ADB Notify", NotificationManager.IMPORTANCE_HIGH));
        }
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ? new Notification.Builder(context, CHANNEL_ID)
            : new Notification.Builder(context);
        builder.setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setShowWhen(true)
            .addAction(action);
        manager.notify(id.hashCode(), builder.build());
    }

    private void clearNotification(Context context, String id) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(id.hashCode());
        debug("clearNotification id=" + id);
    }

    private static String value(Intent intent, String key, String fallback) {
        String value = intent.getStringExtra(key);
        return value == null ? fallback : value;
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

    private static String getLang(Context context) {
        return getPrefs(context).getString("lang", "pt");
    }

    private static String localized(Context context, String ptText, String enText) {
        return "pt".equals(getLang(context)) ? ptText : enText;
    }

    static void writeExternalFile(Context context, String fileName, String data) {
        try {
            String baseDir = "/sdcard/Documents/adb-notify/";
            File dir = new File(baseDir);
            if (dir.isDirectory()) {
                File[] stale = dir.listFiles((d, name) -> name.startsWith(fileName));
                if (stale != null) {
                    for (File f : stale) f.delete();
                }
            }
            if (Build.VERSION.SDK_INT >= 29) {
                Uri collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL);
                context.getContentResolver().delete(collection,
                    MediaStore.Files.FileColumns.DISPLAY_NAME + " LIKE ? AND " +
                    MediaStore.Files.FileColumns.RELATIVE_PATH + "=?",
                    new String[]{fileName + "%", "Documents/adb-notify"});
                ContentValues values = new ContentValues();
                values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.Files.FileColumns.MIME_TYPE, "text/plain");
                values.put(MediaStore.Files.FileColumns.RELATIVE_PATH, "Documents/adb-notify");
                Uri uri = context.getContentResolver().insert(collection, values);
                if (uri != null) {
                    try (OutputStream os = context.getContentResolver().openOutputStream(uri)) {
                        if (os != null) os.write(data.getBytes(StandardCharsets.UTF_8));
                    }
                }
            } else {
                File file = new File(baseDir + fileName);
                File parent = file.getParentFile();
                if (parent != null) parent.mkdirs();
                try (FileOutputStream fos = new FileOutputStream(file, false)) {
                    fos.write(data.getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (Exception e) {
            debug("writeExternalFile failed: " + fileName + " " + e.getMessage());
        }
    }

    static void debug(String message) {
        try {
            String text = "[" + System.currentTimeMillis() + "] " + message + "\n";
            File file = new File("/sdcard/Documents/adb-notify/debug/notify.log");
            File parent = file.getParentFile();
            if (parent != null) parent.mkdirs();
            try (FileOutputStream fos = new FileOutputStream(file, true)) {
                fos.write(text.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception ignored) {
        }
    }
}
