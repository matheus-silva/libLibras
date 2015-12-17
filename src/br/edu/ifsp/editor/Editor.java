package br.edu.ifsp.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import br.edu.ifsp.util.Load;
import br.edu.ifsp.util.Save;

public class Editor extends JFrame implements ChangeListener, ActionListener {

	private File currentFile;
	private HistoryCoordinate history;

	private JPanel pnMain;
	private JPanel pnBody;
	private JPanel pnTimeline;
	private JPanel pnCropArea;

	private JSlider slTimeline;
	private JCheckBox cbMoveTimeline;

	private JMenuItem mOpen;
	private JMenuItem mSave;
	private JMenuItem mSaveAs;
	private JMenuItem mClose;
	private JMenuItem mExit;
	private JMenuItem mUndo;
	private JMenuItem mRedo;
	private JMenuItem mInvert;
	private JMenuItem mZoomIn;
	private JMenuItem mZoomOut;
	private JMenuItem mMoveUp;
	private JMenuItem mMoveDown;
	private JMenuItem mMoveLeft;
	private JMenuItem mMoveRight;
	private JRadioButtonMenuItem mSkeleton;
	private JRadioButtonMenuItem mCircle;
	private JRadioButtonMenuItem mNumber;

	private JButton btCrop;

	private JTable tbCoords;

	private JSpinner spLower;
	private JSpinner spUpper;

	private RangeSlider slCrop;
	private Comp comp;
	private Comp.Modification modification;

	private boolean saved;
	private float[][][] coords;

	public Editor() {
		this(null);
	}

