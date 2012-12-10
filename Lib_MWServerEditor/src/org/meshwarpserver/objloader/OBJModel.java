package org.meshwarpserver.objloader;

/*
 * Alias .obj loader for processing
 * programmed by Tatsuya SAITO / UCLA Design | Media Arts 
 * Created on 2005/04/17
 */

import ch.maybites.tools.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.meshwarpserver.util.GLCommands;
import org.meshwarpserver.util.PVector;

/**
 * 
 * @author tatsuyas
 * @author mditton
 * @author Ekene Ijeoma
 * @author martin froehlich
 * 
 * @TODO: Add documentation and examples to the google code repository
 * @TODO: Add java doc commenting
 * @TODO: Add vertex normals and face normals from Collada Loader
 * 
 *        google code address (because I always forget)
 *        http://code.google.com/p/saitoobjloader/
 */

public class OBJModel implements Constants{

	// global variables
	private ArrayList<Integer> selectedVerticesIndices; // vertexes
	private ArrayList<Integer> selectedTextureVerticesIndices; // texture coordinates

	private ArrayList<PVector> modelVertices; // vertexes
	private ArrayList<PVector> textureVertices; // texture coordinates
	private ArrayList<PVector> normalVertices; // normals
	private ArrayList<Segment> segments;

	private Hashtable<String, Group> groups;

	String name = "default";

	private Group defaultGroup = new Group("default");
	private Segment defaultSegment = new Segment();

	private SketcherCommands parent;

	// runtime rendering variables
	private int shapeMode = TRIANGLES; // render mode (ex. POLYGON,
	// POINTS ..)

	public static String RELATIVE = "relative";
	public static String ABSOLUTE = "absolute";

	public Debug debug;



	/**
	 * Class Constructor to setup an empty obj model
	 */
	public OBJModel(SketcherCommands _parent) {
		parent = _parent;
		debug = new Debug();
		debug.enabled = false;
		setup();
	}

	/**
	 * Class Constructor, loads the string as an obj from the data directory
	 */
	public OBJModel(SketcherCommands _parent, String filename) {
		this(_parent);
		load(filename);
	}

	/**
	 * Class Constructor, loads the string as an obj from the data directory. <br>
	 * </br> The boolean decides if local paths should be used when loading the
	 * mtl and textures in the mtl.<br>
	 * </br> The int sets the draw mode, to the processing draw mode, eg.
	 * TRIANGLES, POINTS, POLYGON, LINES, TRIANGLE_STRIP, QUAD_STRIP, QUADS.<br>
	 * </br>
	 */
	public OBJModel(SketcherCommands _parent, String _fileName, int _shapeMode) {
		this(_parent, _fileName);
		shapeMode(_shapeMode);
	}
	
	/****************************************************************
	 * 					Selection Methods
	 ****************************************************************/
	
	public int getSelectedModelVerticeCount() {
		return this.selectedVerticesIndices.size();
	}

	public int getSelectedTextureVerticeCount() {
		return this.selectedTextureVerticesIndices.size();
	}
	
	private boolean addSelectedModelVertice(PVector v){
		if(!selectedVerticesIndices.contains(getModelVerticeIndex(v))){
			selectedVerticesIndices.add(getModelVerticeIndex(v));
			return true;
		}
		return false;
	}
	
	private boolean unselectSelectedVertice(PVector v){
		if(selectedVerticesIndices.contains(new Integer(getModelVerticeIndex(v)))){
			selectedVerticesIndices.remove(new Integer(getModelVerticeIndex(v)));
			return true;
		}
		return false;
	}

	private boolean addSelectedTextureVertice(PVector v){
		if(!selectedTextureVerticesIndices.contains(getTextureVerticeIndex(v))){
			selectedTextureVerticesIndices.add(getTextureVerticeIndex(v));
			return true;
		}
		return false;
	}
	
	private boolean unselectSelectedTextureVertice(PVector v){
		if(selectedTextureVerticesIndices.contains(new Integer(getTextureVerticeIndex(v)))){
			selectedTextureVerticesIndices.remove(new Integer(getTextureVerticeIndex(v)));
			return true;
		}
		return false;
	}

	/**
	 * This method selects all model vertices plus their corresponding
	 * texture vertices that are within the range
	 * @param useX
	 * @param useY
	 * @param useZ
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 * @return
	 */
	public int selectModelVertices(int useX, int useY, int useZ, float x1, float y1, float z1, float x2, float y2, float z2) {
		int selectflag = 0;

		Segment tmpModelSegment;
		Face tmpSegmentFace;
		PVector tmpTextureVertice, tmpVertice;

		for (int s = 0; s < getSegmentCount(); s++) {
			tmpModelSegment = segments.get(s);
			for (int f = 0; f < tmpModelSegment.getFaceCount(); f++) {
				tmpSegmentFace = tmpModelSegment.getFace(f);
				for (int v = 0; v < tmpSegmentFace.getVertexCount(); v++) {
					tmpTextureVertice = tmpSegmentFace.getUvs(v);
					tmpVertice = tmpSegmentFace.getVertice(v);
					if(flagVerticeWithinRange(tmpVertice, useX, useY, useZ, x1, y1, z1, x2, y2, z2)){
						addSelectedTextureVertice(tmpTextureVertice);
						addSelectedModelVertice(tmpVertice);
						selectflag++;
					}
				}
			}
		}
		//debug.println("selected " + selectflag + " face vertices. total selected model vertices: " + this.getSelectedModelVerticeCount());
		return selectflag;
	}

