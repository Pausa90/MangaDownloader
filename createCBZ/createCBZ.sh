#!/bin/bash

for chapter in *; do

	if [[ -d $chapter ]]; then	
		zip -r -q "$chapter".cbr "$chapter"
	
		#Notify it
		echo "$chapter done"
	fi
done
