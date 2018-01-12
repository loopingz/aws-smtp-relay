FROM java
MAINTAINER Damien Metzler <dmetzler@nuxeo.com>
ARG IMAGE_VERSION
LABEL version $IMAGE_VERSION

ARG JAR_FILE
ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/aws-smtp-relay/aws-smtp-relay.jar", "-b", "0.0.0.0"]
EXPOSE 10025
ADD $JAR_FILE /usr/share/aws-smtp-relay/aws-smtp-relay.jar