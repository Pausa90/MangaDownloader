#!/bin/bash

#Avvia extractPdf su più cartelle
for chapter in */; do
	#Calculate the chapter number by it's name
	number=`echo "$chapter" | awk -v FS="/" '{ print $1 }'`
	#Copy the script into each folder
	cp "extractPdf.sh" "$chapter""extractPdf.sh"
	#Enter in it
	cd "$chapter"
	#Lunch it
	./extractPdf.sh "$number"
	#Move the content to the manga's directory
	mv *.pdf ../
	#Come back to it
	cd ../
	#Notify it
	echo "$chapter done"
done
