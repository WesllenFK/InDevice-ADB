package com.miniadbnotify;

import android.app.NotificationManager;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class ReplyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String id = intent.getStringExtra(NotifyReceiver.EXTRA_ID);
        String target = intent.getStringExtra(NotifyReceiver.EXTRA_TARGET);
        String modeTag = intent.getStringExtra(NotifyReceiver.EXTRA_MODE_TAG);

        Bundle results = RemoteInput.getResultsFromIntent(intent);
        CharSequence reply = results == null ? null : results.getCharSequence(NotifyReceiver.KEY_TEXT_REPLY);
        NotifyReceiver.debug("replyReceiver start id=" + id
            + " target=" + target + " modeTag=" + modeTag
            + " hasResults=" + (results != null) + " replyNull=" + (reply == null));

        if (NotifyReceiver.MODE_TAG_PAIR.equals(modeTag) && target != null && reply != null) {
            String replyStr = reply.toString().trim();
            String jsonMode = intent.getStringExtra(NotifyReceiver.EXTRA_JSON);
            String data;
            if ("true".equals(jsonMode)) {
                data = "{\"target\":\"" + target + "\",\"code\":\"" + replyStr + "\"}\n";
            } else {
                data = target + "\n" + replyStr + "\n";
            }
            boolean ok = NotifyReceiver.writeExternalFile(context, "reply.txt", data);
            if (ok) {
                Toast.makeText(context, "pair data sent", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "pair data failed: no write permission", Toast.LENGTH_LONG).show();
            }
            NotifyReceiver.debug("replyReceiver wrote pair data: " + target + " / " + reply + " json=" + jsonMode + " ok=" + ok);
        } else if (reply != null) {
            String data = reply.toString() + "\n";
            boolean ok = NotifyReceiver.writeExternalFile(context, "reply-last.txt", data);
            if (ok) {
                Toast.makeText(context, "reply received", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "reply failed: no write permission", Toast.LENGTH_LONG).show();
            }
            NotifyReceiver.debug("replyReceiver wrote reply id=" + id + " text=" + reply + " ok=" + ok);
        }

        if (id != null) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(id.hashCode());
        }
    }
}
