package br.edu.ifsp.application.capturer;

import java.awt.Container;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

import br.edu.ifsp.capturer.ShowObject;

public class Camera extends JFrame {

	public static final String COLOR_STREAM = "Color", DEPTH_STREAM = "Depth", IR_STREAM = "Ir";
	private ShowObject view = null;

	public Camera(ShowObject componentViewer) {
		super("Camera");

		this.view = componentViewer;
		initialize();
		setComponentView(COLOR_STREAM);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(640, 480);
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