	public Editor(File arquivo) {
		super("Editor");

		setJMenuBar(getMenu());

		if (!(arquivo == null || arquivo.equals(""))) {
			openFile(arquivo);
		}

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 600);
		setLocationRelativeTo(this);
	}

	private void openFile(File arquivo) {
		currentFile = arquivo;
		setTitle("Editor - " + arquivo.getAbsolutePath());

		float[][][] c = new Load().loadFile(arquivo);
		history = new HistoryCoordinate(c);
		if (mUndo != null) {
			mUndo.setEnabled(!history.isFirst());
		}
		if (mRedo != null) {
			mRedo.setEnabled(!history.isLast());
		}

		comp = null;

		loadCoords(history.getCurrentState());
		saved = true;
	}

	private void loadCoords(float[][][] coords) {
		if (comp != null) {
			modification = comp.getModification();
		}
		close();
		getContentPane().add(BorderLayout.CENTER, getMainPanel(coords));
		
		revalidate();
		repaint();
	}

	private JMenuBar getMenu() {
		JMenuBar menu = new JMenuBar();

		mOpen = new JMenuItem("Open");
		mSave = new JMenuItem("Save");
		mSaveAs = new JMenuItem("Save As...");
		mClose = new JMenuItem("Close");
		mExit = new JMenuItem("Exit");
		mUndo = new JMenuItem("Undo");
		mRedo = new JMenuItem("Redo");
		mInvert = new JMenuItem("Invert");

		mZoomIn = new JMenuItem("In");
		mZoomOut = new JMenuItem("Out");

		mMoveUp = new JMenuItem("Up");
		mMoveDown = new JMenuItem("Down");
		mMoveLeft = new JMenuItem("Left");
		mMoveRight = new JMenuItem("Right");

		mCircle = new JRadioButtonMenuItem("Circle");
		mNumber = new JRadioButtonMenuItem("Number");
		mSkeleton = new JRadioButtonMenuItem("Skeleton");

		ButtonGroup viewGroup = new ButtonGroup();
		viewGroup.add(mCircle);
		viewGroup.add(mNumber);
		viewGroup.add(mSkeleton);
		mSkeleton.setSelected(true);

		mOpen.addActionListener(this);
		mSave.addActionListener(this);
		mSaveAs.addActionListener(this);
		mClose.addActionListener(this);
		mExit.addActionListener(this);
		mUndo.addActionListener(this);
		mRedo.addActionListener(this);
		mInvert.addActionListener(this);
		mZoomIn.addActionListener(this);
		mZoomOut.addActionListener(this);
		mMoveUp.addActionListener(this);
		mMoveDown.addActionListener(this);
		mMoveLeft.addActionListener(this);
		mMoveRight.addActionListener(this);
		mCircle.addActionListener(this);
		mNumber.addActionListener(this);
		mSkeleton.addActionListener(this);

		JMenu file = new JMenu("File");
		JMenu edit = new JMenu("Edit");
		JMenu view = new JMenu("View");

		mOpen.setAccelerator(KeyStroke.getKeyStroke("control O"));
		mSave.setAccelerator(KeyStroke.getKeyStroke("control S"));
		mSaveAs.setAccelerator(KeyStroke.getKeyStroke("control shift S"));
		mUndo.setAccelerator(KeyStroke.getKeyStroke("control Z"));
		mRedo.setAccelerator(KeyStroke.getKeyStroke("control shift Z"));

		mInvert.setAccelerator(KeyStroke.getKeyStroke("alt shift I"));

		mZoomIn.setAccelerator(KeyStroke.getKeyStroke("alt shift P"));
		mZoomOut.setAccelerator(KeyStroke.getKeyStroke("alt shift L"));

		mMoveUp.setAccelerator(KeyStroke.getKeyStroke("alt shift UP"));
		mMoveDown.setAccelerator(KeyStroke.getKeyStroke("alt shift DOWN"));
		mMoveLeft.setAccelerator(KeyStroke.getKeyStroke("alt shift LEFT"));
		mMoveRight.setAccelerator(KeyStroke.getKeyStroke("alt shift RIGHT"));

		mCircle.setAccelerator(KeyStroke.getKeyStroke("alt shift C"));
		mNumber.setAccelerator(KeyStroke.getKeyStroke("alt shift N"));
		mSkeleton.setAccelerator(KeyStroke.getKeyStroke("alt shift S"));

		mUndo.setEnabled(false);
		mRedo.setEnabled(false);

		file.add(mOpen);
		file.addSeparator();
		file.add(mSave);
		file.add(mSaveAs);
		file.addSeparator();
		file.add(mClose);
		file.add(mExit);

		edit.add(mUndo);
		edit.add(mRedo);

		JMenu move = new JMenu("Move");
		move.add(mMoveUp);
		move.add(mMoveDown);
		move.add(mMoveLeft);
		move.add(mMoveRight);

		JMenu zoom = new JMenu("Zoom");
		zoom.add(mZoomIn);
		zoom.add(mZoomOut);

		JMenu option = new JMenu("Options");
		option.add(mCircle);
		option.add(mNumber);
		option.add(mSkeleton);

		view.add(mInvert);
		view.add(move);
		view.add(zoom);
		view.add(option);

		menu.add(file);
		menu.add(edit);
		menu.add(view);

		file.setMnemonic(KeyEvent.VK_F);
		mOpen.setMnemonic(KeyEvent.VK_O);
		mSave.setMnemonic(KeyEvent.VK_S);
		mSaveAs.setMnemonic(KeyEvent.VK_A);
		mClose.setMnemonic(KeyEvent.VK_C);
		mExit.setMnemonic(KeyEvent.VK_X);

		edit.setMnemonic(KeyEvent.VK_E);
		mUndo.setMnemonic(KeyEvent.VK_U);
		mRedo.setMnemonic(KeyEvent.VK_R);

		view.setMnemonic(KeyEvent.VK_V);
		mInvert.setMnemonic(KeyEvent.VK_I);
		move.setMnemonic(KeyEvent.VK_M);
		mMoveDown.setMnemonic(KeyEvent.VK_D);
		mMoveLeft.setMnemonic(KeyEvent.VK_L);
		mMoveRight.setMnemonic(KeyEvent.VK_R);
		mMoveUp.setMnemonic(KeyEvent.VK_U);
		option.setMnemonic(KeyEvent.VK_O);
		mSkeleton.setMnemonic(KeyEvent.VK_S);
		mCircle.setMnemonic(KeyEvent.VK_C);
		mNumber.setMnemonic(KeyEvent.VK_N);
		zoom.setMnemonic(KeyEvent.VK_Z);
		mZoomIn.setMnemonic(KeyEvent.VK_I);
		mZoomOut.setMnemonic(KeyEvent.VK_O);

		return menu;
	}

	public JPanel getMainPanel(float[][][] c) {
		coords = c;

		pnMain = new JPanel(new BorderLayout());

		JPanel pnEast = new JPanel(new BorderLayout());
		pnEast.add(BorderLayout.NORTH, getTimelinePanel());
		pnEast.add(BorderLayout.CENTER, getCoordsPanel());

		pnMain.add(BorderLayout.CENTER, getBodyPanel());
		pnMain.add(BorderLayout.EAST, pnEast);
		pnMain.add(BorderLayout.SOUTH, getCropAreaPanel());
		return pnMain;
	}

	private JPanel getBodyPanel() {
		pnBody = new JPanel(new GridLayout(0, 1));
		pnBody.setBorder(new TitledBorder("Coordinates"));

		comp = new Comp(coords);
		comp.setPreferredSize(new Dimension(640, 480));
		comp.setOption(Comp.SKELETON);
		comp.setBackground(Color.white);

		JScrollPane c = new JScrollPane(comp);

		c.setPreferredSize(comp.getPreferredSize());

		if (modification != null) {
			comp.setModification(modification);
			comp.applyModification();
		}
		
		pnBody.add(c);
		return pnBody;
	}

	private JPanel getTimelinePanel() {
		pnTimeline = new JPanel(new GridLayout(0, 1));
		pnTimeline.setBorder(new TitledBorder("Timeline"));

		slTimeline = new JSlider(JSlider.HORIZONTAL, 0, coords.length - 1, 0);

		slTimeline.setSize(pnTimeline.getSize());

		int major = (int) Math.floor((coords.length - 1) / 7);
		if (major % 5 != 0)
			major = (int) Math.floor(major / 5) * 5;
		slTimeline.setMajorTickSpacing(major);
		slTimeline.setMinorTickSpacing(5);
		slTimeline.setPaintTicks(true);
		slTimeline.setPaintLabels(true);

		slTimeline.addChangeListener(this);

		pnTimeline.add(slTimeline);
		return pnTimeline;
	}

	private JPanel getCropAreaPanel() {
		pnCropArea = new JPanel(new GridLayout(0, 1));
		pnCropArea.setBorder(new TitledBorder("Editor - Crop"));

		cbMoveTimeline = new JCheckBox("Move timeline");
		cbMoveTimeline.setSelected(false);

		slCrop = new RangeSlider(0, coords.length - 1);
		int major = (int) Math.floor((coords.length - 1) / 15);
		if (major % 5 != 0)
			major = (int) Math.floor(major / 5) * 5;
		slCrop.setMajorTickSpacing(major);
		slCrop.setMinorTickSpacing(5);
		slCrop.setPaintTicks(true);
		slCrop.setPaintLabels(true);

		slCrop.addChangeListener(this);

		JPanel pnCropTolls = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

		btCrop = new JButton("Crop");
		btCrop.addActionListener(this);

		spLower = new JSpinner();
		spUpper = new JSpinner();
		createJSpinner();
		spLower.addChangeListener(this);
		spUpper.addChangeListener(this);

		pnCropTolls.add(new JLabel("Lower: "));
		pnCropTolls.add(spLower);
		pnCropTolls.add(new JLabel("Upper: "));
		pnCropTolls.add(spUpper);
		pnCropTolls.add(cbMoveTimeline);
		pnCropTolls.add(btCrop);

		pnCropArea.add(pnCropTolls);
		pnCropArea.add(slCrop);
		return pnCropArea;
	}

	public void createJSpinner() {
		SpinnerModel smLower = new SpinnerNumberModel(slCrop.getValue(), 0, slCrop.getUpperValue(), 1);
		SpinnerModel smUpper = new SpinnerNumberModel(slCrop.getUpperValue(), slCrop.getValue(), slCrop.getMaximum(),
				1);

		spLower.setModel(smLower);
		spUpper.setModel(smUpper);

	}

	public JPanel getCoordsPanel() {
		tbCoords = new JTable();
		((DefaultTableModel) tbCoords.getModel()).addColumn("");
		((DefaultTableModel) tbCoords.getModel()).addColumn("Body");
		((DefaultTableModel) tbCoords.getModel()).addColumn("X");
		((DefaultTableModel) tbCoords.getModel()).addColumn("Y");
		((DefaultTableModel) tbCoords.getModel()).addColumn("Z");

		tbCoords.getColumnModel().getColumn(0).setMaxWidth(20);
		tbCoords.getColumnModel().getColumn(1).setMinWidth(100);
		tbCoords.getColumnModel().getColumn(2).setMaxWidth(75);
		tbCoords.getColumnModel().getColumn(3).setMaxWidth(75);
		tbCoords.getColumnModel().getColumn(4).setMaxWidth(75);
		JScrollPane sp = new JScrollPane(tbCoords);

		sp.setPreferredSize(tbCoords.getPreferredSize());

		JPanel pnT = new JPanel(new GridLayout(0, 1));
		pnT.setBorder(new TitledBorder("Editor - Coords"));

		loadTableCoords(coords);

		pnT.add(sp);

		return pnT;
	}

	public void loadTableCoords(float[][][] coords) {
		((DefaultTableModel) tbCoords.getModel()).setRowCount(0);

		float[][] temp = coords[slTimeline.getValue()];
		String[][] c = new String[temp.length][3];

		for (int i = 0; i < temp.length; i++) {
			c[i][0] = String.valueOf(temp[i][0]);
			c[i][1] = String.valueOf(temp[i][1]);
			c[i][2] = String.valueOf(temp[i][2]);
		}

		((DefaultTableModel) tbCoords.getModel()).addRow(new String[] { "0", "Head", c[0][0], c[0][1], c[0][2] });
		((DefaultTableModel) tbCoords.getModel()).addRow(new String[] { "1", "Neck", c[1][0], c[1][1], c[1][2] });
		((DefaultTableModel) tbCoords.getModel())
				.addRow(new String[] { "2", "Shoulder Left", c[2][0], c[2][1], c[2][2] });
		((DefaultTableModel) tbCoords.getModel())
				.addRow(new String[] { "3", "Shoulder Right", c[3][0], c[3][1], c[3][2] });
		((DefaultTableModel) tbCoords.getModel()).addRow(new String[] { "4", "Elbow Left", c[4][0], c[4][1], c[4][2] });
		((DefaultTableModel) tbCoords.getModel())
				.addRow(new String[] { "5", "Elbow Right", c[5][0], c[5][1], c[5][2] });
		((DefaultTableModel) tbCoords.getModel()).addRow(new String[] { "6", "Hand Left", c[6][0], c[6][1], c[6][2] });
		((DefaultTableModel) tbCoords.getModel()).addRow(new String[] { "7", "Hand Right", c[7][0], c[7][1], c[7][2] });
		((DefaultTableModel) tbCoords.getModel()).addRow(new String[] { "8", "Torso", c[8][0], c[8][1], c[8][2] });
		((DefaultTableModel) tbCoords.getModel()).addRow(new String[] { "9", "Hip Left", c[9][0], c[9][1], c[9][2] });
		((DefaultTableModel) tbCoords.getModel())
				.addRow(new String[] { "10", "Hip Right", c[10][0], c[10][1], c[10][2] });
		((DefaultTableModel) tbCoords.getModel())
				.addRow(new String[] { "11", "Knee Left", c[11][0], c[11][1], c[11][2] });
		((DefaultTableModel) tbCoords.getModel())
				.addRow(new String[] { "12", "Knee Right", c[12][0], c[12][1], c[12][2] });
		((DefaultTableModel) tbCoords.getModel())
				.addRow(new String[] { "13", "Foot Left", c[13][0], c[13][1], c[13][2] });
		((DefaultTableModel) tbCoords.getModel())
				.addRow(new String[] { "14", "Foot Right", c[14][0], c[14][1], c[14][2] });
	}

	private void close() {
		if (pnMain != null) {
			getContentPane().remove(pnMain);
			revalidate();
			repaint();
		}
	}

	private boolean closeFile() {
		int v = JOptionPane.showConfirmDialog(this,
				"Are you sure you want to close this file?" + "\nAny unsaved changes will be lost", "Close/Exit",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		if (v != JOptionPane.YES_OPTION) {
			return false;
		}

		return true;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == slTimeline) {
			comp.setIndexAt(slTimeline.getValue());
			comp.repaint();
			loadTableCoords(coords);
		} else if (e.getSource() == slCrop) {
			createJSpinner();
			if (!cbMoveTimeline.isSelected()) {
				return;
			}

			RangeSliderUI r = (RangeSliderUI) slCrop.getUI();
			if (r.isLowerSelected()) {
				slTimeline.setValue(slCrop.getValue());
			} else if (r.isUpperSelected()) {
				slTimeline.setValue(slCrop.getUpperValue());
			}

		} else if (e.getSource() == spLower) {
			slCrop.setValue((int) spLower.getValue());
		} else if (e.getSource() == spUpper) {
			slCrop.setUpperValue((int) spUpper.getValue());
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == mOpen) {
			File f = new Load().open(this);
			if (f != null) {
				openFile(f);
			}
		} else if (e.getSource() == mSave) {
			new Save().saveFile(this, currentFile, history.getCurrentState());
			saved = true;

		} else if (e.getSource() == mSaveAs) {
			Save s = new Save();
			File f = s.getFile(this);
			if (f != null) {
				s.saveFile(this, f, history.getCurrentState());
				// currentFile = f;
				// setTitle("Editor - " + f.getAbsolutePath());

				openFile(f);
			}
		} else if (e.getSource() == mClose) {
			if (closeFile()) {
				close();
			}
		} else if (e.getSource() == mExit) {
			if (closeFile()) {
				System.exit(0);
			}
			if (!new Editor.Util().isEquals(history.getOriginal(), history.getCurrentState())) {
				return;
			}
		} else if (e.getSource() == mUndo) {
			loadCoords(history.undo());
			mUndo.setEnabled(!history.isFirst());
			mRedo.setEnabled(!history.isLast());

		} else if (e.getSource() == mRedo) {
			loadCoords(history.redo());
			mUndo.setEnabled(!history.isFirst());
			mRedo.setEnabled(!history.isLast());

		} else if (e.getSource() == mInvert) {
			comp.invertVertical();
			comp.repaint();

		} else if (e.getSource() == mZoomIn) {
			comp.zoomIn();
			comp.repaint();

		} else if (e.getSource() == mZoomOut) {
			comp.zoomOut();
			comp.repaint();

		} else if (e.getSource() == mMoveUp) {
			comp.moveUp();
			comp.repaint();

		} else if (e.getSource() == mMoveDown) {
			comp.moveDown();
			comp.repaint();

		} else if (e.getSource() == mMoveLeft) {
			comp.moveLeft();
			comp.repaint();

		} else if (e.getSource() == mMoveRight) {
			comp.moveRight();
			comp.repaint();

		} else if (e.getSource() == mCircle) {
			comp.setOption(Comp.CIRCLE);
			comp.repaint();

		} else if (e.getSource() == mNumber) {
			comp.setOption(Comp.NUMBER);
			comp.repaint();

		} else if (e.getSource() == mSkeleton) {
			comp.setOption(Comp.SKELETON);
			comp.repaint();

		} else if (e.getSource() == btCrop) {

			int startIndex = slCrop.getValue(), endIndex = slCrop.getUpperValue();

			history.addChange(new Editor.Util().crop(history.getCurrentState(), startIndex, endIndex));
			loadCoords(history.getCurrentState());
			mUndo.setEnabled(!history.isFirst());
			mRedo.setEnabled(!history.isLast());
			saved = false;
		}
		// System.out.println(history.getCurrentState() == coords);
	}

	public static void main(String args[]) {
		for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
			System.out.println(info.getName());
			if ("Metal".equals(info.getName())) {
				try {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException ex) {
					Logger.getLogger(Editor.class.getName()).log(Level.SEVERE, null, ex);
				}
				break;
			}
		}
		// Editor e = new Editor();
		Editor e = new Editor(new File("data/Dados - Juntas 10.txt"));
		// Editor e = new Editor("data/Dados - Real World.txt");
		e.setVisible(true);
	}

	public static class Util {

		public float[][][] crop(float[][][] coord, int startIndex, int endIndex) {
			if (coord == null) {
				return null;
			}
			if (startIndex == 0 && endIndex == coord.length - 1) {
				return new float[][][] {};

			} else if (startIndex == 0) {
				return Arrays.copyOfRange(coord, endIndex + 1, coord.length);

			} else if (endIndex == coord.length - 1) {
				return Arrays.copyOfRange(coord, 0, startIndex);

			} else {
				float[][][] newArray = new float[startIndex + (coord.length - 1 - endIndex)][][];

				System.arraycopy(coord, 0, newArray, 0, startIndex);
				System.arraycopy(coord, endIndex + 1, newArray, startIndex, coord.length - 1 - endIndex);

				return newArray;
			}
		}

		public boolean isEquals(float[][][] a, float[][][] b) {
			if (a.length != b.length) {
				return false;
			}

			for (int i = 0; i < a.length; i++) {
				if (a[i].length != b[i].length) {
					return false;
				}

				for (int j = 0; j < a[i].length; j++) {
					if (a[i][j].length != b[i][j].length) {
						return false;
					}

					for (int k = 0; k < a[i][j].length; k++) {

						if (a[i][j][k] != b[i][j][k]) {
							return false;
						}
					}
				}
			}
			return true;
		}

	}
}