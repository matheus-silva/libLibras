package br.edu.ifsp.coordinates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.primesense.nite.JointType;
import com.primesense.nite.NiTE;
import com.primesense.nite.Point2D;
import com.primesense.nite.PoseType;
import com.primesense.nite.Skeleton;
import com.primesense.nite.SkeletonJoint;
import com.primesense.nite.SkeletonState;
import com.primesense.nite.UserData;
import com.primesense.nite.UserTracker;
import com.primesense.nite.UserTrackerFrameRef;;

public class BodyCoordinate implements InterfaceCoordinate, UserTracker.NewFrameListener {

	public static final int X = 0, Y = 1, Z = 2;
	public static final int REAL_WORLD = 10, DEPTH = 11;
	public static final int HEAD = 0, NECK = 1, LEFT_SHOULDER = 2, RIGHT_SHOULDER = 3, LEFT_ELBOW = 4, RIGHT_ELBOW = 5,
			LEFT_HAND = 6, RIGHT_HAND = 7, TORSO = 8, LEFT_HIP = 9, RIGHT_HIP = 10, LEFT_KNEE = 11, RIGHT_KNEE = 12,
			LEFT_FOOT = 13, RIGHT_FOOT = 14;

	private UserTracker userTracker = null;
	private Map<Short, List<Float[][]>> coordinates = null;
	private boolean realWorld = true;
	private PoseType startingPose = null, stoppingPose = null;
	private boolean startRecordingUsers = false, startTimer = false;
	private ComponentViewer view = null;
	private Integer seconds, secondsRemaining = 0;
	private int delay;

	private StateChangedListener stateChanged = null;
	// public StringBuffer status;

	/**
	 * Default Constructor
	 */
	public BodyCoordinate() {
		this(null);
	}

	/**
	 * This Constructor allows you to set a {@link ComponentViewer} object. In
	 * this sense, every time that a new skeleton was tracked, the object
	 * informed will receive the new data.
	 * 
	 * @param view
	 *            The object used to show the images created by the sensor.
	 */
	public BodyCoordinate(ComponentViewer view) {
		// OpenNI.initialize();
		NiTE.initialize();

		this.coordinates = this.createMapStructure();
		this.userTracker = UserTracker.create();
		this.view = view;

		userTracker.addNewFrameListener(this);
	}

	/**
	 * Set method of the {@link ComponentViewer} object. This method allows you
	 * to set the object used to display the images created by the sensor, so
	 * this object will receive the new data automatically.
	 * 
	 * @param view
	 *            The object used to show the images created by the sensor. Set
	 *            null to remove the object.
	 */
	public void setView(ComponentViewer view) {
		this.view = view;
	}

	/**
	 * Method called every time that a new frame was created by the NiTE
	 * middleware.
	 */
	@Override
	public synchronized void onNewFrame(UserTracker userTracker) {
		this.userTracker = userTracker;

		/*
		 * Get the object that stores all the information of the currently frame
		 * that NiTE is able to say
		 */
		UserTrackerFrameRef frame = userTracker.readFrame();
		// System.out.println("NiTE: "+frame.getDepthFrame().getFrameIndex());

		/* For each users detected by the middleware NiTE in this frame */
		List<UserData> users = frame.getUsers();
		for (UserData user : users) {

			/* If the currently user is not ready to be tracked */
			if (!isUserReadyToTrack(user)) {
				continue;
			}

			/* Call the method that has the policy of the startingPose */
			detectingStartingPose(user);

			/* Call the method that has the policy of the stoppingPose */
			detectingStoppingPose(user);

			/* Get the joints of the current user */
			Float[][] joints = trackingUser(user);

			if (!startRecordingUsers) {
				return;
			}

			System.out.print("");
			for (Float[] fs : joints) {
				System.out.print(Arrays.toString(fs));
			}
			System.out.println();

			List<Float[][]> userMoves = coordinates.get(user.getId());

			if (userMoves == null) {
				userMoves = this.createListStructure();
				coordinates.put(user.getId(), userMoves);
			}

			userMoves.add(joints);
		}
	}

