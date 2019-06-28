FROM alpine
MAINTAINER Damien Metzler <dmetzler@nuxeo.com>
MAINTAINER Remi Cattiau <remi@cattiau.com>
MAINTAINER Morgan Christiansson <docker@mog.se>

ADD target/aws-smtp-relay /usr/local/bin/aws-smtp-relay

ENTRYPOINT ["/usr/local/bin/aws-smtp-relay", "-b", "0.0.0.0"]

EXPOSE 10025
