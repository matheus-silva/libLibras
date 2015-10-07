package br.edu.ifsp.coordinates;

import org.openni.OpenNI;
import org.openni.SensorType;

import com.primesense.nite.NiTE;

public class Viewer {
	
	private ComponentViewer depth, color;
	
	public Viewer(){
		OpenNI.initialize();
		//NiTE.initialize();
		
		depth = new ComponentViewer(SensorType.DEPTH);
		color = new ComponentViewer(SensorType.COLOR);
	}
	
	public ComponentViewer getDepth(){
		return this.depth;
	}
	
	public ComponentViewer getColor(){
		return this.color;
	}

}
