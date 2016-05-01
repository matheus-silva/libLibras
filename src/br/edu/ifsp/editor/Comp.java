/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.ifsp.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author a1402153
 */
public class Comp extends Component {

	protected float[][][] data;
	private float[] zeroPoint = { 0, 0 };
	private int index = 0;
	private int[][] skelCoor = { { 0, 1 }, { 1, 8 }, { 8, 9 }, { 8, 10 }, { 9, 11 }, { 11, 13 }, { 10, 12 }, { 12, 14 },
			{ 1, 3 }, { 3, 5 }, { 5, 7 }, { 1, 2 }, { 2, 4 }, { 4, 6 } };
	private String option = "Skeleton";
	private int moveFactor = 25;
	private float zoomFactor = 1.2f;
	private Modification modification;
	private boolean isCenterVisible = false;
	public static final String SKELETON = "Skeleton", NUMBER = "Number", CIRCLE = "Circle";
	
	public static Comp getInstanceByMap(Map<Long, Float[][]> map){
		float[][][] data = new float[map.size()][][];
		Set<Long> keys = map.keySet();
		
		Iterator<Long> it = keys.iterator();
		for(int i = 0; i < data.length; i++){
			Float[][] temp = map.get(it.next());
			data[i] = new float[temp.length][];
			for(int j = 0; j < temp.length; j++){
				
				Float[] temp1 = temp[j];
				data[i][j] = new float[temp1.length];
				
				for(int k = 0; k < temp1.length; k++){
					data[i][j][k] = temp[j][k];
				}
			}
		}
		
		return new Comp(data);
	}
	
	public static Comp getInstanceByList(List<Float[][]> list){
		float[][][] data = new float[list.size()][][];
		
		for(int i = 0; i < list.size(); i++){
			
			Float[][] temp = list.get(i);
			data[i] = new float[temp.length][];
			
			for(int j = 0; j < temp.length; j++){
				
				Float[] temp1 = temp[j];
				data[i][j] = new float[temp1.length];
				
				for(int k = 0; k < temp1.length; k++){
					data[i][j][k] = temp[j][k];
				}
			}
		}
		return new Comp(data);
	}
	
	public Comp(float[][][] data) {
		this.data = new float[data.length][][];
		for (int i = 0; i < data.length; i++) {

			this.data[i] = new float[data[i].length][];
			for (int j = 0; j < data[i].length; j++) {

				this.data[i][j] = new float[data[i][j].length];
				for (int k = 0; k < data[i][j].length; k++) {

					this.data[i][j][k] = data[i][j][k];
				}
			}
		}
		this.modification = new Modification();
	}

	public void adjustView() {
		// Invert
		if (data[0][0][1] > ((data[0][13][1] + data[0][14][1]) / 2)) {
			Action invert = new Action() {

				@Override
				public void action(Comp c) {
					c.invert();
				}
			};
			modification.addAction(invert);
			invert.action(this);
		}

		float maxY = Float.MIN_VALUE, minY = Float.MAX_VALUE;

		for (int i = 0; i < data.length; i++) {

			for (int j = 0; j < data[i].length; j++) {
				if (data[i][j][1] > maxY) {
					maxY = data[i][j][1];
				}
				if (data[i][j][1] < minY) {
					minY = data[i][j][1];
				}
			}
		}

		// Size
		while (true) {
			float sizeCoords = maxY - minY;
			if (sizeCoords >= getPreferredSize().height) {
				maxY /= zoomFactor;
				minY /= zoomFactor;

				Action e = new Action() {

					@Override
					public void action(Comp c) {
						c.zoom(1, zoomFactor);
					}
				};

				modification.addAction(e);
				e.action(this);
			} else {
				break;
			}
		}

		// Move
		float currentX = (data[0][10][0] + data[0][9][0]) / 2;
		float currentY = (data[0][10][1] + data[0][9][1]) / 2;
		int valueX = getPreferredSize().width / 2;
		int valueY = getPreferredSize().height / 2;

		if (currentX != valueX) {
			final int keyX, vX;
			if (currentX > valueX) {
				keyX = KeyEvent.VK_LEFT;
				vX = (int) (currentX - valueX);
			} else {
				keyX = KeyEvent.VK_RIGHT;
				vX = (int) (valueX - currentX);
			}
			Action eX = new Action() {

				@Override
				public void action(Comp c) {
					c.moveCoords(keyX, vX);
				}
			};
			modification.addAction(eX);
			eX.action(this);

			final int keyY, vY;
			if (currentY > valueY) {
				keyY = KeyEvent.VK_UP;
				vY = (int) (currentY - valueY);
			} else {
				keyY = KeyEvent.VK_DOWN;
				vY = (int) (valueY - currentY);
			}
			Action eY = new Action() {

				@Override
				public void action(Comp c) {
					c.moveCoords(keyY, vY);
				}
			};
			modification.addAction(eY);
			eY.action(this);
		}
	}

