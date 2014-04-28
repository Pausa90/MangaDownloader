package it.iuland.mangaDownloader;
import it.iuland.mangaDownloader.view.CompleteDownloadView;
import it.iuland.mangaDownloader.view.ResumeDownloadView;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

public class Main{
		
	public static void main(String[] args){
		
		
		final JFrame choiceFrame = new JFrame();
		choiceFrame.getContentPane().setLayout(new GridLayout(0,1));
		choiceFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		choiceFrame.add(new JLabel("Choice"));
		choiceFrame.add(new JTextArea("Full Download: download the entire manga\nResume Download: download manga from a specific chapter"));
		
		JButton fullDownload = new JButton("Full Download");
		JButton resumeDownload = new JButton("Resume Download");
		
		fullDownload.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				CompleteDownloadView completeView = new CompleteDownloadView();		
				completeView.setSize(600,400);	
				completeView.setVisible(true);	
				choiceFrame.setVisible(false);
			}
		});
		
		resumeDownload.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ResumeDownloadView resumeView = new ResumeDownloadView();		
				resumeView.setSize(600,400);	
				resumeView.setVisible(true);	
				choiceFrame.setVisible(false);
			}
		});
		
		choiceFrame.add(fullDownload);
		choiceFrame.add(resumeDownload);
		
		choiceFrame.setSize(400,400);	
		choiceFrame.setVisible(true);	
		
	}

	

}