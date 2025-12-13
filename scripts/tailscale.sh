#!/usr/bin/bash

INTERVAL_S=10
SENDMSG="/pulsar-scripts/sendmessage.sh"
filter() {
	/usr/bin/tailscale status --json | /usr/sbin/jq '.Peer[] as $peer | {name: $peer.HostName, online: $peer.Online, dns: $peer.DNSName}' -S
}


prevtss=$(filter)
while : 
do
	currtss=$(filter)
	diff <(echo "$prevtss") <(echo "$currtss") > test.dat
	div=$?
	if [[ $div -ne 0 ]] then
		$SENDMSG 6 network "\"Tailnet status changed\""
	fi

	prevtss=$(echo "$currtss")

	sleep $INTERVAL_S
done
