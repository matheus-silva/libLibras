package br.edu.ifsp.application.capturer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

import org.openni.Device;
import org.openni.ImageRegistrationMode;
import org.openni.OpenNI;
import org.openni.SensorType;
import org.openni.VideoFrameRef;
import org.openni.VideoMode;
import org.openni.VideoStream;

import com.primesense.nite.NiTE;
import com.primesense.nite.PoseType;
import com.primesense.nite.SkeletonState;
import com.primesense.nite.UserData;
import com.primesense.nite.UserTracker;
import com.primesense.nite.UserTrackerFrameRef;
import com.sun.media.jfxmedia.control.VideoDataBuffer;

import br.edu.ifsp.capturer.Coordinate;
import br.edu.ifsp.capturer.ImageCapture;
import br.edu.ifsp.capturer.Segmentation;
import br.edu.ifsp.capturer.ShowObject;
import br.edu.ifsp.util.CaptureData;
import br.edu.ifsp.util.CaptureData.CaptureMetadata;;

/**
 * This is the class responsible for recording the movements of the users. It
 * receives the coordinates of the users supplied by the middleware NiTE, and
 * stores this coordinates for posterior use.
 * 
 * @author Matheus da Silva Ferreira
 *
 */
public class Capture implements UserTracker.NewFrameListener, VideoStream.NewFrameListener {

	private Device device = null;
	private UserTracker userTracker = null;
	private UserTrackerFrameRef frame;
	private VideoStream videoColor;
	private VideoFrameRef frameColor, frameDepth;
	private PoseType startingPose = null, stoppingPose = null;
	private boolean startRecordingUsers = false, startTimer = false;
	private ShowObject view = null;
	private Coordinate coor = null;
	private Segmentation seg = null;
	private ImageCapture imgColor = null, imgDepth = null;
	private CaptureMetadata metadata;
	private File file;
	// private Set<Long> timestamp = new TreeSet<>();
	private int frames;
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
		System.out.println("Loading libraries");
		try {
			OpenNI.initialize();
			NiTE.initialize();
		} catch (Exception e) {
			System.out.println("Error during the loading of the dependents libraries.");
			e.printStackTrace();
		}
		coor = new Coordinate(view);
		seg = new Segmentation(view);
		imgColor = new ImageCapture(view, ShowObject.COLOR);
		imgDepth = new ImageCapture(view, ShowObject.DEPTH);
		this.view = view;

