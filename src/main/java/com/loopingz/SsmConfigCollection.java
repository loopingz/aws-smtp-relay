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
import com.amazonaws.util.StringUtils;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class SsmConfigCollection {

    Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private CommandLine cmd;
    private DeliveryDetails deliveryDetails;

    public SsmConfigCollection(CommandLine cmd, DeliveryDetails deliveryDetails) {
        this.cmd = cmd;
        this.deliveryDetails = deliveryDetails;
    }

    public DeliveryDetails getConfig() {
        String prefix = (cmd.hasOption("ssmP") ? cmd.getOptionValue("ssmP") : "/smtpRelay/");

        try {
            AWSCredentialsProvider credentials = InstanceProfileCredentialsProvider.getInstance();
            AWSSimpleSystemsManagement simpleSystemsManagementClient =
                    AWSSimpleSystemsManagementClientBuilder.standard().withCredentials(credentials).withRegion(Regions.getCurrentRegion().getName()).build();

            GetParametersByPathRequest parameterRequest = new GetParametersByPathRequest();
            parameterRequest.withPath(prefix).withRecursive(true).setWithDecryption(true);
            GetParametersByPathResult parameterResult = simpleSystemsManagementClient.getParametersByPath(parameterRequest);
            log.trace("length is: " + parameterResult.getParameters().size());
            for (Parameter param : parameterResult.getParameters()) {
                String key = param.getName();
                String value = param.getValue();
                log.trace("key is: " + key +" value: " + value);
                if (key.endsWith("/region")) {
                    deliveryDetails.setRegion(value);
                } else if (key.endsWith("/configuration")) {
                    deliveryDetails.setConfiguration(value);
                } else if (key.endsWith("/sourceArn")) {
                    deliveryDetails.setSourceArn(value);
                } else if (key.endsWith("/smtpOverride")) {
                    deliveryDetails.setSmtpOverride(value);
                } else if (key.endsWith("/smtpHost")) {
                    deliveryDetails.setSmtpHost(value);
                } else if (key.endsWith("/smtpPort")) {
                    deliveryDetails.setSmtpPort(value);
                } else if (key.endsWith("/smtpUsername")) {
                    deliveryDetails.setSmtpUsername(value);
                } else if (key.endsWith("/smtpPassword")) {
                    deliveryDetails.setSmtpPassword(value);
                }
            }
        } catch (AWSSimpleSystemsManagementException e) {
            throw new RuntimeException("Failed to pass SSM arguments", e);
        }
        return deliveryDetails;
    }
}
