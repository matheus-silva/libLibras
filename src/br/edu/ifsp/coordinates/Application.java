package br.edu.ifsp.coordinates;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class Application extends JFrame {
	
	private SimpleViewer viewComponent;
	private Viewer view;
	private BodyCoordinate coor;

	public Application() {
		super("Component");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(640, 480);
		setVisible(true);

		initialize2();
	}

	private void initialize() {		
		getContentPane().setLayout(new GridLayout(1, 1));
		
		Manager manager = new Manager();
		BodyCoordinate coor = manager.getCoordinate();
		Component view = manager.getUserViewer();
		
		coor.setCoordinateSystem(BodyCoordinate.DEPTH);
		view.setSize(640, 480);

		this.getContentPane().add(view);
		
		Thread t = new Thread(manager);
		t.start();

		coor.startRecordingUsers();
	}
	
	private void initialize2() {
		Load.load();
		
		getContentPane().setLayout(new GridLayout(1, 1));
		
		view = new Viewer();
		//viewComponent = view.getColor();
		viewComponent = view.getDepth();
		
		coor = new BodyCoordinate();
		coor.setView(viewComponent);
		coor.setCoordinateSystem(BodyCoordinate.DEPTH);

		this.getContentPane().add(viewComponent);

		coor.startRecordingUsers();
		
		//changeView();
	}
	
	private void changeView(){
		boolean cor = true;
		while(true){
			try {
				Thread.sleep(3_000);
				if(cor){
					viewComponent = view.getColor();
				} else {
					viewComponent = view.getDepth();
				}
				cor = !cor;
				coor.setView(viewComponent);
				getContentPane().add(viewComponent);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
		System.out.println("Started");
		new Application();
	}

}
