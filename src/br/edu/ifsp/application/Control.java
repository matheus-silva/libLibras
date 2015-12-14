package br.edu.ifsp.application;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.primesense.nite.PoseType;

import br.edu.ifsp.coordinates.BodyCoordinate;
import br.edu.ifsp.coordinates.ComponentViewer;
import br.edu.ifsp.coordinates.Viewer;
import br.edu.ifsp.util.Save;

public class Control extends JFrame implements ItemListener, ActionListener, ChangeListener {

	private Camera camera;
	private ComponentViewer viewComponent;
	private Viewer view;
	private BodyCoordinate coor;
	private BodyCoordinate.StateChangedListener stateChanged;

	private String poseOptions[] = new String[] { "Manual", "Crossed Hands", "PSI" };
	private JComboBox<String> cbStartingPose = new JComboBox<String>(poseOptions);
	private JComboBox<String> cbStoppingPose = new JComboBox<String>(poseOptions);
	private JPanel pnTimer, pnCameras, pnSetup, pnRecord, pnSave;
	private JRadioButton rbColor, rbDepth, rbIr;
	private ButtonGroup btCamerasGroup;
	private JSpinner sSeconds;
	private JButton btStart, btStop, btSave, btClear;
	private JLabel lblSeconds, lblCount;

	public Control() {
		super("Control");

		initialize();
		initializeComponentsForm();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocation(camera.getLocation().x + camera.getWidth(), camera.getLocation().y);
		setSize(500, 350);
		setVisible(true);
	}

	private void initialize() {
		view = new Viewer();
		viewComponent = view.getColor();

		coor = new BodyCoordinate(viewComponent);
		coor.setCoordinateSystem(BodyCoordinate.REAL_WORLD);

		camera = new Camera(view);
		stateChanged = new BodyCoordinate.StateChangedListener() {

			@Override
			public void stateChanged(int value) {
				switch (value) {
				case BodyCoordinate.StateChangedListener.RECORDING_STARTED:
					lblSeconds.setText("\u25CF");
					view.setStatus("\u25CF");
					break;
				case BodyCoordinate.StateChangedListener.RECORDING_STOPPED:
					lblSeconds.setText("\u25A0");
					view.setStatus("\u25A0");
					break;
				case BodyCoordinate.StateChangedListener.TIMER_CHANGED:
					lblSeconds.setText(String.valueOf(coor.getSeconds()));
					view.setStatus(String.valueOf(coor.getSeconds()));
					break;
				case BodyCoordinate.StateChangedListener.NEW_SKELETON_STORED:
					lblCount.setText("Frames: " + coor.getFramesCount());
					break;
				}
			}
		};
		coor.setStateChanged(stateChanged);
		// new Thread(camera).start();
	}

