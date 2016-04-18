package br.edu.ifsp.application.capturer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.StringTokenizer;
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
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.primesense.nite.PoseType;

import br.edu.ifsp.capturer.ShowObject;
import br.edu.ifsp.util.Save;
import javafx.scene.web.PromptData;

public class Control extends JFrame implements ItemListener, ActionListener, ChangeListener {

	private Camera camera;
	private ShowObject view;
	private Capture capture;
	private Capture.StateChangedListener stateChanged;

	private String poseOptions[] = new String[] { "Manual", "Crossed Hands", "PSI" };
	private JComboBox<String> cbStartingPose = new JComboBox<String>(poseOptions);
	private JComboBox<String> cbStoppingPose = new JComboBox<String>(poseOptions);
	private JPanel pnTimer, pnCameras, pnSetup, pnRecord, pnSave;
	private JRadioButton rbColor, rbDepth, rbIr;
	private ButtonGroup btCamerasGroup;
	private JSpinner sSeconds;
	private JButton btStart, btStop, btSave, btClear, btDirectory;
	private JLabel lblSeconds, lblCount;

	public Control() {
		super("New Control");

		initialize();
		initializeComponentsForm();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocation(camera.getLocation().x + camera.getWidth(), camera.getLocation().y);
		setSize(500, 500);
		setVisible(true);
	}

	private void initialize() {
		view = new ShowObject();

		capture = new Capture(view);

		camera = new Camera(view);
		stateChanged = new Capture.StateChangedListener() {

			@Override
			public void stateChanged(int value) {
				switch (value) {
				case Capture.StateChangedListener.RECORDING_STARTED:
					lblSeconds.setText("\u25CF");
					view.setStatus("\u25CF");

					cbStartingPose.setEnabled(false);
					btStart.setEnabled(false);
					cbStoppingPose.setEnabled(true);
					btStop.setEnabled(true);
					break;
				case Capture.StateChangedListener.RECORDING_STOPPED:
					lblSeconds.setText("\u25A0");
					view.setStatus("\u25A0");

					cbStartingPose.setEnabled(true);
					btStart.setEnabled(true);
					cbStoppingPose.setEnabled(false);
					btStop.setEnabled(false);
					break;
				case Capture.StateChangedListener.TIMER_CHANGED:
					lblSeconds.setText(String.valueOf(capture.getSeconds()));
					view.setStatus(String.valueOf(capture.getSeconds()));

					cbStartingPose.setEnabled(false);
					btStart.setEnabled(false);
					cbStoppingPose.setEnabled(false);
					btStop.setEnabled(false);
					break;
				case Capture.StateChangedListener.NEW_DATA_ARRIVED:
					statusBar();
					break;
				}
			}
		};
		capture.setStateChanged(stateChanged);
	}

	private void statusBar() {
		Runtime run = Runtime.getRuntime();
		long used = (run.maxMemory() - run.freeMemory());
		double p = (100 * used / run.maxMemory());
		lblCount.setText(
				"Frames: " + capture.getFramesCount() + " | Memory:  " + (used / 1_048_576) + " Mb (" + (p) + "%)");
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
		rbColor.setSelected(true);
		rbDepth.setVisible(true);
		rbColor.setVisible(true);
		rbIr.setVisible(false);

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
		
		btDirectory = new JButton("Select");
		
		JPanel pnDirectory = new JPanel(new BorderLayout());
		pnDirectory.add(BorderLayout.WEST, new JLabel("Path: "));
		pnDirectory.add(BorderLayout.CENTER, new JTextField());
		pnDirectory.add(BorderLayout.EAST, btDirectory);
		
		JPanel pnPerson = new JPanel(new BorderLayout());
		pnPerson.add(BorderLayout.WEST, new JLabel("User: "));
		pnPerson.add(BorderLayout.CENTER, new JTextField());
		
		JPanel pnSign = new JPanel(new BorderLayout());
		pnSign.add(BorderLayout.WEST, new JLabel("Sign: "));
		pnSign.add(BorderLayout.CENTER, new JTextField());
		
		JPanel pnPersonSign = new JPanel(new GridLayout(1, 4));
		pnPersonSign.add(pnPerson);
		pnPersonSign.add(pnSign);
		
		JPanel pnFile = new JPanel(new BorderLayout());
		pnFile.setBorder(new TitledBorder("File"));
		pnFile.add(BorderLayout.NORTH, pnDirectory);
		pnFile.add(BorderLayout.CENTER, pnPersonSign);
		
		c.add(BorderLayout.NORTH, pnFile);
		c.add(BorderLayout.CENTER, pnTimer);
		c.add(BorderLayout.EAST, pnSetup);
		c.add(BorderLayout.SOUTH, pnStatus);

		pnSave.setBorder(new TitledBorder("Save"));
		pnSave.add(btSave);
		pnSave.add(btClear);
		pnSetup.add(BorderLayout.SOUTH, pnSave);

		statusBar();
	}

	@Override
	public void itemStateChanged(ItemEvent ie) {
		if (ie.getStateChange() == ItemEvent.DESELECTED) {
			return;
		}
		if (ie.getSource() == rbColor) {
			camera.setComponentView(Camera.COLOR_STREAM);

		} else if (ie.getSource() == rbDepth) {
			camera.setComponentView(Camera.DEPTH_STREAM);

		} else if (ie.getSource() == rbIr) {
			camera.setComponentView(Camera.IR_STREAM);

		} else if (ie.getSource() == cbStartingPose) {
			// btStart.setEnabled(cbStartingPose.getSelectedItem().equals("Manual"));
			startingPoseDetection();

		} else if (ie.getSource() == cbStoppingPose) {
			// btStop.setEnabled(cbStoppingPose.getSelectedItem().equals("Manual"));
			stoppingPoseDetection();
		} else if(ie.getSource() == btDirectory){
			
		}
	}

	private void startingPoseDetection() {
		if (cbStartingPose.getSelectedItem().equals("Crossed Hands")) {
			capture.startRecordingUsers(PoseType.CROSSED_HANDS, (int) sSeconds.getValue());
		} else if (cbStartingPose.getSelectedItem().equals("PSI")) {
			capture.startRecordingUsers(PoseType.PSI, (int) sSeconds.getValue());
		} else if (cbStartingPose.getSelectedItem().equals("Manual")) {
			capture.startRecordingUsers(null, (int) sSeconds.getValue());
		}
	}

	private void stoppingPoseDetection() {
		if (cbStoppingPose.getSelectedItem().equals("Crossed Hands")) {
			capture.stopRecordingUsers(PoseType.CROSSED_HANDS);
		} else if (cbStoppingPose.getSelectedItem().equals("PSI")) {
			capture.stopRecordingUsers(PoseType.PSI);
		} else if (cbStoppingPose.getSelectedItem().equals("Manual")) {
			capture.stopRecordingUsers(null);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		startingPoseDetection();
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == btStart) {
			capture.startRecordingUsers();
		} else if (ae.getSource() == btStop) {
			capture.stopRecordingUsers();
		} else if (ae.getSource() == btSave) {
			Save save = new Save();
			File f = save.openFile(this);
			if (f != null) {
				save.saveFile(this, f, capture.getRecordedData());
				save.clearData();
			}
		} else if (ae.getSource() == btClear) {
			capture.clearMoviments();

			Runtime run = Runtime.getRuntime();
			run.runFinalization();
			run.gc();

			statusBar();
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
