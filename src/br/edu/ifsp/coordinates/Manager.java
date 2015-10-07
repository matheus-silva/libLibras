package br.edu.ifsp.coordinates;

import java.awt.Component;

import org.openni.OpenNI;

import com.primesense.nite.NiTE;
import com.primesense.nite.UserTracker;

public class Manager implements UserTracker.NewFrameListener, Runnable {

	private UserTracker userTracker;
	private ComponentViewer view;
	private BodyCoordinate coor;

	public Manager() {
		OpenNI.initialize();
		NiTE.initialize();

		//userTracker = UserTracker.create();
		//this.view = new Viewer(userTracker);
		Viewer viewManager = new Viewer();
		this.view = viewManager.getColor();
		this.coor = new BodyCoordinate(view);
	}

	@Override
	public synchronized void onNewFrame(UserTracker userTracker) {
		//view.onNewFrame(userTracker);
		coor.onNewFrame(userTracker);
		//view.repaint();
	}

	public BodyCoordinate getCoordinate() {
		return this.coor;
	}

	public Component getUserViewer() {
		return this.view;
	}

	@Override
	public void run() {
		//userTracker.addNewFrameListener(this);
	}
}
