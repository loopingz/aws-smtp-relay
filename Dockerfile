FROM maven:alpine
MAINTAINER Damien Metzler <dmetzler@nuxeo.com>
MAINTAINER Remi Cattiau <remi@cattiau.com>

RUN mkdir -p /opt/aws-smtp-relay-src/
RUN mkdir -p /usr/share/aws-smtp-relay
ADD . /opt/aws-smtp-relay-src/
RUN cd /opt/aws-smtp-relay-src/ && mvn package
RUN cp /opt/aws-smtp-relay-src/target/*-with-dependencies.jar /usr/share/aws-smtp-relay/aws-smtp-relay.jar


ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/aws-smtp-relay/aws-smtp-relay.jar", "-b", "0.0.0.0"]
EXPOSE 10025
