package br.edu.ifsp.util;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

public class Util {

	private Thread thLoading;
	private JDialog jdLoading;

	public boolean isFileEmpty(File file) {
		if (!file.exists()) {
			return true;
		}
		File depth = new File(file.getAbsolutePath() + File.separator + "Depth");
		File color = new File(file.getAbsolutePath() + File.separator + "Color");
		File coord = new File(file.getAbsolutePath() + File.separator + "Coordinates");
		File seg = new File(file.getAbsolutePath() + File.separator + "Segmentation");

		if (depth.exists() && depth.listFiles().length > 0) {
			return false;
		}

		if (color.exists() && color.listFiles().length > 0) {
			return false;
		}

		if (coord.exists() && coord.listFiles().length > 0) {
			return false;
		}

		if (seg.exists() && seg.listFiles().length > 0) {
			return false;
		}

		return true;
	}

	public long getFileSize(File file) {
		if (!file.exists()) {
			return 0;
		}
		if (file.isFile()) {
			return file.length();
		}
		long size = 0;
		for (File f : file.listFiles()) {
			size += getFileSize(f);
		}
		return size;
	}

	public void openLoadingWindowThread(JFrame father, String title) {
		thLoading = new Thread(new Runnable() {

			@Override
			public void run() {
				createLoadingWindow(father, title);
				showLoadingWindow();
			}
		});
		thLoading.start();
	}

	public void createLoadingWindow(JFrame father, String title) {
		jdLoading = new JDialog(father, title, true);
		jdLoading.setSize(300, 75);
		jdLoading.setLocationRelativeTo(father);
		jdLoading.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		jdLoading.setResizable(false);

		JProgressBar pb = new JProgressBar();
		pb.setIndeterminate(true);
		jdLoading.getContentPane().add(BorderLayout.CENTER, pb);
	}
	
	public void showLoadingWindow(){
		if(jdLoading != null){
			jdLoading.setVisible(true);
		}
	}
	
	public void closeLoadingWindow() {
		try {
			if (jdLoading != null) {
				jdLoading.dispose();
				jdLoading = null;
			}
			//Thread.sleep(500);
			if (thLoading != null) {
				thLoading.join();
				thLoading = null;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
