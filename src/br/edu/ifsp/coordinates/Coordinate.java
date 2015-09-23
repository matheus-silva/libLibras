package br.edu.ifsp.coordinates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.primesense.nite.Point3D;

import com.primesense.nite.JointType;
import com.primesense.nite.Skeleton;
import com.primesense.nite.SkeletonJoint;
import com.primesense.nite.SkeletonState;
import com.primesense.nite.UserData;
import com.primesense.nite.UserTracker;
import com.primesense.nite.UserTrackerFrameRef;;

public class Coordinate implements Runnable, UserTracker.NewFrameListener {

	public static final int X = 0, Y = 1, Z = 2;
	private UserTracker userTracker = null;
	private Map<Short, List<float[][]>> coordinates;

	public Coordinate(UserTracker userTracker, Map<Short, List<float[][]>> coordinates) {
		this.userTracker = userTracker;
		this.coordinates = coordinates;
	}

	public Coordinate(Map<Short, List<float[][]>> coordinates) {
		userTracker = UserTracker.create();
		this.coordinates = coordinates;
	}

	@Override
	public void onNewFrame(UserTracker userTracker) {
		UserTrackerFrameRef frame = userTracker.readFrame();

		List<UserData> users = frame.getUsers();
		for (UserData user : users) {
			if(!isUserReadyToTrack(user)){
				continue;
			}

			float[][] joints = tracking(user);
			List<float[][]> userMoves = coordinates.get(user.getId());

			if (userMoves == null) {
				//userMoves = new CopyOnWriteArrayList<>();
				userMoves = new ArrayList<>();
				coordinates.put(user.getId(), userMoves);
			}

			userMoves.add(joints);
		}
		
		frame.release();
	}

	@Override
	public void run() {
		userTracker.addNewFrameListener(this);
	}
	
	private boolean isUserReadyToTrack(UserData user){
		if (user.isLost()) {
			System.out.println("User " + user.getId() + " was lost.");
			return false;
		}
		if (user.isNew()) {
			System.out.println("User " + user.getId() + " found. Starting tracking.");
			userTracker.startSkeletonTracking(user.getId());
			return false;
		}
		if(!user.getSkeleton().getState().equals(SkeletonState.TRACKED)){
			System.out.println("User " + user.getId() + " not tracked yet.");
			return false;
		}
		return true;
	}

	private float[][] tracking(UserData user) {
		Skeleton skeleton = user.getSkeleton();
		JointType[] jointTypes = JointType.values();
		float[][] joints = new float[jointTypes.length][3];

		for (int i = 0; i < jointTypes.length; i++) {
			SkeletonJoint joint = skeleton.getJoint(jointTypes[i]);
			joints[i][X] = joint.getPosition().getX();
			joints[i][Y] = joint.getPosition().getY();
			joints[i][Z] = joint.getPosition().getZ();
		}
		
		return joints;
	}
	
	public void stopDetectingFrames(){
		userTracker.removeNewFrameListener(this);
	}
	
	public Map<Short, List<float[][]>> getCoordinates(){
		return coordinates;
	}
}