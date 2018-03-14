FROM java
MAINTAINER Damien Metzler <dmetzler@nuxeo.com>
MAINTAINER Remi Cattiau <remi@cattiau.com>
MAINTAINER Morgan Christiansson <docker@mog.se>

ADD target/*-jar-with-dependencies.jar /usr/share/aws-smtp-relay/aws-smtp-relay.jar

ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/aws-smtp-relay/aws-smtp-relay.jar", "-b", "0.0.0.0"]

EXPOSE 10025
