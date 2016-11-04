package br.edu.ifsp.editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import br.edu.ifsp.capturer.ShowObject;
import br.edu.ifsp.util.CaptureData;
import br.edu.ifsp.util.CaptureData.CaptureMetadata;
import br.edu.ifsp.util.Load;

public class SimpleViewer extends JDialog implements ChangeListener, ActionListener, ListSelectionListener {

	private ShowObject viewColor, viewDepth, view;
	private JSlider slColor, slDepth, slSync;
	private JList lRecords;
	private File parent, current;
	private Load load;
	private JFrame father;
	private Dimension dimensionDepth, dimensionColor;
	private CaptureData data;
	private List<CaptureData.SyncData> syncData;
	private JMenuItem mOpen, mOpenFile, mSaveImage, mRealWorld, mCut;

	public SimpleViewer(JFrame father) {
		this(null, father);
	}

	public SimpleViewer(File file, JFrame father) {
		super(new JFrame(), "Simple Viewer", true);

		this.load = new Load();
		this.father = father;

		setJMenuBar(createMenuBar());

		if (file != null) {
			setTitle("Simple Viewer - " + file.getAbsolutePath());
			this.data = openData(file);
			if (this.data != null) {
				this.syncData = this.data.synchronize();
				loadDataDirectory(this.data);
				loadRecords(file);
			}
		}

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(1366, 600);
	}

	private JMenuBar createMenuBar() {
		JMenuBar menu = new JMenuBar();

		mOpen = new JMenuItem("Open Directory");
		mOpenFile = new JMenuItem("Open File");
		mSaveImage = new JMenuItem("Save as Image");
		mRealWorld = new JMenuItem("Real World Coordinate");
		mCut = new JMenuItem("Cut");

		mSaveImage.setEnabled(false);
		
		mOpen.setAccelerator(KeyStroke.getKeyStroke("control O"));
		mOpenFile.setAccelerator(KeyStroke.getKeyStroke("control shift O"));
		mSaveImage.setAccelerator(KeyStroke.getKeyStroke("control shift S"));
		mRealWorld.setAccelerator(KeyStroke.getKeyStroke("control R"));
		mCut.setAccelerator(KeyStroke.getKeyStroke("control T"));

		mOpen.addActionListener(this);
		mOpenFile.addActionListener(this);
		mSaveImage.addActionListener(this);
		mRealWorld.addActionListener(this);
		mCut.addActionListener(this);

		JMenu file = new JMenu("File");
		file.add(mOpen);
		file.add(mOpenFile);
		file.addSeparator();
		file.add(mSaveImage);

		JMenu view = new JMenu("View");
		view.add(mRealWorld);

		JMenu edit = new JMenu("Edit");
		edit.add(mCut);

		menu.add(file);
		menu.add(edit);
		menu.add(view);

		return menu;
	}

	private CaptureData openData(File file) {
		CaptureData data = null;
		if (file != null) {
			current = file;
			data = load.loadDirectory(father, file);
		}
		return data;
	}

	private void loadDataFile(ByteBuffer buff, CaptureMetadata metadata, int camera) {
		Container c = getContentPane();

		Dimension d;
		if (metadata != null) {
			d = new Dimension(metadata.getColorWidth(), metadata.getColorHeight());
		} else {
			d = new Dimension(640, 480);
		}

		view = new ShowObject();

		view.setCamera(camera);

		view.setPreferredSize(d);

		view.setBackground(buff, d);

		c.add(BorderLayout.CENTER, view);

		view.revalidate();

		view.repaint();

		setSize(d.width, d.height + 100);
		
		mSaveImage.setEnabled(true);
		mCut.setEnabled(false);
		mRealWorld.setEnabled(false);
	}

