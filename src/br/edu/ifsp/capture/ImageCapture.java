package br.edu.ifsp.capture;

import java.awt.EventQueue;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import org.openni.Device;
import org.openni.OpenNI;
import org.openni.SensorType;
import org.openni.VideoFrameRef;
import org.openni.VideoStream;

public class ImageCapture implements VideoStream.NewFrameListener {

	public static final int COLOR = 0, DEPTH = 1;

	private VideoStream video;
	private VideoFrameRef frame;
	private boolean startRecording = false;
	private int camera;
	private ShowObject view;
	private ByteBuffer buff;
	private Map<Long, ByteBuffer> imageCapture;

	public ImageCapture(int camera) {
		this(null, camera);
	}

	public ImageCapture(ShowObject view, int camera) {
		this.view = view;
		this.camera = camera;
		this.imageCapture = createMapStructure();
	}
	
	public static Map<Long, ByteBuffer> createMapStructure(){
		return new HashMap<>();
	}

	public void captureData() {
		SensorType sensor;
		if (camera == COLOR) {
			sensor = SensorType.COLOR;
		} else if (camera == DEPTH) {
			sensor = SensorType.DEPTH;
		} else {
			return;
		}

		OpenNI.initialize();
		Device device = Device.open();

		this.video = VideoStream.create(device, sensor);
		this.video.addNewFrameListener(this);
		this.video.start();
	}

	@Override
	public void onFrameReady(VideoStream video) {
		this.video = video;

		this.frame = video.readFrame();

		setImageData(frame);
		
		this.frame.release();
	}

	public void setImageData(VideoFrameRef frame) {
		this.frame = frame;
		this.buff = frame.getData().order(ByteOrder.LITTLE_ENDIAN);
		
		if (view != null){
			view.setCamera(camera);
			view.setBackground(this.buff, frame.getWidth(), frame.getHeight());
			view.repaint();
		}
		
		if(startRecording){
			imageCapture.put(frame.getTimestamp(), buff);
		}
	}

	public void startRecording() {
		this.startRecording = true;
	}

	public void stopRecording() {
		this.startRecording = false;
	}
	
	public void clearRecordedData(){
		imageCapture = createMapStructure();
	}
	
	public Map<Long, ByteBuffer> getRecordedData(){
		return imageCapture;
	}

	public static void main(String args[]){
		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				ShowObject view = new ShowObject();
				ImageCapture img = new ImageCapture(view, DEPTH);
				img.captureData();
				
				JFrame frame = new JFrame("Image Capture");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setSize(640, 480);
				frame.add(view);
				frame.setVisible(true);
			}
		});
	}
}
