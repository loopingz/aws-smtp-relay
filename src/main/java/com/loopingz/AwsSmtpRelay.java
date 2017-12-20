package com.loopingz;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.nio.ByteBuffer;

import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.server.SMTPServer;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.helper.SimpleMessageListener;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.regions.Regions;

import org.apache.commons.io.IOUtils;

public class AwsSmtpRelay implements SimpleMessageListener {

    private Properties properties;

    AwsSmtpRelay() {

    }

    public boolean accept(String from, String to) {
        return true;
    }

    public void deliver(String from, String to, InputStream inputStream) throws TooMuchDataException, IOException {
        AmazonSimpleEmailService client =
                AmazonSimpleEmailServiceClientBuilder.standard().build();
        byte[] msg = IOUtils.toByteArray(inputStream);
        RawMessage rawMessage =
                new RawMessage(ByteBuffer.wrap(msg));
        SendRawEmailRequest rawEmailRequest =
                new SendRawEmailRequest(rawMessage)
                        .withConfigurationSetName("SES");
        client.sendRawEmail(rawEmailRequest);
    }

    void run() {
        SMTPServer smtpServer = new SMTPServer(new SimpleMessageListenerAdapter(this));
        smtpServer.setPort(10025);
        smtpServer.start();
    }

    public static void main(String[] args) {
        AwsSmtpRelay server = new AwsSmtpRelay();
        server.run();
    }
}
