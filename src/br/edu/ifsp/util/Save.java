package br.edu.ifsp.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

public class Save extends Thread {

	private Component father;
	private File file;
	private JDialog d;
	private CaptureData data;
	private float[][][] moviments;

	private static String directory = "/home/matheus/Música";
	
	public File getFile(Component father) {
		JFileChooser chooser = new JFileChooser(Save.directory);
		// chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (chooser.showSaveDialog(father) == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		return null;
	}
	
	public void clearData(){
		data = null;
	}

	private void saveString(File file, String value) throws IOException {
		Files.write(Paths.get(file.toURI()), value.getBytes());
	}

	private void saveCoords(File file, Map<Long, Float[][]> coords) throws IOException {
		StringBuilder sb = new StringBuilder();

		for (Long timestamp : coords.keySet()) {
			sb.append(timestamp + " ");
			for (Float[] f : coords.get(timestamp)) {
				sb.append(Arrays.toString(f));
			}
			sb.append("\n");
		}

		saveString(file, sb.toString());
		// Files.write(Paths.get(file.toURI()),
		// getCoords(moviments).getBytes());
	}

	private void saveBuffer(File file, ByteBuffer buff) {
		BufferedOutputStream out;
		byte b[] = new byte[buff.limit()];

		buff.rewind();
		buff.get(b);

		try {
			out = new BufferedOutputStream(new FileOutputStream(file));
			out.write(b);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveBuffers(File file, Map<Long, ByteBuffer> map) {
		String caminho = file.getAbsolutePath();

		for (Long timestamp : map.keySet()) {
			ByteBuffer buff = map.get(timestamp);
			saveBuffer(new File(caminho + File.separator + timestamp + ".bin"), buff);
		}

	}

	private void saveByte(File file, byte[] b) {
		BufferedOutputStream out;
		try {
			out = new BufferedOutputStream(new FileOutputStream(file));
			out.write(b);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void saveBytes(File file, Map<Long, byte[]> map) {
		String caminho = file.getAbsolutePath();

		for (Long timestamp : map.keySet()) {
			byte[] b = map.get(timestamp);
			saveByte(new File(caminho + File.separator + timestamp + ".bin"), b);
		}

	}

	public void saveFile(Component father, File file, CaptureData data) {
		this.father = father;
		this.file = file;
		this.data = data;

		d = new JDialog((JFrame) father, "Saving...", true);
		d.setSize(300, 75);
		d.setLocationRelativeTo(father);
		d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		d.setResizable(false);

		JProgressBar pb = new JProgressBar();
		pb.setIndeterminate(true);
		d.getContentPane().add(BorderLayout.CENTER, pb);

		this.start();

		d.setVisible(true);

	}

	public void saveFile(Component father, File file, Float[][][] moviments) {
		float[][][] data = new float[moviments.length][][];
		for (int i = 0; i < moviments.length; i++) {

			data[i] = new float[moviments[i].length][];
			for (int j = 0; j < moviments[i].length; j++) {

				data[i][j] = new float[moviments[i][j].length];
				for (int k = 0; k < moviments[i][j].length; k++) {

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

		this.father = father;
		this.file = file;
		this.moviments = moviments;

		d = new JDialog((JFrame) father, "Saving...", true);
		d.setSize(300, 75);
		d.setLocationRelativeTo(father);
		d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		d.setResizable(false);

		JProgressBar pb = new JProgressBar();
		pb.setIndeterminate(true);
		d.getContentPane().add(BorderLayout.CENTER, pb);

		this.start();

		d.setVisible(true);
	}

	@Override
	public void run() {
		System.out.println("Saving");
		try {

			Path directory = Files.createDirectory(file.toPath());
			Path depth = Files.createDirectory(new File(directory.toFile().getAbsolutePath() + File.separator + "Depth").toPath());
			Path color = Files.createDirectory(new File(directory.toFile().getAbsolutePath() + File.separator + "Color").toPath());
			Path segmentation = Files
					.createDirectory(new File(directory.toFile().getAbsolutePath() + File.separator + "Segmentation").toPath());
			Path coordinates = Files
					.createDirectory(new File(directory.toFile().getAbsolutePath() + File.separator + "Coordinates").toPath());

			StringBuilder sb = new StringBuilder();
			sb.append("Width: " + data.getWidth() + "\n");
			sb.append("Height: " + data.getHeight() + "\n");
			sb.append("FPS: " + data.getFps() + "\n");

			saveString(new File(directory.toFile().getAbsolutePath() + File.separator + "Config.txt"), sb.toString());
			saveCoords(new File(coordinates.toFile().getAbsolutePath() + File.separator + "Depth.txt"), data.getCoordinateDepth());
			saveCoords(new File(coordinates.toFile().getAbsolutePath() + File.separator + "Real.txt"), data.getCoordinateReal());

			saveBuffers(depth.toFile(), data.getImageDepth());
			saveBuffers(color.toFile(), data.getImageColor());
			saveBuffers(segmentation.toFile(), data.getSegmentation());

			// saveCoords(null, 0L, null);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(father,
					"An error happened. Try again later!\n" + "Message: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}

		d.dispose();
	}

	private String getCoords(float[][][] coor) {
		String coords = new String();
		if (coor == null) {
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