	@Override
	public void paint(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, getPreferredSize().width, getPreferredSize().height);

		// drawCenter(g);

		g.setColor(Color.red);
		g.drawRect(-1, -1, getPreferredSize().width, getPreferredSize().height);
		g.setFont(new Font("serif", Font.BOLD, 15));
		g.drawString("Frame: " + index, 0, 15);

		switch (option) {
		case SKELETON:
			setSkeleton(g);
			break;
		case NUMBER:
			setNumbers(g);
			break;
		case CIRCLE:
			setCircles(g);
			break;
		default:
			setSkeleton(g);
			break;
		}
	}

	public void setCenter(boolean value) {
		this.isCenterVisible = value;
	}

	public void setOption(String option) {
		this.option = option;
	}

	private void drawCenter(Graphics g) {
		if (!isCenterVisible) {
			return;
		}
		int size = 10;

		g.setColor(Color.black);
		g.drawString("(0, 0)", (int) zeroPoint[0] + size, (int) zeroPoint[1] - 5);
		g.drawLine((int) zeroPoint[0] - size, (int) zeroPoint[1], (int) zeroPoint[0] + size, (int) zeroPoint[1]);
		g.drawLine((int) zeroPoint[0], (int) zeroPoint[1] - size, (int) zeroPoint[0], (int) zeroPoint[1] + size);
	}

	private void setCircles(Graphics g) {
		for (int i = 0; i < 15; i++) {
			int z = (int) Math.pow((data[index][i][2]), 2) / 100500;
			int x = ((int) data[index][i][0] - (z / 2)) - 1;
			int y = ((int) data[index][i][1] - (z / 2)) - 1;
			g.drawOval(x, y, z, z);
			drawDot(g, (int) data[index][i][0], (int) data[index][i][1]);
		}
	}

	private void setNumbers(Graphics g) {
		for (int i = 0; i < 15; i++) {
			// g.drawOval((int) data[index][i][0], (int) data[index][i][1], 10,
			// 10);
			g.drawString(String.valueOf(i), (int) data[index][i][0], (int) data[index][i][1]);
			drawDot(g, (int) data[index][i][0], (int) data[index][i][1]);
		}
	}

	private void setSkeleton(Graphics g) {
		for (int i = 0; i < skelCoor.length; i++) {
			g.drawLine((int) data[index][skelCoor[i][0]][0], (int) data[index][skelCoor[i][0]][1],
					(int) data[index][skelCoor[i][1]][0], (int) data[index][skelCoor[i][1]][1]);
		}
	}

	private void drawDot(Graphics g, int x, int y) {
		g.drawOval(x - 1, y - 1, 1, 1);
	}

	public synchronized void setIndexAt(int index) {
		if (index < 0 || index >= this.data.length) {
			throw new RuntimeException("Invalid index value");
		}
		this.index = index;
	}

	public synchronized void nextIndex() {
		this.index++;
		if (this.index >= this.data.length) {
			this.index = 0;
		}
	}

	public synchronized void previousIndex() {
		this.index--;
		if (this.index <= -1) {
			this.index = this.data.length - 1;
		}
	}

	public Modification getModification() {
		return this.modification;
	}

	public void setModification(Modification m) {
		this.modification = m;
	}

	public void applyModification() {
		this.modification.applyModification(this);
	}

	public void invertVertical() {
		invert();
		modification.addAction(new Action() {

			@Override
			public void action(Comp c) {
				c.invert();
			}
		});
	}

	public void zoomIn() {
		int option = -1;
		zoom(-1, zoomFactor);
		modification.addAction(new Action() {

			@Override
			public void action(Comp c) {
				c.zoom(option, zoomFactor);
			}
		});
	}

	public void zoomOut() {
		int option = 1;
		zoom(option, zoomFactor);
		modification.addAction(new Action() {

			@Override
			public void action(Comp c) {
				c.zoom(option, zoomFactor);
			}
		});
	}

	public void moveUp() {
		int key = KeyEvent.VK_UP;
		moveCoords(key, moveFactor);
		modification.addAction(new Action() {

			@Override
			public void action(Comp c) {
				c.moveCoords(key, moveFactor);
			}
		});
	}

	public void moveDown() {
		int key = KeyEvent.VK_DOWN;
		moveCoords(key, moveFactor);
		modification.addAction(new Action() {

			@Override
			public void action(Comp c) {
				c.moveCoords(key, moveFactor);
			}
		});
	}

	public void moveLeft() {
		int key = KeyEvent.VK_LEFT;
		moveCoords(key, moveFactor);
		modification.addAction(new Action() {

			@Override
			public void action(Comp c) {
				c.moveCoords(key, moveFactor);
			}
		});
	}

	public void moveRight() {
		int key = KeyEvent.VK_RIGHT;
		moveCoords(key, moveFactor);
		modification.addAction(new Action() {

			@Override
			public void action(Comp c) {
				c.moveCoords(key, moveFactor);
			}
		});
	}

	private void moveCoords(int key, int value) {
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				if (key == KeyEvent.VK_UP) {
					data[i][j][1] -= value;
					// System.out.println("UP " + data[i][j][1]);
				}
				if (key == KeyEvent.VK_DOWN) {
					data[i][j][1] += value;
					// System.out.println("Down " + data[i][j][1]);
				}
				if (key == KeyEvent.VK_LEFT) {
					data[i][j][0] -= value;
					// System.out.println("Left " + data[i][j][0]);
				}
				if (key == KeyEvent.VK_RIGHT) {
					data[i][j][0] += value;
					// System.out.println("Right " + data[i][j][0]);
				}
			}
		}
		if (key == KeyEvent.VK_UP) {
			zeroPoint[1] -= value;
		}
		if (key == KeyEvent.VK_DOWN) {
			zeroPoint[1] += value;
		}
		if (key == KeyEvent.VK_LEFT) {
			zeroPoint[0] -= value;
		}
		if (key == KeyEvent.VK_RIGHT) {
			zeroPoint[0] += value;
		}
	}

	private void zoom(int option, float factor) {
		for (float[][] c : data) {
			for (float[] c1 : c) {
				for (int i = 0; i < c1.length - 1; i++) {
					if (option == 1) {
						c1[i] = c1[i] / factor;
					} else if (option == -1) {
						c1[i] = c1[i] * factor;
					}
				}
			}
		}
		if (option == 1) {
			zeroPoint[0] /= factor;
			zeroPoint[1] /= factor;
		} else if (option == -1) {
			zeroPoint[0] *= factor;
			zeroPoint[1] *= factor;
		}
	}

	private void invert() {
		for (int i = 0; i < data.length; i++) {
			float minY = Float.MAX_VALUE;
			float maxY = Float.MIN_VALUE;
			for (int j = 0; j < data[i].length; j++) {
				if (data[i][j][1] > maxY) {
					maxY = data[i][j][1];
				}
				if (data[i][j][1] < minY) {
					minY = data[i][j][1];
				}
			}
			for (int j = 0; j < data[i].length; j++) {
				data[i][j][1] += ((maxY - data[i][j][1]) * 2);
				data[i][j][1] -= maxY - minY;
			}
			zeroPoint[1] += ((maxY - zeroPoint[1]) * 2);
			zeroPoint[1] -= maxY - minY;
		}

	}

	public class Modification {

		private List<Action> actions;

		private Modification() {
			// actions = new CopyOnWriteArrayList<>();
			actions = new ArrayList<>();
		}

		private void addAction(Action e) {
			actions.add(e);
		}

		public void applyModification(Comp c) {
			for (Action a : actions) {
				a.action(c);
			}
			c.repaint();

			return;
		}
	}

	private interface Action {
		public void action(Comp c);
	}

}
