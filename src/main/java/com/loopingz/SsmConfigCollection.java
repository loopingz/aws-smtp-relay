package com.loopingz;

import java.lang.invoke.MethodHandles;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.AWSSimpleSystemsManagementException;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;

public class SsmConfigCollection {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private String prefix = "/smtpRelay/";
    private DeliveryDetails deliveryDetails;
    private Timer timer;
    private int interval;

    public SsmConfigCollection(CommandLine cmd, DeliveryDetails deliveryDetails) {
        prefix = (cmd.hasOption("ssmP") ? cmd.getOptionValue("ssmP") : "/smtpRelay/");
        this.deliveryDetails = deliveryDetails;
        timer = new Timer();
        if (cmd.hasOption("ssmR")) {
            this.interval = Integer.parseInt(cmd.getOptionValue("ssmR")) * 60 * 1000;
        }
    }

    public boolean updateConfig() {
        return this.updateConfig(deliveryDetails);
    }

    public boolean updateConfig(DeliveryDetails deliveryDetails) {
        boolean result = false;
        try {
            AWSCredentialsProvider credentials = InstanceProfileCredentialsProvider.getInstance();
            AWSSimpleSystemsManagement simpleSystemsManagementClient = AWSSimpleSystemsManagementClientBuilder.standard()
                .withCredentials(credentials).withRegion(Regions.getCurrentRegion().getName()).build();

            GetParametersByPathRequest parameterRequest = new GetParametersByPathRequest();
            parameterRequest.withPath(prefix).withRecursive(true).setWithDecryption(true);
            GetParametersByPathResult parameterResult = simpleSystemsManagementClient.getParametersByPath(parameterRequest);
            LOG.trace("length is: " + parameterResult.getParameters().size());

            for (Parameter param : parameterResult.getParameters()) {
                String key = param.getName();
                String value = param.getValue();
                LOG.trace("key is: " + key + " value: " + value);
                if (key.endsWith("/region") && !value.equals(deliveryDetails.getRegion())) {
                    result = true;
                    deliveryDetails.setRegion(value);
                } else if (key.endsWith("/configuration") && !value.equals(deliveryDetails.getConfiguration())) {
                    result = true;
                    deliveryDetails.setConfiguration(value);
                } else if (key.endsWith("/sourceArn") && !value.equals(deliveryDetails.getSourceArn())) {
                    result = true;
                    deliveryDetails.setSourceArn(value);
                } else if (key.endsWith("/smtpOverride") && !value.equals(deliveryDetails.getSmtpOverride())) {
                    result = true;
                    deliveryDetails.setSmtpOverride(value);
                } else if (key.endsWith("/smtpHost") && !value.equals(deliveryDetails.getSmtpHost())) {
                    result = true;
                    deliveryDetails.setSmtpHost(value);
                } else if (key.endsWith("/smtpPort") && !value.equals(deliveryDetails.getSmtpPort())) {
                    result = true;
                    deliveryDetails.setSmtpPort(value);
                } else if (key.endsWith("/smtpUsername") && !value.equals(deliveryDetails.getSmtpUsername())) {
                    result = true;
                    deliveryDetails.setSmtpUsername(value);
                } else if (key.endsWith("/smtpPassword") && !value.equals(deliveryDetails.getSmtpPassword())) {
                    result = true;
                    deliveryDetails.setSmtpPassword(value);
                }
            }
        } catch (

            AWSSimpleSystemsManagementException e) {
            throw new RuntimeException("Failed to pass SSM arguments", e);
        }
        return result;
    }

    public void run() {
        if (interval <= 0) {
            return;
        }
        // Should set timer
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (updateConfig()) {
                        SmtpRelay.reload();
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage());
                }
            }
        }, interval, interval);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
