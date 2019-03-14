package com.loopingz;


import com.amazonaws.util.StringUtils;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class BasicSmtpRelay implements SimpleMessageListener {

    private DeliveryDetails deliveryDetails;
    private Properties props;
    private boolean authRequest;

    BasicSmtpRelay(DeliveryDetails deliveryDetails) {
        this.deliveryDetails = deliveryDetails;

        props = new Properties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", deliveryDetails.getSmtpHost());
        props.put("mail.smtp.port", deliveryDetails.getSmtpPort());

        if (!StringUtils.isNullOrEmpty(deliveryDetails.getSmtpUsername()) && !StringUtils.isNullOrEmpty(deliveryDetails.getSmtpPassword())) {
            props.put("mail.smtp.auth", "true");
            authRequest = true;
        } else {
            props.put("mail.smtp.auth", "false");
            authRequest = false;
        }
    }

    public boolean accept(String from, String to) {
        return true;
    }

    public void deliver(String from, String to, InputStream inputStream) throws IOException {

        try {
            Session session = getSession();
            Message msg = new MimeMessage(session, inputStream);
            msg.setFrom();
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setFrom(new InternetAddress(from));

            Transport.send(msg);
        } catch (MessagingException ex){
            throw new IOException(ex.getMessage());
        }
    }

    private Session getSession() {
        if (authRequest) {
            return Session.getDefaultInstance(props,
                    new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(deliveryDetails.getSmtpUsername(), deliveryDetails.getSmtpPassword());
                        }
                    });
        }
        return Session.getDefaultInstance(props);
    }

    void run() throws UnknownHostException {
        SMTPServer.Builder builder = new SMTPServer.Builder();
        builder.bindAddress(InetAddress.getByName(deliveryDetails.getBindAddress()))
                .port(deliveryDetails.getPort())
                .simpleMessageListener(this);
        SMTPServer smtpServer = builder.build();
        smtpServer.start();
    }


}
