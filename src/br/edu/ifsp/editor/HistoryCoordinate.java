package br.edu.ifsp.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoryCoordinate {

	private List<float[][][]> history;
	private int index;
	
	public HistoryCoordinate(float[][][] start){
		history = new ArrayList<>();
		history.add(start);
		index = 0;
	}
	
	public void addChange(float[][][] change){
		
		if(index != history.size() - 1){
			for(int i = history.size() - 1; i > index; i--){
				history.remove(i);
			}
		}
		
		history.add(change);
		index++;
	}
	
	public float[][][] getOriginal(){
		return history.get(0);
	}
	
	public boolean isFirst(){
		return index == 0;
	}
	
	public boolean isLast(){
		return index == history.size() - 1;
	}
	
	public float[][][] getCurrentState(){
		return history.get(index);
	}
	
	public float[][][] undo(){
		if(index > 0){
			index--;
		}
		return getCurrentState();
	}
	
	public float[][][] redo(){
		if(index < history.size() - 1){
			index++;
		}
		return getCurrentState();
	}
}
