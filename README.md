# aws-smtp-relay

![logo](https://raw.githubusercontent.com/loopingz/aws-smtp-relay/master/docs/aws-smtp-relay-logo.png)

Current master: 

Circle CI: [![CircleCI](https://circleci.com/gh/loopingz/aws-smtp-relay.svg?style=svg)](https://circleci.com/gh/loopingz/aws-smtp-relay)

Travis CI: [![TravisCI](https://travis-ci.org/loopingz/aws-smtp-relay.svg?branch=master)](https://travis-ci.org/github/loopingz/aws-smtp-relay)

Local SMTP server that convert SMTP message to AWS SES API Call to allow you to use **AWS Role Instance**.

If you follow the AWS SES postfix relay : http://docs.aws.amazon.com/ses/latest/DeveloperGuide/postfix.html

You can have a simple relay for your email, this issue with it is you have to create SMTP credentials.

To follow AWS Best practices you need to rotate those keys at least every 90 days, so the AWS Role are easier to use.

Sending an email with Postfix relay looks like this :

![Postfix Schema](https://raw.githubusercontent.com/loopingz/aws-smtp-relay/master/docs/postfix.png)

Sending an email with aws-smtp-relay looks like this :
![aws-smtp-relay Schema](https://raw.githubusercontent.com/loopingz/aws-smtp-relay/master/docs/aws-smtp-relay.png)

## Compile

Just run the maven project

```
git clone https://github.com/loopingz/aws-smtp-relay.git
cd aws-smtp-relay
mvn clean compile assembly:single
```

## Run

Take the result of your compilation or download the static jar here

```
java -jar aws-smtp-relay.jar
```

**By default the SMTP run on port 10025**

### Arguments

```
usage: aws-smtp-relay
 -a,--sourceArn <arg>          AWS Source ARN of the sending authorization policy
 -b,--bindAddress <arg>        Address to listen to
 -c,--configuration <arg>      AWS SES configuration to use
 -f,--fromArn <arg>            AWS From ARN of the sending authorization policy
 -p,--port <arg>               Port number to listen to
 -r,--region <arg>             AWS region to use
 -smtpH,--smtpHost <arg>       SMTP variable Host
 -smtpO,--smtpOverride <arg>   Not use SES but set SMTP variables t/f true/false
 -smtpP,--smtpPort <arg>       SMTP variable Port
 -smtpU,--smtpUsername <arg>   SMTP variable Username
 -smtpW,--smtpPassword <arg>   SMTP variable password
 -ssm,--ssmEnable              Use SSM Parameter Store to get configuration
 -ssmP,--ssmPrefix <arg>       SSM prefix to find variables default is /smtpRelay
 -ssmR,--ssmRefresh <arg>      SSM refresh rate to reload parameter
 -t,--returnPathArn <arg>      AWS Return Path ARN of the sending authorization policy
 -h,--help                     Display this help
```
"/smtpRelay" can be changed with -ssmP

smtpOverride allows you to point it to a mail catcher such as [MailHog](https://github.com/mailhog/MailHog/) to disable outbound email

If ssm (Simple Systems Manager) Parameter store is used please add to your region
https://ap-southeast-2.console.aws.amazon.com/systems-manager/parameters
once setup, you can change the configuration by restarting the service or rebooting the ec2 instance

```
                /smtpRelay/region 
                /smtpRelay/configuration 
                /smtpRelay/sourceArn 
                /smtpRelay/fromArn
                /smtpRelay/smtpOverride
                /smtpRelay/smtpHost
                /smtpRelay/smtpPort
                /smtpRelay/smtpUsername
                /smtpRelay/smtpPassword
```

"/smtpRelay" can be changed with -ssmP/--ssmPrefix

smtpOverride allows you to point it to a mail catcher such as [MailHog](https://github.com/mailhog/MailHog/) to disable outbound email

# Docker hub

You have a Docker image available

```
docker run -p 10025:10025 loopingz/aws-smtp-relay
```

# IAM Policy

Use this IAM Policy JSON to allow sending emails.

```
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "ses:SendRawEmail",
      "Resource": "*"
    }
  ]
}
```

## IAM Policy for SSM Paramater store access

Use this IAM Policy JSON to allow SSM Paramater variables to be used instead of the command line
Replace ```$SSMKEY``` with KMS key arn for the alias aws/ssm i.e. ```arn:aws:kms:ap-southeast-2:111222333444:key/111111111-2222-3333-4444-555555555555```

```
{
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ssm:DescribeParameters"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "ssm:GetParameters",
        "ssm:GetParameter",
        "ssm:GetParametersByPath"
      ],
      "Resource": [
        "arn:aws:ssm:*:*:parameter/smtpRelay",
        "arn:aws:ssm:*:*:parameter/smtpRelay/*"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "kms:Decrypt"
      ],
      "Resource": [
        "$SSMKEY"
      ]
    }
  ]
}
```

# Changelog

* argument `````--smtpPW````` is now ```--smtpW```
* SSM refresh rate to reload parameter added (```-ssmR,--ssmRefresh <arg>```)
* AWS From ARN of the sending authorization policy added (```-f,--fromArn <arg>```)