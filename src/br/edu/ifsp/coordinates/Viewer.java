package br.edu.ifsp.coordinates;

import org.openni.Device;
import org.openni.OpenNI;
import org.openni.SensorType;

import com.primesense.nite.NiTE;

public class Viewer {

	private ComponentViewer depth, color, ir;
	private Device d;

	public Viewer() {
		OpenNI.initialize();
		// NiTE.initialize();
		d = Device.open(OpenNI.enumerateDevices().get(0).getUri());
		
		if(isDepthSupported()){
			depth = createComponent(SensorType.DEPTH);
		}
		if(isColorSupported()){
			color = createComponent(SensorType.COLOR);
		}
		if(isIrSupported()){
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

	public void setStatus(String value) {
		depth.setStatus(value);
		color.setStatus(value);
	}

}
