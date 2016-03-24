package br.edu.ifsp.capture;

import java.awt.EventQueue;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.swing.JFrame;

import org.openni.OpenNI;

import com.primesense.nite.NiTE;
import com.primesense.nite.UserData;
import com.primesense.nite.UserMap;
import com.primesense.nite.UserTracker;
import com.primesense.nite.UserTrackerFrameRef;

public class Segmentation implements UserTracker.NewFrameListener {

	private UserTracker user;
	private UserTrackerFrameRef frame;
	private boolean startRecording = false;
	private ShowObject view;

	public Segmentation() {
		this(null);
	}

	public Segmentation(ShowObject view) {
		this.view = view;
	}

	public void captureData() {
		OpenNI.initialize();
		NiTE.initialize();
		user = UserTracker.create();
		user.addNewFrameListener(this);
	}

	@Override
	public void onNewFrame(UserTracker user) {
		this.user = user;
		this.frame = user.readFrame();
		
		
		if (frame != null && frame.getUserMap() != null) {
			setUserMap(frame.getUserMap());
		}
		
		this.frame.release();
	}

	public synchronized void setUserMap(UserMap user) {
		ByteBuffer buff = user.getPixels().order(ByteOrder.LITTLE_ENDIAN);

		if (view != null) {
			view.setUserMap(buff);
		}
	}

	public void startRecording() {
		this.startRecording = true;
	}

	public void stopRecording() {
		this.startRecording = false;
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
