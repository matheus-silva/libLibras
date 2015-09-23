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
		coor.setCoordinateSystem(Coordinate.REAL_WORLD);
		
		Thread t = new Thread(coor);
		t.start();

		try {
			Thread.sleep(7_000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		coor.stopDetectingFrames();
		
		Map<Short, float[][][]> converter = coor.structureConverter(usersCoordinates);
		Set<Short> keys = usersCoordinates.keySet();
		for (Short key : keys) {
			float[][][] moves = converter.get(key); 
			System.out.println("User " + key + " in " + moves.length + " frames detected.");
			print(moves);
			
		}
		
	}
	
	private static void print(float[][][] moves) {
		for (int h = 0; h < moves.length; h++) {
			System.out.print(h);
			float[][] joints = moves[h];
			
			for (int i = 0; i < joints.length; i++) {
				System.out.print(Arrays.toString(joints[i]));
			}
			System.out.println("");
		}
	}
}
