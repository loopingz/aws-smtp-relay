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

  private AwsSmtpRelay() {
  }

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static CommandLine cmd;
  private static DeliveryDetails deliveryDetails = new DeliveryDetails();

    private static void getCmdConfig() {
        if (cmd.hasOption(Params.BIND_ADDRESS.key())) {
            deliveryDetails.setBindAddress(cmd.getOptionValue(Params.BIND_ADDRESS.key()));
        }
        if (cmd.hasOption(Params.PORT.key())) {
            deliveryDetails.setPort(cmd.getOptionValue(Params.PORT.key()));
        }
        if (cmd.hasOption(Params.REGION.key())) {
            deliveryDetails.setRegion(cmd.getOptionValue(Params.REGION.key()));
        }
        if (cmd.hasOption(Params.SOURCE_ARN.key())) {
            deliveryDetails.setSourceArn(cmd.getOptionValue(Params.SOURCE_ARN.key()));
        }
        if (cmd.hasOption(Params.CONFIGURATION.key())) {
            deliveryDetails.setConfiguration(cmd.getOptionValue(Params.CONFIGURATION.key()));
        }
        if (cmd.hasOption(Params.FROM_ARN.key())) {
            deliveryDetails.setFromArn(cmd.getOptionValue(Params.FROM_ARN.key()));
        }
        if (cmd.hasOption(Params.RETURN_PATH_ARN.key())) {
            deliveryDetails.setReturnPathArn(cmd.getOptionValue(Params.RETURN_PATH_ARN.key()));
        }
        if (cmd.hasOption(Params.SMTP_OVERRIDE.key())) {
            deliveryDetails.setSmtpOverride(cmd.getOptionValue(Params.SMTP_OVERRIDE.key()));
        }
        setSmtpDirectCreds();
    }

    private static void setSmtpDirectCreds() {
        if (deliveryDetails.isSmtpOverride()) {
            if (cmd.hasOption(Params.SMTP_HOST.key())) {
                deliveryDetails.setSmtpHost(cmd.getOptionValue(Params.SMTP_HOST.key()));
            }
            if (cmd.hasOption(Params.SMTP_PORT.key())) {
                deliveryDetails.setSmtpPort(cmd.getOptionValue(Params.SMTP_PORT.key()));
            }
            if (cmd.hasOption(Params.SMTP_USERNAME.key())) {
                deliveryDetails.setSmtpUsername(cmd.getOptionValue(Params.SMTP_USERNAME.key()));
            }
            if (cmd.hasOption(Params.SMTP_PASSWORD.key())) {
                deliveryDetails.setSmtpPassword(cmd.getOptionValue(Params.SMTP_PASSWORD.key()));
            }
        }
    }
  }

    public static void main(String[] args) throws UnknownHostException {
        Options options = new Options();
        options.addOption(Params.SSM_ENABLE.key(), Params.SSM_ENABLE.toString(), false, "Use SSM Parameter Store to get configuration");
        options.addOption(Params.SSM_PREFIX.key(), Params.SSM_PREFIX.toString(), true, "SSM prefix to find variables default is /smtpRelay");
        options.addOption(Params.SSM_REFRESH.key(), Params.SSM_REFRESH.toString(), true, "SSM refresh rate to reload parameter in minutes");

        options.addOption(Params.PORT.key(), Params.PORT.toString(), true, "Port number to listen to");
        options.addOption(Params.BIND_ADDRESS.key(), Params.BIND_ADDRESS.toString(), true, "Address to listen to");
        options.addOption(Params.REGION.key(), Params.REGION.toString(), true, "AWS region to use");
        options.addOption(Params.CONFIGURATION.key(), Params.CONFIGURATION.toString(), true, "AWS SES configuration to use");
        options.addOption(Params.SOURCE_ARN.key(), Params.SOURCE_ARN.toString(), true, "AWS Source ARN of the sending authorization policy");
        options.addOption(Params.FROM_ARN.key(), Params.FROM_ARN.toString(), true, "AWS From ARN of the sending authorization policy");
        options.addOption(Params.RETURN_PATH_ARN.key(), Params.RETURN_PATH_ARN.toString(), true, "AWS Return Path ARN of the sending authorization policy");

        options.addOption(Params.SMTP_OVERRIDE.key(), Params.SMTP_OVERRIDE.toString(), true, "Not use SES but set SMTP variables t/f, true/false");
        options.addOption(Params.SMTP_HOST.key(), Params.SMTP_HOST.toString(), true, "SMTP variable Host");
        options.addOption(Params.SMTP_PORT.key(), Params.SMTP_PORT.toString(), true, "SMTP variable Port");
        options.addOption(Params.SMTP_USERNAME.key(), Params.SMTP_USERNAME.toString(), true, "SMTP variable Username");
        options.addOption(Params.SMTP_PASSWORD.key(), Params.SMTP_PASSWORD.toString(), true, "SMTP variable password");

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
            if (cmd.hasOption(Params.SSM_ENABLE.key())) {
                SsmConfigCollection collector = new SsmConfigCollection(cmd, deliveryDetails);
                collector.updateConfig();
                collector.run();
            }

      // select sender (ses or other)
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
