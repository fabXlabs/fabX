#!/bin/bash

PWD="$1"

if [ $# -eq 1 ]; then
    echo "fabXfabXfabX${#PWD}$PWD"
    echo -n "fabXfabXfabX${#PWD}$PWD" | openssl dgst -binary -sha256 | openssl base64
else
    echo "usage: $0 [password]"
fi