	private void loadDataDirectory(CaptureData data) {
		Container c = getContentPane();
		
		viewColor = new ShowObject();
		viewDepth = new ShowObject();
		slColor = new JSlider();
		slDepth = new JSlider();
		slSync = new JSlider();
		lRecords = new JList<>();
		JScrollPane scrollList = new JScrollPane(lRecords);

		lRecords.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		viewColor.setCamera(ShowObject.COLOR);
		viewDepth.setCamera(ShowObject.DEPTH);

		CaptureMetadata metadata = data.getMetadata();
		
		if(metadata != null) {
			dimensionColor = new Dimension(metadata.getColorWidth(), metadata.getColorHeight());
			dimensionDepth = new Dimension(metadata.getDepthWidth(), metadata.getDepthHeight());
		} else {
			dimensionColor = dimensionDepth = new Dimension(640, 480); 
		}
		
		viewColor.setPreferredSize(dimensionColor);
		viewDepth.setPreferredSize(dimensionDepth);

		slColor.setMaximum(0);
		slDepth.setMaximum(0);
		slSync.setMaximum(0);

		if (data != null) {
			if (data.hasImageColor()) {
				slColor.setMaximum(data.getImageColor().size() - 1);
			}
			if (data.hasImageDepth()) {
				slDepth.setMaximum(data.getImageDepth().size() - 1);
			}
			if (syncData != null) {
				slSync.setMaximum(syncData.size() - 1);
			}
		}

		slColor.setValue(0);
		slDepth.setValue(0);
		slSync.setValue(0);

		slColor.setMajorTickSpacing(10);
		slDepth.setMajorTickSpacing(10);
		slSync.setMajorTickSpacing(10);

		slColor.setMinorTickSpacing(1);
		slDepth.setMinorTickSpacing(1);
		slSync.setMinorTickSpacing(1);

		slColor.setPaintTicks(true);
		slDepth.setPaintTicks(true);

		slColor.setPaintTrack(true);
		slDepth.setPaintTrack(true);

		slColor.setPaintLabels(true);
		slDepth.setPaintLabels(true);

		slColor.addChangeListener(this);
		slDepth.addChangeListener(this);
		slSync.addChangeListener(this);
		lRecords.addListSelectionListener(this);

		JPanel pnSliderColor = new JPanel(new BorderLayout());
		pnSliderColor.add(BorderLayout.CENTER, slColor);
		pnSliderColor.setBorder(new TitledBorder("Color"));

		JPanel pnSliderDepth = new JPanel(new BorderLayout());
		pnSliderDepth.add(BorderLayout.CENTER, slDepth);
		pnSliderDepth.setBorder(new TitledBorder("Depth"));

		JPanel pnColor = new JPanel(new BorderLayout());
		pnColor.add(BorderLayout.CENTER, viewColor);
		pnColor.add(BorderLayout.SOUTH, pnSliderColor);
		pnColor.setBorder(new TitledBorder("Color"));

		JPanel pnDepth = new JPanel(new BorderLayout());
		pnDepth.add(BorderLayout.CENTER, viewDepth);
		pnDepth.add(BorderLayout.SOUTH, pnSliderDepth);
		pnDepth.setBorder(new TitledBorder("Depth"));

		JPanel pnViewer = new JPanel(new GridLayout(0, 2));
		pnViewer.add(pnColor);
		pnViewer.add(pnDepth);

		c.add(BorderLayout.CENTER, pnViewer);
		c.add(BorderLayout.SOUTH, slSync);
		if (data != null) {
			c.add(BorderLayout.WEST, scrollList);
		}
		slSync.requestFocusInWindow();

		viewColor.repaint();
		viewDepth.repaint();

		setSize(1366, 600);
		
		mSaveImage.setEnabled(false);
		mCut.setEnabled(true);
		mRealWorld.setEnabled(true);
	}

	private void loadRecords(File file) {
		parent = file.getParentFile();
		File[] children = parent.listFiles();
		DefaultListModel<String> model = new DefaultListModel<>();

		for (File f : children) {
			model.addElement(f.getName());
		}
		lRecords.setModel(model);
		lRecords.setSelectedIndex(model.indexOf(file.getName()));
	}

	public int getIndexByTimestamp(Long timestamp, Set<Long> timestamps) {
		int index = 0;
		for (Long time : timestamps) {
			if (time == timestamp) {
				return index;
			}
			index++;
		}
		return 0;
	}

