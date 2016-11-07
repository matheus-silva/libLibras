package br.edu.ifsp.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import br.edu.ifsp.capturer.ShowObject;
import br.edu.ifsp.util.Save;

public class CutterGUI extends JDialog implements ActionListener {

	private ByteBuffer buffColor, buffDepth;
	private long timeColor, timeDepth;
	private JPanel pnColor, pnDepth;
	private Dimension dimColor, dimDepth;
	private Dimension selectionSize = new Dimension(75, 75);
	private Dimension selectionReference = new Dimension(640, 480);
	private Cutter ctColor, ctDepth;
	private JMenuItem mSave, mView;

	public CutterGUI(JDialog father, boolean modal) {
		super(father, "Cutter", modal);

		mSave = new JMenuItem("Save");
		mView = new JMenuItem("View Selection");

		mSave.addActionListener(this);
		mView.addActionListener(this);

		mSave.setAccelerator(KeyStroke.getKeyStroke("control S"));
		mView.setAccelerator(KeyStroke.getKeyStroke("control T"));

		JMenu file = new JMenu("Options");
		file.add(mSave);
		file.addSeparator();
		file.add(mView);

		JMenuBar menu = new JMenuBar();
		menu.add(file);
		setJMenuBar(menu);

		setSize(1300, 550);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		initializeComponentsForm();
	}

	/*
	 * public void loadImages(ByteBuffer color, long timeColor, ByteBuffer
	 * depth, long timeDepth) { this.buffColor = color; this.buffDepth = depth;
	 * this.timeColor = timeColor; this.timeDepth = timeDepth;
	 * 
	 * ctColor = new Cutter(); ctDepth = new Cutter();
	 * 
	 * ctColor.setCamera(ShowObject.COLOR); ctDepth.setCamera(ShowObject.DEPTH);
	 * 
	 * ctColor.setBackground(buffColor, 640, 480);
	 * ctDepth.setBackground(buffDepth, 640, 480);
	 * 
	 * Dimension d = new Dimension(640, 480);
	 * 
	 * ctColor.setPreferredSize(d); ctDepth.setPreferredSize(d);
	 * 
	 * JPanel pnColor = new JPanel(new BorderLayout());
	 * pnColor.add(BorderLayout.CENTER, ctColor);
	 * 
	 * JPanel pnDepth = new JPanel(new BorderLayout());
	 * pnDepth.add(BorderLayout.CENTER, ctDepth);
	 * 
	 * getContentPane().removeAll(); getContentPane().revalidate();
	 * getContentPane().add(BorderLayout.WEST, pnColor);
	 * getContentPane().add(BorderLayout.EAST, pnDepth);
	 * getContentPane().revalidate(); getContentPane().repaint();
	 * 
	 * ctColor.setSelectionSize(selectionSize);
	 * ctDepth.setSelectionSize(selectionSize);
	 * 
	 * ctColor.requestFocusInWindow(); ctDepth.requestFocusInWindow();
	 * 
	 * ctColor.setCutterMirror(ctDepth); ctDepth.setCutterMirror(ctColor);
	 * 
	 * ctColor.repaint(); ctDepth.repaint(); }
	 */

	public void setColorImage(ByteBuffer color, long timeColor, Dimension dimColor) {
		this.buffColor = color;
		this.timeColor = timeColor;
		this.dimColor = dimColor;

		ctColor.setBackground(buffColor, dimColor);
		ctColor.setPreferredSize(dimColor);
		this.repaint();
	}

	public void setDepthImage(ByteBuffer depth, long timeDepth, Dimension dimDepth) {
		this.buffDepth = depth;
		this.timeDepth = timeDepth;
		this.dimDepth = dimDepth;

		ctDepth.setBackground(buffDepth, dimDepth);
		ctDepth.setPreferredSize(dimDepth);
		this.repaint();
	}
	
	@Override
	public void setVisible(boolean visible){
		Dimension windowsSize = Toolkit.getDefaultToolkit().getScreenSize();

		if (dimColor.getWidth() + dimDepth.getWidth() > windowsSize.getWidth()) {
			singlePanel(pnColor);
		} else {
			dualPanel();
		}
		
		this.getContentPane().repaint();
		super.setVisible(visible);
	}

