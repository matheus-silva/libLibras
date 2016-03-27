package br.edu.ifsp.capture;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ShowObject extends Component {

	public static final int COLOR = 0, DEPTH = 1;
	private int[][] skelCoor = { { 0, 1 }, { 1, 8 }, { 8, 9 }, { 8, 10 }, { 9, 11 }, { 11, 13 }, { 10, 12 }, { 12, 14 },
			{ 1, 3 }, { 3, 5 }, { 5, 7 }, { 1, 2 }, { 2, 4 }, { 4, 6 } };
	private int width = 1, height = 1;
	private int camera = COLOR;
	private ByteBuffer buffBackground, buffUser;
	private int[] pixels;
	private List<Float[][]> coordinate = new ArrayList<>();
	private int[] mColors = new int[] { 0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFF00, 0xFFFF00FF, 0xFF00FFFF };
	private String status;

	public void setStatus(String status) {
		this.status = status;
	}

	public void setCamera(int camera) {
		this.camera = camera;
	}

	public int getCamera() {
		return camera;
	}

	public void setUserMap(ByteBuffer buff) {
		this.buffUser = buff;
	}

	public void setBackground(ByteBuffer buff, int width, int height) {
		this.buffBackground = buff;
		this.width = width;
		this.height = height;
	}

	public void setUserCoordinate(Float[][] coordinate, int width, int height) {
		this.coordinate.add(coordinate);
	}

	@Override
	public synchronized void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		int[] background = getBackgroundImage();

		int[] userBackground = getUserMapImage(background);

		drawBackground(g, userBackground);

		drawUserSkeleton(g2d);

		drawStatus(g2d);

		coordinate = new ArrayList<>();
	}

	private void drawStatus(Graphics2D g) {
		if (!(status == null || status.equals(""))) {
			g.setFont(new Font("Serif", Font.BOLD, 46));
			g.setColor(Color.red);
			g.drawString(status, 10, 46);
		}
	}

	private void drawBackground(Graphics g, int[] background) {
		if (background == null) {
			return;
		}
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		img.setRGB(0, 0, width, height, background, 0, width);
		g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
	}

	public void drawUserSkeleton(Graphics2D g) {
		for (Float[][] fs : coordinate) {
			drawSkeleton(g, fs);
		}
	}

	public void drawSkeleton(Graphics2D g, Float[][] fs) {
		g.setColor(Color.red);
		g.setPaintMode();
		for (int i = 0; i < skelCoor.length; i++) {
			/*
			 * g.drawLine((int) (getWidth() * fs[skelCoor[i][0]][0] / width),
			 * (int) (getHeight() * fs[skelCoor[i][0]][1] / height), (int)
			 * (getWidth() * fs[skelCoor[i][1]][0] / width), (int) (getHeight()
			 * * fs[skelCoor[i][1]][1]) / height);
			 */
			g.drawLine(fs[skelCoor[i][0]][0].intValue(), fs[skelCoor[i][0]][1].intValue(),
					fs[skelCoor[i][1]][0].intValue(), fs[skelCoor[i][1]][1].intValue());
		}
	}

	private int[] getUserMapImage(int background[]) {
		if (buffUser == null) {
			return background;
		}

		// int pos = 0;
		// while (buffBackground.remaining() > 0) {
		// short depth = buffBackground.getShort();
		// short userId = buffUser.getShort();
		// short pixel = (short) mHistogram[depth<0?0:depth];
		// int color = 0xFFFFFFFF;
		// if (userId > 0) {
		// color = 0xFF00FF00; //mColors[userId % mColors.length];
		// }

		// int p = background[pos];
		// background[pos] = color & (0xFF000000 | (p << 16) | (p << 8) | p);
		// pos++;
		// }

		return background;
	}

	private int[] getBackgroundImage() {
		int pixels[] = new int[] { 0 };

		if (buffBackground == null) {
			return pixels;
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
			data.rewind();
			while (data.remaining() > 0) {
				int pos = data.position();
				int depth = data.get();
				short pixel = (short) mHistogram[depth & 0xFFFF];

				pixels[pos] = 0xFF000000 | (pixel << 16) | (pixel << 8) | pixel;
				// pixels[pos] = depth;
			}
		} else {
			// ByteBufefer back = buffBackground.duplicate();
			// ByteBuffer user = buffUser.duplicate();

			buffBackground.rewind();

			int pos = 0;
			// System.out.println("Background: " + buffBackground.limit() + " |
			// Segmentation: " + buffUser.limit());
			try {
				while (buffBackground.remaining() > 0) {
					short depth = buffBackground.getShort();
					short userId = buffUser.getShort();
					short pixel = (short) mHistogram[depth];
					// short pixel = (short) mHistogram[depth<0?0:depth];
					int color = 0xFFFFFFFF;
					if (userId > 0) {
						color = mColors[userId % mColors.length];
					}

					pixels[pos] = color & (0xFF000000 | (pixel << 16) | (pixel << 8) | pixel);
					pos++;
				}
			} catch (ArrayIndexOutOfBoundsException e) {

			}
			// System.out.println(buffBackground.remaining() + " " +
			// buffUser.remaining());
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
		float mHistogram[] = new float[width * height * 4];

		// reset
		for (int i = 0; i < mHistogram.length; ++i)
			mHistogram[i] = 0;

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
