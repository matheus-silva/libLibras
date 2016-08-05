package br.edu.ifsp.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.google.gson.Gson;

import br.edu.ifsp.capturer.ShowObject;
import br.edu.ifsp.util.CaptureData.CaptureMetadata;

public class Load {
	
	private static File lastFile;
	private Util util;
	private File file;
	private Component father;
	private CaptureData data;
	private boolean loaded;
	
	public static void main(String args[]) {
		ByteBuffer buff = new Load().loadBuffer(new File("/home/matheus/Música/Olá/Depth/3608575622.bin"));

		ShowObject view = new ShowObject();
		view.setCamera(ShowObject.DEPTH);
		view.setBackground(buff, 640, 480);
		view.repaint();

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(640, 480);
		frame.setVisible(true);

		frame.getContentPane().add(BorderLayout.CENTER, view);
		frame.getContentPane().revalidate();
		frame.getContentPane().repaint();
	}

	public File openFile(Component father) {
		return open(father, JFileChooser.FILES_ONLY);
	}

	public File openDirectory(Component father) {
		return open(father, JFileChooser.DIRECTORIES_ONLY);
	}

	private File open(Component father, int mode) {
		JFileChooser chooser = new JFileChooser();

		if(lastFile != null && lastFile.exists()){
			chooser.setCurrentDirectory(lastFile);
		} else if (Config.getInstance() != null && Config.getInstance().getDirectory() != null) {
			chooser.setCurrentDirectory(new File(Config.getInstance().getDirectory()));
		}

		chooser.setFileSelectionMode(mode);
		if (chooser.showOpenDialog(father) == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			lastFile = f;
			return f;
		}
		return null;
	}

