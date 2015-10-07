package br.edu.ifsp.coordinates;

import java.awt.*;
import java.awt.image.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.openni.*;

public class ComponentViewer extends Component implements VideoStream.NewFrameListener {

	float mHistogram[];
	int[] mImagePixels;
	VideoStream mVideoStream;
	VideoFrameRef mLastFrame;
	BufferedImage mBufferedImage;

	private List<float[][]> usersBodyMoviments = new ArrayList<>();
	private int[][] skelCoor = { { BodyCoordinate.HEAD, 1 }, { 1, 8 }, { 8, 9 }, { 8, 10 }, { 9, 11 }, { 11, 13 }, { 10, 12 }, { 12, 14 },
			{ 1, 3 }, { 3, 5 }, { 5, 7 }, { 1, 2 }, { 2, 4 }, { 4, 6 } };

	public ComponentViewer(SensorType type) {
		//Device d = Device.open();
		Device d = Device.open(OpenNI.enumerateDevices().get(0).getUri());
		mVideoStream = VideoStream.create(d, type);

		d.setImageRegistrationMode(ImageRegistrationMode.DEPTH_TO_COLOR);
		mVideoStream.addNewFrameListener(this);
		mVideoStream.start();
	}

	public synchronized void addUserMoviments(float[][] moviment) {
		this.usersBodyMoviments.add(moviment);
	}

	@Override
	public synchronized void paint(Graphics g) {
		drawBackground(g);
		drawUsers(g);
		g.setFont(new Font("Serif", Font.BOLD, 46));
		g.setColor(Color.red);
		g.drawString(BodyCoordinate.seconds + "", 10, 46);
		usersBodyMoviments = new ArrayList<>();
	}

	public void drawUsers(Graphics g) {
		for (float[][] fs : usersBodyMoviments) {
			drawSkeleton(g, fs);
		}
	}

	public void drawSkeleton(Graphics g, float[][] fs) {
		g.setColor(Color.red);
		for (int i = 0; i < skelCoor.length; i++) {
			g.drawLine((int) (getWidth() * fs[skelCoor[i][0]][0] / mLastFrame.getWidth()),
					(int) (getHeight() * fs[skelCoor[i][0]][1] / mLastFrame.getHeight()),
					(int) (getWidth() * fs[skelCoor[i][1]][0] / mLastFrame.getWidth()),
					(int) (getHeight() * fs[skelCoor[i][1]][1]) / mLastFrame.getHeight());
		}
	}

	private void drawBackground(Graphics g) {
		if (mLastFrame == null) {
			return;
		}

		int width = mLastFrame.getWidth();
		int height = mLastFrame.getHeight();

		// make sure we have enough room
		if (mBufferedImage == null || mBufferedImage.getWidth() != width || mBufferedImage.getHeight() != height) {
			mBufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		}

		mBufferedImage.setRGB(0, 0, width, height, mImagePixels, 0, width);

		g.drawImage(mBufferedImage, 0, 0, getWidth(), getHeight(), null);
	}

	@Override
	public synchronized void onFrameReady(VideoStream stream) {
		if (mLastFrame != null) {
			mLastFrame.release();
			mLastFrame = null;
		}

		mLastFrame = mVideoStream.readFrame();
		ByteBuffer frameData = mLastFrame.getData().order(ByteOrder.LITTLE_ENDIAN);

		// make sure we have enough room
		if (mImagePixels == null || mImagePixels.length < mLastFrame.getWidth() * mLastFrame.getHeight()) {
			
			mImagePixels = new int[mLastFrame.getWidth() * mLastFrame.getHeight()];
		}

		switch (mLastFrame.getVideoMode().getPixelFormat()) {
		case DEPTH_1_MM:
		case DEPTH_100_UM:
		case SHIFT_9_2:
		case SHIFT_9_3:
			calcHist(frameData);
			frameData.rewind();
			int pos = 0;
			while (frameData.remaining() > 0) {
				int depth = (int) frameData.getShort() & 0xFFFF;
				// int depth = (int)frameData.getShort() & 0xFFFFFF;
				short pixel = (short) mHistogram[depth];
				mImagePixels[pos] = 0xFF000000 | (pixel << 16) | (pixel << 8 | (pixel));
				pos++;
			}
			break;
		case RGB888:
			pos = 0;
			while (frameData.remaining() > 0) {
				int red = (int) frameData.get() & 0xFF;
				int green = (int) frameData.get() & 0xFF;
				int blue = (int) frameData.get() & 0xFF;
				mImagePixels[pos] = 0xFF000000 | (red << 16) | (green << 8) | blue;
				pos++;
			}
			break;
		default:
			// don't know how to draw
			mLastFrame.release();
			mLastFrame = null;
		}

		repaint();
	}

	private void calcHist(ByteBuffer depthBuffer) {
		// make sure we have enough room
		if (mHistogram == null || mHistogram.length < mVideoStream.getMaxPixelValue()) {
			mHistogram = new float[mVideoStream.getMaxPixelValue()];
		}

		// reset
		for (int i = 0; i < mHistogram.length; ++i)
			mHistogram[i] = 0;

		int points = 0;
		while (depthBuffer.remaining() > 0) {
			int depth = depthBuffer.getShort() & 0xFFFF;
			if (depth != 0) {
				mHistogram[depth]++;
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
	}
}
