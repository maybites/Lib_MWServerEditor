package org.meshwarpserver.util;

import com.cycling74.max.*;

public class GLCommands {

	public static Atom[] sketch_glcolor(float[] color){
		if(color.length == 4){
			return new Atom[]{Atom.newAtom("glcolor"), Atom.newAtom(color[0]), Atom.newAtom(color[1]), Atom.newAtom(color[2]), Atom.newAtom(color[3])};
		}
		return new Atom[]{Atom.newAtom("glcolor"), Atom.newAtom(1.f), Atom.newAtom(1.f), Atom.newAtom(1.f), Atom.newAtom(1.f)};
	}

	public static Atom[] sketch_reset(){
		return new Atom[]{Atom.newAtom("reset")};
	}

	public static Atom[] sketch_moveto(float x, float y, float z){
		return new Atom[]{Atom.newAtom("moveto"), Atom.newAtom(x), Atom.newAtom(y), Atom.newAtom(z)};
	}

	public static Atom[] sketch_lineto(float x, float y, float z){
		return new Atom[]{Atom.newAtom("lineto"), Atom.newAtom(x), Atom.newAtom(y), Atom.newAtom(z)};
	}

	public static Atom[] sketch_point(float x, float y, float z){
		return new Atom[]{Atom.newAtom("point"), Atom.newAtom(x), Atom.newAtom(y), Atom.newAtom(z)};
	}

	public static Atom[] sketch_pointSize(float size){
		return new Atom[]{Atom.newAtom("point_size"), Atom.newAtom(size)};
	}

	public static Atom[] sketch_beginPolygonShape(){
		return new Atom[]{Atom.newAtom("glbegin"), Atom.newAtom("polygon")};
	}
	
	public static Atom[] sketch_beginTriangleShape(){
		return new Atom[]{Atom.newAtom("glbegin"), Atom.newAtom("triangle")};
	}

	public static Atom[] sketch_vertex(float x, float y, float z){
		return new Atom[]{Atom.newAtom("glvertex"), Atom.newAtom(x), Atom.newAtom(y), Atom.newAtom(z)};
	}

	public static Atom[] sketch_normal(float x, float y, float z){
		return new Atom[]{Atom.newAtom("glnormal"), Atom.newAtom(x), Atom.newAtom(y), Atom.newAtom(z)};
	}

	public static Atom[] sketch_texture(float x, float y){
		return new Atom[]{Atom.newAtom("gltexcoord"), Atom.newAtom(x), Atom.newAtom(y)};
	}

	public static Atom[] sketch_endShape(){
		return new Atom[]{Atom.newAtom("glend")};
	}

}
