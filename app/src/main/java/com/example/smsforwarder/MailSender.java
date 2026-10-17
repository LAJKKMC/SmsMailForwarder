package com.example.smsforwarder;

import android.content.SharedPreferences;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public final class MailSender {
    private MailSender() {}
    public static void send(SharedPreferences p, String subject, String body, String label) throws Exception {
        String host=p.getString("host",""); String port=p.getString("port","465"); String user=p.getString("username",""); String pass=p.getString("password",""); String to=p.getString("recipient","");
        boolean ssl="465".equals(port);
        Properties props=new Properties(); props.put("mail.smtp.host",host); props.put("mail.smtp.port",port); props.put("mail.smtp.auth","true"); props.put("mail.smtp.connectiontimeout","15000"); props.put("mail.smtp.timeout","15000"); props.put("mail.smtp.writetimeout","15000");
        if(ssl) props.put("mail.smtp.ssl.enable","true"); else { props.put("mail.smtp.starttls.enable","true"); props.put("mail.smtp.starttls.required","true"); }
        Session session=Session.getInstance(props,new Authenticator(){ @Override protected PasswordAuthentication getPasswordAuthentication(){return new PasswordAuthentication(user,pass);} });
        MimeMessage message=new MimeMessage(session); message.setFrom(new InternetAddress(user)); message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to,false)); message.setSubject(subject,"UTF-8"); message.setText(body,"UTF-8");
        Transport.send(message);
    }
}
