#!/bin/bash

# $1 the name of vm
# $2 the new number of vcpu
echo "111111" | sudo -S virsh setvcpus $1 $2