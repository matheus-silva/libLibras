package br.edu.ifsp.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class SaveOnlineCoordinate implements Runnable {

	private File file;
	private Long timestamp;
	private Float[][] coords;

	public SaveOnlineCoordinate(File file, Long timestamp, Float[][] coords) {
		this.file = file;
		this.timestamp = timestamp;
		this.coords = coords;
	}

	@Override
	public void run() {
		String value = getStringValues(timestamp, coords);
		saveValue(file, value);
	}

	private String getStringValues(Long timestamp, Float[][] coords) {
		StringBuilder sb = new StringBuilder();
		sb.append(timestamp + " ");
		for (Float[] f : coords) {
			sb.append(Arrays.toString(f));
		}
		sb.append("\n");
		return sb.toString();
	}

	private void saveValue(File file, String value) {
		try {
			Files.write(file.toPath(), value.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
