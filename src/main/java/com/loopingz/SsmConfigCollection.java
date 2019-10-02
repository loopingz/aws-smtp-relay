package com.loopingz;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.AWSSimpleSystemsManagementException;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Timer;
import java.util.TimerTask;

public class SsmConfigCollection {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String SLASH = "/";

    private String prefix = "/smtpRelay/"; //Default
    private DeliveryDetails deliveryDetails;
    private Timer timer;
    private int interval;

    public SsmConfigCollection(CommandLine cmd, DeliveryDetails deliveryDetails) {
        if (cmd.hasOption(Params.SSM_PREFIX.key())) {
            this.prefix = cmd.getOptionValue(Params.SSM_PREFIX.key());
        }
        if (cmd.hasOption(Params.SSM_REFRESH.key())) {
            this.interval = Integer.parseInt(cmd.getOptionValue(Params.SSM_REFRESH.key())) * 60 * 1000;
        }
        this.deliveryDetails = deliveryDetails;
        timer = new Timer();
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
                result = setProperty(deliveryDetails, result, key, value);
            }
        } catch (

            AWSSimpleSystemsManagementException e) {
            throw new IllegalArgumentException("Failed to pass SSM arguments", e);
        }
        return result;
    }

    private boolean setProperty(DeliveryDetails deliveryDetails, boolean inResult, String key, String value) {
        boolean result = inResult;
        if (key.endsWith(getSuffix(Params.REGION)) && !value.equals(deliveryDetails.getRegion())) {
            result = true;
            deliveryDetails.setRegion(value);
        } else if (key.endsWith(getSuffix(Params.CONFIGURATION)) && !value.equals(deliveryDetails.getConfiguration())) {
            result = true;
            deliveryDetails.setConfiguration(value);
        } else if (key.endsWith(getSuffix(Params.SOURCE_ARN)) && !value.equals(deliveryDetails.getSourceArn())) {
            result = true;
            deliveryDetails.setSourceArn(value);
        } else if (key.endsWith(getSuffix(Params.SMTP_OVERRIDE)) && !value.equals(deliveryDetails.getSmtpOverride())) {
            result = true;
            deliveryDetails.setSmtpOverride(value);
        } else if (key.endsWith(getSuffix(Params.SMTP_HOST)) && !value.equals(deliveryDetails.getSmtpHost())) {
            result = true;
            deliveryDetails.setSmtpHost(value);
        } else if (key.endsWith(getSuffix(Params.SMTP_PORT)) && !value.equals(deliveryDetails.getSmtpPort())) {
            result = true;
            deliveryDetails.setSmtpPort(value);
        } else if (key.endsWith(getSuffix(Params.SMTP_USERNAME)) && !value.equals(deliveryDetails.getSmtpUsername())) {
            result = true;
            deliveryDetails.setSmtpUsername(value);
        } else if (key.endsWith(getSuffix(Params.SMTP_PASSWORD)) && !value.equals(deliveryDetails.getSmtpPassword())) {
            result = true;
            deliveryDetails.setSmtpPassword(value);
        }
        return result;
    }

    String getSuffix(Params config) {
        return SLASH + config.toString();
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
