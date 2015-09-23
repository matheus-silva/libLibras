package br.edu.ifsp.coordinates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.primesense.nite.Point3D;
import com.primesense.nite.JointType;
import com.primesense.nite.Point2D;
import com.primesense.nite.Skeleton;
import com.primesense.nite.SkeletonJoint;
import com.primesense.nite.SkeletonState;
import com.primesense.nite.UserData;
import com.primesense.nite.UserTracker;
import com.primesense.nite.UserTrackerFrameRef;;

public class Coordinate implements Runnable, UserTracker.NewFrameListener {

	public static final int X = 0, Y = 1, Z = 2;
	public static final int REAL_WORLD = 10, DEPTH = 11;

	private UserTracker userTracker = null;
	private Map<Short, List<float[][]>> coordinates;
	private boolean realWorld = true;

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
			if (!isUserReadyToTrack(user)) {
				continue;
			}

			float[][] joints = tracking(user);
			List<float[][]> userMoves = coordinates.get(user.getId());

			if (userMoves == null) {
				// userMoves = new CopyOnWriteArrayList<>();
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

	public void stopDetectingFrames() {
		userTracker.removeNewFrameListener(this);
	}

	public Map<Short, List<float[][]>> getCoordinates() {
		return coordinates;
	}

	public void setCoordinateSystem(int coordinate) {
		switch (coordinate) {
		case REAL_WORLD:
			realWorld = true;
			break;
		case DEPTH:
			realWorld = false;
			break;
		}
	}

	public Map<Short, float[][][]> structureConverter(Map<Short, List<float[][]>> coordinates) {
		Map<Short, float[][][]> newCoordinates = new HashMap<>();
		List<float[][]> list;
		float[][][] array;
		
		for(Short userID : coordinates.keySet()){
			list = coordinates.get(userID);
			array = list.toArray(new float[list.size()][JointType.values().length][3]);
			newCoordinates.put(userID, array);
		}
		return newCoordinates;
	}

	private boolean isUserReadyToTrack(UserData user) {
		if (user.isLost()) {
			System.out.println("User " + user.getId() + " was lost.");
			return false;
		}
		if (user.isNew()) {
			System.out.println("User " + user.getId() + " found. Starting tracking.");
			userTracker.startSkeletonTracking(user.getId());
			return false;
		}
		if (!user.getSkeleton().getState().equals(SkeletonState.TRACKED)) {
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

			float[] system = converterCoordinateSystem(joint.getPosition());

			joints[i][X] = system[X];
			joints[i][Y] = system[Y];
			joints[i][Z] = system[Z];
		}

		return joints;
	}

	private float[] converterCoordinateSystem(Point3D<Float> point) {
		float saida[] = new float[3];
		saida[Z] = point.getZ();

		if (realWorld) {
			saida[X] = point.getX();
			saida[Y] = point.getY();
		} else {
			Point2D<Float> pointDepth = userTracker.convertJointCoordinatesToDepth(point);
			saida[X] = pointDepth.getX();
			saida[Y] = pointDepth.getY();
		}

		return saida;
	}
}