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

PATH=/sbin:/usr/sbin:/bin:/usr/bin
NAME=aws-smtp-relay
DESC="AWS SMTP Relay"
CWD="/usr/share/$NAME"
PIDFILE="/var/run/$NAME.pid"
CMD=/usr/bin/aws-smtp-relay
USER="$NAME"
GROUP="$NAME"

OPTS=

# Read configuration variable file if it is present
[ -r /etc/default/aws-smtp-relay ] && . /etc/default/aws-smtp-relay

# Define LSB log_* functions.
. /lib/lsb/init-functions

start() {
  log_daemon_msg "Starting $DESC" "$NAME"

  start-stop-daemon --start --quiet --make-pidfile --background \
    --pidfile $PIDFILE \
    --chuid $USER \
    --user $USER \
    --chdir $CWD \
    --exec $CMD \
    -- $OPTS

  case "$?" in
    0)
      log_end_msg 0 ;;
    1)
      log_progress_msg "already started"
      log_end_msg 0 ;;
    *)
      log_end_msg 1 ;;
  esac
}

stop() {
  log_daemon_msg "Stopping $DESC" "$NAME"

  start-stop-daemon --stop --quiet --retry=TERM/30/KILL/5 \
    --user $USER \
    --pidfile $PIDFILE \
    --exec $CMD \

  case "$?" in
    0)
      log_end_msg 0 ;;
    1)
      log_progress_msg "already stopped"
      log_end_msg 0 ;;
    *)
      log_end_msg 1 ;;
  esac
}

status() {
  status_of_proc -p $PIDFILE $CMD "$NAME" && exit 0 || exit $?
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