	/**
	 * this method removes model- and texture vertices from the selection.
	 * @param useX
	 * @param useY
	 * @param useZ
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 * @return
	 */
	public int unSelectModelVertices(int useX, int useY, int useZ, float x1, float y1, float z1, float x2, float y2, float z2) {
		int unselectflag = 0;

		Segment tmpModelSegment;
		Face tmpSegmentFace;
		PVector tmpVertice, tmpTextureVertice;

		for (int s = 0; s < getSegmentCount(); s++) {
			tmpModelSegment = segments.get(s);
			for (int f = 0; f < tmpModelSegment.getFaceCount(); f++) {
				tmpSegmentFace = tmpModelSegment.getFace(f);
				for (int v = 0; v < tmpSegmentFace.getVertexCount(); v++) {
					tmpTextureVertice = tmpSegmentFace.getUvs(v);
					tmpVertice = tmpSegmentFace.getVertice(v);
					if(flagVerticeWithinRange(tmpVertice, useX, useY, useZ, x1, y1, z1, x2, y2, z2)){
						unselectSelectedVertice(tmpVertice);
						unselectSelectedTextureVertice(tmpTextureVertice);
						unselectflag++;
					}
				}
			}
		}
		debug.println("unselected " + unselectflag + " face vertices. total selected model vertices: " + this.getSelectedModelVerticeCount());
		return unselectflag;
	}

	/**
	 * This method selects all model and texture vertices that are within the range
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public int selectTextureVertices(float x1, float y1, float x2, float y2) {
		int selectflag = 0;

		Segment tmpModelSegment;
		Face tmpSegmentFace;
		PVector tmpTextureVertice, tmpVertice;

		for (int s = 0; s < getSegmentCount(); s++) {
			tmpModelSegment = segments.get(s);
			for (int f = 0; f < tmpModelSegment.getFaceCount(); f++) {
				tmpSegmentFace = tmpModelSegment.getFace(f);
				for (int v = 0; v < tmpSegmentFace.getTextureIndexCount(); v++) {
					tmpTextureVertice = tmpSegmentFace.getUvs(v);
					tmpVertice = tmpSegmentFace.getVertice(v);
					if(flagTextureVerticeWithinRange(tmpTextureVertice, x1, y1, x2, y2)){
						addSelectedTextureVertice(tmpTextureVertice);
						addSelectedModelVertice(tmpVertice);
						selectflag++;
					}
				}
			}
		}
		debug.println("selected " + selectflag + " texture vertices. total selected texture vertices: " + this.getSelectedTextureVerticeCount());
		return selectflag;
	}

	/**
	 * this method removes ONLY texture vertices from the selection. it doesnt affect the model
	 * vertice selection, since several texture vertices can be associated with one model vertice
	 * (but not the other way around)
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public int unSelectTextureVertices(float x1, float y1, float x2, float y2) {
		int unselectflag = 0;

		Segment tmpModelSegment;
		Face tmpSegmentFace;
		PVector tmpTextureVertice;

		for (int s = 0; s < getSegmentCount(); s++) {
			tmpModelSegment = segments.get(s);
			for (int f = 0; f < tmpModelSegment.getFaceCount(); f++) {
				tmpSegmentFace = tmpModelSegment.getFace(f);
				for (int v = 0; v < tmpSegmentFace.getTextureIndexCount(); v++) {
					tmpTextureVertice = tmpSegmentFace.getUvs(v);
					if(flagTextureVerticeWithinRange(tmpTextureVertice, x1, y1, x2, y2)){
						unselectSelectedTextureVertice(tmpTextureVertice);
						unselectflag++;
					}
				}
			}
		}
		debug.println("unselected " + unselectflag + " texture vertices. total selected texture vertices: " + this.getSelectedTextureVerticeCount());
		return unselectflag;
	}


	public PVector getSelectedModelVertice(int i) {
		return modelVertices.get(selectedVerticesIndices.get(i));
	}

	public PVector getSelectedTextureVertice(int i) {
		return textureVertices.get(selectedTextureVerticesIndices.get(i));
	}

	public int getSelectedModelVerticeIndex(int i) {
		return selectedVerticesIndices.get(i);
	}

	public void unselectAllVertices() {
		selectedVerticesIndices.clear();
		selectedTextureVerticesIndices.clear();
	}

	public void selectAllVertices(){
		selectedVerticesIndices.clear();
		selectedTextureVerticesIndices.clear();
		for(int i = 0; i < getModelVerticesCount(); i++){
			selectedVerticesIndices.add(i);
		}
		for(int i = 0; i < getTextureVerticeCount(); i++){
			selectedTextureVerticesIndices.add(i);
		}
	}

	public PVector getSelected3DAverageCenter(){
		PVector averageCenter = new PVector();
		for(int i = 0; i < getSelectedModelVerticeCount(); i++){
			averageCenter.add(getSelectedModelVertice(i));
		}
		averageCenter.div(getSelectedModelVerticeCount());
		return averageCenter;
	}

	public PVector getSelected2DAverageCenter(){
		PVector averageCenter = new PVector();
		for(int i = 0; i < getSelectedTextureVerticeCount(); i++){
			averageCenter.add(getSelectedTextureVertice(i));
		}
		averageCenter.div(getSelectedTextureVerticeCount());
		averageCenter.z = 0.1f; // makes sure the handler is slightly above the texture level
		return averageCenter;
	}

	private boolean flagTextureVerticeWithinRange(PVector v, float x1, float y1, float x2, float y2) {
		if(x1 > x2){
			if(v.x > x1 || v.x < x2)
				return false;
		}else{
			if(v.x < x1 || v.x > x2)
				return false;
		}
		if(y1 > y2){
			if(v.y > y1 || v.y < y2)
				return false;
		}else{
			if(v.y < y1 || v.y > y2)
				return false;
		}
		return true;
	}

	private boolean flagVerticeWithinRange(PVector v, int useX, int useY, int useZ, float x1, float y1, float z1, float x2, float y2, float z2) {
		if(useX == 1){
			if(x1 > x2){
				if(v.x > x1 || v.x < x2)
					return false;
			}else{
				if(v.x < x1 || v.x > x2)
					return false;
			}
		}
		if(useY == 1){
			if(y1 > y2){
				if(v.y > y1 || v.y < y2)
					return false;
			}else{
				if(v.y < y1 || v.y > y2)
					return false;
			}
		}
		if(useZ == 1){
			if(z1 > z2){
				if(v.z > z1 || v.z < z2)
					return false;
			}else{
				if(v.z < z1 || v.z > z2)
					return false;
			}
		}
		return true;
	}

	
	
	private void setup() {
		selectedVerticesIndices = new ArrayList<Integer>(); // vertexes
		selectedTextureVerticesIndices = new ArrayList<Integer>(); // texture coordinates
		modelVertices = new ArrayList<PVector>();
		segments = new ArrayList<Segment>();
		textureVertices = new ArrayList<PVector>();
		normalVertices = new ArrayList<PVector>();

		groups = new Hashtable<String, Group>();
	}

	// -------------------------------------------------------------------------
	// ------------------------------------------------------------------- Utils
	// -------------------------------------------------------------------------

	/**
	 * A Debug method that prints information about the loaded model<br>
	 * </br> This method only prints information if the debugMode is true.<br>
	 * </br> V Size = The number of vertex positions<br>
	 * </br> Vt Size = The number of UV positions<br>
	 * </br> Vn Size = The number of normals <br>
	 * </br> G Size = the number of Groups in the model <br>
	 * </br> S Size = the number of segments in the model, this should directly
	 * equate to the number of unique materials in the mtl file<br>
	 * </br>
	 */
	public void printModelInfo() {
		debug.println("Obj Name: \t\t" + name);
		debug.println("");
		debug.println("V  Size: \t\t" + modelVertices.size());
		debug.println("Vt Size: \t\t" + textureVertices.size());
		debug.println("Vn Size: \t\t" + normalVertices.size());
		debug.println("G  Size: \t\t" + groups.size());
		debug.println("S  Size: \t\t" + getSegmentCount());
		for(int i = 0; i < getSegmentCount(); i++){
			debug.println("Segment: \t\t" + i + " has " + this.getIndexCountInSegment(i) + " faces.");
		}
		debug.println("");
	}

