package br.edu.ifsp.coordinates;

import java.util.List;
import java.util.Map;

import com.primesense.nite.UserTracker;

public interface InterfaceCoordinate {
	
	public Map<Short, List<Float[][]>> getMovimentsList();
	public Map<Short, Float[][][]> getMovimentsArray();
	public void startRecordingUsers();
	public void stopRecordingUsers();
	//public void onNewFrame(UserTracker userTracker);
	
}