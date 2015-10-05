package br.edu.ifsp.coordinates;

import java.util.List;
import java.util.Map;

import com.primesense.nite.UserTracker;

public interface CoordinateInterface {
	
	public Map<Short, List<float[][]>> getMovimentsList();
	public Map<Short, float[][][]> getMovimentsArray();
	public void startRecordingUsers();
	public void stopRecordingUsers();
	public void onNewFrame(UserTracker userTracker);
	
}