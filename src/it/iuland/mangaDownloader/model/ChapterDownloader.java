package it.iuland.mangaDownloader.model;

import it.iuland.mangaDownloader.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ChapterDownloader {

	private String url;
	private String number;
	private String name;
	private List<String> pagesUrl;
	private final String siteName = "http://www.mangaeden.com";
	private final int concurrencyLevel = 5;
	private int activeThread;
	private int lastChapterDownloading;
	private List<String> imagesUrl;
	
	private View view;
	private String path;
	private MangaDownloader mangaDownloader;
	private boolean linuxOS;
	private String mangaName;

	public ChapterDownloader(String url, String name, boolean linuxOS, String mangaName) {
		this.url = url;
		this.name = name;
		this.setNumber();
		this.pagesUrl = new ArrayList<String>();
		//this.pagesUrl.add(url);
		this.activeThread = 0;
		this.lastChapterDownloading = 0;
		this.linuxOS = linuxOS;
		this.mangaName = mangaName;
	}

	private void setNumber() {
		String[] splitted = this.url.split("/");
		this.number = splitted[splitted.length-2];
	}
	
	public String getNumber(){
		return this.number;
	}

	private void extractPages() {
		Document indexHtml = null;
		try {
			indexHtml = Jsoup.connect(this.url).timeout(3000).get();
		} catch (IOException e) {
			this.view.newPopUp("Internal Error:\n"+ e.toString());
		}
		
		/** Change after MangaEden changed **/
		/*Elements link_nodes = indexHtml.select("div.pagination > a"); //in xpath: //div[@class="pagination"]/a
		
		for (Element element : link_nodes) {			
			String partial_url = element.attr("href");
			if (partial_url.length()>0)
				this.pagesUrl.add(siteName + partial_url);
		}		
		//Elimino le ripetizioni (prev e next)
		try{
			if (Integer.parseInt(this.number) > 1)
				this.pagesUrl.remove(1);
		} catch (NumberFormatException e){
			if (Double.parseDouble(this.number) > 1)
				this.pagesUrl.remove(1);
		}
		this.pagesUrl.remove(this.pagesUrl.size()-1);*/
		Elements link_nodes = indexHtml.select("select#pageSelect > option"); //in xpath: //select[@id="pageSelect"]/option
		for (Element element : link_nodes) {
			String partial_url = element.attr("value");
			this.pagesUrl.add(siteName + partial_url);
		}
	}
	

	private List<String> extractImages() throws java.net.SocketTimeoutException {
		
		List<String> images = new ArrayList<String>();
		for (String page : this.pagesUrl){
			Document html_document = null;
			try {
				html_document = Jsoup.connect(page).timeout(6000).get();
			} catch (IOException e) {
				this.view.newPopUp("Internal Error:\n"+ e.toString());
			}
		
			Elements link_nodes = html_document.select("img[id=mainImg]"); //in xpath: //image[@id=mainImg]
		
			for (Element element : link_nodes) {
				this.addUrl(images, element.attr("src"));
			}		
		}
		return images;
	}
	
	private void addUrl(List<String> images, String url) {
		//Le nuove modifiche di MangaEden hanno introdotto i cdn, pertanto va eliminato l'iniziale //
		url = url.substring(2);
		if (this.isWellformed(url))
			images.add(url);
		else
			images.add("http:" + url);		
	}

	private boolean isWellformed(String url) {
		return url.matches("^(http|https|www)://");
	}

	public void fetch(String path,View view, MangaDownloader istanceMangaDownloader) {
		this.view = view;
		this.mangaDownloader = istanceMangaDownloader;
		
		extractPages();
		
		this.view.print("Processing (can take some time)");
		this.imagesUrl = new LinkedList<String>();
		boolean downloaded = false;
		while (!downloaded){
			try{
				this.imagesUrl = extractImages();	
				downloaded = true;
			} catch (SocketTimeoutException e){
				downloaded = false;
			}
		}
		
		this.view.print("Creating chapter " + this.number + " foldier");
		this.view.setPagesNumber(imagesUrl.size());
		
		//Creo la directory
		if (this.linuxOS) this.path = path+this.name+"/";
		else this.path = path+this.mangaName+" "+this.number+"\\";
		try {
			this.makeDirectory(this.path);
		} catch(FileNotFoundException e){
			this.view.newPopUp("Can't write on disk:\n"+ e.toString());
		}

		this.view.print("Download chapter " + this.number);		
		this.download(0);
	}
	
	private void makeDirectory(String path) throws FileNotFoundException{
		new File(path).mkdirs();
	}
	
	private void download(final int number) {
		this.activeThread++;
		new Thread(){ public void run(){ downloadThread(imagesUrl.get(number), number); } }.start();
		this.threadHandler();
	}

	private void downloadThread(String url, int number ){
		try {				
			this.downloadImage(url,path+(number+1)+".jpg");
			this.view.updatePagesBar();
			this.activeThread--;
			this.threadHandler();
		} catch (IOException e) {
			this.view.newPopUp("Can't write on disk:\n"+ e.toString());
			System.exit(1);
		}
	}

	private void threadHandler() {
		if (this.activeThread<this.concurrencyLevel){		
			this.lastChapterDownloading++;
			if (this.lastChapterDownloading<this.imagesUrl.size()){	
				this.download(this.lastChapterDownloading);
			} else if (this.activeThread==0){
				this.mangaDownloader.chapterEnd();
			}
		}
	}

	//controllare se è un png
	private void downloadImage(String stringUrl, String file_name) throws IOException {
		//Fix the url
		String protocol = stringUrl.split(":")[0];
		stringUrl = protocol + "://" + stringUrl.substring(protocol.length()+1);
		
		//Fix the exstension
		String[] file_name_splitted = file_name.split("\\.");
		String[] stringUrl_splitted = stringUrl.split("\\.");
		String file_name_extension = file_name_splitted[file_name_splitted.length-1];
		String image_extension = stringUrl_splitted[stringUrl_splitted.length-1];
		if (!file_name_extension.equals(image_extension)) {
			file_name = file_name.substring(0, file_name.length()-file_name_extension.length()) + image_extension;
		}
		
		
		URL url = new URL(stringUrl);
		InputStream is = url.openStream();
		OutputStream os = new FileOutputStream(file_name);

		byte[] b = new byte[2048];
		int length;

		while ((length = is.read(b)) != -1) {
			os.write(b, 0, length);
		}
		is.close();
		os.close();
	}
		
//	public String toString(){
//		return "chapter: "+this.number;
//	}
	
	public String toString(){
		return "chapter number: " + this.number + " name: " + this.name + " url: " + this.url;
	}
	
}