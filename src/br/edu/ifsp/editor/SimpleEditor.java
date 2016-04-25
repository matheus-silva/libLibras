package br.edu.ifsp.editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import br.edu.ifsp.capturer.Coordinate;
import br.edu.ifsp.capturer.ImageCapture;
import br.edu.ifsp.capturer.Segmentation;
import br.edu.ifsp.capturer.ShowObject;
import br.edu.ifsp.util.CaptureData;
import br.edu.ifsp.util.Load;

public class SimpleEditor extends JDialog implements ActionListener, ChangeListener {

	private CaptureData data;
	private Load load;
	private ShowObject view;
	private Coordinate coor;
	private Segmentation seg;
	private ImageCapture imgDepth;
	private ImageCapture imgColor;

	private JPanel pnView;
	private JSlider slider;
	private JMenuBar menu;
	private JMenu mFile, mView;
	private JMenuItem mOpen;
	private JMenuItem mColor, mDepth, mSkeleton, mSegmentation;
	private JMenuItem mSaveImage;
	
	private JFrame father;

	public SimpleEditor(JFrame father) {
		this(null, father);
	}

	public SimpleEditor(File file, JFrame father) {
		super(new JFrame(), "Simple Editor", true);
		
		this.load = new Load();
		this.father = father;

		setJMenuBar(getMenu());
		initializeComponents();
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(640, 580);
		
		if (file != null) {
			initialize(file);
			loadData();
		}
		
		setVisible(true);
		
	}

	private CaptureData load(File file) {
		return load.loadFile(father, file);
	}

	private void initialize(File file) {
		if (file != null) {
			data = load(file);
		}
		view = new ShowObject();
		coor = new Coordinate(view);
		seg = new Segmentation(view);
		imgDepth = new ImageCapture(view, ImageCapture.DEPTH);
		imgColor = new ImageCapture(view, ImageCapture.COLOR);
	}

	private void initializeComponents() {
		Container c = getContentPane();
		c.setLayout(new BorderLayout());

		c.removeAll();
		c.revalidate();

		pnView = getComponent();
		c.add(BorderLayout.CENTER, pnView);
		c.add(BorderLayout.SOUTH, getControl());

		c.revalidate();
		c.repaint();
	}

	private JMenuBar getMenu() {
		menu = new JMenuBar();

		mFile = new JMenu("File");
		mView = new JMenu("View");

		mOpen = new JMenuItem("Open");

		mColor = new JMenuItem("Color");
		mDepth = new JMenuItem("Depth");
		mSkeleton = new JMenuItem("Skeleton");
		mSegmentation = new JMenuItem("Segmentation");
		mSaveImage = new JMenuItem("Save as Image");
		mOpen.addActionListener(this);

		mColor.addActionListener(this);
		mDepth.addActionListener(this);
		mSkeleton.addActionListener(this);
		mSegmentation.addActionListener(this);
		mSaveImage.addActionListener(this);

		mFile.add(mOpen);
		mFile.add(mSaveImage);

		mView.add(mColor);
		mView.add(mDepth);
		mView.add(mSkeleton);
		mView.add(mSegmentation);

		mView.setEnabled(false);
		mSaveImage.setEnabled(false);

		menu.add(mFile);
		menu.add(mView);

		return menu;
	}

	private void loadData() {
		getContentPane().add(BorderLayout.CENTER, getComponent());

		mView.setEnabled(true);
		mColor.setEnabled(data.hasImageColor());
		mDepth.setEnabled(data.hasImageDepth());
		mSkeleton.setEnabled(data.hasCoordinatesDepth());
		mSegmentation.setEnabled(data.hasSegmentation());
		mSaveImage.setEnabled(true);

		slider.setMinimum(0);
		slider.setMaximum(data.getTimestamp().size());
		slider.setValue(0);
	}

	private JPanel getComponent() {
		JPanel c = new JPanel(new GridLayout(1, 0));
		c.setBorder(new TitledBorder("View"));
		if (view != null) {
			c.add(view);
		}
		c.setSize(640, 480);
		return c;
	}

	private JPanel getControl() {
		JPanel c = new JPanel(new BorderLayout());
		c.setBorder(new TitledBorder("Control"));
		c.setSize(640, 100);

		slider = new JSlider();
		slider.setValue(0);
		slider.setPaintTicks(true);
		slider.setMinorTickSpacing(1);
		slider.setMajorTickSpacing(25);
		slider.setPaintLabels(true);
		slider.addChangeListener(this);

		c.add(BorderLayout.CENTER, slider);
		return c;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == mColor) {
			view.setCamera(ShowObject.COLOR);

			Set<Long> time = data.getImageColor().keySet();
			data.setTimestamp(time);
			slider.setMaximum(time.size() - 1);

		} else if (e.getSource() == mDepth) {
			view.setCamera(ShowObject.DEPTH);

			Set<Long> time = data.getImageDepth().keySet();
			data.setTimestamp(time);
			slider.setMaximum(time.size() - 1);
		} else if (e.getSource() == mSkeleton) {

			Set<Long> time = data.getCoordinateDepth().keySet();
			data.setTimestamp(time);
			slider.setMaximum(time.size() - 1);
		} else if (e.getSource() == mSegmentation) {
			view.setCamera(ShowObject.DEPTH);

			Set<Long> time = data.getSegmentation().keySet();
			data.setTimestamp(time);
			slider.setMaximum(time.size() - 1);
		} else if (e.getSource() == mOpen) {
			File file = load.openDirectory(father);

			initialize(file);
			loadData();

		} else if (e.getSource() == mSaveImage) {
			File file = load.openDirectory(father);

			if (file == null || !file.exists()) {
				return;
			}

			long timestamp = data.getTimestampByIndex(slider.getValue());
			String name = File.separator + timestamp + ".png";

			view.saveFrame(new File(file.getAbsoluteFile() + name));
		}
		stateChanged(new ChangeEvent(slider));
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == slider) {
			int index = slider.getValue();

			Long timestamp = data.getTimestampByIndex(index);

			if (data.hasCoordinatesDepth()) {
				view.setUserCoordinate(data.getCoordinateDepth().get(timestamp), 0, 0);
			}

			if (data.hasSegmentation()) {
				ByteBuffer buffSegmentation = data.getSegmentation().get(timestamp);

				if (buffSegmentation != null) {
					buffSegmentation.rewind();
				}

				view.setUserMap(seg, buffSegmentation, timestamp);
			}

			ByteBuffer buffBackground = null;
			if (view.getCamera() == ShowObject.COLOR) {
				if (data.hasImageColor()) {
					buffBackground = data.getImageColor().get(timestamp);
				}
			} else if (view.getCamera() == ShowObject.DEPTH) {
				if (data.hasImageDepth()) {
					buffBackground = data.getImageDepth().get(timestamp);
				}
			} else {
				return;
			}

			if (buffBackground != null) {
				buffBackground.rewind();
			}

			view.setBackground(buffBackground, 640, 480);

			view.revalidate();
			view.repaint();
			
			setTitle("Simple Editor - " + data.getTimestampByIndex(slider.getValue()));
		}
	}

	public static void main(String args[]) {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				new SimpleEditor(new JFrame());
			}
		});
	}

}
