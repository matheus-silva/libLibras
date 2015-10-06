package br.edu.ifsp.coordinates;

import org.openni.SensorType;

public class Viewer {
	
	private SimpleViewer depth, color;
	
	public Viewer(){
		depth = new SimpleViewer(SensorType.DEPTH);
		color = new SimpleViewer(SensorType.COLOR);
	}
	
	public SimpleViewer getDepth(){
		return this.depth;
	}
	
	public SimpleViewer getColor(){
		return this.color;
	}

}
