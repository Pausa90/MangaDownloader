function printBlue () {
	#echo -e "\e[1;34m$1\e[0m"
	inutile=""
}

function elaborateFile () {
	file="$1"
	destDir="$wokingDir""/$2/"
	iterativeFileName=$(echo "$file" | awk -v FS="/" '{print $NF}')
	printBlue "$iterativeFileName"
	cp "$file" "$destDir" 
	iterativeFileNameNoExtension=$(echo "$file" | awk -v FS="/" '{print $NF}' | awk -v FS="." '{print $1}')
	let "newiterativeFileName=iterativeFileNameNoExtension+numPage"
	mv "$destDir""/$iterativeFileName" "$destDir""/0$newiterativeFileName"".jpg"
	convert "$destDir""/0$newiterativeFileName"".jpg" "$destDir""/0$newiterativeFileName"".jpg"
	printBlue "Copied with numPage=$newiterativeFileName"
}

function extractPDF () {
	cd "$1"
	#pdfName="$fileName""0$2"".pdf"
	pdfName="$fileName"".pdf"
	convert $(ls * | sort -V) "$pdfName"
	mv "$pdfName" ..
	cd "$originalPath"
}


#Preparazione Variabili 
scriptName=$(echo "$0" | awk -v FS="/" '{print $NF}')
mangaName=$(pwd | awk -v FS="/" '{print $NF}')
#fileName="$mangaName"
fileName="Volume0$mangaName"
numPage=0
#echo "How many chapter per pdf?"
#read chaptersNumbers
chaptersNumbers=100
#echo "Start working"

#Inizializzazione ambiente con cartella di lavoro e lista file ordinata
originalPath=$(pwd)
wokingDir=$(pwd)"/working"
printBlue "Create working foldier"
mkdir "$wokingDir"

dirList="files"
echo "$(ls | sort -V )" > "$dirList"
numRows=$(cat "$dirList" | wc -l)


#Inizio elaborazione
i=1
iterativeCN=1 #iterative chapter number
currentCN=0 #current chapter number
while [ "$i" -le "$numRows" ]; do
	chapter=$(cat "$dirList" | awk -v FS="\n" -v RS="$" -v V=$i '{print $V}')

	if  test "$chapter" != "working"  -a  "$chapter" != "$dirList" -a "$chapter" != "$scriptName" ; then 
		printBlue "Chapter: $chapter"
		
		isNewFoldier=$(expr $(expr $iterativeCN - 1) % $chaptersNumbers)
		if test "$isNewFoldier" -eq "0"; then
			#Se non è la prima cartella creo il pdf
			if test "$currentCN" -gt "0"; then
				extractPDF "$wokingDir""/$currentCN" "$currentCN"
				echo "Chapter $currentCN done"
			fi
			let currentCN+=1
			mkdir "$wokingDir""/$currentCN"
		fi
		
		for file in "$chapter"/*; do
			elaborateFile "$file" "$currentCN"
		done
		let "numPage+=$(ls "$chapter" | wc -l)"
		printBlue "numPage = $numPage"
		let iterativeCN+=1
	fi

 	let i+=1
 done
#L'ultimo pdf non viene automaticamente estratto
extractPDF "$wokingDir""/$currentCN" "$currentCN"

for pdf in "$wokingDir/"*.pdf; do
	mv "$pdf" .
done

rm -R "$wokingDir"
rm "$dirList"

#echo "Done"
