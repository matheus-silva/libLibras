package br.edu.ifsp.capturer;

import java.awt.EventQueue;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import com.primesense.nite.NiTE;
import com.primesense.nite.UserMap;
import com.primesense.nite.UserTracker;
import com.primesense.nite.UserTrackerFrameRef;

public class Segmentation implements UserTracker.NewFrameListener {

	private UserTracker user;
	private UserTrackerFrameRef frame;
	private boolean startRecording = false;
	private ShowObject view;
	private Map<Long, ByteBuffer> segmentation;

	public Segmentation() {
		this(null);
	}

	public Segmentation(ShowObject view) {
		this.segmentation = createMapStructure();
		this.view = view;
	}

	public static Map<Long, ByteBuffer> createMapStructure() {
		return new HashMap<>();
	}

	public void captureData() {
		NiTE.initialize();
		user = UserTracker.create();
		user.addNewFrameListener(this);
	}

	@Override
	public void onNewFrame(UserTracker user) {
		this.user = user;
		this.frame = user.readFrame();

		if (frame != null && frame.getUserMap() != null) {
			setUserMap(frame.getUserMap(), frame.getTimestamp());
		}

		this.frame.release();
	}

	public synchronized void setUserMap(UserMap user, long timestamp) {
		ByteBuffer buff = user.getPixels().order(ByteOrder.LITTLE_ENDIAN);

		if (startRecording) {
			// byte b[] = new byte[buff.limit()];
			// buff.get(b);
			// buff.rewind();
			// ByteBuffer newBuffer =
			// ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);

			// ByteBuffer newBuffer = ByteBuffer.allocate(buff.capacity());
			// buff.rewind();
			// newBuffer.put(buff);
			// buff.rewind();
			// newBuffer.flip();

			//int[] mColors = new int[] { 0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFF00, 0xFFFF00FF, 0xFF00FFFF };
			//int b[] = new int[buff.limit()];
			//buff.rewind();
			//int pos = 0;
			//while (buff.remaining() > 0) {
				//short userId = buff.getShort();
				//int color = 0xFFFFFFFF;
				//if (userId > 0) {
					//color = mColors[userId % mColors.length];
				//}
				//b[pos] = color;
				//pos++;
			//}
			//ByteBuffer newBuffer = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);

			// System.out.println("Create Short Buffer");
			// ShortBuffer shortBuffer = buff.asShortBuffer();
			// System.out.println("Rewind Short Buffer");
			// shortBuffer.rewind();
			// System.out.println("Create array");
			// short s[] = new short[shortBuffer.limit()];
			// System.out.println("Create pos");
			// int pos = 0;
			// while (shortBuffer.remaining() > 2) {
			// s[pos] = shortBuffer.get();
			// System.out.println("Get short value " + pos + " | " +
			// shortBuffer.remaining() + " | " + s[pos]);
			// pos++;
			// }

			// segmentation.put(timestamp, newBuffer);
			System.out.println("Segmentation Received");
		}

		if (view != null) {
			view.setUserMap(this, buff, timestamp);

		}

	}
	
	public void store(byte values[], long timestamp){
		if (startRecording) {
			ByteBuffer buff = ByteBuffer.wrap(values).order(ByteOrder.LITTLE_ENDIAN);
			segmentation.put(timestamp, buff);
		}
	}

	public void startRecording() {
		this.startRecording = true;
	}

	public void stopRecording() {
		this.startRecording = false;
	}

	public void clearRecordedData() {
		segmentation = createMapStructure();
	}

	public Map<Long, ByteBuffer> getRecordedData() {
		return segmentation;
	}

	public static void main(String args[]) {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				ShowObject view = new ShowObject();
				Segmentation seg = new Segmentation(view);
				seg.captureData();

				ImageCapture img = new ImageCapture(view, ImageCapture.DEPTH);
				img.captureData();

				JFrame frame = new JFrame("Segmentation");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setSize(640, 480);
				frame.add(view);
				frame.setVisible(true);

			}
		});
	}

}
