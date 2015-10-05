package br.edu.ifsp.coordinates;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.openni.VideoFrameRef;

import com.primesense.nite.*;

public class Viewer extends Component implements UserTracker.NewFrameListener {

	private UserTracker userTracker;
	private VideoFrameRef frame;
	private List<float[][]> moviments = new ArrayList<>();
	private float mHistogram[];
	private int[] mDepthPixels;
	private int[] mColors = new int[] { 0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFF00, 0xFFFF00FF, 0xFF00FFFF };
	private int[][] skelCoor = { { 0, 1 }, { 1, 8 }, { 8, 9 }, { 8, 10 }, { 9, 11 }, { 11, 13 }, { 10, 12 }, { 12, 14 },
			{ 1, 3 }, { 3, 5 }, { 5, 7 }, { 1, 2 }, { 2, 4 }, { 4, 6 } };

	public Viewer(UserTracker tracker) {
		userTracker = tracker;
		// mTracker.addNewFrameListener(this);
	}

	@Override
	public void onNewFrame(UserTracker userTracker) {
		createBackground(userTracker);
	}
	
	public void createBackground(UserTracker userTracker){
		frame = userTracker.readFrame().getDepthFrame();

		if (frame != null) {
			ByteBuffer frameData = frame.getData().order(ByteOrder.LITTLE_ENDIAN);
			ByteBuffer usersFrame = userTracker.readFrame().getUserMap().getPixels().order(ByteOrder.LITTLE_ENDIAN);

			// make sure we have enough room
			if (mDepthPixels == null || mDepthPixels.length < frame.getWidth() * frame.getHeight()) {
				mDepthPixels = new int[frame.getWidth() * frame.getHeight()];
			}

			calcHist(frameData);
			frameData.rewind();

			int pos = 0;
			while (frameData.remaining() > 0) {
				short depth = frameData.getShort();
				short userId = usersFrame.getShort();
				short pixel = (short) mHistogram[depth];
				int color = 0xFFFFFFFF;
				if (userId > 0) {
					color = mColors[userId % mColors.length];
				}

				mDepthPixels[pos] = color & (0xFF000000 | (pixel << 16) | (pixel << 8) | pixel);
				pos++;
			}
		}

	}

	private void calcHist(ByteBuffer depthBuffer) {
		// make sure we have enough room
		if (mHistogram == null) {
			mHistogram = new float[10000];
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

	public void addMoviments(float[][] moviment) {
		this.moviments.add(moviment);
	}

	public void paint(Graphics g) {
		drawBackground(g);
		
		drawUsers(g);
		this.moviments = new ArrayList<>();
	}

	private void drawBackground(Graphics g) {

		BufferedImage buffImg = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
		buffImg.setRGB(0, 0, frame.getWidth(), frame.getHeight(), mDepthPixels, 0, frame.getWidth());
		g.drawImage(buffImg, 0, 0, null);
	}

	public void drawUsers(Graphics g) {
		for (float[][] fs : moviments) {
			drawSkeleton(g, fs);
		}
	}

	public void drawSkeleton(Graphics g, float[][] fs) {

		for (int i = 0; i < skelCoor.length; i++) {
			g.drawLine((int) fs[skelCoor[i][0]][0], (int) fs[skelCoor[i][0]][1], (int) fs[skelCoor[i][1]][0],
					(int) fs[skelCoor[i][1]][1]);
		}
	}
}
