package br.edu.ifsp.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Delete {

	public void deleteFile(File file) throws IOException {
		File files[] = file.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					this.deleteFile(f);
				} else if (f.isFile()) {
					Files.delete(f.toPath());
				}
			}
		}
		Files.delete(file.toPath());
	}

	public void deleteUsedFile(File file) throws IOException {
		File depth = new File(file.getAbsolutePath() + File.separator + "Depth");
		File color = new File(file.getAbsolutePath() + File.separator + "Color");
		File coord = new File(file.getAbsolutePath() + File.separator + "Coordinates");
		File seg = new File(file.getAbsolutePath() + File.separator + "Segmentation");
		File info = new File(file.getAbsolutePath() + File.separator + "info.json");

		if (depth.exists()) {
			deleteFile(depth);
		}

		if (color.exists()) {
			deleteFile(color);
		}

		if (coord.exists()) {
			deleteFile(coord);
		}

		if (seg.exists()) {
			deleteFile(seg);
		}

		if (info.exists()) {
			deleteFile(info);
		}
	}
}
