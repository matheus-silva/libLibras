package br.edu.ifsp.coordinates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.primesense.nite.Point3D;
import com.primesense.nite.JointType;
import com.primesense.nite.Point2D;
import com.primesense.nite.Skeleton;
import com.primesense.nite.SkeletonJoint;
import com.primesense.nite.SkeletonState;
import com.primesense.nite.UserData;
import com.primesense.nite.UserTracker;
import com.primesense.nite.UserTrackerFrameRef;;

public class BodyCoordinate implements CoordinateInterface, UserTracker.NewFrameListener {

	public static final int X = 0, Y = 1, Z = 2;
	public static final int REAL_WORLD = 10, DEPTH = 11;
	public static final int HEAD = 0, NECK = 1, LEFT_SHOULDER = 2, RIGHT_SHOULDER = 3, LEFT_ELBOW = 4, RIGHT_ELBOW = 5,
			LEFT_HAND = 6, RIGHT_HAND = 7, TORSO = 8, LEFT_HIP = 9, RIGHT_HIP = 10, LEFT_KNEE = 11, RIGHT_KNEE = 12,
			LEFT_FOOT = 13, RIGHT_FOOT = 14;
	
	private UserTracker userTracker = null;
	private Map<Short, List<float[][]>> coordinates;
	private boolean realWorld = true;
	private boolean startRecordingUsers = false;
	private Viewer view = null;

	public BodyCoordinate(UserTracker userTracker, Viewer view) {
		coordinates = this.createMapStructure();
		this.userTracker = userTracker;
		this.view = view;
	}

	@Override
	public synchronized void onNewFrame(UserTracker userTracker) {
		if (!startRecordingUsers) {
			return;
		}

		UserTrackerFrameRef frame = userTracker.readFrame();

		List<UserData> users = frame.getUsers();
		for (UserData user : users) {
			if (!isUserReadyToTrack(user)) {
				System.out.println("User: " + user.getId() + " not ready to track.");
				continue;
			}

			float[][] joints = trackingUser(user);
			
			if(view != null)
			view.addMoviments(joints);

			System.out.print(0);
			for (float[] fs : joints) {
				System.out.print(Arrays.toString(fs));
			}
			System.out.println();
			
			List<float[][]> userMoves = coordinates.get(user.getId());

			if (userMoves == null) {
				// userMoves = new CopyOnWriteArrayList<>();
				userMoves = this.createListStructure();
				coordinates.put(user.getId(), userMoves);
			}

			userMoves.add(joints);
		}
		frame.release();
	}

	@Override
	public void startRecordingUsers() {
		startRecordingUsers = true;
	}

	@Override
	public void stopRecordingUsers() {
		startRecordingUsers = false;
	}

	@Override
	public Map<Short, List<float[][]>> getMovimentsList() {
		return coordinates;
	}

	@Override
	public Map<Short, float[][][]> getMovimentsArray() {
		Map<Short, float[][][]> newCoordinates = new HashMap<>();
		List<float[][]> list;
		float[][][] array;

		for (Short userID : coordinates.keySet()) {
			list = coordinates.get(userID);
			array = list.toArray(new float[list.size()][JointType.values().length][3]);
			newCoordinates.put(userID, array);
		}
		return newCoordinates;
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

	private List<float[][]> createListStructure() {
		return new ArrayList<float[][]>();
	}

	private Map<Short, List<float[][]>> createMapStructure() {
		return new HashMap<Short, List<float[][]>>();
	}

	private boolean isUserReadyToTrack(UserData user) {
		if (!user.isVisible()) {
			return false;
		}
		if (user.isLost()) {
			userTracker.stopSkeletonTracking(user.getId());
			return false;
		}
		if (user.isNew()) {
			userTracker.startSkeletonTracking(user.getId());
			return false;
		}
		if (user.getSkeleton().getState() != SkeletonState.TRACKED) {
			return false;
		}
		return true;
	}

	private float[][] trackingUser(UserData user) {
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