package com.loopingz;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.nio.ByteBuffer;

import org.apache.commons.cli.*;
import org.subethamail.smtp.server.SMTPServer;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.helper.SimpleMessageListener;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.regions.Regions;

import org.apache.commons.io.IOUtils;

public class AwsSmtpRelay implements SimpleMessageListener {

    private static CommandLine cmd;

    AwsSmtpRelay() {

    }

    public boolean accept(String from, String to) {
        return true;
    }

    public void deliver(String from, String to, InputStream inputStream) throws IOException {
        AmazonSimpleEmailService client;
        if (cmd.hasOption("r")) {
            client = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(cmd.getOptionValue("r")).build();
        } else {
            client = AmazonSimpleEmailServiceClientBuilder.standard().build();
        }
        byte[] msg = IOUtils.toByteArray(inputStream);
        RawMessage rawMessage =
                new RawMessage(ByteBuffer.wrap(msg));
        SendRawEmailRequest rawEmailRequest =
                new SendRawEmailRequest(rawMessage);
        if (cmd.hasOption("c")) {
            rawEmailRequest = rawEmailRequest.withConfigurationSetName(cmd.getOptionValue("c"));
        }
        client.sendRawEmail(rawEmailRequest);
    }

    void run() throws UnknownHostException {

        String bindAddress = cmd.hasOption("b") ? cmd.getOptionValue("b") : "127.0.0.1";

        SMTPServer smtpServer = new SMTPServer(new SimpleMessageListenerAdapter(this));
        smtpServer.setBindAddress(InetAddress.getByName(bindAddress));
        if (cmd.hasOption("p")) {
            smtpServer.setPort(Integer.parseInt(cmd.getOptionValue("p")));
        } else {
            smtpServer.setPort(10025);
        }
        smtpServer.start();
    }

    public static void main(String[] args) throws UnknownHostException {
        Options options = new Options();
        options.addOption("p", "port", true, "Port number to listen to");
        options.addOption("b", "bindAddress", true, "Address to listen to");
        options.addOption("r", "region", true, "AWS region to use");
        options.addOption("c", "configuration", true, "AWS SES configuration to use");
        options.addOption("h", "help", false, "Display this help");
        try {
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                // Should display version here
                formatter.printHelp( "aws-smtp-relay", options );
                return;
            }
            AwsSmtpRelay server = new AwsSmtpRelay();
            server.run();
        } catch (ParseException ex) {
            System.err.println(ex.getMessage());
        }
    }
}