		System.out.println("Creating streams");
		try {
			device = Device.open(OpenNI.enumerateDevices().get(0).getUri());

			this.userTracker = UserTracker.create();

			userTracker.addNewFrameListener(this);

			if (device.isImageRegistrationModeSupported(ImageRegistrationMode.DEPTH_TO_COLOR)) {
				device.setImageRegistrationMode(ImageRegistrationMode.DEPTH_TO_COLOR);
			}

			// d.setDepthColorSyncEnabled(true);

			this.videoColor = VideoStream.create(device, SensorType.COLOR);

			for (VideoMode mode: device.getSensorInfo(videoColor.getSensorType()).getSupportedVideoModes()){
				if (mode.getResolutionX() == 1280 & mode.getResolutionY() == 960) {
					videoColor.setVideoMode(mode);
					break;
				}
			}
			
			videoColor.addNewFrameListener(this);
			videoColor.start();

		} catch (Exception e) {
			System.out.println("Error during the loading of the sensor.");
			System.out.println("Make sure that there is a sensor connected and try again.");
			e.printStackTrace();
			System.exit(0);
		}
	}

	@Override
	public void onFrameReady(VideoStream videoStream) {
		this.videoColor = videoStream;

		this.frameColor = videoStream.readFrame();

		/* If the recording is not allowed. */
		if (!startRecordingUsers) {
			imgColor.stopRecording();
		} else {
			checkFile(file);
			createInfoFile();
			imgColor.startRecording();
		}

		// new Thread(new Runnable() {

		// @Override
		// public void run() {
		imgColor.setImageData(frameColor);
		// }
		// }).start();

		if (startRecordingUsers) {
			// timestamp.add(frameColor.getTimestamp());
			frames++;

			/* Call the client's listener that are waiting for some events. */
			if (stateChanged != null) {
				stateChanged.stateChanged(Capture.StateChangedListener.NEW_DATA_ARRIVED);
			}
		}
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
				checkFile(file);
				createInfoFile();
				coor.startRecording();
			}

			// new Thread(new Runnable() {
			// @Override
			// public void run() {
			coor.setUserJoints(userTracker, user, frameDepth.getTimestamp(), frameDepth.getWidth(),
					frameDepth.getHeight());
			// }
			// }).start();

		}

		/* If the recording is not allowed. */
		if (!startRecordingUsers) {
			// seg.stopRecording();
			imgDepth.stopRecording();
		} else {
			checkFile(file);
			createInfoFile();
			// seg.startRecording();
			imgDepth.startRecording();
		}

		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		// seg.setUserMap(frame.getUserMap(), frame.getTimestamp());
		// }
		// }).start();

		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		imgDepth.setImageData(frameDepth);
		// }
		// }).start();

		if (startRecordingUsers) {
			// timestamp.add(frameDepth.getTimestamp());
			frames++;

			/* Call the client's listener that are waiting for some events. */
			if (stateChanged != null) {
				stateChanged.stateChanged(Capture.StateChangedListener.NEW_DATA_ARRIVED);
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
	public void startRecordingUsers() {
		for (UserData user : frame.getUsers()) {
			startRecording(user);
		}
		startRecordingUsers = true;
		/* Call the client's listener that are waiting for some events. */
		if (stateChanged != null) {
			stateChanged.stateChanged(StateChangedListener.RECORDING_STARTED);
		}
	}

	/**
	 * Stop recording the user movements right away
	 */
	public void stopRecordingUsers() {
		for (UserData user : frame.getUsers()) {
			stopRecording(user);
		}
		startRecordingUsers = false;
		/* Call the client's listener that are waiting for some events. */
		if (stateChanged != null) {
			stateChanged.stateChanged(StateChangedListener.RECORDING_STOPPED);
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

	public CaptureData getRecordedData() {
		CaptureData data = new CaptureData();

		data.setMetadata(metadata);

		// data.setTimestamp(timestamp);

		data.setSegmentation(seg.getRecordedData());

		data.setImageDepth(imgDepth.getRecordedData());
		data.setImageColor(imgColor.getRecordedData());

		Map<Short, Map<Long, Float[][]>> depth = coor.getRecordedDepthData();
		Short idShort = null;
		int maxValue = -1;
		for (Short id : depth.keySet()) {
			if (depth.get(id).size() > maxValue) {
				idShort = id;
			}
		}

		data.setCoordinateDepth(coor.getRecordedDepthData().get(idShort));
		data.setCoordinateReal(coor.getRecordedRealData().get(idShort));
		return data;
	}

	public List<VideoMode> getSupportedVideoModes() {
		return device.getSensorInfo(SensorType.COLOR).getSupportedVideoModes();
	}

	public void setVideoMode(VideoMode mode) {
		if (mode != null) {
			this.videoColor.stop();
			this.videoColor = VideoStream.create(this.device, SensorType.COLOR);
			this.videoColor.setVideoMode(mode);
			this.videoColor.addNewFrameListener(this);
			this.videoColor.start();
		}
	}
	
	public String getColorResolutionInfo(){
		VideoMode mode = videoColor.getVideoMode();
		return mode.getResolutionX() + "x" + mode.getResolutionY() + " @ " + mode.getFps() + " FPS";
	}
	
	public String getDepthResolutionInfo(){
		VideoMode mode = frameDepth.getVideoMode();
		return mode.getResolutionX() + "x" + mode.getResolutionY() + " @ " + mode.getFps() + " FPS";
	}

	private void checkFile(File file) {
		if (!file.exists()) {

			if (!file.mkdirs()) {
				System.err.println("The creation of the directories failed.");
			}
		}
		setFile(file, metadata);
	}

	private void createInfoFile() {
		File info = new File(file.getAbsolutePath() + File.separator + "info.json");
		if (!info.exists()) {

			StringBuilder sb = new StringBuilder();
			sb.append("{\n");
			sb.append("\t\"person\": \"" + metadata.getPerson() + "\",\n");
			sb.append("\t\"sign\": \"" + metadata.getSign() + "\",\n");
			sb.append("\t\"folder\": \"" + metadata.getFolder() + "\",\n");
			sb.append("\t\"record\": \"" + metadata.getRecord() + "\",\n");
			sb.append("\t\"creator\": \"" + metadata.getCreator() + "\",\n");
			sb.append("\t\"depthWidth\": " + metadata.getDepthWidth() + ",\n");
			sb.append("\t\"depthHeight\": " + metadata.getDepthHeight() + ",\n");
			sb.append("\t\"depthFPS\": " + metadata.getDepthFPS() + ",\n");
			sb.append("\t\"depthPixelFormat\": \"" + metadata.getDepthPixelFormat() + "\",\n");
			sb.append("\t\"colorWidth\": " + metadata.getColorWidth() + ",\n");
			sb.append("\t\"colorHeight\": " + metadata.getColorHeight() + ",\n");
			sb.append("\t\"colorFPS\": " + metadata.getColorFPS() + ",\n");
			sb.append("\t\"colorPixelFormat\": \"" + metadata.getColorPixelFormat() + "\"\n");
			sb.append("}");

			try {
				Files.write(info.toPath(), sb.toString().getBytes(), StandardOpenOption.CREATE);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public void setFile(File file, CaptureMetadata metadata) {
		if (!file.equals(this.file)) {
			this.frames = 0;
		}
		this.file = file;
		this.metadata = metadata;

		metadata.setDepthWidth(frameDepth.getVideoMode().getResolutionX());
		metadata.setDepthHeight(frameDepth.getVideoMode().getResolutionY());
		metadata.setDepthFPS(frameDepth.getVideoMode().getFps());
		metadata.setDepthPixelFormat(frameDepth.getVideoMode().getPixelFormat().name());

		metadata.setColorWidth(frameColor.getVideoMode().getResolutionX());
		metadata.setColorHeight(frameColor.getVideoMode().getResolutionY());
		metadata.setColorFPS(frameColor.getVideoMode().getFps());
		metadata.setColorPixelFormat(frameColor.getVideoMode().getPixelFormat().name());

		File depth = new File(file.getAbsolutePath() + File.separator + "Depth");
		File color = new File(file.getAbsolutePath() + File.separator + "Color");
		File coord = new File(file.getAbsolutePath() + File.separator + "Coordinates");

		if (!depth.exists()) {
			try {
				Files.createDirectories(depth.toPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (!color.exists()) {
			try {
				Files.createDirectory(color.toPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (!coord.exists()) {
			try {
				Files.createDirectory(coord.toPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		imgDepth.setDirectory(depth);
		imgColor.setDirectory(color);
		coor.setDirectory(coord);
	}

	/**
	 * Get the amount of frames stored. This method identify what user has more
	 * frames stored, and return the amount of frames for this user.
	 * 
	 * @return The amount of frames stored to the user who has more frames
	 */
	public int getFramesCount() {
		return frames;
		// return timestamp.size();
	}

	public void setFramesCount(int frames) {
		this.frames = frames;
	}

	/**
	 * Delete all the movements stored. This method clean the movements that was
	 * stored.
	 */
	public void clearMoviments() {
		// timestamp = new TreeSet<>();
		coor.clearRecordedData();
		seg.clearRecordedData();
		imgColor.clearRecordedData();
		imgDepth.clearRecordedData();
	}

	/**
	 * Get the seconds remaining to start storing the movements. Useful when a
	 * chronometer has already been started. Use the class
	 * {@link Capture.StateChangedListener} to receive more informations.
	 * 
	 * @return The seconds remaining to start storing the movements.
	 */
	public Integer getSecondsRemaining() {
		return secondsRemaining;
	}

	public void setSeconds(int seconds) {
		if (!startTimer) {
			this.seconds = seconds;
			this.secondsRemaining = seconds;
		}
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

	public boolean isRecording() {
		return startRecordingUsers;
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
		public static final int NEW_DATA_ARRIVED = 3;

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