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
	private UserTrackerFrameRef frame;
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
		this.frame = userTracker.readFrame();
		// System.out.println("NiTE: "+frame.getDepthFrame().getFrameIndex());

		/* For each users detected by the middleware NiTE in this frame */
		List<UserData> users = frame.getUsers();
		for (UserData user : users) {

			/* If the currently user is not ready to be tracked */
			if (!isUserTracked(user)) {
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

			if (stateChanged != null) {
				stateChanged.stateChanged(BodyCoordinate.StateChangedListener.NEW_SKELETON_STORED);
			}
		}
	}

	/**
	 * Method that has the policy of the startingPose.
	 * 
	 * @param user
	 *            The user to check.
	 */
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

		/*
		 * If the seconds remaining are lower than 1, it means that it is time
		 * to record the user movements. However, it will only start the
		 * recording of the current user if he is holding the startingPose, or
		 * the chronometer has been started.
		 */
		if (secondsRemaining <= 0 && (user.getPoses(startingPose).isHeld() || startTimer)) {
			startRecording(user);
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

			stopRecording(user);
		}
	}

	private void startRecording(UserData user) {

		/*
		 * =================== CHECK ===================
		 */
		if (startingPose != null) {
			userTracker.stopPoseDetection(user.getId(), startingPose);
		}

		/* Turn off the chronometer */
		startTimer = false;

		secondsRemaining = 0;

		/* Call the client's listener that are waiting for some events. */
		startRecordingUsers = true;
		if (stateChanged != null) {
			stateChanged.stateChanged(StateChangedListener.RECORDING_STARTED);
		}

		/*
		 * =================== CHECK ===================
		 */
		if (stoppingPose != null) {
			userTracker.startPoseDetection(user.getId(), stoppingPose);
		}
	}

	private void stopRecording(UserData user) {
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

	/**
	 * Start recording the user movements right away
	 */
	@Override
	public void startRecordingUsers() {
		for (UserData user : frame.getUsers()) {
			startRecording(user);
		}
	}

	/**
	 * Stop recording the user movements right away
	 */
	@Override
	public void stopRecordingUsers() {
		for (UserData user : frame.getUsers()) {
			stopRecording(user);
		}
	}

	public void startRecordingUsers(PoseType pose, int seconds) {
		this.seconds = seconds;
		this.secondsRemaining = seconds;
		for (UserData user : frame.getUsers()) {
			if (this.startingPose != null) {
				userTracker.stopPoseDetection(user.getId(), startingPose);
			}
			if (pose != null)
				if (!startRecordingUsers) {
					userTracker.startPoseDetection(user.getId(), pose);
				}
		}
		this.startingPose = pose;
	}

	/**
	 * Stop recording the user movements when an user held the informed pose.
	 * 
	 * @param pose
	 *            Pose Pose to be detected
	 */
	public void stopRecordingUsers(PoseType pose) {
		for (UserData user : frame.getUsers()) {
			if (this.stoppingPose != null) {
				userTracker.stopPoseDetection(user.getId(), stoppingPose);
			}
			if (pose != null)
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

	/**
	 * Get the amount of frames stored. This method identify what user has more
	 * frames stored, and return the amount of frames for this user.
	 * 
	 * @return The amount of frames stored to the user who has more frames
	 */
	public int getFramesCount() {
		int max = 0;
		for (List<Float[][]> userFrames : coordinates.values()) {
			if (max < userFrames.size()) {
				max = userFrames.size();
			}
		}
		return max;
	}

	/**
	 * Delete all the movements stored. This method clean the movements that was
	 * stored.
	 */
	public void clearMoviments() {
		coordinates = createMapStructure();
	}

	/**
	 * Set the coordinate system used to store the coordinates of the users'
	 * joints. There are to coordinate system used: Real World and Depth. Check
	 * the OpenNI Documentation for more details.
	 * 
	 * @param coordinate
	 *            The coordinate system used. Please, use the constants
	 *            {@link BodyCoordinate}.REAL_WORLD or {@link BodyCoordinate}
	 *            .DEPTH.
	 */
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

	/**
	 * Get the seconds remaining to start storing the movements. Useful when a
	 * chronometer has already been started. Use the class
	 * {@link BodyCoordinate.StateChangedListener} to receive more informations.
	 * 
	 * @return The seconds remaining to start storing the movements.
	 */
	public Integer getSeconds() {
		return secondsRemaining;
	}

	/**
	 * Check a chronometer is activated.
	 * 
	 * @return true if there is a chronometer counting the time, false
	 *         otherwise.
	 */
	public boolean isTimerActivated() {
		return startTimer;
	}

	/**
	 * Set a listener which will be called every time that something happens.
	 * 
	 * @param stateChanged
	 *            Object {@link StateChangedListener} which method will be
	 *            called every time that something new happens.
	 * @see StateChangedListener
	 */
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

	/**
	 * Check if the user informed was already tracked.
	 * 
	 * @param user
	 *            User to check.
	 * @return true if the user was tracked, false otherwise.
	 */
	private boolean isUserTracked(UserData user) {

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

	/**
	 * Get the position of the joints of the informed user. This method return
	 * the coordinates, according to the coordinate system informed, of the
	 * joints of the informed user. Moreover, this method also feed the
	 * {@link ComponentViewer} object with the newest data.
	 * 
	 * @param user
	 *            The current user The current user whose coordinates will be
	 *            returned.
	 * @return An array representing the coordinates, where: Float[] represents
	 *         the joints; Float[][] represents the positions in the axis X, Y
	 *         and Z
	 */
	private Float[][] trackingUser(UserData user) {
		/* Get the skeleton of the current user */
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
				if (stateChanged != null) {
					stateChanged.stateChanged(StateChangedListener.TIMER_CHANGED);
				}

				try {
					Thread.sleep(1_000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				secondsRemaining--;
			}
		}
	}

	public static interface StateChangedListener {

		public static final int RECORDING_STARTED = 0;
		public static final int RECORDING_STOPPED = 1;
		public static final int TIMER_CHANGED = 2;
		public static final int NEW_SKELETON_STORED = 3;

		public void stateChanged(int value);

	}
}