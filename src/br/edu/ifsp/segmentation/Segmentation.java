package br.edu.ifsp.segmentation;

import com.primesense.nite.UserMap;
import com.primesense.nite.UserTracker;
import com.primesense.nite.UserTrackerFrameRef;

public class Segmentation implements UserTracker.NewFrameListener {

	private UserTracker user;
	private UserTrackerFrameRef frame;
	
	@Override
	public void onNewFrame(UserTracker user) {
		this.user = user;
		this.frame = user.readFrame();
		
		getUserMap(frame.getUserMap());
	}
	
	public synchronized void getUserMap(UserMap user){
		
	}

}