	private void initializeComponentsForm() {
		Container c = getContentPane();
		c.setLayout(new BorderLayout());

		// Creating objects
		pnTimer = new JPanel(new GridLayout(1, 1, 0, 0));
		pnSetup = new JPanel(new BorderLayout(0, 0));
		pnCameras = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
		pnRecord = new JPanel(new GridLayout(8, 1, 0, 1));
		JPanel pnStatus = new JPanel(new BorderLayout());
		pnSave = new JPanel(new GridLayout(0, 1));
		rbColor = new JRadioButton("Color");
		rbDepth = new JRadioButton("Depth");
		rbIr = new JRadioButton("IR");
		btCamerasGroup = new ButtonGroup();
		sSeconds = new JSpinner(new SpinnerNumberModel(5, 0, null, 1));
		btStart = new JButton("Start Recording");
		btStop = new JButton("Stop Recording");
		btSave = new JButton("Save");
		btClear = new JButton("Clear");
		lblSeconds = new JLabel();
		lblCount = new JLabel("Frames: 0");

		// Creating listeners
		rbDepth.addItemListener(this);
		rbColor.addItemListener(this);
		rbIr.addItemListener(this);
		cbStartingPose.addItemListener(this);
		cbStoppingPose.addItemListener(this);
		btStart.addActionListener(this);
		btStop.addActionListener(this);
		btSave.addActionListener(this);
		btClear.addActionListener(this);
		sSeconds.addChangeListener(this);

		// Basic configurations
		camera.setComponentView("Color");
		rbColor.setSelected(true);
		rbDepth.setVisible(view.isDepthSupported());
		rbColor.setVisible(view.isColorSupported());
		rbIr.setVisible(view.isIrSupported());

		lblSeconds.setFont(new Font("Serif", Font.BOLD, 100));
		lblSeconds.setForeground(Color.red);
		lblSeconds.setText("\u25A0");

		pnTimer.setBorder(new TitledBorder("Timer"));
		pnCameras.setBorder(new TitledBorder("Cameras"));
		pnRecord.setBorder(new TitledBorder("Record"));
		pnStatus.setBorder(new TitledBorder("Status"));
		btCamerasGroup.add(rbColor);
		btCamerasGroup.add(rbDepth);
		btCamerasGroup.add(rbIr);

		pnSetup.add(BorderLayout.NORTH, pnCameras);
		pnSetup.add(BorderLayout.CENTER, pnRecord);

		pnCameras.add(rbColor);
		pnCameras.add(rbDepth);
		pnCameras.add(rbIr);

		pnRecord.add(new JLabel("Seconds:"));
		pnRecord.add(sSeconds);
		pnRecord.add(new JLabel("Start Recording Pose:"));
		pnRecord.add(cbStartingPose);
		pnRecord.add(new JLabel("Stop Recording Pose:"));
		pnRecord.add(cbStoppingPose);
		pnRecord.add(btStart);
		pnRecord.add(btStop);

		pnTimer.add(lblSeconds);
		
		pnStatus.add(BorderLayout.CENTER, lblCount);
		
		lblSeconds.setHorizontalAlignment(SwingConstants.CENTER);
		c.add(BorderLayout.CENTER, pnTimer);
		c.add(BorderLayout.EAST, pnSetup);
		c.add(BorderLayout.SOUTH, pnStatus);

		pnSave.setBorder(new TitledBorder("Save"));
		pnSave.add(btSave);
		pnSave.add(btClear);
		pnSetup.add(BorderLayout.SOUTH, pnSave);
	}

	@Override
	public void itemStateChanged(ItemEvent ie) {
		if (ie.getStateChange() == ItemEvent.DESELECTED) {
			return;
		}
		if (ie.getSource() == rbColor) {
			camera.setComponentView(Camera.COLOR_STREAM);
			coor.setView(view.getColor());

		} else if (ie.getSource() == rbDepth) {
			camera.setComponentView(Camera.DEPTH_STREAM);
			coor.setView(view.getDepth());

		} else if (ie.getSource() == rbIr) {
			camera.setComponentView(Camera.IR_STREAM);
			coor.setView(view.getIr());

		} else if (ie.getSource() == cbStartingPose) {
			btStart.setEnabled(cbStartingPose.getSelectedItem().equals("Manual"));
			startingPoseDetection();

		} else if (ie.getSource() == cbStoppingPose) {
			btStop.setEnabled(cbStoppingPose.getSelectedItem().equals("Manual"));

			if (cbStoppingPose.getSelectedItem().equals("Crossed Hands")) {
				coor.stopRecordingUsers(PoseType.CROSSED_HANDS);
			} else if (cbStoppingPose.getSelectedItem().equals("PSI")) {
				coor.stopRecordingUsers(PoseType.PSI);
			}
		}
	}

	private void startingPoseDetection() {
		if (cbStartingPose.getSelectedItem().equals("Crossed Hands")) {
			coor.startRecordingUsers(PoseType.CROSSED_HANDS, (int) sSeconds.getValue());
		} else if (cbStartingPose.getSelectedItem().equals("PSI")) {
			coor.startRecordingUsers(PoseType.PSI, (int) sSeconds.getValue());
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		startingPoseDetection();
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == btStart) {
			coor.startRecordingUsers();
		} else if (ae.getSource() == btStop) {
			coor.stopRecordingUsers();
		} else if (ae.getSource() == btSave) {
			Save save = new Save();
			File f = save.getFile(this);
			if (f != null) {
				Float[][][] moviments = null;
				for (Float[][][] userFrames : coor.getMovimentsArray().values()) {
					moviments = userFrames;
					break;
				}
				save.saveFile(this, f, moviments);
			}
		} else if (ae.getSource() == btClear) {
			coor.clearMoviments();
			lblCount.setText("Frames: " + coor.getFramesCount());
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
					Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
				}
				break;
			}
		}

		new Control();
	}
}