	/**
	 * Enables the debug mode.<br>
	 * </br> Prints version and contact information to the console.<br>
	 * </br>
	 */
	public void enableDebug() {
		debug.enabled = true;
		debug.println("");
		debug.println("objloader version 019");
		debug.println("19 January 2010");
		debug.println("http://code.google.com/p/saitoobjloader/");
		debug.println("");
	}

	public void disableDebug() {
		debug.enabled = false;
	}

	/**
	 * Clears all Vectors ready for loading in a new model.<br>
	 * </br> Doing something like this<br>
	 * </br> <br>
	 * </br> OBJModel model = new OBJModel(this, "myobj.obj");<br>
	 * </br> // do something with model<br>
	 * </br> model.clear();<br>
	 * </br> model.load("myobj.obj");<br>
	 * </br> <br>
	 * </br> is vastly better for memory use than doing something like this<br>
	 * </br> <br>
	 * </br> OBJModel model = new OBJModel(this, "myobj.obj");<br>
	 * </br> // do something with model<br>
	 * </br> model = new OBJModel(this, "myOtherObj.obj");<br>
	 * </br> <br>
	 * </br> The second example is really bad because the original model is
	 * still in memory but nothing is pointing to it.<br>
	 * </br> We would have to wait for the Garbage Collector to do the clean up
	 * before freeing the memory. <br>
	 * </br> If loading in lots of stuff is important then using the
	 * model.clear() could help a lot.<br>
	 * </br>
	 */
	public void reset() {
		modelVertices.clear();
		textureVertices.clear();
		normalVertices.clear();
		groups.clear();
		segments.clear();
		debug.println("OBJModel is empty");
	}

	/**
	 * Set's the beginShape mode for drawing the model. <br>
	 * </br> This will vary depending on the model and the export settings.<br>
	 * </br> A safe bet is to triangulate the model before exporting and set the
	 * drawmode to TRANGLES.<br>
	 * </br> Also due to inconsistencies in OPENGL points, the POINTS mode may
	 * draw nothing in OPENGL.<br>
	 * </br> A common misconception is that LINES will result in a wireframe.
	 * For this effect you should leave the drawmode as the correct mode and
	 * disable the material and use sroke() to get a wireframe<br>
	 * </br>
	 * 
	 * @param TRIANGLES
	 *            , POINTS, POLYGON, LINES, TRIANGLE_STRIP, QUAD_STRIP, QUADS<br>
	 *            </br>
	 */
	public void shapeMode(int mode) {
		this.shapeMode = mode;

		switch (mode) {
		case (POINTS):
			debug.println("draw mode:\t\tPOINTS");
		break;

		case (LINES):
			debug.println("draw mode:\t\tLINES");
		break;

		case (POLYGON):
			debug.println("draw mode:\t\tPOLYGON");
		break;

		case (TRIANGLES):
			debug.println("draw mode:\t\tTRIANGLES");
		break;

		case (TRIANGLE_STRIP):
			debug.println("draw mode:\t\tTRIANGLE_STRIP");
		break;

		case (QUADS):
			debug.println("draw mode:\t\tQUADS");
		break;

		case (QUAD_STRIP):
			debug.println("draw mode:\t\t");
		break;
		}
	}

	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------- Draw
	// -------------------------------------------------------------------------

