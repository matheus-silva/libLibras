package br.edu.ifsp.capture;

import com.primesense.nite.NiTE;
import com.primesense.nite.UserMap;
import com.primesense.nite.UserTracker;
import com.primesense.nite.UserTrackerFrameRef;

public class Segmentation implements UserTracker.NewFrameListener {

	private UserTracker user;
	private UserTrackerFrameRef frame;
	private boolean startRecording = false;
	
	public void captureData(){
		NiTE.initialize();
		user = UserTracker.create();
		user.addNewFrameListener(this);
	}
	
	@Override
	public void onNewFrame(UserTracker user) {
		this.user = user;
		this.frame = user.readFrame();
		
		setUserMap(frame.getUserMap());
	}
	
	public synchronized void setUserMap(UserMap user){
		
	}
	
	public void startRecording(){
		this.startRecording = true;
	}
	
	public void stopRecording(){
		this.startRecording = false;
	}

}
