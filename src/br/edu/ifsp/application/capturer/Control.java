package br.edu.ifsp.application.capturer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
import br.edu.ifsp.editor.SimpleViewer;
import br.edu.ifsp.util.CaptureData.CaptureMetadata;
import br.edu.ifsp.util.Config;
import br.edu.ifsp.util.Delete;
import br.edu.ifsp.util.Load;
import br.edu.ifsp.util.Util;

public class Control extends JFrame implements ItemListener, ActionListener, ChangeListener {

	private Camera camera;
	private ShowObject view;
	private Capture capture;
	private Capture.StateChangedListener stateChanged;

	private String poseOptions[] = new String[] { "Manual", "Crossed Hands", "PSI" };
	private JComboBox<String> cbStartingPose = new JComboBox<String>(poseOptions);
	private JComboBox<String> cbStoppingPose = new JComboBox<String>(poseOptions);
	private JComboBox<Object> cbSign;
	private String lastSelectedStartPose = "Manual";
	private JPanel pnTimer, pnCameras, pnSetup, pnRecord, pnOption;
	private JRadioButton rbColor, rbDepth, rbIr;
	private JTextField txtDirectory, txtPerson;
	// private JTextField txtPerson, txtSign;
	private ButtonGroup btCamerasGroup;
	private JSpinner sSeconds;
	private JButton btStart, btStop, btOpenData, btDelete, btDirectory, btCreate, btGarbage;
	private long lastTimestamp;
	private JLabel lblSeconds, lblCount;

