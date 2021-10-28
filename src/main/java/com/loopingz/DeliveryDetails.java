package com.loopingz;

import java.net.InetAddress;

import com.amazonaws.util.StringUtils;

public class DeliveryDetails {

  private static final int DEFAULT_PORT = 10025;
  private String bindAddress;
  private String port;

  private String region;
  private String configuration;

  private String authorizationLambda;

  private String sourceArn;
  private String returnPathArn;

  private String fromArn;
  private String smtpOverride;
  private String smtpHost;
  private String smtpPort;
  private String smtpUsername;
  private String smtpPassword;

  public String getBindAddress() {
    if (StringUtils.isNullOrEmpty(bindAddress)) {
      return InetAddress.getLoopbackAddress().getHostAddress();
    }
    return bindAddress;
  }

  public void setBindAddress(String bindAddress) {
    this.bindAddress = bindAddress;
  }

  public int getPort() {
    if (StringUtils.isNullOrEmpty(port)) {
      return DEFAULT_PORT;
    }
    return Integer.parseInt(port);
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public boolean hasRegion() {
    return !StringUtils.isNullOrEmpty(region);
  }

  public String getConfiguration() {
    return configuration;
  }

  public void setConfiguration(String configuration) {
    this.configuration = configuration;
  }

  public boolean hasConfiguration() {
    return !StringUtils.isNullOrEmpty(configuration);
  }

  public String getSourceArn() {
    return sourceArn;
  }

  public boolean hasSourceArn() {
    return !StringUtils.isNullOrEmpty(sourceArn);
  }

  public void setSourceArn(String sourceArn) {
    this.sourceArn = sourceArn;
  }

  public String getSmtpOverride() {
    return smtpOverride;
  }

  // if starts with t its true, else we don't override
  public boolean isSmtpOverride() {
    return !StringUtils.isNullOrEmpty(smtpHost) && StringUtils.beginsWithIgnoreCase(smtpOverride, "t");
  }

  public void setSmtpOverride(String smtpOverride) {
    this.smtpOverride = smtpOverride;
  }

  public String getSmtpHost() {
    return smtpHost;
  }

  public void setSmtpHost(String smtpHost) {
    this.smtpHost = smtpHost;
  }

  public String getSmtpPort() {
    return smtpPort;
  }

  public void setSmtpPort(String smtpPort) {
    this.smtpPort = smtpPort;
  }

  public String getSmtpUsername() {
    return smtpUsername;
  }

  public void setSmtpUsername(String smtpUsername) {
    this.smtpUsername = smtpUsername;
  }

  public String getSmtpPassword() {
    return smtpPassword;
  }

  public void setSmtpPassword(String smtpPassword) {
    this.smtpPassword = smtpPassword;
  }

  public String getReturnPathArn() {
    return returnPathArn;
  }

  public void setReturnPathArn(String returnPathArn) {
    this.returnPathArn = returnPathArn;
  }

  public boolean hasReturnPathArn() {
    return !StringUtils.isNullOrEmpty(returnPathArn);
  }

  public String getFromArn() {
    return fromArn;
  }

  public void setFromArn(String fromArn) {
    this.fromArn = fromArn;
  }

  public boolean hasFromArn() {
    return !StringUtils.isNullOrEmpty(returnPathArn);
  }

  public String getAuthorizationLambda() {
    return authorizationLambda;
  }

  public void setAuthorizationLambda(String authorizationLambda) {
    this.authorizationLambda = authorizationLambda;
  }

  public boolean hasAuthorizationLambda() {
    return !StringUtils.isNullOrEmpty(authorizationLambda);
  }
}
