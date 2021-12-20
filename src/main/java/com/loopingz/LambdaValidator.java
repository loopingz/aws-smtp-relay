package com.loopingz;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.auth.UsernamePasswordValidator;

import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;

public class LambdaValidator implements UsernamePasswordValidator {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected DeliveryDetails deliveryDetails;
    private final AWSLambda client;

    private static class NamePassword {
        private String name;
        private String password;

        NamePassword(String name, String password) {
            this.name = name;
            this.password = password;
        }

        public String toJSON() {
            // There is probably a generic way of turning this into JSON (perhaps using Gson?)
            return "{"
                   + "\"name\":\"" + name + "\","
                   + "\"password\":\"" + password + "\""
                   + "}";
        }
    }

    LambdaValidator(DeliveryDetails deliveryDetails) {
        this.deliveryDetails = deliveryDetails;

        // (1) Define the AWS Region in which the function is to be invoked
        Regions region = Regions.fromName(deliveryDetails.getRegion());

        // (2) Instantiate AWSLambdaClientBuilder to build the Lambda client
        AWSLambdaClientBuilder builder = AWSLambdaClientBuilder.standard().withRegion(region);

        // (3) Build the client, which will ultimately invoke the function
        this.client = builder.build();
    }

    public void login(final String username, final String password) throws LoginFailedException {
        NamePassword namePassword = new NamePassword(username, password);
        String payload = namePassword.toJSON();

        // (4) Create an InvokeRequest with required parameters
        InvokeRequest req = new InvokeRequest()
                .withFunctionName(deliveryDetails.getAuthenticationLambda())
                .withPayload(payload); // optional

        // (5) Invoke the function and capture response
        InvokeResult result = client.invoke(req);

        // (6) Handle result
        String response = new String(result.getPayload().array(), StandardCharsets.UTF_8);
        if ("\"OK\"".equals(response)) {
            LOG.info("{} is authorized", username);
        } else {
            LOG.info("{} is not authorized", username);
            throw new LoginFailedException();
        }
    }
}