	private void detectingStartingPose(UserData user) {
		/* If there is not a startingPose */
		if (startingPose == null) {
			return;
		}

		/* If the system is already recording the user movements */
		if (startRecordingUsers) {
			return;
		}

		/*
		 * If the delay still not arrived at the value 0. Used to create a time
		 * between the detection of the startingPose and the stoppingPose
		 */
		if (delay > 0) {
			delay--;
			return;
		}

		/* If the user is held the startingPose */
		if (user.getPoses(startingPose).isHeld()) {
			System.out.println("Start: " + startingPose + " is Held");
		}

		if (secondsRemaining <= 0 && (user.getPoses(startingPose).isHeld() || startTimer)) {
			userTracker.stopPoseDetection(user.getId(), startingPose);

			startTimer = false;
			startRecordingUsers = true;
			if (stateChanged != null) {
				stateChanged.stateChanged(StateChangedListener.RECORDING_STARTED);
			}

			if (stoppingPose != null) {
				userTracker.startPoseDetection(user.getId(), stoppingPose);
			}
		}

		if (user.getPoses(startingPose).isHeld() && secondsRemaining > 0 && !startTimer) {
			new Thread(new Timer()).start();
			startTimer = true;
			userTracker.stopPoseDetection(user.getId(), startingPose);
		}

	}

	private void detectingStoppingPose(UserData user) {
		if (stoppingPose == null) {
			return;
		}

		if (!startRecordingUsers) {
			return;
		}

		if (user.getPoses(stoppingPose).isHeld()) {
			System.out.println("Stop: " + stoppingPose + " is Held");
		}

		if (user.getPoses(stoppingPose).isHeld()) {
			userTracker.stopPoseDetection(user.getId(), stoppingPose);
			startRecordingUsers = false;

			delay = 60;

			if (stateChanged != null) {
				stateChanged.stateChanged(StateChangedListener.RECORDING_STOPPED);
			}

			if (startingPose != null) {

				if (user.getPoses(startingPose).isHeld()) {
					System.out.println("Start: " + startingPose + " is Held");
				}

				userTracker.startPoseDetection(user.getId(), startingPose);
				secondsRemaining = seconds;
			}
		}
	}

	@Override
	public void startRecordingUsers() {
		startRecordingUsers = true;
		if (stateChanged != null) {
			stateChanged.stateChanged(StateChangedListener.RECORDING_STARTED);
		}
	}

	@Override
	public void stopRecordingUsers() {
		startRecordingUsers = false;
		if (stateChanged != null) {
			stateChanged.stateChanged(StateChangedListener.RECORDING_STOPPED);
		}
	}

	public void startRecordingUsers(PoseType pose, int seconds) {
		this.seconds = seconds;
		this.secondsRemaining = seconds;
		for (UserData user : userTracker.readFrame().getUsers()) {
			if (this.startingPose != null) {
				userTracker.stopPoseDetection(user.getId(), startingPose);
			}
			if (!startRecordingUsers) {
				userTracker.startPoseDetection(user.getId(), pose);
			}
		}
		this.startingPose = pose;
	}

	public void stopRecordingUsers(PoseType pose) {
		for (UserData user : userTracker.readFrame().getUsers()) {
			if (this.stoppingPose != null) {
				userTracker.stopPoseDetection(user.getId(), stoppingPose);
			}
			if (startRecordingUsers) {
				userTracker.startPoseDetection(user.getId(), pose);
			}
		}
		this.stoppingPose = pose;
	}

	@Override
	public Map<Short, List<Float[][]>> getMovimentsList() {
		return coordinates;
	}

