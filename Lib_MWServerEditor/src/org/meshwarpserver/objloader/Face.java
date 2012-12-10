package org.meshwarpserver.objloader;

/*
 * Alias .obj loader for processing
 * programmed by Tatsuya SAITO / UCLA Design | Media Arts 
 * Created on 2005/04/17
 *
 * 
 *  
 */

import java.util.ArrayList;
import java.util.Comparator;

import org.meshwarpserver.util.GLCommands;
import org.meshwarpserver.util.PVector;

import com.cycling74.max.*;

/**
 * @author tatsuyas
 * @author mattDitton
 * @author Ekene Ijeoma
 * 
 *         Each model element contains the indexes to the vertices, normals and
 *         UV's needed to make a face
 */

public class Face implements Constants, Comparable {

	public int indexType = POLYGON;
	
	public ArrayList<Integer> vertexIndices;
	public ArrayList<Integer> uvIndices;
	public ArrayList<Integer> normalIndices;

	public ArrayList<PVector> vertices;
	public ArrayList<PVector> normals;
	public ArrayList<PVector> uvs;
	
	protected ArrayList<Atom[]> _sketchFaceCommands; 
	protected ArrayList<Atom[]> sketchLineCommands; 
	protected ArrayList<Atom[]> sketchTexFaceCommands; 
	protected ArrayList<Atom[]> sketchTexLineCommands;
	
	private int flagForDeletionCounter = 0;
	
	protected FaceSubDivision _mySubFaces;
	
	protected int _subdivisionLevel; // 0 would be the smallest subface
	
	private float textureShiftX = -0.5f;
	private float textureShiftY = -0.5f;

	/**
	 * Constructor for the Face. The Face class contains an a collection of arrays for the vert, normal and uv indexes. 
	 * For convenience there are PVector Arrays that hold the verts, normals and uv's of the face. 
	 * These arrays are only references back to the main Arrays that live in OBJModel.
	 */
	public Face() {
		vertexIndices = new ArrayList<Integer>();
		uvIndices = new ArrayList<Integer>();
		normalIndices = new ArrayList<Integer>();

		vertices = new ArrayList<PVector>();
		normals = new ArrayList<PVector>();
		uvs = new ArrayList<PVector>();
		_sketchFaceCommands = new ArrayList<Atom[]>();
		sketchLineCommands = new ArrayList<Atom[]>();
		sketchTexFaceCommands = new ArrayList<Atom[]>();
		sketchTexLineCommands = new ArrayList<Atom[]>();
	}

	public void makeSubFaces(int subDivision){
		_subdivisionLevel = subDivision;
		if(subDivision > 0){
			_mySubFaces = new FaceSubDivision(this, subDivision);
		}else{
			_mySubFaces = null;
		}
	}
	
	public Face clone(){
		Face clone = new Face();
		for(int i = 0; i < vertexIndices.size(); i++){
			clone.vertexIndices.add(vertexIndices.get(i));
		}
		for(int i = 0; i < uvIndices.size(); i++){
			clone.uvIndices.add(uvIndices.get(i));
		}
		for(int i = 0; i < normalIndices.size(); i++){
			clone.normalIndices.add(normalIndices.get(i));
		}
		clone._mySubFaces = _mySubFaces;
		return clone;
	}
	
	public ArrayList<Atom[]> drawSketchFace(){
		return _sketchFaceCommands;
	}
	
	public ArrayList<Atom[]> drawSketchLines(){
		return sketchLineCommands;
	}
	
	public ArrayList<Atom[]> drawSketchTexFace(){
		return sketchTexFaceCommands;
	}
	
	public ArrayList<Atom[]> drawSketchTexLines(){
		return sketchTexLineCommands;
	}
	
	protected void createTexFaceCommands(){
		if (getUVCount() > 0) {
			sketchTexFaceCommands.add(GLCommands.sketch_beginShape());
			for(int fp = 0; fp < getUVCount(); fp++){
				PVector texture = getUvs(fp);
				if(texture != null){
					sketchTexFaceCommands.add(GLCommands.sketch_texture(texture.x, texture.y));
					sketchTexFaceCommands.add(GLCommands.sketch_vertex(texture.x+textureShiftX, texture.y+textureShiftY, 0.f));
				}
			} 
			sketchTexFaceCommands.add(GLCommands.sketch_endShape());
		}
	}
	
