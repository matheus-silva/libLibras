package br.edu.ifsp.editor;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
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

import javax.swing.JFrame;

public class RealWorldComp extends Comp {

	protected int startX, endX, startY, endY;

	public RealWorldComp(float[][][] coords) {
		super(coords);
		setIndexAt(76);
		invertVertical();
		//putCenter();
		this.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				for (float[][] c : data) {
					for (float[] c1 : c) {
						for (int i = 0; i < c1.length; i++) {
							if (e.getWheelRotation() == 1) {
								c1[i] = c1[i] / 2;
							} else if (e.getWheelRotation() == -1) {
								c1[i] = c1[i] * 2;
							}
						}
					}
				}
				repaint();
				
			}
		});
		this.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.isControlDown()) {
					if (e.getKeyCode() == KeyEvent.VK_LEFT) {
						previousIndex();
					}
					if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
						nextIndex();
					}
				} else {
					for (float[][] c : data) {
						for (float[] c1 : c) {

							if (e.getKeyCode() == KeyEvent.VK_UP) {
								// System.out.println("UP");
								c1[1] += 100;
							}
							if (e.getKeyCode() == KeyEvent.VK_DOWN) {
								// System.out.println("Down");
								c1[1] -= 100;
							}
							if (e.getKeyCode() == KeyEvent.VK_LEFT) {
								// System.out.println("Left");
								c1[0] -= 100;
							}
							if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
								// System.out.println("Right");
								c1[0] += 100;
							}
						}
					}
				}
				repaint();
			}
		});
	}
	
	public void putCenter(){
		int screenX = getWidth() / 2;
		int screenY = getHeight() / 2;
		System.out.println(getWidth() + " " + getHeight());
		for(int i = 0; i < data.length; i++){
			float changeX = data[i][8][0];
			float changeY = data[i][8][1];
			changeX = screenX - changeX;
			changeY = screenY - changeY;
			for(int j = 0; j < data[i].length; j++){
				data[i][j][0] += changeX;
				data[i][j][1] += changeY;
			}
		}
	}

	public void invertVertical() {
		for (int i = 0; i < data.length; i++) {
			float maxY = Float.MIN_VALUE;
			for (int j = 0; j < data[i].length; j++) {
				if (data[i][j][1] > maxY) {
					maxY = data[i][j][1];
				}
			}
			for (int j = 0; j < data[i].length; j++) {
				data[i][j][1] += (maxY - data[i][j][1]) * 2;
			}
		}

	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setSize(640, 480);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		RealWorldComp comp = new RealWorldComp(loadFile("Dados - Real World.txt"));
		comp.setSize(frame.getSize());
		//comp.putCenter();
		comp.setOption(Comp.NUMBER);
		frame.getContentPane().add(comp);
		comp.requestFocus();
		comp.repaint();
	}

	private static float[][][] loadFile(String arquivo) {
		Charset c = StandardCharsets.UTF_8;
		Path get = Paths.get(arquivo);
		List<String> readAllLines = new ArrayList<>();
		try {
			readAllLines = Files.readAllLines(get, c);
		} catch (IOException ex) {
			Logger.getLogger(RealWorldComp.class.getName()).log(Level.SEVERE, null, ex);
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
				moves[i][j][0] = Float.parseFloat(coordTemp[0]);
				moves[i][j][1] = Float.parseFloat(coordTemp[1]);
				moves[i][j][2] = Float.parseFloat(coordTemp[2]);
			}

		}
		return moves;
	}
}
