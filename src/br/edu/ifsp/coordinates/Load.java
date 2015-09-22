package br.edu.ifsp.coordinates;

import java.util.List;

import org.openni.Device;
import org.openni.DeviceInfo;
import org.openni.OpenNI;

import com.primesense.nite.NiTE;

public class Load {

	public static final String LIB_OpenNI = "OpenNI";
	public static final String LIB_NiTE = "NiTE";
	private static List<DeviceInfo> devices;

	public static void load() {
		loadOpenNI();
		loadNiTE();
	}

	public static void load(String library) {
		switch (library) {
		case LIB_OpenNI:
			loadOpenNI();
			break;
		case LIB_NiTE:
			loadNiTE();
			break;
		default:

		}
	}

	private static void loadOpenNI() {
		OpenNI.initialize();
		devices = OpenNI.enumerateDevices();
	}

	private static void loadNiTE() {
		NiTE.initialize();
	}

	public boolean hasDeviceConnected() {
		return !devices.isEmpty();
	}

	public static List<DeviceInfo> getDevicesDetected() {
		return devices;
	}

	public static Device openDevice(int index) {
		if (index < 0 || index >= devices.size()) {
			throw new IllegalArgumentException(
					"Index is out of the array limits\n" + "It should be 0<=index<" + devices.size() + ", but it was " + index);
		}
		return Device.open(devices.get(index).getUri());
	}

}
