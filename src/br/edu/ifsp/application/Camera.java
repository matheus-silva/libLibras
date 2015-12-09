package br.edu.ifsp.application;

import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.JFrame;

import br.edu.ifsp.coordinates.Viewer;

public class Camera extends JFrame implements Runnable {

	public static final String COLOR_STREAM = "Color", DEPTH_STREAM = "Depth", IR_STREAM = "Ir";
	private Viewer view = null;

	public void run() {
	}

	public Camera(Viewer componentViewer) {
		super("Camera");

		this.view = componentViewer;
		initialize();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(640, 480);
		setVisible(true);
	}

	private void initialize() {
		Container c = this.getContentPane();
		c.setLayout(new GridLayout(1, 1));
	}

	public void setComponentView(String stream) {
		Container c = this.getContentPane();
		c.removeAll();
		c.revalidate();
		c.repaint();

		switch (stream) {
		case COLOR_STREAM:
			if (view.isColorSupported()) {
				c.add(view.getColor());
			}
			break;
		case DEPTH_STREAM:
			if (view.isDepthSupported()) {
				c.add(view.getDepth());
			}
			break;
		case IR_STREAM:
			if (view.isIrSupported()) {
				c.add(view.getIr());
			}
			break;
		}

		c.repaint();
	}
}
