package br.edu.ifsp.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Delete {

	public void deleteFile(File file) throws IOException {
		File files[] = file.listFiles();
		for (File f : files) {
			if (f.isDirectory()) {
				this.deleteFile(f);
			} else if (f.isFile()) {
				Files.delete(f.toPath());
			}
		}
		Files.delete(file.toPath());
	}

}
