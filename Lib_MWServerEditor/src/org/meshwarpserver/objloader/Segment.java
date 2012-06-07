package org.meshwarpserver.objloader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.nio.*;

import ch.maybites.mxj.math.*;

public class Segment {
	public ArrayList<Face> faces;

	public IntBuffer indexIB;
	public FloatBuffer dataFB;

	/**
	 * Constructor for the ModelSegment, each Segment holds a Vector of
	 * Elements. each element is a collection of indexes to the vert, normal,
	 * and UV arrays that make up a single face.
	 */
	public Segment() {
		faces = new ArrayList<Face>();
	}
	
	public void refreshSketchCommands(){
		for (int f = 0; f < getFaceCount(); f++) {
			getFace(f).refreshSketchCommands();
		}
	}

	public Face getFace(int index) {
		return faces.get(index);
	}
	
	public Face[] getFaces() {
		return faces.toArray(new Face[faces.size()]);
	}

	public PVector[] getIndices() {
		ArrayList<PVector> indices = new ArrayList<PVector>();

		for (int i = 0; i < faces.size(); i++)
			indices.addAll(Arrays.asList(getFace(i).getVertices()));

		return indices.toArray(new PVector[indices.size()]);
	}


	public int getFaceCount() {
		return faces.size();
	}

	public int getIndexCount() {
		int count = 0;

		for (int i = 0; i < getFaceCount(); i++)
			count += (getFace(i)).getVertexIndexCount();

		return count;
	}

	public void sortFacesByX() {
		Collections.sort(faces, Face.FaceXComparator);
	}

	public void sortFacesByY() {
		Collections.sort(faces, Face.FaceYComparator);
	}

	public void sortFacesByZ() {
		Collections.sort(faces, Face.FaceZComparator);
	}

	public Segment clone(){
		Segment clone = new Segment();
		for(int i = 0; i < getFaceCount(); i++){
			clone.faces.add(getFace(i).clone());
		}
		clone.dataFB = this.dataFB;
		clone.indexIB = this.indexIB;
		return clone;
	}

}
