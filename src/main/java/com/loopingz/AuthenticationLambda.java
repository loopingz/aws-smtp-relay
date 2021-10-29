package com.loopingz;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;

import java.nio.charset.StandardCharsets;

public class AuthenticationLambda {
    protected DeliveryDetails deliveryDetails;
    AWSLambda client;

    private class EmailPassword {
        public String email;
        public String password;

        EmailPassword(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String toJSON() {
            return "{" +
                    "\"email\":\"" + email + "\"," +
                    "\"password\":\"" + password + "\"" +
                    "}";
        }
    }

    AuthenticationLambda(DeliveryDetails deliveryDetails) {
        this.deliveryDetails = deliveryDetails;

        // (1) Define the AWS Region in which the function is to be invoked
        Regions region = Regions.fromName(deliveryDetails.getRegion());

        // (2) Instantiate AWSLambdaClientBuilder to build the Lambda client
        AWSLambdaClientBuilder builder = AWSLambdaClientBuilder.standard().withRegion(region);

        // (3) Build the client, which will ultimately invoke the function
        this.client = builder.build();
    }

    public boolean authenticate(String email, String password) {
        EmailPassword emailPassword = new EmailPassword(email, password);
        String payload = emailPassword.toJSON();

        // (4) Create an InvokeRequest with required parameters
        InvokeRequest req = new InvokeRequest()
                .withFunctionName(deliveryDetails.getAuthenticationLambda())
                .withPayload(payload); // optional

        // (5) Invoke the function and capture response
        InvokeResult result = client.invoke(req);

        // (6) Handle result
        try {
            String response = new String(result.getPayload().array(), StandardCharsets.UTF_8);
            return "\"OK\"".equals(response);
        }
        catch (Exception e) {
            return false;
        }
    }
}
