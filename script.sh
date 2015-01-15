#!/bin/bash

GINPATH="$GINSIM"

if [ -n "$1" ] ; then 
    GINPATH="$1"
fi 
if [ -z "$GINPATH" ] ; then 
    echo "Please either set environement variable GINSIM to the path of GINsim folder, or provide it as argument"
else
    rsync -av --exclude '*~' GINsim/ $GINPATH
fi
