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
import java.util.Arrays;

import javax.swing.ComponentInputMap;

/**
 *
 * @author a1402153
 */
public class Comp extends Component {

	protected float[][][] data;
	private int index = 0;
	private int[][] skelCoor = { { 0, 1 }, { 1, 8 }, { 8, 9 }, { 8, 10 }, { 9, 11 }, { 11, 13 }, { 10, 12 }, { 12, 14 },
			{ 1, 3 }, { 3, 5 }, { 5, 7 }, { 1, 2 }, { 2, 4 }, { 4, 6 } };
	private String option = "Skeleton";
	public static final String SKELETON = "Skeleton", NUMBER = "Number", CIRCLE = "Circle";

	public Comp(float[][][] data) {
		this.data = new float[data.length][][];
		for(int i = 0; i < data.length; i++){
			
			this.data[i] = new float[data[i].length][];
			for(int j = 0; j < data[i].length; j++){
				
				this.data[i][j] = new float[data[i][j].length];
				for(int k = 0; k < data[i][j].length; k++){
					
					this.data[i][j][k] = data[i][j][k];
				}
			}
		}
	}

	@Override
	public void paint(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, getPreferredSize().width, getPreferredSize().height);
		g.setColor(Color.red);
		g.drawRect(-1, -1, getPreferredSize().width, getPreferredSize().height);
		g.setFont(new Font("serif", Font.BOLD, 15));
		g.drawString("Frame: " + index, 0, 15);

		switch (option) {
		case "Skeleton":
			setSkeleton(g);
			break;
		case "Number":
			setNumbers(g);
			break;
		case "Circle":
			setCircles(g);
			break;
		default:
			setSkeleton(g);
			break;
		}
	}

	public void setOption(String option) {
		this.option = option;
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

	public void invert() {
		for (int i = 0; i < data.length; i++) {
			float minY = Float.MAX_VALUE;
			float maxY = Float.MIN_VALUE;
			for (int j = 0; j < data[i].length; j++) {
				if (data[i][j][1] > maxY) {
					maxY = data[i][j][1];
				}
				if(data[i][j][1] < minY){
					minY = data[i][j][1];
				}
			}
			for (int j = 0; j < data[i].length; j++) {
				data[i][j][1] += ((maxY - data[i][j][1]) * 2);
				data[i][j][1] -= maxY - minY;
			}
		}
	}

	public void zoomIn() {
		zoom(-1);
	}

	public void zoomOut() {
		zoom(1);
	}
	
	public void moveUp(int value){
		moveCoords(KeyEvent.VK_UP, value);
	}
	
	public void moveDown(int value){
		moveCoords(KeyEvent.VK_DOWN, value);
	}
	
	public void moveLeft(int value){
		moveCoords(KeyEvent.VK_LEFT, value);
	}
	
	public void moveRight(int value){
		moveCoords(KeyEvent.VK_RIGHT, value);
	}

	private void moveCoords(int key, int value) {
		for(int i = 0; i < data.length; i++){
			for(int j = 0; j < data[i].length; j++){
				if (key == KeyEvent.VK_UP) {
					// System.out.println("UP");
					data[i][j][1] -= value;
				}
				if (key == KeyEvent.VK_DOWN) {
					// System.out.println("Down");
					data[i][j][1] += value;
				}
				if (key == KeyEvent.VK_LEFT) {
					// System.out.println("Left");
					data[i][j][0] -= value;
				}
				if (key == KeyEvent.VK_RIGHT) {
					// System.out.println("Right");
					data[i][j][0] += value;
				}
			}
		}
		
	}

	private void zoom(int value) {
		float f = 1.2f;
		for (float[][] c : data) {
			for (float[] c1 : c) {
				for (int i = 0; i < c1.length - 1; i++) {
					if (value == 1) {
						c1[i] = c1[i] / f;
					} else if (value == -1) {
						c1[i] = c1[i] * f;
					}
				}
			}
		}
	}
}
