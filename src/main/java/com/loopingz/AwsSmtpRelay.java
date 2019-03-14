package com.loopingz;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.server.SMTPServer;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.AmazonSimpleEmailServiceException;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;

public class AwsSmtpRelay implements SimpleMessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static CommandLine cmd;
    private static DeliveryDetails deliveryDetails = new DeliveryDetails();

    AwsSmtpRelay() {
    }

    @Override
    public boolean accept(String from, String to) {
        return true;
    }

    @Override
    public void deliver(String from, String to, InputStream inputStream) throws IOException {
        AmazonSimpleEmailService client;
        if (deliveryDetails.hasRegion()) {
            client = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(deliveryDetails.getRegion()).build();
        } else {
            client = AmazonSimpleEmailServiceClientBuilder.standard().build();
        }
        byte[] msg = IOUtils.toByteArray(inputStream);
        RawMessage rawMessage =
                new RawMessage(ByteBuffer.wrap(msg));
        SendRawEmailRequest rawEmailRequest =
                new SendRawEmailRequest(rawMessage).withSource(from)
                                                   .withDestinations(to);
        if (deliveryDetails.hasSourceArn()) {
            rawEmailRequest = rawEmailRequest.withSourceArn(deliveryDetails.getSourceArn());
        }
        if (deliveryDetails.hasFromArn()) {
            rawEmailRequest = rawEmailRequest.withFromArn(deliveryDetails.getFromArn());
        }
        if (deliveryDetails.hasReturnPathArn()) {
            rawEmailRequest = rawEmailRequest.withReturnPathArn(deliveryDetails.getReturnPathArn());
        }
        if (deliveryDetails.hasConfiguration()) {
            rawEmailRequest = rawEmailRequest.withConfigurationSetName(deliveryDetails.getConfiguration());
        }
        try {
            client.sendRawEmail(rawEmailRequest);
        } catch (AmazonSimpleEmailServiceException e) {
            throw new IOException(e.getMessage());
        }
    }

    void run() throws UnknownHostException {
        SMTPServer.Builder builder = new SMTPServer.Builder();
        builder.bindAddress(InetAddress.getByName(deliveryDetails.getBindAddress()))
            .port(deliveryDetails.getPort())
            .simpleMessageListener(this);
        SMTPServer smtpServer = builder.build();
        smtpServer.start();
    }

    private static void getCmdConfig() {
        if (cmd.hasOption("b")) {
            deliveryDetails.setBindAddress(cmd.getOptionValue("b"));
        }
        if (cmd.hasOption("p")) {
            deliveryDetails.setPort(cmd.getOptionValue("p"));
        }
        if (cmd.hasOption("r")) {
            deliveryDetails.setRegion(cmd.getOptionValue("r"));
        }
        if (cmd.hasOption("a")) {
            deliveryDetails.setSourceArn(cmd.getOptionValue("a"));
        }
        if (cmd.hasOption("c")) {
            deliveryDetails.setConfiguration(cmd.getOptionValue("c"));
        }
        if (cmd.hasOption("f")) {
        	deliveryDetails.setFromArn(cmd.getOptionValue("f"));
        }
        if (cmd.hasOption("t")) {
        	deliveryDetails.setReturnPathArn(cmd.getOptionValue("t"));
        }
        if (cmd.hasOption("smtpO")) {
            deliveryDetails.setSmtpOverride(cmd.getOptionValue("smtpO"));
        }
        if (deliveryDetails.isSmtpOverride()) {
            if (cmd.hasOption("smtpH")) {
                deliveryDetails.setSmtpHost(cmd.getOptionValue("smtpH"));
            }
            if (cmd.hasOption("smtpP")) {
                deliveryDetails.setSmtpPort(cmd.getOptionValue("smtpP"));
            }
            if (cmd.hasOption("smtpU")) {
                deliveryDetails.setSmtpUsername(cmd.getOptionValue("smtpU"));
            }
            if (cmd.hasOption("smtpPW")) {
                deliveryDetails.setSmtpPassword(cmd.getOptionValue("smtpPW"));
            }
        }
    }

    public static void main(String[] args) throws UnknownHostException {
        Options options = new Options();
        options.addOption("ssm", "ssmEnable", false, "Use SSM Parameter Store to get configuration");
        options.addOption("ssmP", "ssmPrefix", true, "SSM prefix to find variables default is /smtpRelay");

        options.addOption("p", "port", true, "Port number to listen to");
        options.addOption("b", "bindAddress", true, "Address to listen to");
        options.addOption("r", "region", true, "AWS region to use");
        options.addOption("c", "configuration", true, "AWS SES configuration to use");
        options.addOption("a", "sourceArn", true, "AWS Source ARN of the sending authorization policy");
        options.addOption("f", "fromArn", true, "AWS From ARN of the sending authorization policy");
        options.addOption("t", "returnPathArn", true, "AWS Return Path ARN of the sending authorization policy");


        options.addOption("smtpO", "smtpOverride", true, "Not use SES but set SMTP variables true/false");
        options.addOption("smtpH", "smtpHost", true, "SMTP variable Host");
        options.addOption("smtpP", "smtpPort", true, "SMTP variable Port");
        options.addOption("smtpU", "smtpUsername", true, "SMTP variable Username");
        options.addOption("smtpW", "smtpPassword", true, "SMTP variable password");

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

            getCmdConfig();
            //get configuration
            if (cmd.hasOption("ssm") ){
                deliveryDetails = new SsmConfigCollection(cmd, deliveryDetails).getConfig();
            }

            //select sender (ses or other)
            if (deliveryDetails.isSmtpOverride()) {
                BasicSmtpRelay server = new BasicSmtpRelay(deliveryDetails);
                server.run();
            } else {
                AwsSmtpRelay server = new AwsSmtpRelay();
                server.run();
            }
        } catch (ParseException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }
}
