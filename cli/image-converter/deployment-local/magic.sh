#!/bin/bash

echo " param 1 : $1"
echo " param 2 : $2"
echo " param 3 : $3"

set -x

dcraw -v -c -w -t $1 -T $2 > $3
