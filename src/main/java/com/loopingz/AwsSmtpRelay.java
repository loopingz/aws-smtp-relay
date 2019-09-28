package com.loopingz;

import java.lang.invoke.MethodHandles;
import java.net.UnknownHostException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AwsSmtpRelay {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static CommandLine cmd;
    private static DeliveryDetails deliveryDetails = new DeliveryDetails();

    private AwsSmtpRelay() {
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
        options.addOption("ssmR", "ssmRefresh", true, "SSM refresh rate to reload parameter");

        options.addOption("p", "port", true, "Port number to listen to");
        options.addOption("b", "bindAddress", true, "Address to listen to");
        options.addOption("r", "region", true, "AWS region to use");
        options.addOption("c", "configuration", true, "AWS SES configuration to use");
        options.addOption("a", "sourceArn", true, "AWS ARN of the sending authorization policy");
        options.addOption("f", "fromArn", true, "AWS From ARN of the sending authorization policy");
        options.addOption("t", "returnPathArn", true, "AWS Return Path ARN of the sending authorization policy");

        options.addOption("smtpO", "smtpOverride", true, "Not use SES but set SMTP variables true/false");
        options.addOption("smtpH", "smtpHost", true, "SMTP variable Host");
        options.addOption("smtpP", "smtpPort", true, "SMTP variable Port");
        options.addOption("smtpU", "smtpUsername", true, "SMTP variable Username");
        options.addOption("smtpPW", "smtpPassword", true, "SMTP variable password");

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
            if (cmd.hasOption("ssm")) {
                SsmConfigCollection collector = new SsmConfigCollection(cmd, deliveryDetails);
                collector.updateConfig();
                collector.run();
            }

            //select sender (ses or other)
            if (deliveryDetails.isSmtpOverride()) {
                SmtpRelay.init(new BasicSmtpRelay(deliveryDetails));
            } else {
                SmtpRelay.init(new SesSmtpRelay(deliveryDetails));
            }
        } catch (ParseException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }
}
