package br.edu.ifsp.util;

import java.io.File;
import java.nio.ByteBuffer;

import br.edu.ifsp.util.Save;

public class SaveOnline implements Runnable {

	public static int COUNT = 0;

	private File file;
	private ByteBuffer frame;

	private Save save = new Save();

	public SaveOnline(File file, ByteBuffer frame) {
		this.file = file;
		this.frame = frame;
	}

	@Override
	public void run() {
		save.saveBuffer(file, frame);

		COUNT++;
		if (COUNT % 250 == 0) {
			//callGC();
		}
		
		frame = null;
	}

	private synchronized void callGC() {
		Runtime run = Runtime.getRuntime();
		run.runFinalization();
		run.gc();

	}
}
