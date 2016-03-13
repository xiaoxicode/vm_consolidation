#!/bin/bash

# $1 represent first parameter, the name of vm
echo "111111" | sudo -S virsh dumpxml $1| head -n 10