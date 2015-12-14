package br.edu.ifsp.editor;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class EditorValidacaoEqualsTest {
	
	private Editor.Util v;
	
	@Before
	public void initialize(){
		v = new Editor.Util();
	}
	
	@Test
	public void sameSize(){
		a = new float[10][15][3];
		b = new float[10][15][3];
		assertTrue(v.isEquals(a, b));
	}
	
	@Test
	public void differentSize1(){
		a = new float[10][15][3];
		b = new float[3][15][3];
		assertFalse(v.isEquals(a, b));
	}
	
	@Test
	public void differentSize2(){
		a = new float[10][15][3];
		b = new float[10][10][3];
		assertFalse(v.isEquals(a, b));
	}
	
	@Test
	public void differentSize3(){
		a = new float[10][15][3];
		b = new float[10][15][8];
		assertFalse(v.isEquals(a, b));
	}
	
	@Test
	public void sameValues(){
		a = new float[][][]{
			{{1, 1, 1}},
			{{2, 2, 2}},
			{{3, 3, 3}}
		};
		b = new float[][][]{
			{{1, 1, 1}},
			{{2, 2, 2}},
			{{3, 3, 3}}
		};
		assertTrue(v.isEquals(a, b));
	}
	
	@Test
	public void differentValues(){
		a = new float[][][]{
			{{1, 1, 1}},
			{{2, 2, 2}},
			{{3, 3, 3}}
		};
		b = new float[][][]{
			{{4, 4, 4}},
			{{2, 2, 2}},
			{{3, 3, 3}}
		};
		assertFalse(v.isEquals(a, b));
	}
	
	@Test
	public void differentValues1(){
		a = new float[][][]{
			{{1, 1, 1}},
			{{2, 2, 2}},
			{{3, 3, 3}}
		};
		b = new float[][][]{
			{{1, 1, 1, 1, 1, 1}},
			{{2, 2, 2}},
			{{3, 3, 3}}
		};
		assertFalse(v.isEquals(a, b));
	}
	
	@Test
	public void differentValues2(){
		a = new float[][][]{
			{{1, 1, 1}},
			{{2, 2, 2, 2, 2, 2}},
			{{3, 3, 3}}
		};
		b = new float[][][]{
			{{1, 1, 1}},
			{{2, 2, 2}},
			{{3, 3, 3}}
		};
		assertFalse(v.isEquals(a, b));
	}
	
	@Test
	public void differentValues3(){
		a = new float[][][]{
			{{1, 1, 1}},
			{{2, 2, 2}},
			{{3, 3, 3}}
		};
		b = new float[][][]{
			{{1, 1, 1}, {1, 1, 1}},
			{{2, 2, 2}},
			{{3, 3, 3}}
		};
		assertFalse(v.isEquals(a, b));
	}
	
	@Test
	public void differentValues4(){
		a = new float[][][]{
			{{1, 1, 1}},
			{{2, 2, 2}, {2, 2, 2}},
			{{3, 3, 3}}
		};
		b = new float[][][]{
			{{1, 1, 1}},
			{{2, 2, 2}},
			{{3, 3, 3}}
		};
		assertFalse(v.isEquals(a, b));
	}
	
	@Test
	public void differentValues5(){
		a = new float[][][]{
			{{1, 1, 1}},
			{{2, 2, 2}},
			{{3, 3, 3}}
		};
		b = new float[][][]{
			{{1, 1, 1}},
			{{2, 2, 2}},
			{{3, 3, 3}},
			{{4, 4, 4}}
		};
		assertFalse(v.isEquals(a, b));
	}
	
	@Test
	public void differentValues6(){
		a = new float[][][]{
			{{1, 1, 1}},
			{{2, 2, 2}},
			{{3, 3, 3}},
			{{4, 4, 4}}
		};
		b = new float[][][]{
			{{1, 1, 1}},
			{{2, 2, 2}},
			{{3, 3, 3}}
		};
		assertFalse(v.isEquals(a, b));
	}
	
	float[][][] a, b;
}
