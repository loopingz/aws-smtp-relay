package com.loopingz;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apache.commons.io.IOUtils;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.AmazonSimpleEmailServiceException;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;

public class SesSmtpRelay extends SmtpRelay {

  SesSmtpRelay(DeliveryDetails deliveryDetails) {
    super(deliveryDetails);
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
    RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(msg));
    SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage).withSource(from).withDestinations(to);
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
      throw new IOException(e.getMessage(), e);
    }
  }
}
