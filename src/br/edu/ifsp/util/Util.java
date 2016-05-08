package br.edu.ifsp.util;

import java.io.File;

public class Util {

	public boolean isFileEmpty(File file){
		if(!file.exists()){
			return true;
		}
		File depth = new File(file.getAbsolutePath() + File.separator + "Depth");
		File color = new File(file.getAbsolutePath() + File.separator + "Color");
		File coord = new File(file.getAbsolutePath() + File.separator + "Coordinates");
		File seg = new File(file.getAbsolutePath() + File.separator + "Segmentation");
		
		if(depth.exists() && depth.listFiles().length > 0){
			return false;
		}
		
		if(color.exists() && color.listFiles().length > 0){
			return false;
		}
		
		if(coord.exists() && coord.listFiles().length > 0){
			return false;
		}
		
		if(seg.exists() && seg.listFiles().length > 0){
			return false;
		}
		
		return true;
	}
	
	public long getFileSize(File file){
		if(!file.exists()){
			return 0;
		}
		if(file.isFile()){
			return file.length();
		}
		long size = 0;
		for(File f : file.listFiles()){
			size += getFileSize(f);
		}
		return size;
	}
	
}
