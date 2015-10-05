package br.edu.ifsp.coordinates;

import java.awt.Component;

import org.openni.OpenNI;

import com.primesense.nite.NiTE;
import com.primesense.nite.UserTracker;

public class NewFrameInterface implements UserTracker.NewFrameListener, Runnable {

	private UserTracker userTracker;
	private Viewer view;
	private BodyCoordinate coor;

	public NewFrameInterface() {
		OpenNI.initialize();
		NiTE.initialize();

		userTracker = UserTracker.create();
		this.view = new Viewer(userTracker);
		this.coor = new BodyCoordinate(userTracker, view);
	}

	@Override
	public synchronized void onNewFrame(UserTracker userTracker) {
		view.onNewFrame(userTracker);
		coor.onNewFrame(userTracker);
		view.repaint();
	}

	public BodyCoordinate getCoordinate() {
		return this.coor;
	}

	public Component getUserViewer() {
		return this.view;
	}

	@Override
	public void run() {
		userTracker.addNewFrameListener(this);
	}
}
