#!/bin/sh

rm -f /var/run/chrony/chronyd.pid
chronyd -d &

/opt/tsa/bin/tsa-json