	private void initializeComponentsForm() {
		ctColor = new Cutter();
		ctDepth = new Cutter();	
		
		ctColor.setCamera(ShowObject.COLOR);
		ctDepth.setCamera(ShowObject.DEPTH);
		
		pnColor = new JPanel(new BorderLayout());
		pnDepth = new JPanel(new BorderLayout());

		JScrollPane scColor = new JScrollPane(ctColor);
		JScrollPane scDepth = new JScrollPane(ctDepth);
		
		pnColor.add(BorderLayout.CENTER, scColor);
		pnDepth.add(BorderLayout.CENTER, scDepth);

		ctColor.setSelectionSize(selectionSize, selectionReference);
		ctDepth.setSelectionSize(selectionSize, selectionReference);

		ctColor.requestFocusInWindow();
		ctDepth.requestFocusInWindow();

		ctColor.setCutterMirror(ctDepth);
		ctDepth.setCutterMirror(ctColor);
	}

	private void dualPanel() {
		if (pnDepth != null || pnColor != null) {
			Container c = this.getContentPane();
			c.removeAll();
			c.revalidate();

			c.add(BorderLayout.WEST, pnColor);
			c.add(BorderLayout.EAST, pnDepth);

			c.revalidate();
			c.repaint();
		}
	}

	private void singlePanel(JPanel panel) {
		if (panel != null) {
			Container c = this.getContentPane();
			c.removeAll();
			c.revalidate();
			
			c.add(BorderLayout.CENTER, panel);
			
			c.revalidate();
			c.repaint();
		}
	}

	private ByteBuffer getDepthSelection(ByteBuffer buff, Point origin, Dimension size) {
		byte values[] = new byte[size.width * size.height * 2 + 2];

		buff.rewind();

		int indexValue = 0, pos = 0;
		while (buff.remaining() > 0) {
			byte pixel1 = buff.get();
			byte pixel2 = buff.get();
			int pixelX = pos % 640;
			int pixelY = pos / 640;
			if ((pixelX >= origin.x && pixelX < (origin.x + size.width))
					&& (pixelY >= origin.y && pixelY < (origin.y + size.height))) {
				values[indexValue] = pixel1;
				values[indexValue + 1] = pixel2;
				indexValue += 2;
			}
			pos++;
		}
		return ByteBuffer.wrap(values).order(ByteOrder.LITTLE_ENDIAN);
	}

	private ByteBuffer getColorSelection(ByteBuffer buff, Point origin, Dimension size) {
		byte values[] = new byte[size.width * size.height * 3 + 3];

		buff.rewind();

		int indexValue = 0, pos = 0;
		while (buff.remaining() > 0) {
			byte pixel1 = buff.get();
			byte pixel2 = buff.get();
			byte pixel3 = buff.get();
			int pixelX = pos % 640;
			int pixelY = pos / 640;
			if ((pixelX >= origin.x && pixelX < (origin.x + size.width))
					&& (pixelY >= origin.y && pixelY < (origin.y + size.height))) {
				values[indexValue] = pixel1;
				values[indexValue + 1] = pixel2;
				values[indexValue + 2] = pixel3;
				indexValue += 3;
			}
			pos++;
		}
		return ByteBuffer.wrap(values).order(ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == mSave) {
			if (ctDepth.getPointBeginning() == null) {
				return;
			}

			ByteBuffer bColor = getColorSelection(buffColor, ctColor.getPointBeginning(), selectionSize);
			ByteBuffer bDepth = getDepthSelection(buffDepth, ctDepth.getPointBeginning(), selectionSize);

			Save save = new Save();
			File file = save.openFile(this);

			if (file != null) {
				File fColor = new File(file.getAbsolutePath() + "-" + timeColor + "-Color.bin");
				File fDepth = new File(file.getAbsolutePath() + "-" + timeDepth + "-Depth.bin");

				save.saveBuffer(fColor, bColor);
				save.saveBuffer(fDepth, bDepth);
			}

		} else if (ae.getSource() == mView) {
			if (ctDepth.getPointBeginning() == null) {
				return;
			}

			Dimension d = new Dimension(150, 150);
			CutterResult jdColor = new CutterResult(this, "Color", false, d);
			CutterResult jdDepth = new CutterResult(this, "Depth", false, d);

			jdColor.setByteBuffer(getColorSelection(buffColor, ctColor.getPointBeginning(), selectionSize),
					ShowObject.COLOR, selectionSize);
			jdDepth.setByteBuffer(getDepthSelection(buffDepth, ctDepth.getPointBeginning(), selectionSize),
					ShowObject.DEPTH, selectionSize);

			jdColor.setLocation(ctColor.getPointBeginning());
			jdDepth.setLocation(new Point(ctDepth.getPointBeginning().y + 640, ctDepth.getPointBeginning().y));

			jdColor.setVisible(true);
			jdDepth.setVisible(true);
		}
	}

