package br.edu.ifsp.coordinates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openni.Device;
import org.openni.ImageRegistrationMode;
import org.openni.SensorType;
import org.openni.VideoFrameRef;
import org.openni.VideoStream;

import com.primesense.nite.HandTracker.NewFrameListener;
import com.primesense.nite.JointType;
import com.primesense.nite.NiTE;
import com.primesense.nite.Point2D;
import com.primesense.nite.PoseType;
import com.primesense.nite.Skeleton;
import com.primesense.nite.SkeletonJoint;
import com.primesense.nite.SkeletonState;
import com.primesense.nite.UserData;
import com.primesense.nite.UserTracker;
import com.primesense.nite.UserTrackerFrameRef;

import br.edu.ifsp.capture.Coordinate;
import br.edu.ifsp.capture.ImageCapture;
import br.edu.ifsp.capture.Segmentation;
import br.edu.ifsp.capture.ShowObject;;

/**
 * This is the class responsible for recording the movements of the users. It
 * receives the coordinates of the users supplied by the middleware NiTE, and
 * stores this coordinates for posterior use.
 * 
 * @author Matheus da Silva Ferreira
 *
 */
public class Capture implements InterfaceCoordinate, UserTracker.NewFrameListener, VideoStream.NewFrameListener {

	private UserTracker userTracker = null;
	private UserTrackerFrameRef frame;
	private VideoStream videoColor;
	private VideoFrameRef frameColor, frameDepth;
	private Map<Short, List<Float[][]>> coordinates = null;
	private boolean realWorld = true;
	private PoseType startingPose = null, stoppingPose = null;
	private boolean startRecordingUsers = false, startTimer = false;
	private ShowObject view = null;
	private Coordinate coor = null;
	private Segmentation seg = null;
	private ImageCapture imgColor = null, imgDepth = null;
	private Integer seconds, secondsRemaining = 0;
	private int delay;

	private StateChangedListener stateChanged = null;

