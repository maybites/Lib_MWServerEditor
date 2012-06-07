package org.meshwarpserver.objloader;

//import processing.core.*;

public class Debug {

	public boolean enabled = true;

	public Debug() {
	}

	public void println(String str) {
		if (enabled) {
			System.out.println(str);
		}
	}

	public void println(int i) {
		if (enabled)
			System.out.println(i);
	}

	public void println(int[] i) {
		if (enabled)
			System.out.println(i);
	}

	public void println(float[] f) {
		if (enabled)
			System.out.println(f);
	}

	/**
	public void println(PVector v) {
		if (enabled)
			System.out.println(v.x + " : " + v.y + " : " + v.z);
	}
	**/
	
	// public void println(Vertex v){
	// if(enabled)
	// PApplet.println(v.vx + " : " + v.vy + " : " + v.vz);
	// }
	public void print(String str) {
		if (enabled)
			System.out.print(str);
	}
}
