package br.edu.ifsp.editor;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Before;
import org.junit.Test;

public class EditorValidacaoTest {
	
	private Editor.Util v;
	private float[][][] array = new float[][][]{
		{
			{1, 1},
			{1, 1}
		},
		{
			{2, 2},
			{2, 2}
		},
		{
			{3, 3},
			{3, 3}
		},
		{
			{4, 4},
			{4, 4}
		}
	};
	
	@Before
	public void initialize(){
		v = new Editor.Util();
	}

	@Test
	public void vazio() {		
		float[][][] result = v.crop(null, 0, array.length - 1);
		assertArrayEquals(null, result);
	}

	@Test
	public void cropAll() {
		float[][][] result = v.crop(array, 0, array.length - 1);
		assertArrayEquals(new float[][][]{}, result);
	}
	
	@Test
	public void cropStart() {
		float[][][] result = v.crop(array, 0, 2);
		float[][][] compare = new float[][][]{{{4, 4}, {4, 4}}};
		assertArrayEquals(compare, result);
	}
	
	@Test
	public void cropEnd() {
		float[][][] result = v.crop(array, 1, 3);
		float[][][] compare = new float[][][]{{{1, 1}, {1, 1}}};
		assertArrayEquals(compare, result);
	}
	
	@Test
	public void cropNormal() {
		float[][][] result = v.crop(array, 0, 1);
		float[][][] compare = new float[][][]{{{3, 3}, {3, 3}}, {{4, 4}, {4, 4}}};
		assertArrayEquals(compare, result);
		
		float[][][] result2 = v.crop(array, 1, 2);
		float[][][] compare2 = new float[][][]{{{1, 1}, {1, 1}}, {{4, 4}, {4, 4}}};
		assertArrayEquals(compare2, result2);
		
		float[][][] result3 = v.crop(array, 2, 3);
		float[][][] compare3 = new float[][][]{{{1, 1}, {1, 1}}, {{2, 2}, {2, 2}}};
		assertArrayEquals(compare3, result3);
	}
	
	@Test
	public void cropOneFrame(){
		float[][][] result1 = v.crop(array, 1, 1);
		float[][][] compare1 = new float[][][]{{{1, 1}, {1, 1}}, {{3, 3}, {3, 3}}, {{4, 4}, {4, 4}}};
		assertArrayEquals(compare1, result1);
		
		float[][][] result2 = v.crop(array, 2, 2);
		float[][][] compare2 = new float[][][]{{{1, 1}, {1, 1}}, {{2, 2}, {2, 2}}, {{4, 4}, {4, 4}}};
		assertArrayEquals(compare2, result2);
	}
	
	@Test
	public void cropFirstFrame(){
		float[][][] result = v.crop(array, 0, 0);
		float[][][] compare = new float[][][]{{{2, 2}, {2, 2}}, {{3, 3}, {3, 3}}, {{4, 4}, {4, 4}}};
		assertArrayEquals(compare, result);
	}
	
	@Test
	public void cropLastFrame(){
		float[][][] result = v.crop(array, 3, 3);
		float[][][] compare = new float[][][]{{{1, 1}, {1, 1}}, {{2, 2}, {2, 2}}, {{3, 3}, {3, 3}}};
		assertArrayEquals(compare, result);
	}
}