	public void drawTexFaces(){
		Segment tmpModelSegment;

		// render all triangles
		for (int s = 0; s < getSegmentCount(); s++) {
			tmpModelSegment = segments.get(s);
			for (int f = 0; f < tmpModelSegment.getFaceCount(); f++) {
				parent.command(tmpModelSegment.getFace(f).drawSketchTexFace());
			}
		}
	}

	public void drawTexLines(){
		Segment tmpModelSegment;

		// render all triangles
		for (int s = 0; s < getSegmentCount(); s++) {
			tmpModelSegment = segments.get(s);
			for (int f = 0; f < tmpModelSegment.getFaceCount(); f++) {
				parent.command(tmpModelSegment.getFace(f).drawSketchTexLines());
			}
		}
	}

	public void drawModelFaces(){
		Segment tmpModelSegment;

		// render all triangles
		for (int s = 0; s < getSegmentCount(); s++) {
			tmpModelSegment = segments.get(s);
			for (int f = 0; f < tmpModelSegment.getFaceCount(); f++) {
				parent.command(tmpModelSegment.getFace(f).drawSketchFace());
			}
		}
	}

	public void drawModelLines(){
		Segment tmpModelSegment;

		// render all triangles
		for (int s = 0; s < getSegmentCount(); s++) {
			tmpModelSegment = segments.get(s);
			for (int f = 0; f < tmpModelSegment.getFaceCount(); f++) {
				parent.command(tmpModelSegment.getFace(f).drawSketchLines());
			}
		}
	}