	protected void createTexLineCommands(){
		if (getUVCount() > 0) {
			PVector vertice = getUvs(getUVCount()-1);
			sketchTexLineCommands.add(GLCommands.sketch_moveto(vertice.x+textureShiftX, vertice.y+textureShiftY, 0.f));

			for(int fp = 0; fp < getUVCount(); fp++){
				PVector vertex = getUvs(fp);
				if(vertex != null){
					sketchTexLineCommands.add(GLCommands.sketch_lineto(vertex.x+textureShiftX, vertex.y+textureShiftY, 0.f));
				}
			} 
		}
	}

	protected void createLineCommands(){
		if (getVertexCount() > 0) {
			PVector vertice = getVertice(getVertexCount()-1);
			sketchLineCommands.add(GLCommands.sketch_moveto(vertice.x, vertice.y, vertice.z));

			for(int fp = 0; fp < getVertexCount(); fp++){
				PVector vertex = getVertice(fp);
				if(vertex != null){
					sketchLineCommands.add(GLCommands.sketch_lineto(vertex.x, vertex.y, vertex.z));
				}
			} 
		}
	}

	protected void createFaceCommands(){
		if (getVertexCount() > 0) {
			_sketchFaceCommands.add(GLCommands.sketch_beginShape()); // specify render mode
			for(int fp = 0; fp < getVertexCount(); fp++){
				PVector texture = getUvs(fp);
				if(texture != null){
					_sketchFaceCommands.add(GLCommands.sketch_texture(texture.x, texture.y));
				}
				PVector normal = getNormal(fp);
				if(normal != null){
					_sketchFaceCommands.add(GLCommands.sketch_normal(normal.x, normal.y, normal.z));
				}
				PVector vertex = getVertice(fp);
				if(vertex != null){
					_sketchFaceCommands.add(GLCommands.sketch_vertex(vertex.x, vertex.y, vertex.z));
				}
			} 
			_sketchFaceCommands.add(GLCommands.sketch_endShape());
		}
	}

	/**
	 * Before this method is call it is recommended to update the vertices, normals and texture
	 * coordinates.
	 */
	public void refreshSketchCommands(){
		sketchTexFaceCommands.clear();
		sketchTexLineCommands.clear();
		sketchLineCommands.clear();
		_sketchFaceCommands.clear();				

		createTexFaceCommands();

		if(_mySubFaces == null){
			createFaceCommands();
			createLineCommands();
			createTexLineCommands();
		} else {
			_mySubFaces.refreshSketchCommands();
		}
	}
	
	public int getVertexIndexCount() {
		return vertexIndices.size();
	}
	
	public int getTextureIndexCount() {
		return uvIndices.size();
	}

	public int getNormalIndexCount() {
		return normalIndices.size();
	}

	public int getVertexCount() {
		return vertices.size();
	}

	public int getNormalCount() {
		return normals.size();
	}

	public int getUVCount() {
		return uvs.size();
	}

	public int[] getVertexIndices() {
		int[] v = new int[getVertexIndexCount()];

		for (int i = 0; i < v.length; i++)
			v[i] = getVertexIndex(i);

		return v;
	}

	public int[] getNormalIndices() {
		int[] v = new int[getNormalIndexCount()];

		for (int i = 0; i < v.length; i++)
			v[i] = getNormalIndex(i);

		return v;
	}

	public int[] getTextureIndices() {
		
		int[] v = new int[getTextureIndexCount()];

		for (int i = 0; i < v.length; i++)
			v[i] = getTextureIndex(i);

		return v;
	}

	/**
	 * Returns an array of PVectors that make up this face
	 * @return PVector []
	 */
	
	public PVector[] getVertices() {
		return vertices.toArray(new PVector[vertices.size()]);
	}

	public PVector getVertice(int i) {
		try{
			return vertices.get(i);
		}catch(IndexOutOfBoundsException e){
			;
		}
		return null;
	}
	
	/**
	 * Returns an array of normals that make up this face
	 * @return PVector []
	 */
	public PVector[] getNormals() {
		return normals.toArray(new PVector[normals.size()]);
	}

	public PVector getNormal(int i) {
		try{
			return normals.get(i);
		}catch(IndexOutOfBoundsException e){
			;
		}
		return null;
	}