	/**
	 * Default Constructor
	 */
	public Capture() {
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
	public Capture(ShowObject view) {
		// OpenNI.initialize();
		NiTE.initialize();

		this.userTracker = UserTracker.create();
		this.view = view;

		userTracker.addNewFrameListener(this);

		Device d = Device.open();
		d.setImageRegistrationMode(ImageRegistrationMode.DEPTH_TO_COLOR);

		VideoStream video = VideoStream.create(d, SensorType.COLOR);

		video.addNewFrameListener(this);

		coor = new Coordinate(view);
		seg = new Segmentation(view);
		imgColor = new ImageCapture(view, ShowObject.COLOR);
		imgDepth = new ImageCapture(view, ShowObject.DEPTH);
	}

	@Override
	public void onFrameReady(VideoStream videoStream) {
		this.videoColor = videoStream;

		this.frameColor = videoStream.readFrame();

		/* If the recording is not allowed. */
		if (!startRecordingUsers) {
			imgColor.stopRecording();
		} else {
			imgColor.startRecording();
		}

		imgColor.setImageData(frameColor);
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

		this.frameDepth = frame.getDepthFrame();
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

			/* If the recording is not allowed. */
			if (!startRecordingUsers) {
				coor.stopRecording();
			} else {
				coor.startRecording();
			}

			new Thread(new Runnable() {
				@Override
				public void run() {
					coor.getUserJoints(user, frameDepth.getWidth(), frameDepth.getHeight());
				}
			}).start();
			
			/* Call the client's listener that are waiting for some events. */
			if (stateChanged != null) {
				stateChanged.stateChanged(Capture.StateChangedListener.NEW_SKELETON_STORED);
			}
		}

		/* If the recording is not allowed. */
		if (!startRecordingUsers) {
			coor.stopRecording();
			seg.stopRecording();
			imgDepth.stopRecording();
		} else {
			coor.startRecording();
			seg.startRecording();
			imgDepth.startRecording();
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				seg.setUserMap(frame.getUserMap());
			}
		}).start();

		new Thread(new Runnable() {
			@Override
			public void run() {
				imgDepth.setImageData(frameDepth);
			}
		}).start();
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

		/*
		 * If the user is holding the startingPose, the seconds remaining are
		 * greater than 0 and the chronometer has not been started, the system
		 * will create a new {@link Timer} object, that will count the seconds;
		 * start the chronometer and then it will stop detecting the
		 * startingPose.
		 */
		if (user.getPoses(startingPose).isHeld() && secondsRemaining > 0 && !startTimer) {
			new Thread(new Timer()).start();
			startTimer = true;
			userTracker.stopPoseDetection(user.getId(), startingPose);
		}

	}

	/**
	 * Method that has the policy of the stoppingPose.
	 * 
	 * @param user
	 *            The user to check.
	 */
	private void detectingStoppingPose(UserData user) {
		/* If there is not a stoppingPose */
		if (stoppingPose == null) {
			return;
		}

		/* If the system is not recording the user movements */
		if (!startRecordingUsers) {
			return;
		}

		if (user.getPoses(stoppingPose).isHeld()) {
			System.out.println("Stop: " + stoppingPose + " is Held");
		}

		/* If the user is helding the startingPose */
		if (user.getPoses(stoppingPose).isHeld()) {
			userTracker.stopPoseDetection(user.getId(), stoppingPose);

			stopRecording(user);
		}
	}

	/**
	 * Method responsible for starting recording the user movements.
	 * 
	 * @param user
	 *            Current user. It is used when a pose detection has been
	 *            defined.
	 */
	private void startRecording(UserData user) {

		/*
		 * If there is a pose detection defined to start recording, the system
		 * will stop detecting this pose.
		 */
		if (startingPose != null) {
			userTracker.stopPoseDetection(user.getId(), startingPose);
		}

		/* Turn off the chronometer */
		startTimer = false;

		secondsRemaining = 0;
		startRecordingUsers = true;

		/* Call the client's listener that are waiting for some events. */
		if (stateChanged != null) {
			stateChanged.stateChanged(StateChangedListener.RECORDING_STARTED);
		}

		/*
		 * If there is a pose detection defined to stop recording, the system
		 * will start detecting this pose.
		 */
		if (stoppingPose != null) {
			userTracker.startPoseDetection(user.getId(), stoppingPose);
		}
	}

	/**
	 * Method responsible for stopping recording the user movements.
	 * 
	 * @param user
	 *            Current user. It is used when a pose detection has been
	 *            defined.
	 */
	private void stopRecording(UserData user) {
		startRecordingUsers = false;

		/* The amount of frames to wait before allowing the recording. */
		delay = 60;

		/* Call the client's listener that are waiting for some events. */
		if (stateChanged != null) {
			stateChanged.stateChanged(StateChangedListener.RECORDING_STOPPED);
		}

		/*
		 * If there is a pose detection defined to start recording, the system
		 * will stop detecting this pose and prepare the seconds of the
		 * chronometer.
		 */
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

	/**
	 * Start recording the user movements. When an user held the pose informed,
	 * the system will wait the amount of seconds informed before start the
	 * recording.
	 * 
	 * @param pose
	 *            Pose to be detected
	 * @param seconds
	 *            The amount of seconds to wait before start the recording.
	 */
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
	 * Stop recording the user movements when an user held the pose informed.
	 * 
	 * @param pose
	 *            Pose to be detected
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

	}

	/**
	 * Get the seconds remaining to start storing the movements. Useful when a
	 * chronometer has already been started. Use the class
	 * {@link Capture.StateChangedListener} to receive more informations.
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
	 * This class is an implementation of the class {@link Runnable}. It is used
	 * as a chronometer, which will decrease the seconds of the seconds
	 * remaining.
	 * 
	 * @author Matheus da Silva Ferreira
	 *
	 */
	private class Timer implements Runnable {

		@Override
		public void run() {
			/* If the seconds remaining are greater than 0 */
			while (secondsRemaining > 0) {

				/*
				 * Call the client's listener that are waiting for some events.
				 */
				if (stateChanged != null) {
					stateChanged.stateChanged(StateChangedListener.TIMER_CHANGED);
				}

				/* This thread will sleep for one second */
				try {
					Thread.sleep(1_000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				/* Decrease 1 second of the seconds remaining. */
				secondsRemaining--;
			}
		}
	}

	/**
	 * This class is a listener that allows the client to know every time that a
	 * new event happens.
	 * 
	 * @author Matheus da Silva Ferreira
	 *
	 */
	public static interface StateChangedListener {

		public static final int RECORDING_STARTED = 0;
		public static final int RECORDING_STOPPED = 1;
		public static final int TIMER_CHANGED = 2;
		public static final int NEW_SKELETON_STORED = 3;

		/**
		 * The method that will be called every time that a new event happens.
		 * 
		 * @param value
		 *            An int value which informs what kind of event happend.
		 *            <br>
		 *            It can be:
		 *            <ul>
		 *            <li>BodyCoordinate.StateChangedListener.RECORDING_STARTED
		 *            </li>
		 *            <li>BodyCoordinate.StateChangedListener.RECORDING_STOPPED
		 *            </li>
		 *            <li>BodyCoordinate.StateChangedListener.TIMER_CHANGED</li>
		 *            <li>BodyCoordinate.StateChangedListener.
		 *            NEW_SKELETON_STORED</li>
		 *            </ul>
		 */
		public void stateChanged(int value);

	}
}