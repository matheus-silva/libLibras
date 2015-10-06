package br.edu.ifsp.coordinates;

import java.awt.*;
import java.awt.image.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.openni.*;


public class SimpleViewer extends Component 
                          implements VideoStream.NewFrameListener {
    
    float mHistogram[];
    int[] mImagePixels;
    VideoStream mVideoStream;
    VideoFrameRef mLastFrame;
    BufferedImage mBufferedImage;
    
    private List<float[][]> bodyMoviments = new ArrayList<>();
    private int[][] skelCoor = { { 0, 1 }, { 1, 8 }, { 8, 9 }, { 8, 10 }, { 9, 11 }, { 11, 13 }, { 10, 12 }, { 12, 14 },
			{ 1, 3 }, { 3, 5 }, { 5, 7 }, { 1, 2 }, { 2, 4 }, { 4, 6 } };

    public SimpleViewer() {
    	Device d = Device.open();
    	d.setImageRegistrationMode(ImageRegistrationMode.DEPTH_TO_COLOR);;
    	VideoStream video = VideoStream.create(d, SensorType.COLOR);
    	video.addNewFrameListener(this);
    	this.setStream(video);

    	video.start();
    }
    
    private void setStream(VideoStream videoStream) {
        if (mLastFrame != null) {
            mLastFrame.release();
            mLastFrame = null;
        }
        
        if (mVideoStream != null) {
            mVideoStream.removeNewFrameListener(this);
        }
        
        mVideoStream = videoStream;
        
        if (mVideoStream != null) {
            mVideoStream.addNewFrameListener(this);
        }
    }

    @Override
    public synchronized void paint(Graphics g) {
        drawBackground(g);
        drawUsers(g);
        
        bodyMoviments = new ArrayList<>();
    }

    public synchronized void addMoviments(float[][] moviment) {
		this.bodyMoviments.add(moviment);
	}
    
    public void drawUsers(Graphics g) {
		for (float[][] fs : bodyMoviments) {
			drawSkeleton1(g, fs);
		}
	}

	public void drawSkeleton(Graphics g, float[][] fs) {
		g.setColor(Color.red);
		for (int i = 0; i < skelCoor.length; i++) {
			g.drawLine((int) fs[skelCoor[i][0]][0], (int) fs[skelCoor[i][0]][1], (int) fs[skelCoor[i][1]][0],
					(int) fs[skelCoor[i][1]][1]);
		}
	}
	
	public void drawSkeleton1(Graphics g, float[][] fs) {
		g.setColor(Color.red);
		for (int i = 0; i < skelCoor.length; i++) {
			g.drawLine((int) (getWidth() * fs[skelCoor[i][0]][0] / mLastFrame.getWidth()), 
					(int) (getHeight() * fs[skelCoor[i][0]][1] / mLastFrame.getHeight()), 
					(int) (getWidth() * fs[skelCoor[i][1]][0] / mLastFrame.getWidth()),
					(int) (getHeight() * fs[skelCoor[i][1]][1]) / mLastFrame.getHeight());
		}
	}
    
    private void drawBackground(Graphics g){
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

        int framePosX = (getWidth() - width) / 2;
        int framePosY = (getHeight() - height) / 2;
        //g.drawImage(mBufferedImage, framePosX, framePosY, null);
        //g.drawImage(mBufferedImage, 0, 0, null);
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
        
        switch (mLastFrame.getVideoMode().getPixelFormat())
        {
            case DEPTH_1_MM:
            case DEPTH_100_UM:
            case SHIFT_9_2:
            case SHIFT_9_3:
                calcHist(frameData);
                frameData.rewind();
                int pos = 0;
                while(frameData.remaining() > 0) {
                	int depth = (int)frameData.getShort() & 0xFFFF;
                	//int depth = (int)frameData.getShort() & 0xFFFFFF;
                    short pixel = (short)mHistogram[depth];
                    mImagePixels[pos] = 0xFF000000 | (pixel << 16) | (pixel << 8 | (pixel));
                    pos++;
                }
                break;
            case RGB888:
                pos = 0;
                while (frameData.remaining() > 0) {
                    int red = (int)frameData.get() & 0xFF;
                    int green = (int)frameData.get() & 0xFF;
                    int blue = (int)frameData.get() & 0xFF;
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
