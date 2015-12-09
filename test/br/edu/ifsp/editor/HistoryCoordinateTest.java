package br.edu.ifsp.editor;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class HistoryCoordinateTest {
	
	private HistoryCoordinate h;
	
	@Before
	public void initialize(){
		h = new HistoryCoordinate(start);
	}
	
	@Test
	public void start() {
		assertArrayEquals(start, h.getCurrentState());
		
		assertTrue(h.isFirst());		
		assertTrue(h.isLast());
	}
	
	@Test
	public void add1(){
		h.addChange(change1);
		assertArrayEquals(change1, h.getCurrentState());
		
		assertFalse(h.isFirst());		
		assertTrue(h.isLast());
	}
	
	@Test
	public void add2(){
		h.addChange(change1);
		h.addChange(change2);
		assertArrayEquals(change2, h.getCurrentState());
		
		assertFalse(h.isFirst());		
		assertTrue(h.isLast());
	}

	@Test
	public void undo1(){
		h.addChange(change1);
		h.addChange(change2);
		
		assertArrayEquals(change1, h.undo());
		assertArrayEquals(change1, h.getCurrentState());
		
		assertFalse(h.isFirst());		
		assertFalse(h.isLast());
	}
	
	@Test
	public void undo2(){
		h.addChange(change1);
		h.addChange(change2);
		
		assertArrayEquals(change1, h.undo());
		assertArrayEquals(change1, h.getCurrentState());
		
		assertFalse(h.isFirst());		
		assertFalse(h.isLast());
		
		assertArrayEquals(start, h.undo());
		assertArrayEquals(start, h.getCurrentState());
		
		assertTrue(h.isFirst());		
		assertFalse(h.isLast());
		
		assertArrayEquals(start, h.undo());
		assertArrayEquals(start, h.getCurrentState());
		
		assertTrue(h.isFirst());		
		assertFalse(h.isLast());
	}
	
	@Test
	public void redo1(){
		h.addChange(change1);
		h.addChange(change2);
		
		assertArrayEquals(change1, h.undo());
		assertArrayEquals(change1, h.getCurrentState());
		
		assertFalse(h.isFirst());		
		assertFalse(h.isLast());
		
		assertArrayEquals(change2, h.redo());
		assertArrayEquals(change2, h.getCurrentState());
		
		assertFalse(h.isFirst());		
		assertTrue(h.isLast());
	}
	
	@Test
	public void redo2(){
		h.addChange(change1);
		h.addChange(change2);
		
		assertArrayEquals(change1, h.undo());
		assertArrayEquals(change1, h.getCurrentState());
		
		assertFalse(h.isFirst());		
		assertFalse(h.isLast());
		
		assertArrayEquals(change2, h.redo());
		assertArrayEquals(change2, h.getCurrentState());
		
		assertFalse(h.isFirst());		
		assertTrue(h.isLast());

		assertArrayEquals(change2, h.redo());
		assertArrayEquals(change2, h.getCurrentState());
		
		assertFalse(h.isFirst());		
		assertTrue(h.isLast());
	}
	
	@Test
	public void redoChange(){
		h.addChange(change1);
		h.addChange(change2);
		
		assertArrayEquals(change1, h.undo());
		assertArrayEquals(change1, h.getCurrentState());
		
		assertFalse(h.isFirst());		
		assertFalse(h.isLast());
		
		assertArrayEquals(start, h.undo());
		assertArrayEquals(start, h.getCurrentState());
		
		assertTrue(h.isFirst());		
		assertFalse(h.isLast());
		
		h.addChange(rechange);
		assertArrayEquals(rechange, h.getCurrentState());
		
		assertFalse(h.isFirst());		
		assertTrue(h.isLast());
		
		assertArrayEquals(rechange, h.redo());
		assertArrayEquals(rechange, h.getCurrentState());
		
		assertFalse(h.isFirst());		
		assertTrue(h.isLast());
		
		assertArrayEquals(rechange, h.redo());
		assertArrayEquals(rechange, h.getCurrentState());
		
		assertFalse(h.isFirst());		
		assertTrue(h.isLast());
		
		assertArrayEquals(start, h.undo());
		assertArrayEquals(start, h.getCurrentState());
		
		assertTrue(h.isFirst());		
		assertFalse(h.isLast());
		
		assertArrayEquals(rechange, h.redo());
		assertArrayEquals(rechange, h.getCurrentState());
		
		assertFalse(h.isFirst());		
		assertTrue(h.isLast());
	}
	
	float[][][] start = {
			{
				{0, 0, 0},
				{0, 0, 0},
				{0, 0, 0},
				{0, 0, 0},
			},{
				{0, 0, 0},
				{0, 0, 0},
				{0, 0, 0},
				{0, 0, 0},
			},{
				{0, 0, 0},
				{0, 0, 0},
				{0, 0, 0},
				{0, 0, 0},
			},{
				{0, 0, 0},
				{0, 0, 0},
				{0, 0, 0},
				{0, 0, 0},
			},{
				{0, 0, 0},
				{0, 0, 0},
				{0, 0, 0},
				{0, 0, 0},
			}
	};
	
	float[][][] change1 = {
			{
				{1, 1, 1},
				{1, 1, 1},
				{1, 1, 1},
				{1, 1, 1},
			},{
				{1, 1, 1},
				{1, 1, 1},
				{1, 1, 1},
				{1, 1, 1},
			},{
				{1, 1, 1},
				{1, 1, 1},
				{1, 1, 1},
				{1, 1, 1},
			},{
				{1, 1, 1},
				{1, 1, 1},
				{1, 1, 1},
				{1, 1, 1},
			},{
				{1, 1, 1},
				{1, 1, 1},
				{1, 1, 1},
				{1, 1, 1},
			}
	};
	
	float[][][] change2 = {
			{
				{2, 2, 2},
				{2, 2, 2},
				{2, 2, 2},
				{2, 2, 2},
			},{
				{2, 2, 2},
				{2, 2, 2},
				{2, 2, 2},
				{2, 2, 2},
			},{
				{2, 2, 2},
				{2, 2, 2},
				{2, 2, 2},
				{2, 2, 2},
			},{
				{2, 2, 2},
				{2, 2, 2},
				{2, 2, 2},
				{2, 2, 2},
			},{
				{2, 2, 2},
				{2, 2, 2},
				{2, 2, 2},
				{2, 2, 2},
			}
	};
	
	float[][][] rechange = {
			{
				{3, 3, 3},
				{3, 3, 3},
				{3, 3, 3},
				{3, 3, 3},
			},{
				{3, 3, 3},
				{3, 3, 3},
				{3, 3, 3},
				{3, 3, 3},
			},{
				{3, 3, 3},
				{3, 3, 3},
				{3, 3, 3},
				{3, 3, 3},
			},{
				{3, 3, 3},
				{3, 3, 3},
				{3, 3, 3},
				{3, 3, 3},
			},{
				{3, 3, 3},
				{3, 3, 3},
				{3, 3, 3},
				{3, 3, 3},
			}
	};
}
