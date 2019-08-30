package com.loopingz;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

import com.amazonaws.AmazonServiceException.ErrorType;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.AmazonSimpleEmailServiceException;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;

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
                new SendRawEmailRequest(rawMessage).withSource(from)
                                                   .withDestinations(to);
        if (cmd.hasOption("a")) {
            rawEmailRequest = rawEmailRequest.withSourceArn(cmd.getOptionValue("a"));
        }
        if (cmd.hasOption("f")) {
            rawEmailRequest = rawEmailRequest.withFromArn(cmd.getOptionValue("f"));
        }
        if (cmd.hasOption("t")) {
            rawEmailRequest = rawEmailRequest.withReturnPathArn(cmd.getOptionValue("t"));
        }
        if (cmd.hasOption("c")) {
            rawEmailRequest = rawEmailRequest.withConfigurationSetName(cmd.getOptionValue("c"));
        }
        try {
            client.sendRawEmail(rawEmailRequest);
        } catch (AmazonSimpleEmailServiceException e) {
            if(e.getErrorType() == ErrorType.Client) {
                // If it's a client error, return a permanent error
                throw new RejectException(e.getMessage());
            } else {
                throw new RejectException(451, e.getMessage());
            }
        }
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
        options.addOption("a", "sourceArn", true, "AWS Source ARN of the sending authorization policy");
        options.addOption("f", "fromArn", true, "AWS From ARN of the sending authorization policy");
        options.addOption("t", "returnPathArn", true, "AWS Return Path ARN of the sending authorization policy");
        options.addOption("h", "help", false, "Display this help");
        try {
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                // Should display version here
                formatter.printHelp("aws-smtp-relay", options);
                return;
            }
            AwsSmtpRelay server = new AwsSmtpRelay();
            server.run();
        } catch (ParseException ex) {
            System.err.println(ex.getMessage());
        }
    }
}
