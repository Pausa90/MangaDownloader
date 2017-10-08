package it.iuland.mangaDownloader.view;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

public class View extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Thread thread;
	protected JLabel label;
	protected JProgressBar chaptersBar;
	protected JProgressBar pagesBar;
	protected JTextField mangaNameTF;
	protected JTextField mangaUrlTF;
	protected JTextField pathTF;
	protected JFileChooser pathFC;
	protected JButton pathBT;
	protected JButton startBT;
	protected JButton stopBT;

	protected View getViewInstance(){
		return this;
	}
	
	protected void setThreadInstance(Thread thread){
		this.thread = thread;
	}
	
	protected Thread getThreadInstance(){
		return this.thread;
	}
	
	public void newPopUp(String text){
		JOptionPane.showMessageDialog(null,text);
	}
	

	public void addBars(){
		this.label = new JLabel("");
		this.chaptersBar = new JProgressBar(0, 100);
		this.pagesBar = new JProgressBar(0, 100);
		this.getContentPane().add(new JLabel("Chapters Bar:"));
		this.getContentPane().add(this.chaptersBar);
		this.getContentPane().add(new JLabel("Pages Bar:"));
		this.getContentPane().add(this.pagesBar);
		this.getContentPane().add(this.label);
	}
	
	public void setChaptersNumber(int chaptersNumber){
		this.chaptersBar.setMaximum(chaptersNumber);
	}
	
	public void setPagesNumber(int pagesNumber){
		this.pagesBar.setMaximum(pagesNumber);
	}
	
	public void updateChaptersBar(){
		this.incrementBar(this.chaptersBar);
		this.pagesBar.setValue(0);
	}
	
	public void updatePagesBar(){
		this.incrementBar(this.pagesBar);
	}

	protected void incrementBar(JProgressBar bar) {
		bar.setValue(bar.getValue()+1);		
	}
	
	public void print(String string){
		this.label.setText(string);
	}
	
}