	private class Cutter extends ShowObject {

		private Point point;
		private Point pointBeginning, pointCenter;
		private Dimension dimensionSelected, dimensionReference;
		private Cutter mirror;

		public Cutter() {
			addMouseMotionListener(new MouseMotionAdapter() {

				@Override
				public void mouseMoved(MouseEvent e) {
					point = new Point(e.getX(), e.getY());
					repaint();
					if (mirror != null) {
						mirror.setPoint(point);
					}
				}
			});
			addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent me) {
					pointCenter = new Point(me.getX(), me.getY());
					if (dimensionSelected != null) {
						int x = pointCenter.x - (int) (dimensionSelected.getWidth() / 2);
						int y = pointCenter.y - (int) (dimensionSelected.getHeight() / 2);
						pointBeginning = new Point(x, y);
					}
					repaint();
					if (mirror != null) {
						mirror.setCenterPoint(pointCenter);
					}
				}
			});
		}

		public void setCenterPoint(Point pointCenter) {
			this.pointCenter = pointCenter;
			if (dimensionSelected != null) {
				int x = pointCenter.x - (int) (dimensionSelected.getWidth() / 2);
				int y = pointCenter.y - (int) (dimensionSelected.getHeight() / 2);
				pointBeginning = new Point(x, y);
			}
			repaint();
		}

		public void setSelectionSize(Dimension d, Dimension r) {
			this.dimensionSelected = d;
			this.dimensionReference = r;
		}

		public void setCutterMirror(Cutter cut) {
			this.mirror = cut;
		}

		public Point getPointBeginning() {
			return this.pointBeginning;
		}

		public Dimension getSelectionDimension() {
			return this.dimensionSelected;
		}

		public void setPoint(Point point) {
			this.point = point;
			repaint();
		}

		@Override
		public void drawBackground(Graphics g, int[] background) {
			if (background == null) {
				return;
			}
			img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			img.setRGB(0, 0, width, height, background, 0, width);
			g.drawImage(img, 0, 0, width, height, null);
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			Graphics2D g2d = (Graphics2D) g;

			if (point != null) {
				g2d.setColor(Color.red);
				g2d.drawLine(0, point.y, width, point.y);
				g2d.drawLine(point.x, 0, point.x, height);
			}

			if (pointBeginning != null && dimensionSelected != null) {
				g2d.setColor(new Color(255, 0, 0, 64));
				g2d.fillRect(pointBeginning.x, pointBeginning.y, dimensionSelected.width, dimensionSelected.height);
			}

		}

	}

	private class CutterResult extends JDialog {

		private ShowObject cut;

		public CutterResult(JDialog father, String title, boolean modal, Dimension size) {
			super(father, title, modal);

			setSize(size);
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		}

		public void setByteBuffer(ByteBuffer buff, int option, Dimension size) {
			cut = new ShowObject();
			cut.setCamera(option);
			cut.setBackground(buff, size.width, size.height);

			getContentPane().add(BorderLayout.CENTER, cut);

			revalidate();
			repaint();
		}
	}
}
