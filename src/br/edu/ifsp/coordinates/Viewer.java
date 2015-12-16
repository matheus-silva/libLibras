package br.edu.ifsp.coordinates;

import org.openni.Device;
import org.openni.OpenNI;
import org.openni.SensorType;

public class Viewer {

	private ComponentViewer depth, color, ir;
	private Device d;

	public Viewer() {
		OpenNI.initialize();
		// NiTE.initialize();
		try {
			d = Device.open(OpenNI.enumerateDevices().get(0).getUri());
		} catch (Exception e) {
			System.out.println("Error during the loading of the sensor.");
			System.out.println("Make sure that there is a sensor connected and try again.");
			System.exit(0);
		}

		if (isDepthSupported()) {
			depth = createComponent(SensorType.DEPTH);
		}
		if (isColorSupported()) {
			color = createComponent(SensorType.COLOR);
		}
		if (isIrSupported()) {
			ir = createComponent(SensorType.IR);
		}
	}

	private ComponentViewer createComponent(SensorType sensor) {
		return new ComponentViewer(d, sensor);
	}

	public boolean isDepthSupported() {
		return null != d.getSensorInfo(SensorType.DEPTH);
	}

	public boolean isColorSupported() {
		return null != d.getSensorInfo(SensorType.COLOR);
	}

	public boolean isIrSupported() {
		return null != d.getSensorInfo(SensorType.IR);
	}

	public ComponentViewer getDepth() {
		return this.depth;
	}

	public ComponentViewer getColor() {
		return this.color;
	}

	public ComponentViewer getIr() {
		return this.ir;
	}

	public void setStatus(String value) {
		if (isDepthSupported()) {
			depth.setStatus(value);
		}
		if (isColorSupported()) {
			color.setStatus(value);
		}
		if (isIrSupported()) {
			ir.setStatus(value);
		}
	}

}
