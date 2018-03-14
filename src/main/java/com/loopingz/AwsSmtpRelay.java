package com.loopingz;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.regex.Pattern;

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
    private static final Pattern fromPat = Pattern.compile("(?mi)^From:");

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
        byte[] msgRaw = IOUtils.toByteArray(inputStream);
        String msgStr = new String(msgRaw);
        ByteBuffer msg = ByteBuffer.wrap(msgRaw);

        // Add From: header if missing
        String headers = msgStr.substring(0, msgStr.indexOf("\r\n\r\n"));
        if(!fromPat.matcher(headers).find()) {
            CharBuffer cb = CharBuffer.wrap("From: <"+from+">\r\n");
            ByteBuffer bb = ByteBuffer.allocate(cb.limit() + msg.limit());
            bb.put(StandardCharsets.ISO_8859_1.encode(cb)).put(msg).flip();
            msg = bb;
        }

        RawMessage rawMessage =
                new RawMessage(msg);
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
