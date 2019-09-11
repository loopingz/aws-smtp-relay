package com.loopingz;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.server.SMTPServer;

public abstract class SmtpRelay implements SimpleMessageListener {
    protected DeliveryDetails deliveryDetails;
    
    SmtpRelay(DeliveryDetails deliveryDetails) {
    	this.deliveryDetails = deliveryDetails;
    }
    
	@Override
	public boolean accept(String from, String recipient) {
		return true;
	}

    void run() throws UnknownHostException {
        SMTPServer.Builder builder = new SMTPServer.Builder();
        builder.bindAddress(InetAddress.getByName(deliveryDetails.getBindAddress()))
                .port(deliveryDetails.getPort())
                .simpleMessageListener(this);
        SMTPServer smtpServer = builder.build();
        smtpServer.start();
    }

}
