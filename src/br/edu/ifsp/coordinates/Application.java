package br.edu.ifsp.coordinates;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openni.Device;
import org.openni.DeviceInfo;
import org.openni.OpenNI;

import com.primesense.nite.JointType;
import com.primesense.nite.NiTE;
import com.primesense.nite.SkeletonJoint;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

public class Application {

	public static void main(String args[]) {
		Load.load();
		
		// java.util.ConcurrentModificationException
		// Map<Short, List<float[][]>> usersCoordinates = new ConcurrentHashMap<>();
		Map<Short, List<float[][]>> usersCoordinates = new HashMap<>();
		Coordinate coor = new Coordinate(usersCoordinates);
		Thread t = new Thread(coor);
		t.start();

		try {
			Thread.sleep(7_000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		coor.stopDetectingFrames();

		Set<Short> keys = usersCoordinates.keySet();
		for (Short key : keys) {
			imprimir(usersCoordinates, key);
		}
		
	}
	
	public static void imprimir(Map<Short, List<float[][]>> usersCoordinates, Short key){
		List<float[][]> moves = usersCoordinates.get(key);
		
		System.out.println("User " + key + " in " + moves.size() + " frames detected.");
		
		float[][][] teste = moves.toArray(new float[moves.size()][JointType.values().length][3]);

		for (int h = 0; h < teste.length; h++) {
			System.out.print(h);
			float[][] fs = teste[h];
			
			for (int i = 0; i < fs.length; i++) {
				float[] value = fs[i];
				System.out.print(Arrays.toString(value));
			}
			System.out.println("");
		}
	}
}
