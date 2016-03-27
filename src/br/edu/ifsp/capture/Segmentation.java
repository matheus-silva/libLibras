package br.edu.ifsp.capture;

import java.awt.EventQueue;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
	
	public static Map<Long, ByteBuffer> createMapStructure(){
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
		
		if (view != null) {
			//view.setUserMap(buff);
		}
		
		if(startRecording){
			byte b[] = new byte[buff.limit()];
			buff.get(b);
			segmentation.put(timestamp, ByteBuffer.wrap(b));
			System.out.println("Segmentation Received");
		}
	}

	public void startRecording() {
		this.startRecording = true;
	}

	public void stopRecording() {
		this.startRecording = false;
	}
	
	public void clearRecordedData(){
		segmentation = createMapStructure();
	}
	
	public Map<Long, ByteBuffer> getRecordedData(){
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
