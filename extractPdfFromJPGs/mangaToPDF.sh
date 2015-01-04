#!/bin/bash

#Avvia extractPdf su più cartelle

for chapter in */; do
	cp "extractPdf.sh" "$chapter""extractPdf.sh"
	cd "$chapter"
	./extractPdf.sh
	mv *.pdf ../
	cd ../
	echo "$chapter done"
	#rm -R "$chapter"
done