	@Override
	public Map<Short, Float[][][]> getMovimentsArray() {
		Map<Short, Float[][][]> newCoordinates = new HashMap<>();
		List<Float[][]> list;
		Float[][][] array;

		for (Short userID : coordinates.keySet()) {
			list = coordinates.get(userID);
			array = list.toArray(new Float[list.size()][JointType.values().length][3]);
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

	public Integer getSeconds() {
		return secondsRemaining;
	}

	public boolean isTimerActivated() {
		return startTimer;
	}

	public void setStateChanged(StateChangedListener stateChanged) {
		this.stateChanged = stateChanged;
	}

	private List<Float[][]> createListStructure() {
		// return new CopyOnWriteArrayList<>();
		return new ArrayList<Float[][]>();
	}

	private Map<Short, List<Float[][]>> createMapStructure() {
		// return new ConcurrentHashMap<Short, List<Float[][]>>();
		return new HashMap<Short, List<Float[][]>>();
	}

	private boolean isUserReadyToTrack(UserData user) {

		/*
		 * Check if the user was in the previous frame, but no longer in this
		 * one
		 */
		if (user.isLost()) {

			/* Stop tracking this user */
			userTracker.stopSkeletonTracking(user.getId());

			/*
			 * If there is a startingPose, stop detecting this pose for this
			 * user
			 */
			if (startingPose != null) {
				userTracker.stopPoseDetection(user.getId(), startingPose);
			}

			/*
			 * If there is a stoppingPose, stop detecting this pose for this
			 * user
			 */
			if (stoppingPose != null) {
				userTracker.stopPoseDetection(user.getId(), stoppingPose);
			}
			return false;
		}

		/* If the user is not visible */
		if (!user.isVisible()) {
			return false;
		}

		/* Start tracking this user */
		userTracker.startSkeletonTracking(user.getId());

		/* Check if the user was not in the previous frame */
		if (user.isNew()) {

			/* Start tracking this user */
			userTracker.startSkeletonTracking(user.getId());

			/*
			 * If there is a startingPose and the system is not recording the
			 * user moves
			 */
			if (startingPose != null && !startRecordingUsers) {
				userTracker.startPoseDetection(user.getId(), startingPose);
			}

			/*
			 * If there is a stoppingPose and the system is recording the user
			 * moves
			 */
			if (stoppingPose != null && startRecordingUsers) {
				userTracker.startPoseDetection(user.getId(), stoppingPose);
			}

			return false;
		}

		/* If the user was not tracked yet */
		if (user.getSkeleton().getState() != SkeletonState.TRACKED) {
			return false;
		}
		return true;
	}

	private Float[][] trackingUser(UserData user) {
		Skeleton skeleton = user.getSkeleton();
		JointType[] jointTypes = JointType.values();
		Float[][] joints = new Float[jointTypes.length][3];
		Float[][] depth = new Float[jointTypes.length][3];

		for (int i = 0; i < jointTypes.length; i++) {
			SkeletonJoint joint = skeleton.getJoint(jointTypes[i]);

			// Float[] system = converterCoordinateSystem(joint.getPosition());

			Float[] system;
			Point2D<Float> pointDepth;
			if (!realWorld || view != null) {
				pointDepth = userTracker.convertJointCoordinatesToDepth(joint.getPosition());
				depth[i][X] = pointDepth.getX();
				depth[i][Y] = pointDepth.getY();
				depth[i][Z] = joint.getPosition().getZ();
			}

			if (realWorld) {
				system = new Float[3];
				system[X] = joint.getPosition().getX();
				system[Y] = joint.getPosition().getY();
				system[Z] = joint.getPosition().getZ();
			} else {
				system = depth[i];
			}

			joints[i][X] = system[X];
			joints[i][Y] = system[Y];
			joints[i][Z] = system[Z];
		}

		if (view != null) {
			view.addUserMoviments(depth);
		}

		return joints;
	}

	private class Timer implements Runnable {

		@Override
		public void run() {
			while (secondsRemaining > 0) {
				try {
					Thread.sleep(1_000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if (stateChanged != null) {
					stateChanged.stateChanged(StateChangedListener.TIMER_CHANGED);
				}
				secondsRemaining--;
			}
		}
	}

	public static interface StateChangedListener {

		public static final int RECORDING_STARTED = 0;
		public static final int RECORDING_STOPPED = 1;
		public static final int TIMER_CHANGED = 2;

		public void stateChanged(int value);

	}
}