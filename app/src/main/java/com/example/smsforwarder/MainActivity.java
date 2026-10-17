package com.example.smsforwarder;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;

public class MainActivity extends Activity {
    private EditText sender, recipient, smtpHost, smtpPort, username, password;
    private TextView status;
    private SharedPreferences prefs;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("forwarder", MODE_PRIVATE);
        buildUi();
        if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS}, 41);
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(22), dp(26), dp(22), dp(30)); page.setBackgroundColor(Color.rgb(246,248,251));
        scroll.addView(page);

        TextView title = new TextView(this); title.setText("短信邮件转发"); title.setTextSize(26); title.setTypeface(null, Typeface.BOLD); title.setTextColor(Color.rgb(24,39,58));
        page.addView(title);
        TextView intro = new TextView(this); intro.setText("收到指定号码的短信后，通过邮箱 SMTP 自动转发。请保持手机联网，并允许应用后台运行。"); intro.setTextSize(14); intro.setTextColor(Color.rgb(90,103,120)); intro.setPadding(0,dp(8),0,dp(18)); page.addView(intro);

        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(dp(16),dp(12),dp(16),dp(16));
        GradientDrawable bg = new GradientDrawable(); bg.setColor(Color.WHITE); bg.setCornerRadius(dp(16)); card.setBackground(bg); page.addView(card);
        sender = field(card,"仅转发此号码（留空表示所有号码）", "sender", InputType.TYPE_CLASS_PHONE);
        recipient = field(card,"接收邮件的邮箱", "recipient", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        smtpHost = field(card,"SMTP 服务器，例如 smtp.gmail.com", "host", InputType.TYPE_CLASS_TEXT);
        smtpPort = field(card,"SMTP 端口（通常 465 或 587）", "port", InputType.TYPE_CLASS_NUMBER);
        username = field(card,"SMTP 登录邮箱", "username", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        password = field(card,"邮箱应用专用密码", "password", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        Button save = new Button(this); save.setText("保存并启用"); save.setAllCaps(false); card.addView(save);
        save.setOnClickListener(v -> save());
        Button test = new Button(this); test.setText("发送测试邮件"); test.setAllCaps(false); card.addView(test);
        test.setOnClickListener(v -> testEmail());

        status = new TextView(this); status.setTextSize(14); status.setTextColor(Color.rgb(55,73,94)); status.setPadding(0,dp(16),0,0); page.addView(status);
        TextView note = new TextView(this); note.setText("安全提示：建议只设置验证码发送方号码。短信正文会通过邮件发送并存储在邮箱中；请使用邮箱专用密码，不要填写邮箱主密码。邮件发送需要手机能够连接 SMTP 服务器。"); note.setTextSize(12); note.setTextColor(Color.rgb(110,120,133)); note.setPadding(0,dp(14),0,0); page.addView(note);
        setContentView(scroll);
        load();
    }

    private EditText field(LinearLayout box, String label, String key, int type) {
        TextView tv = new TextView(this); tv.setText(label); tv.setTextSize(13); tv.setTextColor(Color.rgb(65,78,95)); tv.setPadding(0,dp(8),0,dp(4)); box.addView(tv);
        EditText e = new EditText(this); e.setSingleLine(true); e.setTextSize(15); e.setInputType(type); box.addView(e);
        switch(key) { case "sender": sender=e; break; case "recipient": recipient=e; break; case "host": smtpHost=e; break; case "port": smtpPort=e; break; case "username": username=e; break; case "password": password=e; break; }
        return e;
    }
    private void load() {
        sender.setText(prefs.getString("sender","")); recipient.setText(prefs.getString("recipient",""));
        smtpHost.setText(prefs.getString("host","smtp.gmail.com")); smtpPort.setText(prefs.getString("port","465"));
        username.setText(prefs.getString("username","")); password.setText(prefs.getString("password",""));
        status.setText(prefs.getBoolean("enabled",false) ? "已启用。收到匹配短信时会自动发送邮件。" : "尚未启用。请填写设置并保存。");
    }
    private void save() {
        if (recipient.getText().toString().trim().isEmpty() || smtpHost.getText().toString().trim().isEmpty() || smtpPort.getText().toString().trim().isEmpty() || username.getText().toString().trim().isEmpty() || password.getText().toString().isEmpty()) { status.setText("请填写收件邮箱、SMTP 服务器、端口、登录邮箱和专用密码。"); return; }
        prefs.edit().putString("sender",sender.getText().toString().trim()).putString("recipient",recipient.getText().toString().trim()).putString("host",smtpHost.getText().toString().trim()).putString("port",smtpPort.getText().toString().trim()).putString("username",username.getText().toString().trim()).putString("password",password.getText().toString()).putBoolean("enabled",true).apply();
        status.setText("设置已保存并启用。请点“发送测试邮件”验证配置。");
    }
    private void testEmail() {
        save(); if (!prefs.getBoolean("enabled",false)) return;
        status.setText("正在发送测试邮件…");
        new Thread(() -> { try { MailSender.send(prefs,"短信邮件转发测试","应用的邮件配置可正常使用。", "测试"); runOnUiThread(() -> status.setText("测试邮件已发送，请检查收件箱和垃圾邮件。")); }
            catch(Exception ex) { runOnUiThread(() -> status.setText("发送失败："+ex.getMessage())); } }).start();
    }
    private int dp(int n) { return (int)(n*getResources().getDisplayMetrics().density+0.5f); }
}
