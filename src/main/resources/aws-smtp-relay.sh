#!/bin/bash
### BEGIN INIT INFO
# Provides:          aws-smtp-relay
# Required-Start:    $remote_fs $network $named
# Required-Stop:     $remote_fs
# Default-Start:     3 4 5
# Default-Stop:      0 1 2 6
# Short-Description: Relay SMTP traffic to AWS SES
# Description:       Mail relay to convert SMTP traffic to Amazon Simple Email Service API calls.
### END INIT INFO

PIDFILE=/var/run/aws-smtp-relay.pid

start() {
  if [ -e $PIDFILE ]; then
     echo "Found $PIDFILE - relay already running?"
     ps -p `head -1 ${PIDFILE}` > /dev/null && exit 1 || echo "Relay process not found; starting..."
  fi

  java -jar /usr/share/aws-smtp-relay/aws-smtp-relay-${project.version}-jar-with-dependencies.jar -b 127.0.0.1 > /var/log/aws-smtp-relay.log 2>&1 &
  echo $! > ${PIDFILE}
}

stop() {
  if [ -e $PIDFILE ]; then
    head -1 $PIDFILE | xargs kill
    rm $PIDFILE
  fi
}

status() {
  if [ -e $PIDFILE ]; then
    PID=`head -1 $PIDFILE`
  fi

  if [[ "$PID" == "" ]]; then
    echo "AWS SMTP relay is not running"
  else
    echo "AWS SMTP relay is running with PID $PID"
  fi
}

case $1 in
  start)
    start
  ;;
  stop)
    stop
  ;;
  status)
    status
  ;;
  restart)
    stop
    start
  ;;
esac

exit 0