	/**
	 * Returns an array of uvs that make up this face
	 * @return PVector []
	 */
	public PVector[] getUvs() {
		return uvs.toArray(new PVector[uvs.size()]);
	}

	public PVector getUvs(int i) {
		try{
			return uvs.get(i);
		}catch(IndexOutOfBoundsException e){
			;
		}
		return null;
	}
	
	/**
	 * Get's the center position of the face.
	 * @return PVector []
	 */
	public PVector getCenter() {
		PVector c = new PVector();

		for (int i = 0; i < vertices.size(); i++)
			c.add(vertices.get(i));

		c.div(vertices.size());

		return c;
	}

	/**
	 * Returns the face normal. The face normal is calculated from the face center using the cross product of the first and last vert. 
	 * An alternate method would be to get the average of all vert normals. But that one you can do yourself, because in certain situations it's not reliable..  
	 * @return a normalized PVector
	 */
	
	public PVector getNormal() {
		// center vertex
		PVector c = getCenter();

		// center - first vertex
		PVector aToB = PVector.sub(c, vertices.get(0));
		// center - last vertex
		PVector cToB = PVector.sub(c, vertices.get(vertices.size() - 1));
		PVector n = cToB.cross(aToB);

		n.normalize();

		return n;
	}

	// Arrays start at 0 (hence the -1) But OBJ files start the
	// indices at 1.
	public int getVertexIndex(int i) {
		return vertexIndices.get(i) - 1;
	}
	
	public int getTextureIndex(int i) {
		return uvIndices.get(i) - 1;
	}

	public int getNormalIndex(int i) {
		return normalIndices.get(i) - 1;
	}
	
	/**
	 * Used for knowing if a face is pointing in the direction of the supplied PVector. 
	 * In a dense mesh it can be faster to check to see if the face should be draw before drawing it.
	 * Also it can look cool.
	 * 
	 * @param position
	 * @return True if the angle made between the face normal and position from the face center is less than 90.
	 */
	
	
	public boolean isFacingPosition(PVector position) {
		PVector c = getCenter();

		// this works out the vector from the camera to the face.
		PVector positionToFace = new PVector(position.x - c.x, position.y - c.y, position.z - c.z);

		// We now know the vector from the camera to the face,
		// and the vector that describes which direction the face
		// is pointing, so we just need to do a dot-product and
		// based on that we can tell if it's facing the camera or not
		// float result = PVector.dot(cameraToFace, faceNormal);
		float result = positionToFace.dot(getNormal());

		// if the result is positive, then it is facing the camera.
		return result < 0;
	}

	/**
	 * Returns a float value from 0 - 1. With 1 occurring if the position is in direct line with the face normal. 
	 * Likewise 0 is facing completely away from the face normal. And you guessed it. A value of 0.5 comes is from perpendicular faces.
	 * @param position
	 * @return 
	 */
	
	public float getFacingAmount(PVector position) {
		PVector c = getCenter();

		// this works out the vector from the camera to the face.
		PVector positionToFace = new PVector(position.x - c.x, position.y - c.y, position.z - c.z);

		c.normalize();

		positionToFace.normalize();

		return (1.0f - (positionToFace.dot(getNormal()) + 1.0f) / 2.0f);
	}

	public int compareTo(Object f2) throws ClassCastException {
		if (!(f2 instanceof Face))
			throw new ClassCastException("Face object expected.");

		PVector f1Center = getCenter();
		PVector f2Center = ((Face) f2).getCenter();

		return (int) (f1Center.x - f2Center.x);
	}

	public static Comparator<Face> FaceXComparator = new Comparator<Face>() {
		public int compare(Face f1, Face f2) {
			PVector f1Center = f1.getCenter();
			PVector f2Center = f2.getCenter();
			
			return (int) (f1Center.x - f2Center.x);
		}
	};

	public static Comparator<Face> FaceYComparator = new Comparator<Face>() {
		public int compare(Face f1, Face f2) {
			PVector f1Center = f1.getCenter();
			PVector f2Center = f2.getCenter();
			
			return (int) (f1Center.y - f2Center.y);
		}
	};

	public static Comparator<Face> FaceZComparator = new Comparator<Face>() {
		public int compare(Face f1, Face f2) {
			PVector f1Center = f1.getCenter();
			PVector f2Center = f2.getCenter();
			
			return (int) (f1Center.z - f2Center.z);
		}
	};
}