	public Control() {
		super("New Control");
		
		Util util = new Util();
		util.openLoadingWindowThread(this, "Loading...");
		
		initialize();
		initializeComponentsForm();

		view.setFont(getLibrasFont());

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocation(camera.getLocation().x + camera.getWidth(), camera.getLocation().y);
		setSize(500, 450);
		
		callGCAlways(5_000);
		
		util.closeLoadingWindow();

		camera.setVisible(true);
		this.setVisible(true);
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
					view.setStatusCenter(null);

					cbStartingPose.setEnabled(false);
					btStart.setEnabled(false);
					cbStoppingPose.setEnabled(true);
					btStop.setEnabled(true);

					txtPerson.setEnabled(false);
					cbSign.setEnabled(false);
					btCreate.setEnabled(false);

					sSeconds.setEnabled(false);
					btOpenData.setEnabled(false);
					btDirectory.setEnabled(false);
					btDelete.setEnabled(false);
					break;
				case Capture.StateChangedListener.RECORDING_STOPPED:
					lblSeconds.setText("\u25A0");
					view.setStatus("\u25A0");
					view.setStatusCenter(null);

					cbStartingPose.setEnabled(true);
					btStart.setEnabled(true);
					cbStoppingPose.setEnabled(false);
					btStop.setEnabled(false);

					txtPerson.setEnabled(true);
					cbSign.setEnabled(true);
					btCreate.setEnabled(true);

					sSeconds.setEnabled(true);
					btOpenData.setEnabled(true);
					btDirectory.setEnabled(true);
					btDelete.setEnabled(true);
					break;
				case Capture.StateChangedListener.TIMER_CHANGED:
					lblSeconds.setText(String.valueOf(capture.getSecondsRemaining()));
					view.setStatus(String.valueOf(capture.getSecondsRemaining()));
					view.setStatusCenter(String.valueOf(capture.getSecondsRemaining()));

					cbStartingPose.setEnabled(false);
					btStart.setEnabled(false);
					cbStoppingPose.setEnabled(false);
					btStop.setEnabled(false);
					
					txtPerson.setEnabled(false);
					cbSign.setEnabled(false);
					btCreate.setEnabled(false);

					sSeconds.setEnabled(false);
					btOpenData.setEnabled(false);
					btDirectory.setEnabled(false);
					btDelete.setEnabled(false);
					break;
				case Capture.StateChangedListener.NEW_DATA_ARRIVED:
					statusBar();
					break;
				}
			}
		};
		capture.setStateChanged(stateChanged);
	}

	private Font getLibrasFont() {
		Font font = null;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {
			Font fontTemp = Font.createFont(Font.TRUETYPE_FONT, new File("config" + File.separator + "libras.ttf"));
			ge.registerFont(fontTemp);
			font = new Font(fontTemp.getFontName(), Font.BOLD, 100);
		} catch (FontFormatException ex) {
			Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
		}
		return font;
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
		pnRecord = new JPanel(new GridLayout(0, 1, 5, 5));
		JPanel pnStatus = new JPanel(new BorderLayout());
		pnOption = new JPanel(new GridLayout(0, 1));
		rbColor = new JRadioButton("Color");
		rbDepth = new JRadioButton("Depth");
		rbIr = new JRadioButton("IR");
		btCamerasGroup = new ButtonGroup();
		sSeconds = new JSpinner(new SpinnerNumberModel(5, 0, null, 1));
		btStart = new JButton("Start Recording");
		btStop = new JButton("Stop Recording");
		btOpenData = new JButton("View Data");
		btDelete = new JButton("Delete");
		btDirectory = new JButton("Select");
		btCreate = new JButton("Create");
		btGarbage = new JButton("Garbage Collector");
		lblSeconds = new JLabel();
		lblCount = new JLabel("Frames: 0");
		txtDirectory = new JTextField();
		txtPerson = new JTextField(8);
		cbSign = new JComboBox<>();

		// Creating listeners
		rbDepth.addItemListener(this);
		rbColor.addItemListener(this);
		rbIr.addItemListener(this);
		cbStartingPose.addItemListener(this);
		cbStoppingPose.addItemListener(this);
		btStart.addActionListener(this);
		btStop.addActionListener(this);
		btOpenData.addActionListener(this);
		btDelete.addActionListener(this);
		btDirectory.addActionListener(this);
		btCreate.addActionListener(this);
		btGarbage.addActionListener(this);
		sSeconds.addChangeListener(this);

		MouseAdapter showFileSize = new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				File file = getDestinationDirectory();
				if (file.exists() && !capture.isRecording()) {
					long size = new Util().getFileSize(file);
					size = size / 1_024 / 1_024;
					JButton b = (JButton) e.getSource();
					b.setToolTipText(b.getText() + " (" + size + " Mb)");
				} else {
					JButton b = (JButton) e.getSource();
					b.setToolTipText("");
				}
			}
		};

		//btOpenData.addMouseListener(showFileSize);
		//btDelete.addMouseListener(showFileSize);

		// Basic configurations
		rbColor.setSelected(true);
		rbDepth.setVisible(true);
		rbColor.setVisible(true);
		rbIr.setVisible(false);
		
		rbColor.setEnabled(false);
		rbDepth.setEnabled(false);

		txtDirectory.setEditable(false);

		lblSeconds.setFont(new Font("Serif", Font.BOLD, 100));
		lblSeconds.setForeground(Color.red);
		lblSeconds.setText("\u25A0");
		
		txtPerson.setPreferredSize(new Dimension(txtPerson.getWidth(), 25));

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

		//pnRecord.add(new JLabel("Seconds:"));
		//pnRecord.add(sSeconds);
		//pnRecord.add(new JLabel("Start Recording Pose:"));
		//pnRecord.add(cbStartingPose);
		//pnRecord.add(new JLabel("Stop Recording Pose:"));
		//pnRecord.add(cbStoppingPose);
		pnRecord.add(btStart);
		pnRecord.add(btStop);

		pnTimer.add(lblSeconds);

		pnStatus.add(BorderLayout.CENTER, lblCount);
		pnStatus.add(BorderLayout.EAST, btGarbage);

		lblSeconds.setHorizontalAlignment(SwingConstants.CENTER);

		JPanel pnDirectory = new JPanel(new BorderLayout());
		pnDirectory.add(BorderLayout.WEST, new JLabel("Path: "));
		pnDirectory.add(BorderLayout.CENTER, txtDirectory);
		pnDirectory.add(BorderLayout.EAST, btDirectory);

		if (Config.getInstance() != null) {
			txtDirectory.setText(Config.getInstance().getDirectory());
			if (Config.getInstance().getSign() != null) {
				cbSign.setModel(new DefaultComboBoxModel<>(Config.getInstance().getSign().toArray()));
			}
		}
		
		JPanel pnFolder = new JPanel(new BorderLayout());
		pnFolder.add(BorderLayout.WEST, new JLabel("Name: "));
		pnFolder.add(BorderLayout.CENTER, txtPerson);
		//pnFolder.add(BorderLayout.EAST, cbSign);

		JPanel pnFile = new JPanel(new BorderLayout());
		pnFile.setBorder(new TitledBorder("File"));
		pnFile.add(BorderLayout.NORTH, pnDirectory);
		pnFile.add(BorderLayout.CENTER, pnFolder);
		//pnFile.add(BorderLayout.EAST, btCreate);
		
		c.add(BorderLayout.NORTH, pnFile);
		c.add(BorderLayout.CENTER, pnTimer);
		c.add(BorderLayout.EAST, pnSetup);
		c.add(BorderLayout.SOUTH, pnStatus);

		pnOption.setBorder(new TitledBorder("Options"));
		pnOption.add(btOpenData);
		pnOption.add(btDelete);
		pnSetup.add(BorderLayout.SOUTH, pnOption);

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
		}
	}

	private void startingPoseDetection() {
		if (cbStartingPose.getSelectedItem().equals(lastSelectedStartPose)) {
			return;
		}
		if (!isDestinationValidMessage()) {
			cbStartingPose.setSelectedItem(lastSelectedStartPose);
			return;
		}
		lastSelectedStartPose = (String) cbStartingPose.getSelectedItem();
		File file = createDestinationDirectory();
		capture.setFile(file, getMetadata());

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
		if (!capture.isRecording()) {
			capture.setSeconds((int) sSeconds.getValue());
		}
		// startingPoseDetection();
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == btStart) {
			if (!isDestinationValidMessage()) {
				return;
			}
			File file = createDestinationDirectory();
			capture.setFile(file, getMetadata());
			capture.startRecordingUsers();
		} else if (ae.getSource() == btStop) {
			capture.stopRecordingUsers();
		} else if (ae.getSource() == btOpenData) {
			if (!isDestinationValidMessage()) {
				return;
			}

			File file = getDestinationDirectory();
			if (!file.exists()) {
				JOptionPane.showMessageDialog(this, "The informed directory doesn't exist.\n" + "<html><pre>"
						+ file.getAbsolutePath() + "</pre></html>", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}

			SimpleViewer view = new SimpleViewer(file, this);

		} else if (ae.getSource() == btDelete) {
			if (!isDestinationValidMessage()) {
				return;
			}

			File file = getDestinationDirectory();
			if (!file.exists()) {
				JOptionPane.showMessageDialog(this, "The informed directory doesn't exist.\n" + "<html><pre>"
						+ file.getAbsolutePath() + "</pre></html>", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}

			if (file.exists()) {
				int option = JOptionPane.showConfirmDialog(this,
						"Are you sure you want to delete the directory?\n" + "<html><pre>" + file.getAbsolutePath()
								+ "</pre></html>\n" + "All the recorded data will be deleted",
						"Warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

				if (option == JOptionPane.YES_OPTION) {
					try {
						Delete delete = new Delete();
						delete.deleteUsedFile(file);
						JOptionPane
								.showMessageDialog(this,
										"The following directory was deleted:\n" + "<html><pre>"
												+ file.getAbsolutePath() + "</pre></html>\n",
										"Information", JOptionPane.INFORMATION_MESSAGE);
						capture.setFramesCount(0);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(this,
								"An error happened when trying to delete the directory:\n" + "<html><pre>"
										+ file.getAbsolutePath() + "</pre></html>\n",
								"Error", JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					}
				}
			}
		} else if (ae.getSource() == btDirectory) {
			Load load = new Load();
			File file = load.openDirectory(this);
			if (file != null) {
				txtDirectory.setText(file.getAbsolutePath());
			}
		} else if (ae.getSource() == btCreate) {
			if (!isDestinationValidMessage()) {
				return;
			}
			File file = createDestinationDirectory();
			capture.setFile(file, getMetadata());
		} else if (ae.getSource() == btGarbage) {
			callGC();
		}
	}

	private boolean isDestinationValidMessage() {
		String directory = txtDirectory.getText().trim();
		String person = txtPerson.getText().trim();
		//String sign = (String) cbSign.getSelectedItem();

		if (directory == null || directory.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please, inform the destination directory", "Error",
					JOptionPane.ERROR_MESSAGE);
			txtDirectory.requestFocus();
			return false;
		}
		File dir = new File(directory);
		
		if (dir.isFile()) {
			JOptionPane.showMessageDialog(this, "Please, inform a directory instead of a file", "Error",
					JOptionPane.ERROR_MESSAGE);
			txtDirectory.requestFocus();
			return false;
		}
		if (person == null || person.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please, inform the person's name", "Error", JOptionPane.ERROR_MESSAGE);
			txtPerson.requestFocus();
			return false;
		}
		if (person.matches(".*[\\/:*?\"<>|]")) {
			JOptionPane.showMessageDialog(this,
					"Please, don't use the following characters:\n" + "<html><pre>\\ / : * ? \" &lt; > |</pre></html>",
					"Error", JOptionPane.ERROR_MESSAGE);
			txtPerson.requestFocus();
			return false;
		}
		/*if (sign == null || sign.equals("Select")) {
			JOptionPane.showMessageDialog(this, "Please, inform the name of the sign", "Error",
					JOptionPane.ERROR_MESSAGE);
			cbSign.requestFocus();
			return false;
		}*/
		return true;
	}

	private File createDestinationDirectory() {
		String directory = txtDirectory.getText().trim();
		String person = txtPerson.getText().trim();
		String sign = cbSign.getSelectedItem().toString().hashCode() + "";
		String s = File.separator;

		File tempFile = new File(directory + s + person);

		if (!tempFile.exists()) {
			//try {
				//Files.createDirectory(tempFile.toPath());
				tempFile.mkdirs();
			//} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			//}
		}

		/*File tempFile2 = new File(directory + s + person + s + sign);

		if (!tempFile2.exists()) {
			try {
				Files.createDirectory(tempFile2.toPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		
		lastTimestamp = Calendar.getInstance().getTimeInMillis();
		
		File file = new File(tempFile.getAbsolutePath() + s + lastTimestamp);
		if (!file.exists()) {
			try {
				Files.createDirectory(file.toPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println(file.getAbsolutePath());
		return file;
	}

	private File getDestinationDirectory() {
		String directory = txtDirectory.getText().trim();
		String person = txtPerson.getText().trim();
		String sign = cbSign.getSelectedItem().toString().hashCode() + "";
		String s = File.separator;

		// File file = new File(directory + s + person + s + sign + s + lastTimestamp);
		File file = new File(directory + s + person + s + lastTimestamp);

		return file;
	}

	private CaptureMetadata getMetadata() {
		CaptureMetadata metadata = new CaptureMetadata();

		metadata.setPerson(txtPerson.getText().trim());
		metadata.setSign(null);
		metadata.setFolder(String.valueOf(lastTimestamp));
		metadata.setRecord(String.valueOf(lastTimestamp));
		metadata.setCreator(System.getProperty("user.name"));

		return metadata;
	}

	public void callGC() {
		Runtime run = Runtime.getRuntime();
		run.runFinalization();
		run.gc();

		statusBar();
	}

	public void callGCAlways(int millis) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(millis);
						callGC();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
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
