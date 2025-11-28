#!/usr/bin/env bash

# Script for cleaner sending of messages for Pulsar
# Uses txmppc (https://holmeinbuch.de/repo/txmppc/file?name=txmppc.c)

set -eu

CLIENT_JID=""
SERVER_JID=""
SERVER_PASS=""
SERVER_ADDR=""
SENDXMPP="/home/emlyn/bin/sendxmpp"

function sendMessage() {
	time="$(/usr/sbin/date +%s)";

	sev=$1
	class=$2
	body=$3
	
	echo "m $CLIENT_JID {\"class\":$class, \"sev\":$sev, \"timestamp\":$time, \"body\":$body}" | $SENDXMPP $SERVER_JID $SERVER_PASS $SERVER_ADDR
}

set +eu
if [[ -z "$3" || -n "$4" ]] then
	echo "sendMessage [sev] [class] [body]"
	exit 1
fi
set -eu

sendMessage $1 $2 "$3"
