#! /bin/bash

outputFile="Volume$1.pdf"

#Retrieve the ordered chapters list
ls -d */ | sort -V > chapters.list

#Create tmp dir as container
mkdir workingDir

#Iterate on each one
rows=`cat chapters.list | wc -l`
i=1
pageNumber=0
while [ "$i" -le "$rows" ]; do

    #Retrieve each chapter name
    chapter_i=`sed -n "$i"p chapters.list` 
    #echo "$i: $chapter_i"

    #Retrieve each pages
    ls "$chapter_i"* | sort -V > pages.list
    j=1
    pages=`cat pages.list | wc -l`
    while [ "$j" -le "$pages" ]; do
        page_i=`sed -n "$j"p pages.list` 
        #echo "$page_i"
    
        #Split retrieve extension
        extension=`echo "$page_i" | awk -v FS="." '{ print $NF }'`

        #Copy the file
        #echo "copy $page_i workingDir/$pageNumber.$extension"
        cp "$page_i" "workingDir/$pageNumber.$extension"

        #Increment the pageNumber
        let pageNumber+=1

        let j+=1
    done;

    let i+=1

done

#Create the pdf
convert workingDir/* "$outputFile"

#Clean the evnironment
rm -r workingDir
rm pages.list
rm chapters.list
