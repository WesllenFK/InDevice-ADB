package com.indevice.adb;

import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ReplyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String id = intent.getStringExtra(NotifyReceiver.EXTRA_ID);
        String target = intent.getStringExtra(NotifyReceiver.EXTRA_TARGET);
        String modeTag = intent.getStringExtra(NotifyReceiver.EXTRA_MODE_TAG);

        Bundle results = RemoteInput.getResultsFromIntent(intent);
        CharSequence reply = results == null ? null : results.getCharSequence(NotifyReceiver.KEY_TEXT_REPLY);
        NotifyReceiver.debug(context, "replyReceiver id=" + id + " target=" + target + " modeTag=" + modeTag
            + " hasResults=" + (results != null) + " replyNull=" + (reply == null));

        if (NotifyReceiver.MODE_TAG_PAIR.equals(modeTag) && target != null && reply != null) {
            String replyStr = reply.toString().trim();
            String jsonMode = intent.getStringExtra(NotifyReceiver.EXTRA_JSON);
            String data = "true".equals(jsonMode)
                ? "{\"target\":\"" + target + "\",\"code\":\"" + replyStr + "\"}\n"
                : target + "\n" + replyStr + "\n";
            boolean ok = NotifyReceiver.writeExternalFile(context, "reply.txt", data);
            NotifyReceiver.updateNotification(context,
                ok ? "ADB Pair" : "ADB Pair: erro",
                ok ? "Codigo enviado para " + target : "Falha ao escrever codigo",
                null, null, false);
            NotifyReceiver.debug(context, "replyReceiver pair data ok=" + ok);
        } else if (reply != null) {
            String data = reply.toString() + "\n";
            boolean ok = NotifyReceiver.writeExternalFile(context, "reply-last.txt", data);
            NotifyReceiver.updateNotification(context,
                ok ? "ADB Notify" : "ADB Notify: erro",
                ok ? "Resposta recebida: " + reply.toString() : "Falha ao escrever resposta",
                null, null, false);
            NotifyReceiver.debug(context, "replyReceiver wrote reply id=" + id + " text=" + reply + " ok=" + ok);
        }
    }
}
