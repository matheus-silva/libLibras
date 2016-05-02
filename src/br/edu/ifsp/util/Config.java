package br.edu.ifsp.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;

public class Config {
	
	private static Config config;
	private String directory;
	private List<String> people;
	private List<String> sign;
	private List<String> record;
	
	private Config(){}
	
	public static Config getInstance(){
		if(config == null){
			File file = new File("config/config.json");
			
			if(!file.exists()){
				return null;
			}
			
			try {
				List<String> lines = Files.readAllLines(file.toPath());
				String c = new String();
				
				for(String l: lines){
					c += l;
				}
				
				Gson g = new Gson();
				config = g.fromJson(c, Config.class);				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			
		}
		return config;
	}
	
	public String getDirectory() {
		return directory;
	}
	public void setDirectory(String directory) {
		this.directory = directory;
	}
	public List<String> getPeople() {
		return people;
	}
	public void setPeople(List<String> people) {
		this.people = people;
	}
	public List<String> getSign() {
		return sign;
	}
	public void setSign(List<String> sign) {
		this.sign = sign;
	}
	public List<String> getRecord() {
		return record;
	}
	public void setRecord(List<String> record) {
		this.record = record;
	}
}