	public void drawTexturePoints(float shiftX, float shiftY) {
		try {
			PVector v = null;

			for (int f = 0; f < this.getTextureVerticeCount(); f++) {
				v = this.getTextureVertice(f);
				parent.command(GLCommands.sketch_point(v.x+shiftX, v.y+shiftY, v.z));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void drawModelPoints() {
		try {
			PVector v = null;

			for (int f = 0; f < this.getModelVerticesCount(); f++) {
				v = this.getModelVertice(f);
				parent.command(GLCommands.sketch_point(v.x, v.y, v.z));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void drawSelectedTexturePoints(float shiftX, float shiftY) {
		try {
			PVector v = null;

			for (int f = 0; f < this.getSelectedTextureVerticeCount(); f++) {
				v = this.getSelectedTextureVertice(f);
				parent.command(GLCommands.sketch_point(v.x+shiftX, v.y+shiftY, v.z));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void drawSelectedPoints() {
		try {
			PVector v = null;

			for (int f = 0; f < this.getSelectedModelVerticeCount(); f++) {
				v = this.getSelectedModelVertice(f);
				parent.command(GLCommands.sketch_point(v.x, v.y, v.z));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * The manual load method for obj files. This method is automaticly called
	 * when using Constructors that include the file name<br>
	 * </br> The method uses the Processing createReader() function to get a
	 * BufferedReader object in order to read the file one line at a time.<br>
	 * </br> This is slightly better method than loadStrings() as it's less
	 * memory intensive for large obj files. <br>
	 * </br>
	 */
	public void load(String filename) {
		setup();
		parseOBJ(getBufferedReader(filename));

		if (debug.enabled)
			this.printModelInfo();
	}

	/** TOOLS */
	public void scale(float scale) {
		scale(scale, scale, scale);

	}

	public void scale(float scaleX, float scaleY, float scaleZ) {
		int vertexCount = getModelVerticesCount();

		if (vertexCount == 0)
			debug.println("OBJTransform - \tThe model has no verts. Have you loaded it yet?");
		else {
			PVector v;

			for (int i = 0; i < vertexCount; i++) {
				v = getModelVertice(i);

				v.x *= scaleX;
				v.y *= scaleY;
				v.z *= scaleZ;
			}
		}
	}

	public void translate(PVector p) {
		int vertexCount = getModelVerticesCount();

		if (vertexCount == 0)
			debug.println("OBJTransform - \tThe model has no verts. Have you loaded it yet?");
		else {
			PVector v;
			PVector m = new PVector(p.x, p.y, p.z);
			m.mult(-1);

			for (int i = 0; i < vertexCount; i++) {
				v = getModelVertice(i);
				v.add(m);
			}
		}
	}

	public void translateToCenter() {
		BoundingBox obox = new BoundingBox(this);
		translate(obox.getCenter());
	}

	public void mapUVToZeroOne() {
		int count = getTextureVerticeCount();

		PVector minimum = new PVector(MAX_INT, MAX_INT, 0);
		PVector maximum = new PVector(MIN_INT, MIN_INT, 0);

		PVector temp;

		for (int i = 0; i < count; i++) {
			temp = getTextureVertice(i);
			minimum.x = Utils.min(minimum.x, temp.x);
			minimum.y = Utils.min(minimum.y, temp.y);
			maximum.x = Utils.max(maximum.x, temp.x);
			maximum.y = Utils.max(maximum.y, temp.y);
		}

		for (int i = 0; i < count; i++) {
			temp = getTextureVertice(i);
			temp.x = Utils.map(temp.x, minimum.x, maximum.x, 0.0f, 1.0f);
			temp.y = Utils.map(temp.y, minimum.y, maximum.y, 0.0f, 1.0f);
		}
	}

	public void clampUVToZeroOne() {
		int count = getTextureVerticeCount();

		PVector temp;

		for (int i = 0; i < count; i++) {
			temp = getTextureVertice(i);
			temp.x = Utils.constrain(temp.x, 0.0f, 1.0f);
			temp.y = Utils.constrain(temp.y, 0.0f, 1.0f);
		}
	}

	// -------------------------------------------------------------------------
	// ------------------------------------------------------------- Obj Parsing
	// -------------------------------------------------------------------------

	/**
	 * The method that does all the grunt work in reading and processing the obj
	 * file.<br>
	 * </br>
	 */
	private void parseOBJ(BufferedReader bread) {
		try {
			// adding default variables to the global data table creating the
			// default group
			groups.put("default", defaultGroup);

			Group currentGroup = defaultGroup;

			// creating the default model segment
			segments.add(defaultSegment);

			currentGroup.segments.add(defaultSegment);

			Segment currentModelSegment = defaultSegment;

			String line;

			while ((line = bread.readLine()) != null) {
				// debug.println(line);
				// parse the line

				// The below patch/hack comes from Carles Tom‡s Mart’ and is a
				// fix for single backslashes in Rhino obj files

				// BEGINNING OF RHINO OBJ FILES HACK
				// Statements can be broken in multiple lines using '\' at the
				// end of a line.
				// In regular expressions, the backslash is also an escape
				// character.
				// The regular expression \\ matches a single backslash. This
				// regular expression as a Java string, becomes "\\\\".
				// That's right: 4 backslashes to match a single one.
				while (line.contains("\\")) {
					line = line.split("\\\\")[0];
					final String s = bread.readLine();
					if (s != null)
						line += s;
				}
				// END OF RHINO OBJ FILES HACK

				String[] elements = line.split("\\s+");

				// if not a blank line, process the line.
				if (elements.length > 0) {
					if (elements[0].equals("v")) {
						// vertex
						PVector tmpv = new PVector(Float.valueOf(elements[1]).floatValue(), Float.valueOf(elements[2]).floatValue(), Float.valueOf(elements[3]).floatValue());
						modelVertices.add(tmpv);
					} else if (elements[0].equals("vn")) {
						// normal
						PVector tmpn = new PVector(Float.valueOf(elements[1]).floatValue(), Float.valueOf(elements[2]).floatValue(), Float.valueOf(elements[3]).floatValue());
						normalVertices.add(tmpn);
					} else if (elements[0].equals("vt")) {
						// uv
						PVector tmpv = new PVector(Float.valueOf(elements[1]).floatValue(), Float.valueOf(elements[2]).floatValue());
						textureVertices.add(tmpv);
					} else if (elements[0].equals("o")) {
						if (elements[1] != null)
							name = elements[1];
					} else if (elements[0].equals("mtllib")) {
						; // material parsing removed from original
					} else if (elements[0].equals("g")) {
						// grouping setting
						Segment newModelSegment = new Segment();

						segments.add(newModelSegment);

						currentModelSegment = newModelSegment;

						for (int e = 1; e < elements.length; e++) {
							if (groups.get(elements[e]) == null) {
								// debug.println("group '" + elements[e] +"'");
								Group newGroup = new Group(elements[e]);

								groups.put(elements[e], newGroup);
							}
						}
					} else if (elements[0].equals("usemtl")) {
						; // dont care about materials
					} else if (elements[0].equals("f")) {
						// face setting
						Face f = new Face();

						if (elements.length < 3)
							debug.println("Warning: potential model data error");

						for (int i = 1; i < elements.length; i++) {
							String seg = elements[i];

							if (seg.indexOf("/") > 0) {
								String[] forder = seg.split("/");

								if (forder.length > 2) {
									if (forder[0].length() > 0) {
										f.vertexIndices.add(Integer.valueOf(forder[0]));
										// f.vertices.add(getVertex(Integer.valueOf(forder[0])));
									}

									if (forder[1].length() > 0) {
										f.uvIndices.add(Integer.valueOf(forder[1]));
										// f.uvs.add(getVertex(Integer.valueOf(forder[1])));
									}

									if (forder[2].length() > 0) {
										f.normalIndices.add(Integer.valueOf(forder[2]));
										// f.normals.add(getVertex(Integer.valueOf(forder[2])));
									}
								} else if (forder.length > 1) {
									if (forder[0].length() > 0) {
										f.vertexIndices.add(Integer.valueOf(forder[0]));
										// f.vertices.add(getVertex(Integer.valueOf(forder[0])));
									}

									if (forder[1].length() > 0) {
										f.uvIndices.add(Integer.valueOf(forder[1]));
										// f.uvs.add(getVertex(Integer.valueOf(forder[1])));
									}
								} else if (forder.length > 0) {
									if (forder[0].length() > 0) {
										f.vertexIndices.add(Integer.valueOf(forder[0]));
										// f.vertices.add(getVertex(Integer.valueOf(forder[0])));
									}
								}
							} else {
								if (seg.length() > 0) {
									f.vertexIndices.add(Integer.valueOf(seg));
									// f.vertices.add(getVertex(Integer.valueOf(seg));
								}
							}
						}

						currentModelSegment.faces.add(f);

					} else if (elements[0].equals("ll")) {
						// line
						Face f = new Face();
						f.indexType = POLYGON;

						if (elements.length < 2)
							debug.println("Warning: potential model data error");

						for (int i = 1; i < elements.length; i++)
							f.vertexIndices.add(Integer.valueOf(elements[i]));

						currentModelSegment.faces.add(f);
					}
				}
			}

			updateSegments();

		} catch (Exception e) {
			System.err.println("OBJ read: Error while trying to read file");
		}

		// Depending on the layout of the obj file, extra empty modeSegments can
		// be created.
		// Here I check each segment to ensure it contains indexes.
		// If there aren't any then the Segment will draw no faces so it's fine
		// to kill it off.
		for (int i = getSegmentCount() - 1; i >= 0; i--) {
			if (getIndexCountInSegment(i) == 0) { // again with the empty model
				// segments WTF?
				segments.remove(i);
			}
		}

	}
	
	public void setFaceSubDivision(int subDivision){
		for (int i = 0; i < getSegmentCount(); i++) {
			Segment s = segments.get(i);

			for (int j = 0; j < s.getFaceCount(); j++) {
				Face f = s.getFace(j);
				f.makeSubFaces(subDivision);
			}
		}
	}

	public void updateSegments(){
		for (int i = 0; i < getSegmentCount(); i++) {
			Segment s = segments.get(i);

			for (int j = 0; j < s.getFaceCount(); j++) {
				Face f = s.getFace(j);

				int[] vtIndex = f.getVertexIndices();
				int[] nmIndex = f.getNormalIndices();
				int[] uvIndex = f.getTextureIndices();

				f.vertices.clear();
				f.normals.clear();
				f.uvs.clear();
				// three for loops for safety. if there are no normals or
				// uv's then nothing will break
				for (int k = 0; k < vtIndex.length; k++)
					f.vertices.add(modelVertices.get(vtIndex[k]));

				for (int k = 0; k < nmIndex.length; k++)
					f.normals.add(normalVertices.get(nmIndex[k]));

				for (int k = 0; k < uvIndex.length; k++)
					f.uvs.add(textureVertices.get(uvIndex[k]));
			}
			s.refreshSketchCommands();
		}
	}

	// -------------------------------------------------------------------------
	// --------------------------------------------------- Obj saving
	// -------------------------------------------------------------------------


	public boolean saveAs(String filename){
		try
		{
			FileWriter filewriter = new FileWriter(filename);
			BufferedWriter bufferedwriter = new BufferedWriter(filewriter);
			PrintWriter printwriter = new PrintWriter(bufferedwriter, true);
			printwriter.println("# MeshWarpEditor Object File:");
			printwriter.println("# maybites.ch");
			PVector tmpn;

			for(int i = 0; i < modelVertices.size(); i++){
				tmpn = modelVertices.get(i);
				printwriter.println("v " + tmpn.x + " " + tmpn.y + " " + tmpn.z);
			}
			for(int i = 0; i < textureVertices.size(); i++){
				tmpn = textureVertices.get(i);
				printwriter.println("vt " + tmpn.x + " " + tmpn.y);
			}
			for(int i = 0; i < normalVertices.size(); i++){
				tmpn = normalVertices.get(i);
				printwriter.println("vn " + tmpn.x + " " + tmpn.y + " " + tmpn.z);
			}

			for (int i = 0; i < getSegmentCount(); i++) {
				Segment s = segments.get(i);

				for (int j = 0; j < s.getFaceCount(); j++) {
					Face f = s.getFace(j);
					printwriter.print("f");
					for(int k = 0; k < f.getVertexIndexCount(); k++){
						printwriter.print(" " + f.vertexIndices.get(k));
						if(f.uvIndices.size() > 0)
							printwriter.print("/" + f.uvIndices.get(k));
						if(f.normalIndices.size() > 0)
							printwriter.print("/" + f.normalIndices.get(k));
					}
					printwriter.println();
				}
			}

			printwriter.close();
		}
		catch(Exception exception)
		{
			System.err.println("OBJ saveas: Error while trying to save file as: "+filename);
		}
		return true;

	}


	// -------------------------------------------------------------------------
	// --------------------------------------------------- Get and Set Functions
	// -------------------------------------------------------------------------

	public void setShapeMode(int _shapeMode) {
		shapeMode = _shapeMode;
	}

	public int getShapeMode() {
		return shapeMode;
	}

	/**
	 * Gets the size of the Groups from the obj Model. <br>
	 * </br> At the moment groups are an unexplored feature.<br>
	 * </br> So you can get the size, but it's not going to do much for you.<br>
	 * </br>
	 * 
	 * @return int count of the group<br>
	 *         </br>
	 */
	public int getGroupCount() {
		return this.groups.size();
	}

	/**
	 * Returns the group via name <br>
	 * </br> Until I find a practical use for Groups this feature isn't going
	 * anywhere. <br>
	 * </br>
	 * 
	 * @param A
	 *            String of the group name that was in the obj file <br>
	 *            </br>
	 * @return a Group <br>
	 *         </br>
	 */
	public Group getGroup(String groupName) {
		return this.groups.get(groupName);
	}

	public Segment getSegment(int _index) {
		return segments.get(_index);
	}

	public Face getFaceInSegment(int _segmentIndex, int _faceIndex) {
		return ((segments.get(_segmentIndex)).getFace(_faceIndex));
	}

	/**
	 * Gets the number of segments in the model.<br>
	 * </br> A segment is a unique material and an array of indexes into the
	 * vert, norm and uv Vectors<br>
	 * </br>
	 * 
	 * @return int
	 */
	public int getSegmentCount() {
		return this.segments.size();
	}

	/**
	 * Gets the total number of faces in the model.<br>
	 * </br> This is the total of the index count across all segments<br>
	 * </br> This is mostly used when you need raw verts for physics simulation<br>
	 * </br>
	 * 
	 * @return int
	 */
	public int getFaceCount() {
		int count = 0;

		for (int i = 0; i < getSegmentCount(); i++) {
			count += getIndexCountInSegment(i);
		}

		return count;
	}

	/**
	 * Gets an array of PVectors that make up the position co-ordinates of the
	 * face.<br>
	 * </br> This method needs one int that must be between 0 and the
	 * getTotalFaceCount()<br>
	 * </br> This is mostly used when you need raw verts for physics simulation<br>
	 * </br>
	 * 
	 * @return PVector[]
	 */
	public PVector[] getFaceVertices(int faceNumber) {

		int segmentNumber = 0;

		int indexNumber = faceNumber;

		// debug.println("segmentNumber, indexNumber = " + segmentNumber + " " +
		// indexNumber);

		while (indexNumber >= getIndexCountInSegment(segmentNumber)) {
			indexNumber -= getIndexCountInSegment(segmentNumber);
			segmentNumber++;
		}

		// debug.println("segmentNumber, indexNumber = " + segmentNumber + " " +
		// indexNumber);

		int[] vertindexes = getModelVerticeIndicesInSegment(segmentNumber, indexNumber);

		// parent.println(vertindexes);

		PVector[] tmp = new PVector[vertindexes.length];

		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = new PVector();
			tmp[i].set(getModelVertice(vertindexes[i]));
		}

		return tmp;
	}

	/**
	 * Gets the number of Index count in the Segment. <br>
	 * </br> In effect this is the number of faces in the Segment. <br>
	 * </br> As each Index is an Array of ints to the vert, normal, or uv Array <br>
	 * </br>
	 * 
	 * @param a
	 *            number between 0 and the number of segments <br>
	 *            </br>
	 * @return int <br>
	 *         </br>
	 */
	public int getIndexCountInSegment(int i) {
		return (segments.get(i)).getFaceCount();
	}

	// there are just to many casts here. It feels very muddy.
	/**
	 * Returns an array of ints. Use these ints to get the verts of a single
	 * face.<br>
	 * </br>
	 * 
	 * @param the
	 *            segment number<br>
	 *            </br>
	 * @param the
	 *            face number<br>
	 *            </br>
	 * @return int[] of indexes<br>
	 *         </br>
	 */
	public int[] getModelVerticeIndicesInSegment(int i, int num) {
		return ((segments.get(i)).getFace(num)).getVertexIndices();
	}

	/**
	 * Returns an array of ints. Use these ints to get the normals of a single
	 * face.<br>
	 * </br>
	 * 
	 * @param the
	 *            segment number<br>
	 *            </br>
	 * @param the
	 *            face number<br>
	 *            </br>
	 * @return int[] of indexes<br>
	 *         </br>
	 */
	public int[] getNormalVerticeIndicesInSegment(int i, int num) {
		return ((segments.get(i)).getFace(num)).getNormalIndices();
	}

	/**
	 * Returns an array of ints. Use these ints to get the UV's of a single
	 * face.<br>
	 * </br>
	 * 
	 * @param the
	 *            segment number<br>
	 *            </br>
	 * @param the
	 *            face number<br>
	 *            </br>
	 * @return int[] of indexes<br>
	 *         </br>
	 */
	public int[] getTextureVerticeIndicesInSegment(int i, int num) {
		return ((segments.get(i)).getFace(num)).getTextureIndices();
	}

	// public Face getFaceInSegment(int faceIndex) {
	// // debug.println("segmentNumber, indexNumber = " + segmentNumber + " " +
	// // indexNumber);
	// int segmentNumber = 0;
	//
	// while (faceIndex >= getIndexCountInSegment(segmentNumber)) {
	// faceIndex -= getIndexCountInSegment(segmentNumber);
	// segmentNumber++;
	// }
	//		
	// return ((Face) ((Segment) segments.get(i)).getFace(faceIndex));
	// }

	/**
	 * Get's the total number of Verts in the model.<br>
	 * </br>
	 * 
	 * @return an int of the number of verts<br>
	 *         </br>
	 */
	public int getModelVerticesCount() {
		return this.modelVertices.size();
	}

	/**
	 * Get's the total number of Normals in the model. <br>
	 * </br> It can happen that the Normal count is identical to the Vert count.
	 * This will depend on the effecency of the exporter that has been used.<br>
	 * </br> In a situation where the count is identical often there is a
	 * relationship between a certain numbered Normal and the same numbered
	 * Vert.<br>
	 * </br> However this can also be total luck. The correct method of getting
	 * the normal for the correct vert is to go through the ModelSegment to
	 * ModelElement to VertIndex and NormalIndex.<br>
	 * </br>
	 * 
	 * @return an int of the number of normals<br>
	 *         </br>
	 */
	public int getNormalVerticesCount() {
		return this.normalVertices.size();
	}

	/**
	 * Get's the total number of UVs in the model.<br>
	 * </br>
	 * 
	 * @return an int of the number of UV's<br>
	 *         </br>
	 */
	public int getTextureVerticeCount() {
		return this.textureVertices.size();
	}

	/**
	 * Returns a reference to a numbered Vertex. As this is a reference to the
	 * original vertex you can directly manipulate the PVector without having to
	 * set it back.<br>
	 * </br> PVector tmp = model.getVertex(0);<br>
	 * </br> tmp.x += 10;<br>
	 * </br>
	 * 
	 * @param an
	 *            index to the vert<br>
	 *            </br>
	 * @return a PVector<br>
	 *         </br>
	 */
	public PVector getModelVertice(int i) {
		return modelVertices.get(i);
	}

	private int getModelVerticeIndex(PVector v) {
		return modelVertices.indexOf(v);
	}

	/**
	 * Returns a reference to a numbered Normal. As this is a reference to the
	 * original normal you can directly manipulate the PVector without having to
	 * set it back.<br>
	 * </br> PVector tmp = model.getNormal(0);<br>
	 * </br> tmp.mult(-1);<br>
	 * </br>
	 * 
	 * @param an
	 *            index to the normal<br>
	 *            </br>
	 * @return a PVector<br>
	 *         </br>
	 */
	public PVector getNormalVertice(int i) {
		return normalVertices.get(i);
	}

	/**
	 * Returns a reference to a numbered Textured Coordinate. As this is a
	 * reference to the original UV you can directly manipulate the PVector
	 * without having to set it back.<br>
	 * </br> It is important to note that the UV's of an obj model are in a
	 * NORMALIZED space (between 0-1).<br>
	 * </br> Another important issue is that the native processing renderer does
	 * not tile textures that are outside 0-1<br>
	 * </br> This can have the effect of streaking pixel lines at the edges of
	 * the texture.<br>
	 * </br> PVector tmp = model.getUV(0);<br>
	 * </br> tmp.x += 0.01;<br>
	 * </br>
	 * 
	 * @param an
	 *            index to the normal<br>
	 *            </br>
	 * @return a PVector<br>
	 *         </br>
	 */
	public PVector getTextureVertice(int i) {
		return textureVertices.get(i);
	}

	private int getTextureVerticeIndex(PVector v) {
		return textureVertices.indexOf(v);
	}

	/**
	 * Sets the vert at index i to the PVector supplied
	 * 
	 * @param index
	 *            into the vert array
	 * @param A
	 *            supplied PVector
	 */
	public void setModelVertice(int i, PVector vertex) {
		(modelVertices.get(i)).set(vertex);
	}

	/**
	 * Sets the vert at index i to the x,y,z values supplied
	 * 
	 * @param i
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setModelVertice(int i, float x, float y, float z) {
		(modelVertices.get(i)).set(x, y, z);
	}

	/**
	 * Sets the Normal at index i to the PVector supplied
	 * 
	 * @param index
	 *            into the normal array
	 * @param A
	 *            supplied PVector
	 */
	public void setNormalVertice(int i, PVector normal) {
		(normalVertices.get(i)).set(normal);
	}

	/**
	 * Sets the UV at index i to the PVector supplied
	 * 
	 * @param index
	 *            into the uv array
	 * @param A
	 *            supplied PVector
	 */
	public void setTextureVertice(int i, PVector uv) {
		(textureVertices.get(i)).set(uv);
	}


	/**
	 * Used in the loading of obj files and mtl files that come from mtl files.<br>
	 * </br>
	 * 
	 * @param The
	 *            filename. A String containing the location of the obj file.
	 *            The createReader function should take care of finding the file
	 *            in all the usual Processing places.<br>
	 *            </br>
	 * @return a BufferedReader<br>
	 *         </br>
	 */
	private BufferedReader getBufferedReader(String filename) {
		BufferedReader retval = createReader(filename);

		if (retval != null) {
			return retval;
		} else {
			System.err.println("Could not find this file " + filename);
			return null;
		}

	}

	private BufferedReader createReader(String filename){
		return createReader(openInputStream(filename));
	}

	private BufferedReader createReader(InputStream input) {
		InputStreamReader isr = null;
		try {
			isr = new InputStreamReader(input, "UTF-8");
		} catch (UnsupportedEncodingException e) { }  // not gonna happen
		return new BufferedReader(isr);
	}

	private InputStream openInputStream(String s){
		Object obj = null;
		try
		{
			File file = new File(s);
			FileInputStream fileinputstream = new FileInputStream(file);
			if(s.endsWith(".gz"))
			{
				obj = new GZIPInputStream(fileinputstream);
				s = s.substring(0, s.indexOf(".gz"));
			} else
				if(s.endsWith(".zip"))
				{
					obj = new ZipInputStream(fileinputstream);
					s = s.substring(0, s.indexOf(".zip"));
				} else
				{
					obj = new BufferedInputStream(fileinputstream);
				}
		}
		catch(Exception exception)
		{
			System.err.println("Error while opening inputstream: "+ exception.getMessage());  
			obj = null;
		}
		return ((InputStream) (obj));
	}

	public OBJModel clone(){
		OBJModel newClone = new OBJModel(parent);
		newClone.parent = parent;
		newClone.debug = debug;
		newClone.shapeMode = shapeMode;

		newClone.modelVertices = new ArrayList<PVector>();
		for(int i = 0; i < modelVertices.size(); i++){
			newClone.modelVertices.add(modelVertices.get(i).clone());
		}
		newClone.textureVertices = new ArrayList<PVector>();
		for(int i = 0; i < textureVertices.size(); i++){
			newClone.textureVertices.add(textureVertices.get(i).clone());
		}
		newClone.normalVertices = new ArrayList<PVector>();
		for(int i = 0; i < normalVertices.size(); i++){
			newClone.normalVertices.add(normalVertices.get(i).clone());
		}

		newClone.selectedVerticesIndices = new ArrayList<Integer>();
		for(int i = 0; i < selectedVerticesIndices.size(); i++){
			newClone.selectedVerticesIndices.add(selectedVerticesIndices.get(i));
		}
		newClone.selectedTextureVerticesIndices = new ArrayList<Integer>();
		for(int i = 0; i < selectedTextureVerticesIndices.size(); i++){
			newClone.selectedTextureVerticesIndices.add(selectedTextureVerticesIndices.get(i));
		}

		newClone.segments = new ArrayList<Segment>();
		for(int i = 0; i < segments.size(); i++){
			newClone.segments.add(segments.get(i).clone());
		}
		newClone.updateSegments();

		return newClone;
	}

}