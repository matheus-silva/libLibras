package br.edu.ifsp.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class CaptureData {
	
	private CaptureMetadata metadata;
	private Set<Long> timestamp;
	private Map<Long, Float[][]> coordinateReal;
	private Map<Long, Float[][]> coordinateDepth;
	private Map<Long, ByteBuffer> segmentation;
	private Map<Long, ByteBuffer> imageDepth;
	private Map<Long, ByteBuffer> imageColor;

	public boolean hasCoordinatesReal() {
		return !(coordinateReal == null || coordinateReal.isEmpty());
	}

	public boolean hasCoordinatesDepth() {
		return !(coordinateDepth == null || coordinateDepth.isEmpty());
	}

	public boolean hasSegmentation() {
		return !(segmentation == null || segmentation.isEmpty());
	}

	public boolean hasImageDepth() {
		return !(imageDepth == null || imageDepth.isEmpty());
	}

	public boolean hasImageColor() {
		return !(imageColor == null || imageColor.isEmpty());
	}

	public Long getTimestampByIndex(int index) {
		int pos = 0;
		for (Long l : timestamp) {
			if (pos == index) {
				return l;
			}
			pos++;
		}
		return null;
	}

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
	
	public CaptureMetadata getMetadata(){
		return metadata;
	}
	
	public void setMetadata(CaptureMetadata metadata){
		this.metadata = metadata;
	}

	private void printTimeline() {
		List<String> times = new ArrayList<>();
		for (Long l : getImageColor().keySet()) {
			times.add(l + " - Color");
		}
		for (Long l : getImageDepth().keySet()) {
			times.add(l + " - Depth");
		}
		times.sort(new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				return s1.compareTo(s2);
			}
		});
		for (String s : times) {
			System.out.println(s);
		}
	}

	private Long getTimestampClosest(Long timestamp, Set<Long> timestamps) {
		long temp = Long.MAX_VALUE;
		Long t = null;
		for (Long time : timestamps) {
			long dif;
			if (time == timestamp) {
				return timestamp;
			} else if (time > timestamp) {
				dif = time - timestamp;
			} else {
				dif = timestamp - time;
			}
			if (dif < temp) {
				temp = dif;
				t = time;
			}
		}
		return t;
	}

	private CaptureData synchronizeValues(Long timestamp) {
		CaptureData data = new CaptureData();
		String msg = "Line: ";
		if (hasSegmentation()) {
			Long segTime = getTimestampClosest(timestamp, getSegmentation().keySet());
			if (segTime != null) {
				data.setSegmentation(new TreeMap<>());
				data.getSegmentation().put(segTime, getSegmentation().get(segTime));
			}
			msg += "S: " + segTime + " | ";
		}

		if (hasImageColor()) {
			Long colorTime = getTimestampClosest(timestamp, getImageColor().keySet());
			if (colorTime != null) {
				data.setImageColor(new TreeMap<>());
				data.getImageColor().put(colorTime, getImageColor().get(colorTime));
			}
			msg += "C: " + colorTime + " | ";
		}

		if (hasImageDepth()) {
			Long depthTime = getTimestampClosest(timestamp, getImageDepth().keySet());
			if (depthTime != null) {
				data.setImageDepth(new TreeMap<>());
				data.getImageDepth().put(depthTime, getImageDepth().get(depthTime));
			}
			msg += "D: " + depthTime + " | ";
		}

		if (hasCoordinatesDepth()) {
			Set<Long> original = getCoordinateDepth().keySet();
			Set<Long> timeChanged = new TreeSet<>();

			for (Long t : original) {
				String v = t.toString();
				v = v + "0";
				timeChanged.add(Long.parseLong(v));
			}

			Long depthTime = getTimestampClosest(timestamp, timeChanged);
			if (depthTime != null) {
				depthTime = Long.parseLong(depthTime.toString().substring(0, depthTime.toString().length() - 1));
				data.setCoordinateDepth(new TreeMap<>());
				data.getCoordinateDepth().put(depthTime, getCoordinateDepth().get(depthTime));
			}
			msg += "CD: " + depthTime + " | ";
		}

		if (hasCoordinatesReal()) {
			Set<Long> original = getCoordinateReal().keySet();
			Set<Long> timeChanged = new TreeSet<>();

			for (Long t : original) {
				String v = t.toString();
				v = v + "0";
				timeChanged.add(Long.parseLong(v));
			}

			Long realTime = getTimestampClosest(timestamp, timeChanged);
			if (realTime != null) {
				realTime = Long.parseLong(realTime.toString().substring(0, realTime.toString().length() - 1));
				data.setCoordinateReal(new TreeMap<>());
				data.getCoordinateReal().put(realTime, getCoordinateReal().get(realTime));
			}
			msg += "CR: " + realTime + " | ";
		}
		//System.out.println(msg);
		return data;
	}

	public List<SyncData> synchronize() {
		CaptureData data = new CaptureData();
		List<SyncData> sd = new ArrayList<>();
		Map<Long, ByteBuffer> base;

		if (hasImageColor() && hasImageDepth()) {
			if (getImageColor().size() <= getImageDepth().size()) {
				base = getImageColor();
			} else {
				base = getImageDepth();
			}

		} else if (hasImageColor()) {
			base = getImageColor();
		} else if (hasImageDepth()) {
			base = getImageDepth();
		} else {
			return null;
		}

		// printTimeline();

		data.setSegmentation(new TreeMap<>());
		data.setImageColor(new TreeMap<>());
		data.setImageDepth(new TreeMap<>());
		data.setCoordinateDepth(new TreeMap<>());
		data.setCoordinateReal(new TreeMap<>());

		for (Long timestamp : base.keySet()) {
			CaptureData d = synchronizeValues(timestamp);
			SyncData sync = new SyncData();
			if (d.hasSegmentation()) {
				for (Long t : d.getSegmentation().keySet()) {
					// data.getSegmentation().put(t,
					// d.getSegmentation().get(t));
					sync.setTimestampSegmentation(t);
				}
			}

			if (d.hasImageColor()) {
				for (Long t : d.getImageColor().keySet()) {
					// data.getImageColor().put(t, d.getImageColor().get(t));
					sync.setTimestampColor(t);
				}
			}

			if (d.hasImageDepth()) {
				for (Long t : d.getImageDepth().keySet()) {
					// data.getImageDepth().put(t, d.getImageDepth().get(t));
					sync.setTimestampDepth(t);
				}
			}

			if (d.hasCoordinatesReal()) {
				for (Long t : d.getCoordinateReal().keySet()) {
					// data.getCoordinateReal().put(t,
					// d.getCoordinateReal().get(t));
					sync.setTimestampCoordinateReal(t);
				}
			}

			if (d.hasCoordinatesDepth()) {
				for (Long t : d.getCoordinateDepth().keySet()) {
					// data.getCoordinateDepth().put(t,
					// d.getCoordinateDepth().get(t));
					sync.setTimestampCoordinateDepth(t);
				}
			}
			sd.add(sync);
		}

		return sd;
	}

	public class SyncData {

		private Long timestampSegmentation, timestampDepth, timestampColor, timestampCoordinateDepth,
				timestampCoordinateReal;

		public Long getTimestampSegmentation() {
			return timestampSegmentation;
		}

		public void setTimestampSegmentation(Long timestampSegmentation) {
			this.timestampSegmentation = timestampSegmentation;
		}

		public Long getTimestampDepth() {
			return timestampDepth;
		}

		public void setTimestampDepth(Long timestampDepth) {
			this.timestampDepth = timestampDepth;
		}

		public Long getTimestampColor() {
			return timestampColor;
		}

		public void setTimestampColor(Long timestampColor) {
			this.timestampColor = timestampColor;
		}

		public Long getTimestampCoordinateDepth() {
			return timestampCoordinateDepth;
		}

		public void setTimestampCoordinateDepth(Long timestampCoordinateDepth) {
			this.timestampCoordinateDepth = timestampCoordinateDepth;
		}

		public Long getTimestampCoordinateReal() {
			return timestampCoordinateReal;
		}

		public void setTimestampCoordinateReal(Long timestampCoordinateReal) {
			this.timestampCoordinateReal = timestampCoordinateReal;
		}

	}

	public static class CaptureMetadata {
		
		private String person;
		private String sign;
		private String folder;
		private String record;
		private String creator;
		private int depthWidth;
		private int depthHeight;
		private int depthFPS;
		private String depthPixelFormat;
		private int colorWidth;
		private int colorHeight;
		private int colorFPS;
		private String colorPixelFormat;
		
		public String getPerson() {
			return person;
		}
		public void setPerson(String person) {
			this.person = person;
		}
		public String getSign() {
			return sign;
		}
		public void setSign(String sign) {
			this.sign = sign;
		}
		public String getFolder() {
			return folder;
		}
		public void setFolder(String folder) {
			this.folder = folder;
		}
		public String getRecord() {
			return record;
		}
		public void setRecord(String record) {
			this.record = record;
		}
		public String getCreator() {
			return creator;
		}
		public void setCreator(String creator) {
			this.creator = creator;
		}
		public int getDepthWidth() {
			return depthWidth;
		}
		public void setDepthWidth(int depthWidth) {
			this.depthWidth = depthWidth;
		}
		public int getDepthHeight() {
			return depthHeight;
		}
		public void setDepthHeight(int depthHeight) {
			this.depthHeight = depthHeight;
		}
		public int getDepthFPS() {
			return depthFPS;
		}
		public void setDepthFPS(int depthFPS) {
			this.depthFPS = depthFPS;
		}
		public String getDepthPixelFormat() {
			return depthPixelFormat;
		}
		public void setDepthPixelFormat(String depthPixelFormat) {
			this.depthPixelFormat = depthPixelFormat;
		}
		public int getColorWidth() {
			return colorWidth;
		}
		public void setColorWidth(int colorWidth) {
			this.colorWidth = colorWidth;
		}
		public int getColorHeight() {
			return colorHeight;
		}
		public void setColorHeight(int colorHeight) {
			this.colorHeight = colorHeight;
		}
		public int getColorFPS() {
			return colorFPS;
		}
		public void setColorFPS(int colorFPS) {
			this.colorFPS = colorFPS;
		}
		public String getColorPixelFormat() {
			return colorPixelFormat;
		}
		public void setColorPixelFormat(String colorPixelFormat) {
			this.colorPixelFormat = colorPixelFormat;
		}
	}
}
