package br.edu.ifsp.application;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;

import com.primesense.nite.PoseType;

import br.edu.ifsp.coordinates.BodyCoordinate;
import br.edu.ifsp.coordinates.ComponentViewer;
import br.edu.ifsp.coordinates.Viewer;

public class Application extends JFrame implements ItemListener {

	private Camera camera;
	private ComponentViewer viewComponent;
	private Viewer view;
	private BodyCoordinate coor;

	private JComboBox<String> startingPose = new JComboBox<String>(new String[] { "Manual", "Crossed Hands", "PSI" });
	private JComboBox<String> stoppingPose = new JComboBox<String>(new String[] { "Manual", "Crossed Hands", "PSI" });
	private JPanel timer, cameras, setup, record;
	private JRadioButton color, depth, ir;
	private ButtonGroup camerasGroup;

	public Application() {
		super("Application");

		initialize();
		initializeComponentsForm();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(500, 300);
		setVisible(true);
	}

	private void initialize() {
		view = new Viewer();
		viewComponent = view.getColor();

		coor = new BodyCoordinate(viewComponent);
		coor.setCoordinateSystem(BodyCoordinate.DEPTH);

		camera = new Camera(view);
		// new Thread(camera).start();
	}

	private void initializeComponentsForm() {
		Container c = getContentPane();
		c.setLayout(new BorderLayout());

		timer = new JPanel(new GridLayout(1, 1, 0, 0));
		setup = new JPanel(new BorderLayout(0, 0));
		cameras = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
		record = new JPanel(new GridLayout(8, 1, 0, 1));
		color = new JRadioButton("Color");
		depth = new JRadioButton("Depth");
		ir = new JRadioButton("IR");
		camerasGroup = new ButtonGroup();

		camera.setComponentView("Color");
		color.setSelected(true);
		
		depth.addItemListener(this);
		color.addItemListener(this);
		ir.addItemListener(this);
		startingPose.addItemListener(this);
		stoppingPose.addItemListener(this);

		depth.setVisible(view.isDepthSupported());
		color.setVisible(view.isColorSupported());
		ir.setVisible(view.isIrSupported());

		timer.setBorder(new TitledBorder("Timer"));
		cameras.setBorder(new TitledBorder("Cameras"));
		record.setBorder(new TitledBorder("Record"));
		camerasGroup.add(color);
		camerasGroup.add(depth);
		camerasGroup.add(ir);

		setup.add(BorderLayout.NORTH, cameras);
		setup.add(BorderLayout.CENTER, record);

		cameras.add(color);
		cameras.add(depth);
		cameras.add(ir);

		record.add(new JLabel("Seconds:"));
		record.add(new JSpinner());
		record.add(new JLabel("Start Recording Pose:"));
		record.add(startingPose);
		record.add(new JLabel("Stop Recording Pose:"));
		record.add(stoppingPose);
		record.add(new JButton("Start Recording"));
		record.add(new JButton("Stop Recording"));

		// JLabel seconds = new JLabel(String.valueOf((char) 9632));
		JLabel seconds = new JLabel(new String(new char[] { 9632 }));
		seconds.setFont(new Font("Serif", Font.BOLD, 100));
		seconds.setForeground(Color.red);

		timer.add(seconds);
		seconds.setHorizontalAlignment(SwingConstants.CENTER);
		c.add(BorderLayout.CENTER, timer);
		c.add(BorderLayout.EAST, setup);

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

	@Override
	public void itemStateChanged(ItemEvent ie) {
		if (ie.getStateChange() == ItemEvent.DESELECTED) {
			return;
		}
		if (ie.getSource() == color) {
			System.out.println("Color");
			camera.setComponentView("Color");
			coor.setView(view.getColor());
		} else if (ie.getSource() == depth) {
			System.out.println("Depth");
			camera.setComponentView("Depth");
			coor.setView(view.getDepth());
		} else if (ie.getSource() == ir) {
			System.out.println("Infrared");
		} else if (ie.getSource() == startingPose) {
			System.out.println("Starting");
		} else if (ie.getSource() == stoppingPose) {
			System.out.println("Stopping");
		}
	}

	public static void main(String args[]) {
		System.out.println("Started");
		for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
			if ("Metal".equals(info.getName())) {
				try {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException ex) {
					Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
				}
				break;
			}
		}
		new Application();
	}

}
