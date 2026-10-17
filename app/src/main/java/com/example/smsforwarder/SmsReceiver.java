package com.example.smsforwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SmsReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (!Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) return;
        SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        if (messages == null || messages.length == 0) return;
        String address = messages[0].getOriginatingAddress(); StringBuilder body = new StringBuilder();
        long timestamp = messages[0].getTimestampMillis();
        for (SmsMessage message : messages) if (message != null) body.append(message.getMessageBody());
        SharedPreferences p = context.getSharedPreferences("forwarder", Context.MODE_PRIVATE);
        if (!p.getBoolean("enabled", false)) return;
        String filter = p.getString("sender", "").trim();
        if (!filter.isEmpty() && (address == null || !normalize(address).equals(normalize(filter)))) return;
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(timestamp));
        String content = "发件号码："+(address == null ? "未知" : address)+"\n收到时间："+date+"\n\n短信内容：\n"+body;
        final PendingResult pending = goAsync();
        new Thread(() -> { try { MailSender.send(p,"收到短信："+(address == null ? "未知号码" : address),content,"SMS"); }
            catch(Exception e) { Log.e("SmsForwarder","短信邮件发送失败",e); }
            finally { pending.finish(); } }).start();
    }
    private String normalize(String s) { return s.replaceAll("[^+0-9]", ""); }
}