	public CaptureMetadata loadMetadata(File file) {
		CaptureMetadata metadata = null;

		try {
			Path path = file.toPath();
			List<String> lines = Files.readAllLines(path);
			String json = new String();

			for (String s : lines) {
				json += s + "\n";
			}

			Gson g = new Gson();
			metadata = g.fromJson(json, CaptureMetadata.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return metadata;
	}

	public ByteBuffer loadBuffer(File file) {
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

		if (files == null) {
			return null;
		}

		for (File f : files) {
			try {
				long timestamp = Long.parseLong(f.getName().replaceAll("[^0-9]", ""));
				ByteBuffer buff = loadBuffer(f);
				// System.out.println(timestamp + " " + (buff != null));
				map.put(timestamp, buff);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return map;
	}

	public byte[] loadByte(File file) {
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

		return b;
	}

	private Map<Long, Float[][]> loadCoords(File file, Map<Long, Float[][]> map) throws Exception {
		List<String> lines = Files.readAllLines(Paths.get(file.toURI()));

		for (int i = 0; i < lines.size(); i++) {
			String temp[] = lines.get(i).split("[0-9] ");
			Long timestamp = 0L;
			try {
				timestamp = Long.parseLong(temp[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}

			Float[][] coords = new Float[15][3];
			String temp2[] = temp[1].split("]\\[");

			for (int j = 0; j < temp2.length; j++) {
				temp2[j] = temp2[j].replaceAll("]", "").replaceAll("\\[", "");
				String temp3[] = temp2[j].split(", ");
				try {
					coords[j][0] = Float.parseFloat(temp3[0]);
					coords[j][1] = Float.parseFloat(temp3[1]);
					coords[j][2] = Float.parseFloat(temp3[2]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			map.put(timestamp, coords);
		}
		return map;

	}

	private void formatSegmentation(Map<Long, ByteBuffer> segmentation) {
		for (Long v : segmentation.keySet()) {
			ByteBuffer buff = segmentation.get(v).order(ByteOrder.LITTLE_ENDIAN);
			byte b[] = new byte[buff.limit()];
			buff.rewind();
			buff.get(b);
			buff.rewind();
			byte bNew[] = new byte[buff.limit()];
			for (int i = 1, j = 0; i < bNew.length; i += 2, j++) {
				byte value = b[j];
				bNew[i - 1] = 0;
				bNew[i] = value;
			}
			ByteBuffer buffNew = ByteBuffer.wrap(bNew).order(ByteOrder.LITTLE_ENDIAN);
			segmentation.put(v, buffNew);
		}
	}
	
	public synchronized CaptureData loadDirectory(Component father, File file) {
		this.father = father;
		this.file = file;
		this.data = new CaptureData();

		loaded = false;

		util = new Util();
		util.createLoadingWindow((JFrame) father, "Loading...");
		new LoadData().start();
		util.showLoadingWindow();

		while (!loaded) {
			try {
				synchronized (this) {
					this.wait(500);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return data;
	}

	public float[][][] loadFile(File arquivo) {
		List<String> lines = null;
		try {
			lines = Files.readAllLines(Paths.get(arquivo.toURI()));
		} catch (IOException ex) {
			Logger.getLogger(Load.class.getName()).log(Level.SEVERE, null, ex);
		}

		lines.sort(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});

		float[][][] moves = new float[lines.size()][15][3];
		for (int i = 0; i < lines.size(); i++) {

			String temp[] = lines.get(i).split("[0-9] ");
			String temp2[] = temp[1].split("]\\[");
			for (int j = 0; j < temp2.length; j++) {
				temp2[j] = temp2[j].replaceAll("]", "").replaceAll("\\[", "");
				String temp3[] = temp2[j].split(", ");
				try {
					moves[i][j][0] = Float.parseFloat(temp3[0]);
					moves[i][j][1] = Float.parseFloat(temp3[1]);
					moves[i][j][2] = Float.parseFloat(temp3[2]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		return moves;
	}

	private class LoadData extends Thread {
		@Override
		public void run() {

			System.out.println("Loading " + file.getAbsolutePath());
			try {
				File depth = new File(file.getAbsoluteFile() + File.separator + "Depth");
				File color = new File(file.getAbsoluteFile() + File.separator + "Color");
				File segmentation = new File(file.getAbsoluteFile() + File.separator + "Segmentation");
				File coor = new File(file.getAbsoluteFile() + File.separator + "Coordinates");
				File info = new File(file.getAbsolutePath() + File.separator + "info.json");

				File coorDepth = new File(coor.getAbsolutePath() + File.separator + "Depth.txt");
				if (coorDepth.exists()) {
					System.out.println("Coordinate Depth");
					data.setCoordinateDepth(loadCoords(coorDepth, new TreeMap<Long, Float[][]>()));
				}

				File coorReal = new File(coor.getAbsolutePath() + File.separator + "Real.txt");
				if (coorReal.exists()) {
					System.out.println("Coordinate Real");
					data.setCoordinateReal(loadCoords(coorReal, new TreeMap<Long, Float[][]>()));
				}

				if (depth.exists()) {
					System.out.println("Depth");
					data.setImageDepth(loadBuffers(depth, new TreeMap<Long, ByteBuffer>()));
				}

				if (color.exists()) {
					System.out.println("Color");
					data.setImageColor(loadBuffers(color, new TreeMap<Long, ByteBuffer>()));
				}

				if (segmentation.exists()) {
					System.out.println("Segmentation");
					data.setSegmentation(loadBuffers(segmentation, new TreeMap<Long, ByteBuffer>()));
				}

				if (info.exists()) {
					System.out.println("Info");
					data.setMetadata(loadMetadata(info));
				}

				if (data.hasSegmentation()) {
					formatSegmentation(data.getSegmentation());
				}

				Set<Long> time = new TreeSet<>();
				if (data.hasImageDepth()) {
					for (Long l : data.getImageDepth().keySet()) {
						time.add(l);
					}
				}
				data.setTimestamp(time);

			} catch (Exception e) {
				JOptionPane.showMessageDialog(father,
						"An error happened. Try again later!\n" + "Message: " + e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}

			loaded = true;

			util.closeLoadingWindow();

			synchronized (this) {
				this.notifyAll();
			}
		}
	}

}
