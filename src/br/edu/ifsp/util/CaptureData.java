package br.edu.ifsp.util;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;

public class CaptureData {

	private Set<Long> timestamp;
	private Map<Long, Float[][]> coordinateReal;
	private Map<Long, Float[][]> coordinateDepth;
	private Map<Long, ByteBuffer> segmentation;
	private Map<Long, ByteBuffer> imageDepth;
	private Map<Long, ByteBuffer> imageColor;
	
	public Set<Long> getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Set<Long> timestamp) {
		this.timestamp = timestamp;
	}
	public Map<Long, Float[][]> getCoordinateReal() {
		return coordinateReal;
	}
	public void setCoordinateReal(Map<Long, Float[][]> coordinateReal) {
		this.coordinateReal = coordinateReal;
	}
	public Map<Long, Float[][]> getCoordinateDepth() {
		return coordinateDepth;
	}
	public void setCoordinateDepth(Map<Long, Float[][]> coordinateDepth) {
		this.coordinateDepth = coordinateDepth;
	}
	public Map<Long, ByteBuffer> getSegmentation() {
		return segmentation;
	}
	public void setSegmentation(Map<Long, ByteBuffer> segmentation) {
		this.segmentation = segmentation;
	}
	public Map<Long, ByteBuffer> getImageDepth() {
		return imageDepth;
	}
	public void setImageDepth(Map<Long, ByteBuffer> imageDepth) {
		this.imageDepth = imageDepth;
	}
	public Map<Long, ByteBuffer> getImageColor() {
		return imageColor;
	}
	public void setImageColor(Map<Long, ByteBuffer> imageColor) {
		this.imageColor = imageColor;
	}
		
}
