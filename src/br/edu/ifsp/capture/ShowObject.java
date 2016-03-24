package br.edu.ifsp.capture;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
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
	private int camera = DEPTH;
	private ByteBuffer buffBackground, buffUser;
	private int[] pixels;
	private List<Float[][]> coordinate = new ArrayList<>();

	public void setCamera(int camera) {
		this.camera = camera;
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

		drawUserSkeleton(g2d);

		drawBackground(g, userBackground);

		coordinate = new ArrayList<>();
	}

	private void drawBackground(Graphics g, int[] background) {
		if(background == null){
			
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

		int pixels[] = new int[buffUser.limit()];

		return pixels;
	}

	private int[] getBackgroundImage() {
		if (buffBackground == null) {
			return new int[] { 0 };
		}

		int pixels[];

		if (camera == COLOR) {
			pixels = getColorPixel();
		} else {
			pixels = getDepthPixel();
		}

		return pixels;
	}

	private int[] getDepthPixel() {
		ShortBuffer data = buffBackground.asShortBuffer();

		float mHistogram[] = getHistogram(data);
		int pixels[] = new int[data.limit()];

		data.rewind();
		while (data.remaining() > 0) {
			int pos = data.position();
			int depth = data.get();
			short pixel = (short) mHistogram[depth & 0xFFFF];

			pixels[pos] = 0xFF000000 | (pixel << 16) | (pixel << 8) | pixel;
			// pixels[pos] = depth;
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

	private float[] getHistogram(ShortBuffer data) {
		float mHistogram[] = new float[width * height];

		// reset
		for (int i = 0; i < mHistogram.length; ++i)
			mHistogram[i] = 0;

		int points = 0;
		while (data.remaining() > 0) {
			int pixel = data.get() & 0xFFFF;
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
