package br.edu.ifsp.coordinates;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

public class Application extends JFrame {

	public Application() {
		super("Component");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(640, 480);
		setVisible(true);

		initialize();
	}

	private void initialize() {
		// java.util.ConcurrentModificationException
		NewFrameInterface frame = new NewFrameInterface();
		BodyCoordinate coor = frame.getCoordinate();
		coor.setCoordinateSystem(BodyCoordinate.DEPTH);

		this.getContentPane().setLayout(new GridLayout(1, 1));
		Component view = frame.getUserViewer();
		view.setSize(640, 480);

		this.getContentPane().add(view);

		Thread t = new Thread(frame);
		t.start();

		coor.startRecordingUsers();
		

	}

	private void print(float[][][] moves) {
		for (int h = 0; h < moves.length; h++) {
			System.out.print(h);
			float[][] joints = moves[h];

			for (int i = 0; i < joints.length; i++) {
				System.out.print(Arrays.toString(joints[i]));
			}
			System.out.println("");
		}
	}

	public static void main(String args[]) {
		new Application();
	}

}
