package br.edu.ifsp.util;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class Save {

	public File getFile(Component father){
		JFileChooser chooser = new JFileChooser();
		if (chooser.showSaveDialog(father) == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		return null;
	}
	
	public void saveFile(Component father, File file, Float[][][] moviments) {
		float[][][] data = new float[moviments.length][][];
		for(int i = 0; i < moviments.length; i++){
			
			data[i] = new float[moviments[i].length][];
			for(int j = 0; j < moviments[i].length; j++){
				
				data[i][j] = new float[moviments[i][j].length];
				for(int k = 0; k < moviments[i][j].length; k++){
					
					data[i][j][k] = moviments[i][j][k];
				}
			}
		}
		saveFile(father, file, data);
	}
	
	public void saveFile(Component father, File file, float[][][] moviments) {
		if (file.exists()) {
			if (JOptionPane.showConfirmDialog(father,
					"There is already a file with this name.\n" + "Would you like to override it?", "Alert",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
				return;
			}
		}
		try {
			Files.write(Paths.get(file.toURI()), getCoords(moviments).getBytes());
		} catch (IOException e) {
			JOptionPane.showMessageDialog(father, "An error happened. Try again later!\n" + "Message: " + e.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	private String getCoords(float[][][] coor) {
		String coords = new String();
		if(coor == null){
			return "";
		}
		for (int h = 0; h < coor.length; h++) {
			coords += h;
			float[][] joints = coor[h];

			for (int i = 0; i < joints.length; i++) {
				coords += Arrays.toString(joints[i]);
			}
			coords += "\n";
		}
		return coords;
	}
	
}
