package br.edu.ifsp.capturer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.imageio.ImageIO;

public class ShowObject extends Component {

	public static final int COLOR = 0, DEPTH = 1;
	private int[][] skelCoor = { { 0, 1 }, { 1, 8 }, { 8, 9 }, { 8, 10 }, { 9, 11 }, { 11, 13 }, { 10, 12 }, { 12, 14 },
			{ 1, 3 }, { 3, 5 }, { 5, 7 }, { 1, 2 }, { 2, 4 }, { 4, 6 } };
	protected int width = 1, height = 1, widthUser, heightUser;
	private int camera = COLOR;
	protected ByteBuffer buffBackground;
	private ByteBuffer buffUser;
	private Segmentation seg;
	protected BufferedImage img;
	private long timestamp;
	protected List<Float[][]> coordinate;
	private int[] mColors = new int[] { 0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFF00, 0xFFFF00FF, 0xFF00FFFF };
	private String status, statusCenter;
	private Font font;

	public ShowObject() {
		this.coordinate = createStructure();
	}

	protected List<Float[][]> createStructure() {
		return new CopyOnWriteArrayList<>();
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setStatusCenter(String status) {
		this.statusCenter = status;
	}

	public void setCamera(int camera) {
		this.camera = camera;
	}

	public int getCamera() {
		return camera;
	}

	public void setUserMap(Segmentation seg, ByteBuffer buff, long timestamp) {
		this.seg = seg;
		this.buffUser = buff;
		this.timestamp = timestamp;
	}

	public void setBackground(ByteBuffer buff, int width, int height) {
		this.buffBackground = buff;
		this.width = width;
		this.height = height;
	}
	
	public void setBackground(ByteBuffer buff, Dimension dimension) {
		this.setBackground(buff, dimension.width, dimension.height);
	}

	public void setUserCoordinate(Float[][] coordinate, int width, int height) {
		this.coordinate.add(coordinate);
		this.widthUser = width;
		this.heightUser = height;
	}
	
	public void setUserCoordinate(Float[][] coordinate, Dimension dimension) {
		this.setUserCoordinate(coordinate, dimension.width, dimension.height);
	}

	public void setFont(Font font) {
		this.font = font;
	}

	@Override
	public synchronized void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		int[] background = getBackgroundImage();

		drawBackground(g, background);

		drawUserSkeleton(g2d);

		drawStatus(g2d);

		drawStatusCenter(g2d);
		
		coordinate = createStructure();
	}

	protected void drawStatus(Graphics2D g) {
		if (!(status == null || status.equals(""))) {
			g.setFont(new Font("Serif", Font.BOLD, 46));
			g.setColor(Color.red);
			g.drawString(status, 10, 46);
		}
	}

	protected void drawStatusCenter(Graphics2D g) {
		if (statusCenter != null && !statusCenter.equals("") && font != null) {
			g.setFont(font);
			g.setColor(Color.red);
			int size = font.getSize() / 2;
			g.drawString(statusCenter, getWidth() / 2 - size, getHeight() / 2);
		}
	}
	
	protected void drawBackground(Graphics g, int[] background) {
		if (background == null) {
			return;
		}
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		img.setRGB(0, 0, width, height, background, 0, width);
		g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
	}

	public void saveFrame(File file) {
		int[] background = getBackgroundImage();
		if(background == null){
			return;
		}
		
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		img.setRGB(0, 0, width, height, background, 0, width);
		
		try {
			if(ImageIO.write(img, "png", file)){
				System.out.println("File " + file.getAbsolutePath() + " saved");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void drawUserSkeleton(Graphics2D g) {
		for (Float[][] fs : coordinate) {
			drawSkeleton(g, fs);
		}
	}

	public void drawSkeleton(Graphics2D g, Float[][] fs) {
		g.setColor(Color.red);
		g.setPaintMode();

		if (fs == null) {
			return;
		}

		for (int i = 0; i < skelCoor.length; i++) {
			g.drawLine((int) (getWidth() * fs[skelCoor[i][0]][0] / widthUser),
					(int) (getHeight() * fs[skelCoor[i][0]][1] / heightUser),
					(int) (getWidth() * fs[skelCoor[i][1]][0] / widthUser),
					(int) (getHeight() * fs[skelCoor[i][1]][1]) / heightUser);

		}
	}

	protected int[] getBackgroundImage() {
		int pixels[] = new int[] { 0 };

		if (buffBackground == null) {
			return null;
		}

		if (camera == COLOR) {
			pixels = getColorPixel();
		} else if (camera == DEPTH) {
			pixels = getDepthPixel();
		}

		return pixels;
	}

	private int[] getDepthPixel() {
		ShortBuffer data = buffBackground.asShortBuffer();

		float mHistogram[] = getHistogram(buffBackground);
		int pixels[] = new int[buffBackground.limit()];

		buffBackground.rewind();

		if (buffUser == null) {
			pixels = getPixelDepthOnly(data, mHistogram, pixels);
		} else {
			pixels = getPixelDepthSegmentation(data, mHistogram, pixels);
		}

		return pixels;
	}

	private int[] getPixelDepthOnly(ShortBuffer data, float mHistogram[], int pixels[]) {
		data.rewind();
		while (data.remaining() > 0) {
			int pos = data.position();
			int depth = data.get();
			short pixel = (short) mHistogram[depth & 0xFFFF];

			pixels[pos] = 0xFF000000 | (pixel << 16) | (pixel << 8) | pixel;
		}
		return pixels;
	}

	private int[] getPixelDepthSegmentation(ShortBuffer data, float mHistogram[], int pixels[]) {
		buffBackground.rewind();
		buffUser.rewind();
		byte values[] = new byte[buffBackground.limit()];

		int pos = 0;
		try {
			while (buffBackground.remaining() > 0) {
				short depth = buffBackground.getShort();
				short userId = buffUser.getShort();
				short pixel = (short) mHistogram[depth];
				int color = 0xFFFFFFFF;
				int index = 0;
				if (userId > 0) {
					index = userId % mColors.length;
					color = mColors[index];
				}

				pixels[pos] = color & (0xFF000000 | (pixel << 16) | (pixel << 8) | pixel);
				values[pos] = (byte) index;
				pos++;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		if (seg != null) {
			seg.store(values, timestamp);
		}
		return pixels;
	}

	private int[] getColorPixel() {
		int[] pixels = new int[buffBackground.limit()];
		int pos = 0;

		buffBackground.rewind();
		while (buffBackground.remaining() > 2) {
			int red = (int) buffBackground.get() & 0xFF;
			int green = (int) buffBackground.get() & 0xFF;
			int blue = (int) buffBackground.get() & 0xFF;
			pixels[pos] = 0xFF000000 | (red << 16) | (green << 8) | blue;
			pos++;
		}
		return pixels;
	}

	private float[] getHistogram(ByteBuffer data) {
		float mHistogram[] = new float[data.limit()];

		// reset
		for (int i = 0; i < mHistogram.length; ++i)
			mHistogram[i] = 0;

		data.rewind();

		int points = 0;
		while (data.remaining() > 0) {
			int pixel = data.getShort() & 0xFFFF;
			if (pixel != 0) {
				mHistogram[pixel]++;
				points++;
			}
		}

		for (int i = 1; i < mHistogram.length; i++) {
			mHistogram[i] += mHistogram[i - 1];
		}

		if (points > 0) {
			for (int i = 1; i < mHistogram.length; i++) {
				mHistogram[i] = (int) (256 * (1.0f - (mHistogram[i] / (float) points)));
			}
		}
		return mHistogram;
	}
}
