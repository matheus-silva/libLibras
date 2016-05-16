package br.edu.ifsp.editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import br.edu.ifsp.capturer.ShowObject;
import br.edu.ifsp.util.CaptureData;
import br.edu.ifsp.util.Load;

public class SimpleViewer extends JDialog implements ChangeListener, ActionListener {

	private ShowObject viewColor, viewDepth;
	private JSlider slColor, slDepth, slSync;
	private Load load;
	private JFrame father;
	private CaptureData data;
	private List<CaptureData.SyncData> syncData;
	private JMenuItem mOpen, mRealWorld;

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
				loadData(this.data);
			}
		}
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(1300, 600);
		setVisible(true);
	}

	private JMenuBar createMenuBar() {
		JMenuBar menu = new JMenuBar();

		mOpen = new JMenuItem("Open");
		mRealWorld = new JMenuItem("Real World Coordinate");

		mOpen.setAccelerator(KeyStroke.getKeyStroke("control O"));
		mRealWorld.setAccelerator(KeyStroke.getKeyStroke("control R"));

		mOpen.addActionListener(this);
		mRealWorld.addActionListener(this);

		JMenu file = new JMenu("File");
		file.add(mOpen);

		JMenu view = new JMenu("View");
		view.add(mRealWorld);

		menu.add(file);
		menu.add(view);

		return menu;
	}

	private CaptureData openData(File file) {
		CaptureData data = null;
		if (file != null) {
			data = load.loadFile(father, file);
		}
		return data;
	}

	private void loadData(CaptureData data) {
		Container c = getContentPane();

		Dimension d = new Dimension(640, 480);

		viewColor = new ShowObject();
		viewDepth = new ShowObject();
		slColor = new JSlider();
		slDepth = new JSlider();
		slSync = new JSlider();

		viewColor.setCamera(ShowObject.COLOR);
		viewDepth.setCamera(ShowObject.DEPTH);

		viewColor.setSize(d);
		viewDepth.setSize(d);

		if (data != null) {
			if (data.hasImageColor()) {
				slColor.setMaximum(data.getImageColor().size() - 1);
			} else {
				slColor.setMaximum(0);
			}
			if (data.hasImageDepth()) {
				slDepth.setMaximum(data.getImageDepth().size() - 1);
			} else {
				slDepth.setMaximum(0);
			}
			if (syncData != null) {
				slSync.setMaximum(syncData.size() - 1);
			} else {
				slSync.setMaximum(0);
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
		
		slSync.requestFocusInWindow();
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
			if (!data.hasImageColor()) {
				return;
			}
			Map<Long, ByteBuffer> c = data.getImageColor();
			Set<Long> keys = c.keySet();
			int index = slColor.getValue();
			Long timestamp = getTimestampByIndex(index, keys);
			ByteBuffer buff = c.get(timestamp);

			viewColor.setStatus(timestamp.toString());

			viewColor.setBackground(buff, 640, 480);
			viewColor.repaint();
		} else if (e.getSource() == slDepth) {
			if (!data.hasImageDepth()) {
				return;
			}
			Map<Long, ByteBuffer> c = data.getImageDepth();
			Set<Long> keys = c.keySet();
			int index = slDepth.getValue();
			Long timestamp = getTimestampByIndex(index, keys);
			ByteBuffer buff = c.get(timestamp);

			viewDepth.setStatus(timestamp.toString());
			viewDepth.setBackground(buff, 640, 480);
			String sTimestamp = timestamp.toString();
			
			if(data.hasCoordinatesDepth()){
				viewDepth.setUserCoordinate(
						data.getCoordinateDepth().get(Long.parseLong(sTimestamp.substring(0, sTimestamp.length() - 1))),
						640, 480);	
			}

			viewDepth.repaint();
		} else if (e.getSource() == slSync) {
			int index = slSync.getValue();

			if (data.hasImageDepth()) {
				Long timeDepth = syncData.get(index).getTimestampDepth();
				viewDepth.setBackground(data.getImageDepth().get(timeDepth), 640, 480);
				viewDepth.setStatus(timeDepth.toString());
				slDepth.setValue(getIndexByTimestamp(timeDepth, data.getImageDepth().keySet()));
			}
			if (data.hasImageColor()) {
				Long timeColor = syncData.get(index).getTimestampColor();
				viewColor.setBackground(data.getImageColor().get(timeColor), 640, 480);
				viewColor.setStatus(timeColor.toString());
				slColor.setValue(getIndexByTimestamp(timeColor, data.getImageColor().keySet()));
			}
			if (data.hasCoordinatesDepth()) {
				Long timeCoorDepth = syncData.get(index).getTimestampCoordinateDepth();
				viewDepth.setUserCoordinate(data.getCoordinateDepth().get(timeCoorDepth), 640, 480);
				viewColor.setUserCoordinate(data.getCoordinateDepth().get(timeCoorDepth), 640, 480);
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
					loadData(this.data);

					revalidate();
					repaint();
				}
			}
		} else if (ae.getSource() == mRealWorld) {
			if(!data.hasCoordinatesReal()){
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
		}
	}

	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				new SimpleViewer(null, new JFrame());
			}
		});
	}
}
