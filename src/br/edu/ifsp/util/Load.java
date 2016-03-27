package br.edu.ifsp.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import br.edu.ifsp.capture.Coordinate;
import br.edu.ifsp.capture.ImageCapture;
import br.edu.ifsp.capture.Segmentation;
import br.edu.ifsp.editor.Editor;

public class Load extends Thread {

	private JDialog d;
	private Component father;
	private File file;
	private CaptureData data;
	private boolean loaded;

	public File open(Component father) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (chooser.showOpenDialog(father) == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();

			return f;
		}
		return null;
	}

	private ByteBuffer loadBuffer(File file) {
		BufferedInputStream in;
		List<Byte> bytes = new ArrayList<>();

		try {
			in = new BufferedInputStream(new FileInputStream(file));
			int valor;

			while ((valor = in.read()) != -1) {
				bytes.add((byte) valor);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		byte b[] = new byte[bytes.size()];

		for (int i = 0; i < b.length; i++) {
			b[i] = bytes.get(i);
		}

		return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
	}

	private Map<Long, ByteBuffer> loadBuffers(File file, Map<Long, ByteBuffer> map) {
		File[] files = file.listFiles();

		for (File f : files) {
			try {
				long timestamp = Long.parseLong(f.getName());
				ByteBuffer buff = loadBuffer(f);
				map.put(timestamp, buff);
			} catch (Exception e) {

			}
		}

		return map;
	}

	private Map<Long, Float[][]> loadCoords(File file, Map<Long, Float[][]> map) throws Exception {
		List<String> lines = Files.readAllLines(Paths.get(file.toURI()));

		for (int i = 0; i < lines.size(); i++) {
			String temp[] = lines.get(i).split(" ");
			Long timestamp = 0L;
			try {
				timestamp = Long.parseLong(temp[0]);
			} catch (Exception e) {

			}

			Float[][] coords = new Float[15][3];
			String temp2[] = temp[1].split("][");
			for (int j = 0; j < temp2.length; j++) {
				temp2[j] = temp2[j].replaceAll("[", "").replaceAll("]", "");

				String temp3[] = temp2[j].split(", ");
				try {
					coords[j][0] = Float.parseFloat(temp3[0]);
					coords[j][1] = Float.parseFloat(temp3[1]);
					coords[j][2] = Float.parseFloat(temp3[2]);
				} catch (Exception e) {

				}
			}
			map.put(timestamp, coords);
		}
		return map;

	}

	public CaptureData loadFile(Component father, File file) {
		this.father = father;
		this.file = file;

		data = new CaptureData();

		loaded = false;

		d = new JDialog((JFrame) father, "Loading...", true);
		d.setSize(300, 75);
		d.setLocationRelativeTo(father);
		d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		d.setResizable(false);

		JProgressBar pb = new JProgressBar();
		pb.setIndeterminate(true);
		d.getContentPane().add(BorderLayout.CENTER, pb);

		this.start();

		d.setVisible(true);

		while (!loaded) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return data;
	}

	@Override
	public void run() {
		System.out.println("Loading");
		try {
			File depth = new File(file.getAbsoluteFile() + File.separator + "Depth");
			File color = new File(file.getAbsoluteFile() + File.separator + "Color");
			File segmentation = new File(file.getAbsoluteFile() + File.separator + "Segmentation");
			File coor = new File(file.getAbsoluteFile() + File.separator + "Coordinates");

			data.setImageDepth(loadBuffers(depth, ImageCapture.createMapStructure()));
			data.setImageColor(loadBuffers(color, ImageCapture.createMapStructure()));
			data.setSegmentation(loadBuffers(segmentation, Segmentation.createMapStructure()));
			
			data.setCoordinateDepth(loadCoords(new File(coor.getAbsolutePath() + File.separator + "Depth.txt"), Coordinate.createMapStructure()));
			data.setCoordinateReal(loadCoords(new File(coor.getAbsolutePath() + File.separator + "Real.txt"), Coordinate.createMapStructure()));

		} catch (Exception e) {
			JOptionPane.showMessageDialog(father,
					"An error happened. Try again later!\n" + "Message: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}

		loaded = true;
		notifyAll();
		d.dispose();
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
