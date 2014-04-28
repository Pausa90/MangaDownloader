package it.iuland.mangaDownloader.view;

import it.iuland.mangaDownloader.model.MangaDownloader;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class ResumeDownloadView extends View{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField chapterTF;
	private boolean linuxOS = true;

	public ResumeDownloadView(){
		this.mangaUrlTF = new JTextField();
		this.chapterTF = new JTextField();
		this.pathTF = new JTextField();
		this.pathFC = new JFileChooser();
		this.pathFC.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		this.startBT = new JButton("Start");
		this.stopBT = new JButton("Stop");
		
		this.startBT.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String mangaName = "ignore";
				URL mangaUrl = null;
				try {
					mangaUrl = new URL(mangaUrlTF.getText());
				} catch (MalformedURLException e) {
					newPopUp("Incorrect URL");
				}
				
				String path = pathTF.getText();
				final String chapter = chapterTF.getText();
				path = this.completePath(path);
				if (path.equals(""))
					newPopUp("Path fields are empty");
				else {
					
					try{
						final double chapterValidation;
						if (chapter.equals("")) chapterValidation=0;
						else	chapterValidation=Double.parseDouble(chapter);
						
						addBars();
						final MangaDownloader mangaDownloader = new MangaDownloader(mangaName,mangaUrl, path, linuxOS);
						Thread thread = new Thread() { public void run() {mangaDownloader.resumeDownload(chapterValidation,getViewInstance());}};
						setThreadInstance(thread);
						try{
							thread.start();
						} catch (Exception e) {
							newPopUp("Internal Error: " + e.toString());
						}
					} catch (NumberFormatException e){
						newPopUp("Chapter value must be a number");
					}
					

				}
			}

			private String completePath(String path) {
				String[] linux = path.split("/");
				if (linux.length>1)
					return path+"/";
				linuxOS = false;
				return path+"\\";
			}
		});
		
		this.stopBT.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Thread thread = getThreadInstance();
				if (!Thread.interrupted())
					thread.interrupt();				
			}
		});
		
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttons.add(startBT);
		buttons.add(stopBT);
		
		JPanel pathPanel = new JPanel();
		pathPanel.setLayout(new BoxLayout(pathPanel, BoxLayout.LINE_AXIS));
		JButton selectFoldier = new JButton("Select Foldier");
		pathPanel.add(pathTF);
		pathPanel.add(selectFoldier);
		
		selectFoldier.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int returnValue = pathFC.showOpenDialog(getParent());
				if (returnValue == JFileChooser.APPROVE_OPTION)
					pathTF.setText(pathFC.getSelectedFile().getAbsolutePath());
				
			}
		});
		
		
		
		this.getContentPane().setLayout(new GridLayout(0,1));
		this.getContentPane().add(new JLabel("Manga download from www.mangaeden.com"));		
		this.getContentPane().add(new JLabel("Insert manga url:"));
		this.getContentPane().add(this.mangaUrlTF);
		this.getContentPane().add(new JLabel("Insert chapter to start: (if empty, it evaluates by itself)"));
		this.getContentPane().add(this.chapterTF);
		this.getContentPane().add(new JLabel("Insert path manually or click the left button"));
		this.getContentPane().add(pathPanel);
		this.getContentPane().add(new JLabel());
		this.getContentPane().add(buttons);
		
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}
	
	
}