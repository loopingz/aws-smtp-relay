package com.loopingz;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.amazonaws.util.StringUtils;

public class BasicSmtpRelay extends SmtpRelay {

  private Properties props;
  private boolean authRequest;

  BasicSmtpRelay(DeliveryDetails deliveryDetails) {
    super(deliveryDetails);

    props = new Properties();
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.host", deliveryDetails.getSmtpHost());
    props.put("mail.smtp.port", deliveryDetails.getSmtpPort());

    if (!StringUtils.isNullOrEmpty(deliveryDetails.getSmtpUsername())
        && !StringUtils.isNullOrEmpty(deliveryDetails.getSmtpPassword())) {
      props.put("mail.smtp.auth", "true");
      authRequest = true;
    } else {
      props.put("mail.smtp.auth", "false");
      authRequest = false;
    }
  }

  @Override
  public void deliver(String from, String to, InputStream inputStream) throws IOException {

    try {
      Session session = getSession();
      Message msg = new MimeMessage(session, inputStream);
      msg.setFrom();
      msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
      msg.setFrom(new InternetAddress(from));

      Transport.send(msg);
    } catch (MessagingException ex) {
      throw new IOException(ex.getMessage(), ex);
    }
  }

  private Session getSession() {
    if (authRequest) {
      return Session.getDefaultInstance(props, new Authenticator() {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(deliveryDetails.getSmtpUsername(), deliveryDetails.getSmtpPassword());
        }
      });
    }
    return Session.getDefaultInstance(props);
  }
}
