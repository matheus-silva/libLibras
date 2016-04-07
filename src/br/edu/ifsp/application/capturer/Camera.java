package br.edu.ifsp.application.capturer;

import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.JFrame;

import br.edu.ifsp.capturer.ShowObject;

public class Camera extends JFrame implements Runnable {

	public static final String COLOR_STREAM = "Color", DEPTH_STREAM = "Depth", IR_STREAM = "Ir";
	private ShowObject view = null;

	public void run() {
	}

	public Camera(ShowObject componentViewer) {
		super("Camera");

		this.view = componentViewer;
		initialize();
		setComponentView(COLOR_STREAM);

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
			view.setCamera(ShowObject.COLOR);
			break;
		case DEPTH_STREAM:
			view.setCamera(ShowObject.DEPTH);
			break;
		}

		c.add(view);
		c.repaint();
	}
}
