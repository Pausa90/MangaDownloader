package it.iuland.mangaDownloader.model;
import it.iuland.mangaDownloader.exception.DirectoryCreationFailed;
import it.iuland.mangaDownloader.view.View;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MangaDownloader{
	private final String siteName = "http://www.mangaeden.com";
	private String mangaName;
	private URL mangaUrl;
	private Document indexHtml;
	private List<ChapterDownloader> chapters;
	private String path;
	private final MangaDownloader istance = this;
	private final boolean linuxOS;
	
	private View view;
	private int currentChapter = 0;
	
	public MangaDownloader(String mangaName, URL mangaUrl, String path, boolean linuxOS) {
		this.mangaName = mangaName;
		this.mangaUrl = mangaUrl;
		this.path = path;
		this.indexHtml = null;
		this.chapters = new ArrayList<ChapterDownloader>();
		this.linuxOS = linuxOS;
	}

	public String getSiteName(){
		return this.siteName;
	}
	
	private void extractChapters(){
		try {
			this.indexHtml = Jsoup.connect(this.mangaUrl.toString()).timeout(3000).get();
		} catch (IOException e) {
			this.view.newPopUp("Internal Error:\n"+ e.toString());
		}
		Elements link_nodes = indexHtml.select("tbody a");
		
		String manga_name = getNameFromUrl();
		
		for (Element element : link_nodes) {
			String partial_url = element.attr("href");
			String name = element.text();
			if (partial_url.contains(manga_name)){			
				String complete_url = siteName + partial_url;
				ChapterDownloader chapter = new ChapterDownloader(complete_url,name,this.linuxOS,this.mangaName);
				try {
					Double.parseDouble(chapter.getNumber()); //alcuni link potrebbero non essere riferiti a capitoli
					this.chapters.add(chapter);
				} catch (NumberFormatException e){
					//invalid url: do nothing
				}				
			}
		}
		
	}
	
	private String getNameFromUrl() {
		String[] splitted = this.mangaUrl.toString().split("/");
		return splitted[splitted.length-1];
	}
	
	private void initializeEnvironment() {
		//Se non posso creare la directory e non esiste già lancio un eccezione
		if (!new File(path+mangaName).mkdirs() && !(new File(path+mangaName).exists()) ){
			try {
				throw new DirectoryCreationFailed();
			} catch (DirectoryCreationFailed e) {
				this.view.print("Can't write in: " + path);
				e.printStackTrace();
			}
		}
	}
	
	public void startDownload(final View view) {	
		this.view = view;
		
		extractChapters();
		this.view.setChaptersNumber(this.chapters.size());
		this.chapters = revertList(this.chapters);
		this.view.print("Making Foldier " + this.mangaName);
		initializeEnvironment();
	
		if (this.linuxOS)
			this.path = path+mangaName+"/";
		else
			this.path = path+mangaName+"\\";
		this.downloadChapter();
	}
	
	private void downloadChapter(){
		this.chapters.get(this.currentChapter).fetch(this.path, view, this.istance);
	}
	
	public void chapterEnd(){
		this.view.updateChaptersBar();
		this.currentChapter++;
		if (this.currentChapter<this.chapters.size())
			this.downloadChapter();
		else
			this.view.newPopUp("Download Completed");				
	}
	
	private List<ChapterDownloader> revertList(List<ChapterDownloader> list) {
		List<ChapterDownloader> newList = new ArrayList<ChapterDownloader>();
		
		for (int i=list.size()-1; i>=0; i--){
			newList.add(list.get(i));
		}
		
		return newList;
	}
	
	public void resumeDownload(double chapter, final View view) {
		this.view = view;
		this.mangaName = this.getNameFromPath(this.path);
		if (chapter<=0)
			chapter = evaluateChapter();
		if (chapter>=0){	
			extractChapters();
			this.chapters = revertList(this.chapters);
			fixList(chapter);
			int chaptersToDownload = this.getChaptersToDownloadNumber(chapter);
			this.view.setChaptersNumber(chaptersToDownload);		
			this.downloadChapter();			
		}
		
	}
	
	private int getChaptersToDownloadNumber(double chapter) {
		for (int i=0; i<this.chapters.size(); i++) {
			ChapterDownloader chapter_i = this.chapters.get(i);
			double chapter_i_number = Double.parseDouble(chapter_i.getNumber());
			if (chapter_i_number == chapter)
				return this.chapters.size() - i;
		}
		//Should be handled better
		return this.chapters.size();
	}
	
	private double evaluateChapter() {
		try{
			File[] chaptersDirectory = new File(this.path).listFiles(); 
			List<Double> chaptersDouble = this.extractDoubleFromFileName(chaptersDirectory);				
			return Collections.max(chaptersDouble);
		} catch (NumberFormatException e){
			this.view.newPopUp("Cant Evaluate Chapter");
			return -1;
		} catch (NullPointerException e) {
			this.view.newPopUp("Cant Evaluate Chapter");
			return -1;
		}
	}

	private List<Double> extractDoubleFromFileName(File[] chaptersDirectory) throws NumberFormatException, NullPointerException{
//		List<Double> chaptersDouble = new LinkedList<Double>();
//		String fileName;
//		String[] splitted;
//		int index = this.mangaName.split(" ").length;
//		for (File file : chaptersDirectory){
//			fileName = file.getName();	
//			splitted = fileName.split(" ");
//			fileName = splitted[index];
//			if (this.linuxOS){
//				splitted = fileName.split(":");
//				chaptersDouble.add(new Double(splitted[0]));
//			} else 
//				chaptersDouble.add(new Double(fileName));
//		}
//		return chaptersDouble;
		List<Double> chaptersDouble = new LinkedList<Double>();
		Pattern pattern = Pattern.compile(" [0-9]+:");
		
		for (File file : chaptersDirectory){
			Matcher matcher = pattern.matcher(file.getName());
			if (matcher.find()) {
				String chapterNumber = matcher.group();
				chapterNumber = chapterNumber.substring(0, chapterNumber.length()-1);
				chaptersDouble.add(new Double(chapterNumber));
			}
			
		}
		return chaptersDouble;
	}

	private String getNameFromPath(String path){
		String[] splitted;
		if (this.linuxOS)
			splitted = path.split("/");
		else
			splitted = path.split("\\\\");
		return splitted[splitted.length-1];	
	}
	
	//Dalla cartella selezionata estraggo il nome del manga, eliminandolo dal path
	//(viene inserito automaticamente dal sistema automaticamente)
	private void fixList(double chapterFilter) {
		ChapterDownloader chapter;
		for (int i=0; i<this.chapters.size(); i++){
			chapter = this.chapters.get(i);
			try{
				double chapterNumber = Double.parseDouble(chapter.getNumber());
				if (chapterNumber==chapterFilter){
					this.currentChapter=i;
					return;
				}				
			} catch (NumberFormatException e){
				this.view.newPopUp("Internal Error:\n"+ e.toString());
			}
			
		}
	}
	
}