# aws-smtp-relay
![logo](https://raw.githubusercontent.com/8llouch/aws-smtp-relay/master/docs/aws-smtp-relay-logo.png)

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
 -b,--bindAddress <arg>     Address to listen to
 -c,--configuration <arg>   AWS SES configuration to use
 -h,--help                  Display this help
 -p,--port <arg>            Port number to listen to
 -r,--region <arg>          AWS region to use
```

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