	public Long getTimestampByIndex(int index, Set<Long> timestamp) {
		int pos = 0;
		for (Long l : timestamp) {
			if (pos == index) {
				return l;
			}
			pos++;
		}
		return null;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == slColor) {
			if (data == null || !data.hasImageColor()) {
				return;
			}
			Map<Long, ByteBuffer> c = data.getImageColor();
			Set<Long> keys = c.keySet();
			int index = slColor.getValue();
			Long timestamp = getTimestampByIndex(index, keys);
			ByteBuffer buff = c.get(timestamp);

			viewColor.setStatus(timestamp.toString());

			viewColor.setBackground(buff, dimensionColor);
			viewColor.repaint();
		} else if (e.getSource() == slDepth) {
			if (data == null || !data.hasImageDepth()) {
				return;
			}
			Map<Long, ByteBuffer> c = data.getImageDepth();
			Set<Long> keys = c.keySet();
			int index = slDepth.getValue();
			Long timestamp = getTimestampByIndex(index, keys);
			ByteBuffer buff = c.get(timestamp);

			viewDepth.setStatus(timestamp.toString());
			viewDepth.setBackground(buff, dimensionDepth);
			String sTimestamp = timestamp.toString();

			if (data.hasCoordinatesDepth()) {
				viewDepth.setUserCoordinate(
						data.getCoordinateDepth().get(Long.parseLong(sTimestamp.substring(0, sTimestamp.length() - 1))),
						dimensionDepth);
			}

			viewDepth.repaint();
		} else if (e.getSource() == slSync) {
			if (data == null) {
				return;
			}
			int index = slSync.getValue();

			if (data.hasImageDepth()) {
				Long timeDepth = syncData.get(index).getTimestampDepth();
				viewDepth.setBackground(data.getImageDepth().get(timeDepth), dimensionDepth);
				viewDepth.setStatus(timeDepth.toString());
				slDepth.setValue(getIndexByTimestamp(timeDepth, data.getImageDepth().keySet()));
			}
			if (data.hasImageColor()) {
				Long timeColor = syncData.get(index).getTimestampColor();
				viewColor.setBackground(data.getImageColor().get(timeColor), dimensionColor);
				viewColor.setStatus(timeColor.toString());
				slColor.setValue(getIndexByTimestamp(timeColor, data.getImageColor().keySet()));
			}
			if (data.hasCoordinatesDepth()) {
				Long timeCoorDepth = syncData.get(index).getTimestampCoordinateDepth();
				viewDepth.setUserCoordinate(data.getCoordinateDepth().get(timeCoorDepth), dimensionDepth);
				viewColor.setUserCoordinate(data.getCoordinateDepth().get(timeCoorDepth), dimensionDepth);
			}
			if (data.hasSegmentation()) {
				Long timeSeg = syncData.get(index).getTimestampSegmentation();
				viewDepth.setUserMap(null, data.getSegmentation().get(timeSeg), timeSeg);
			}

			viewDepth.repaint();
			viewColor.repaint();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == lRecords) {
			String name = (String) lRecords.getSelectedValue();
			if (parent != null && parent.exists()) {
				File file = new File(parent.getAbsolutePath() + File.separator + name);
				if (file.getAbsolutePath().equals(current.getAbsolutePath())) {
					return;
				}
				if (file.exists()) {
					setTitle("Simple Viewer - " + file.getAbsolutePath());
					this.data = openData(file);
					if (this.data != null) {
						getContentPane().removeAll();
						revalidate();
						repaint();

						this.syncData = this.data.synchronize();
						loadDataDirectory(this.data);
						loadRecords(file);

						revalidate();
						repaint();
					}
				}

			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == mOpen) {
			Load load = new Load();
			File file = load.openDirectory(father);

			if (file != null) {
				setTitle("Simple Viewer - " + file.getAbsolutePath());
				this.data = openData(file);
				if (this.data != null) {
					getContentPane().removeAll();
					revalidate();
					repaint();

					this.syncData = this.data.synchronize();
					loadDataDirectory(this.data);
					loadRecords(file);

					revalidate();
					repaint();
				}
			}
		} else if (ae.getSource() == mOpenFile) {
			Load load = new Load();
			File file = load.openFile(father);

			if (file != null) {
				this.current = file;
				
				setTitle("Simple Viewer - " + file.getAbsolutePath());
				getContentPane().removeAll();
				revalidate();
				repaint();

				ByteBuffer buff = load.loadBuffer(file);
				File info = new File(file.getParentFile().getAbsolutePath() + File.separator + "info.json");
				int camera;

				if (file.getName().toLowerCase().contains("depth")) {
					camera = ShowObject.DEPTH;
				} else if (file.getName().toLowerCase().contains("color")) {
					camera = ShowObject.COLOR;
				} else if (file.getParentFile().getName().toLowerCase().contains("depth")) {
					camera = ShowObject.DEPTH;
				} else {
					camera = ShowObject.COLOR;
				}

				if (info.exists()) {
					CaptureMetadata metadata = load.loadMetadata(info);
					loadDataFile(buff, metadata, camera);
				} else {
					loadDataFile(buff, null, camera);
				}

				revalidate();
				repaint();
			}
		} else if (ae.getSource() == mSaveImage) {
			JFileChooser load = new JFileChooser(current);

			load.setFileSelectionMode(JFileChooser.FILES_ONLY);
			load.setFileFilter(new FileNameExtensionFilter("PNG Image", ".png"));
			
			if(load.showOpenDialog(father) == JFileChooser.APPROVE_OPTION){
				File file = load.getSelectedFile();
				
				if (file != null) {
					if(!file.getName().endsWith(".png")){
						String nome = new String();
						int index = file.getName().lastIndexOf("\\.");
						if(index != -1 ){
							nome = file.getName().substring(0, file.getName().lastIndexOf("\\."));
						}
						nome = file.getName() + ".png";
						file = new File(file.getParentFile().getAbsolutePath() + File.separator + nome);
					}
					view.saveFrame(file);
				}
			}
			
		} else if (ae.getSource() == mRealWorld) {
			if (data == null || !data.hasCoordinatesReal()) {
				return;
			}
			JDialog dialog = new JDialog(this, "Real World Coordinate", true);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setSize(700, 500);

			Comp c = Comp.getInstanceByMap(data.getCoordinateReal());
			c.setSize(640, 480);

			JSlider slider = new JSlider();
			slider.setMinimum(0);
			slider.setMaximum(data.getCoordinateReal().size() - 1);
			slider.setMajorTickSpacing(10);
			slider.setMinorTickSpacing(1);
			slider.setPaintTrack(true);
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);

			slider.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent ce) {
					if (ce.getSource() == slider) {
						int index = slider.getValue();
						c.setIndexAt(index);
						c.repaint();
					}
				}
			});

			JPanel pnSlider = new JPanel(new BorderLayout());
			pnSlider.setBorder(new TitledBorder("Real World"));
			pnSlider.add(BorderLayout.CENTER, slider);

			JPanel main = new JPanel(new BorderLayout());
			main.setBorder(new TitledBorder("Real World"));
			main.add(BorderLayout.CENTER, c);
			main.add(BorderLayout.SOUTH, pnSlider);

			c.adjustView();
			dialog.getContentPane().add(BorderLayout.CENTER, main);
			dialog.setVisible(true);
		} else if (ae.getSource() == mCut) {
			if (data != null) {
				Map<Long, ByteBuffer> mapDepth = data.getImageDepth();
				Set<Long> keysDepth = mapDepth.keySet();
				long timeDepth = getTimestampByIndex(slDepth.getValue(), keysDepth);
				ByteBuffer buffDepth = mapDepth.get(timeDepth);

				Map<Long, ByteBuffer> mapColor = data.getImageColor();
				Set<Long> keysColor = mapColor.keySet();
				long timeColor = getTimestampByIndex(slColor.getValue(), keysColor);
				ByteBuffer buffColor = mapColor.get(timeColor);

				CutterGUI cut = new CutterGUI(this, true);
				cut.loadImages(buffColor, timeColor, buffDepth, timeDepth);
				cut.setVisible(true);
			}
		}
	}

	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				SimpleViewer viewer = new SimpleViewer(null, new JFrame());
				viewer.addWindowListener(new WindowAdapter() {

					@Override
					public void windowClosing(WindowEvent we) {
						System.exit(0);
					}
				});
				viewer.setVisible(true);
			}
		});
	}

}
