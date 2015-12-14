package br.edu.ifsp.util;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import br.edu.ifsp.editor.Editor;

public class Load {

	public File open(Component father) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (chooser.showOpenDialog(father) == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			if (!f.getName().endsWith(".txt")) {
				return null;
			}
			return f;
		}
		return null;
	}

	public float[][][] loadFile(File arquivo) {
		Charset c = StandardCharsets.UTF_8;
		Path get = Paths.get(arquivo.toURI());
		List<String> readAllLines = new ArrayList<>();
		try {
			readAllLines = Files.readAllLines(get, c);
		} catch (IOException ex) {
			Logger.getLogger(Editor.class.getName()).log(Level.SEVERE, null, ex);
		}

		float[][][] moves = new float[readAllLines.size()][15][3];
		for (int i = 0; i < readAllLines.size(); i++) {

			String linha = readAllLines.get(i);
			if (linha == null || linha.equals("")) {
				continue;
			}

			linha = linha.replaceAll("^\\d*", "");
			linha = linha.replaceAll("^\\[", "");
			linha = linha.replaceAll("]$", "");

			String[] jointsTemp = linha.split("]\\[");
			for (int j = 0; j < jointsTemp.length; j++) {
				String coordTemp[] = jointsTemp[j].split(", ");
				try {
					moves[i][j][0] = Float.parseFloat(coordTemp[0]);
					moves[i][j][1] = Float.parseFloat(coordTemp[1]);
					moves[i][j][2] = Float.parseFloat(coordTemp[2]);
				} catch (Exception e) {
					continue;
				}
			}

		}

		return moves;
	}

}
