import org.meshwarpserver.mwserver.*;

import com.cycling74.max.*;
import com.cycling74.jitter.*;


/**
 * @author maf
 *
 * Handles the creation of a repos matrix with a warpmesh
 */
public class MeshWarpEditor extends MaxObject implements GLCanvas{

	int model_faces_Outlet = 0;
	int model_vertices_Outlet = 1;
	int modelSketch_faces_Outlet = 2;
	int modelSketch_vertices_Outlet = 3;
	int handle3DSketchOutlet = 4;
	int textureSketch_faces_Outlet = 5;
	int textureSketch_vertices_Outlet = 6;
	int handle2DSketchOutlet = 7;
	int infoOutlet = 8;

	JitterMatrix stageMatrixSource;
	boolean showMeshMatrix = false;
	String str = new String();
	Sketcher sw;

	private boolean editDrawFaces, editDrawLines, editDrawPoints;
	private boolean mainDrawFaces, mainDrawLines, mainDrawPoints;
	private SketchCanvas modelFaces, modelVertices, modelSketchFaces, modelSketchVertices, textureSketchFaces, textureSketchVertices, handler3DSketch, handler2DSketch;


	MeshWarpEditor(){
		declareInlets(new int[]{ DataTypes.ALL});
		setInletAssist(new String[] { "messages"});

		declareOutlets(new int[]{ DataTypes.ALL,  DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
		setOutletAssist(new String[] { "model faces", "model vertices", "modelEditor faces", "modelEditor vertices", "3dhandlerEditor", "textureEditor faces", "textureEditor vertices", "2dhandlerEditor"});	

		//		this.declareAttribute("size", "getsize", "size");

		sw = null;
		editDrawFaces = true;
		editDrawLines = false;
		editDrawPoints = false;
		mainDrawFaces = true;
		mainDrawLines = false;
		mainDrawPoints = false;
		modelFaces = new SketchCanvas(this, model_faces_Outlet);
		modelVertices = new SketchCanvas(this, model_vertices_Outlet);
		modelSketchFaces = new SketchCanvas(this, modelSketch_faces_Outlet);
		modelSketchVertices = new SketchCanvas(this, modelSketch_vertices_Outlet);
		handler3DSketch = new SketchCanvas(this, handle3DSketchOutlet);
		textureSketchFaces = new SketchCanvas(this, textureSketch_faces_Outlet);
		textureSketchVertices = new SketchCanvas(this, textureSketch_vertices_Outlet);
		handler2DSketch = new SketchCanvas(this, handle2DSketchOutlet);

		post("MeshWarpServerEditor Build 062");
		
	}

	/****************************************************************
	 * 					Drawing
	 ****************************************************************/

	public void draw(){
		if(sw != null){
			sw.draw3D(modelFaces, mainDrawFaces, false, false);
			sw.draw3D(modelVertices, false, mainDrawLines, mainDrawPoints);
			sw.draw3D(modelSketchVertices, false, editDrawLines, editDrawPoints);
			sw.draw3D(modelSketchFaces, editDrawFaces, false, false);
			sw.draw2D(textureSketchFaces, editDrawFaces, false, false);
			sw.draw2D(textureSketchVertices, false, editDrawLines, editDrawPoints);
		}
		drawHandler();
	}

	/**
	 * this method is called by the GLCanvas Interface
	 */
	public void drawGlCommand(Atom[] command, int outletnumber) {		
		outlet(outletnumber, command);
	}

	/****************************************************************
	 * 					Settings
	 ****************************************************************/

	public void editmode(int mode){
		if(mode == 0){
			editDrawFaces = true;
			editDrawLines = false;
			editDrawPoints = false;
		}
		if(mode == 1){
			editDrawFaces = true;
			editDrawLines = true;
			editDrawPoints = true;
		}
		if(mode == 2){
			editDrawFaces = true;
			editDrawLines = true;
			editDrawPoints = false;
		}
		if(mode == 3){
			editDrawFaces = true;
			editDrawLines = false;
			editDrawPoints = true;
		}
		if(mode == 4){
			editDrawFaces = false;
			editDrawLines = true;
			editDrawPoints = true;
		}
		if(mode == 5){
			editDrawFaces = false;
			editDrawLines = true;
			editDrawPoints = false;
		}
		if(mode == 6){
			editDrawFaces = false;
			editDrawLines = false;
			editDrawPoints = true;
		}
		draw();
	}

	public void drawmode(int mode){
		if(mode == 0){
			mainDrawFaces = true;
			mainDrawLines = false;
			mainDrawPoints = false;
		}
		if(mode == 1){
			mainDrawFaces = true;
			mainDrawLines = true;
			mainDrawPoints = true;
		}
		if(mode == 2){
			mainDrawFaces = true;
			mainDrawLines = true;
			mainDrawPoints = false;
		}
		if(mode == 3){
			mainDrawFaces = true;
			mainDrawLines = false;
			mainDrawPoints = true;
		}
		if(mode == 4){
			mainDrawFaces = false;
			mainDrawLines = true;
			mainDrawPoints = true;
		}
		if(mode == 5){
			mainDrawFaces = false;
			mainDrawLines = true;
			mainDrawPoints = false;
		}
		if(mode == 6){
			mainDrawFaces = false;
			mainDrawLines = false;
			mainDrawPoints = true;
		}
		draw();
	}

	public void setlinecolor(float r, float g, float b, float a){
		if(sw != null){
			float[] color = {r, g, b, a};
			sw.setLineColor(color);
		}
		draw();
	}

	public void setpointcolor(float r, float g, float b, float a){
		if(sw != null){
			float[] color = {r, g, b, a};
			sw.setPointColor(color);
		}
		draw();
	}

	public void setselectedpointcolor(float r, float g, float b, float a){
		if(sw != null){
			float[] color = {r, g, b, a};
			sw.setSelectedPointColor(color);
		}
		draw();
	}

	public void setfacecolor(float r, float g, float b, float a){
		if(sw != null){
			float[] color = {r, g, b, a};
			sw.setFaceEditColor(color);
		}
		draw();
	}
	
	public void color(float r, float g, float b, float a){
		if(sw != null){
			float[] color = {r, g, b, a};
			sw.setFaceMainColor(color);
		}
		draw();
	}
	
	public void setfacesubdivision(int subDivision){
		if(sw != null){
			sw.setFaceSubdivision(subDivision);
		}
		draw();
	}

	public void info() {
	}

	/****************************************************************
	 * 					2D and 3D Handler
	 ****************************************************************/

	/*
	 * this method is to set the scaling values of the 3D handler
	 * and NOT the internal model object
	 */
	public void scale(float x, float y, float z) {
		if(sw != null){
			sw.scale3DHandler(x, y, z);
			sw.scale2DHandler(x, y, z);
		}
		drawHandler();
	}

	public void sethandler2grabbing(){
		if(sw != null){
			sw.set3DHandlerGrabbing();	
			sw.set2DHandlerGrabbing();	
		}
		drawHandler();
	}

	public void sethandler2rotating(){
		if(sw != null){
			sw.set3DHandlerRotating();	
			sw.set2DHandlerRotating();	
		}
		drawHandler();
	}

	public void sethandler2scaling(){
		if(sw != null){
			sw.set3DHandlerScaling();	
			sw.set2DHandlerScaling();	
		}
		drawHandler();
	}

	public void drawHandler(){
		if(sw != null){
			sw.draw3DHandler(handler3DSketch);	
			sw.draw2DHandler(handler2DSketch);	
		}
	}

	/****************************************************************
	 * 					Selections
	 ****************************************************************/


	public void selectall(){
		if(sw != null){
			sw.selectAll();
		}
		draw();
	}

	public void select(int useX, int useY, int useZ, float x1, float y1, float z1, float x2, float y2, float z2) {
		if(sw != null){
			sw.selectModel(useX, useY, useZ, x1, y1, z1, x2, y2, z2);
		}
		draw();
	}

	public void unselect(int useX, int useY, int useZ, float x1, float y1, float z1, float x2, float y2, float z2) {
		if(sw != null){
			sw.unSelectModel(useX, useY, useZ, x1, y1, z1, x2, y2, z2);
		}
		draw();
	}

	public void textureselect(float x1, float y1, float x2, float y2) {
		if(sw != null){
			sw.selectTexture(x1, y1, x2, y2);
		}
		draw();
	}

	public void textureunselect(float x1, float y1, float x2, float y2) {
		if(sw != null){
			sw.unSelectTexture(x1, y1, x2, y2);
		}
		draw();
	}
	
	/****************************************************************
	 * 					3D (Model) Transformations
	 ****************************************************************/

	public void applyTranslationPosition(float xDiff, float yDiff, float zDiff) {
		if(sw != null){
			sw.modelTranslationPosition(xDiff, yDiff, zDiff);
		}
		draw();
	}
	
	public void applyTranslationRotation(float xDiff, float yDiff, float zDiff) {
		if(sw != null){
			sw.modelTranslationRotation(xDiff, yDiff, zDiff);
		}
		draw();
	}
	
	public void applyTranslationScale(float xDiff, float yDiff, float zDiff) {
		if(sw != null){
			sw.modelTranslationScale(xDiff, yDiff, zDiff);
		}
		draw();
	}

	public void keygrabbing(float xDiff, float yDiff, float zDiff) {
		if(sw != null){
			sw.modelKeyGrabbing(xDiff, yDiff, zDiff);
		}
		draw();
	}

	public void startgrabbing(int useX, int useY, int useZ, float x1, float y1, float z1, float x2, float y2, float z2) {
		if(sw != null){
			sw.modelGrabbingStart(useX, useY, useZ, x1, y1, z1, x2, y2, z2);
		}
	}

	public void grabbing(int useX, int useY, int useZ, float x1, float y1, float z1, float x2, float y2, float z2) {
		if(sw != null){
			sw.modelGrabbing(useX, useY, useZ, x1, y1, z1, x2, y2, z2);
		}
		draw();
	}

	public void stopgrabbing() {
		if(sw != null){
			sw.modelGrabbingStop();
		}
	}

	public void startscaling(int useX, int useY, int useZ, float x1, float y1, float z1, float x2, float y2, float z2) {
		if(sw != null){
			sw.modelScalingStart(useX, useY, useZ, x1, y1, z1, x2, y2, z2);
		}
	}

	public void scaling(int useX, int useY, int useZ, float x1, float y1, float z1, float x2, float y2, float z2) {
		if(sw != null){
			sw.modelScaling(useX, useY, useZ, x1, y1, z1, x2, y2, z2);
		}
		draw();
	}

	public void stopscaling() {
		if(sw != null){
			sw.modelScalingStop();
		}
	}

	public void startrotating(int useX, int useY, int useZ, float x1, float y1, float z1, float x2, float y2, float z2) {
		if(sw != null){
			sw.modelRotatingStart(useX, useY, useZ, x1, y1, z1, x2, y2, z2);
		}
	}

	public void rotating(int useX, int useY, int useZ, float x1, float y1, float z1, float x2, float y2, float z2) {
		if(sw != null){
			sw.modelRotating(useX, useY, useZ, x1, y1, z1, x2, y2, z2);
		}
		draw();
	}

	public void stoprotating() {
		if(sw != null){
			sw.modelRotatingStop();
		}
	}

	/****************************************************************
	 * 					2D (Texture) Transformations
	 ****************************************************************/


	public void tex_startgrabbing(float x1, float y1, float x2, float y2) {
		if(sw != null){
			sw.textureGrabbingStart(x1, y1, x2, y2);
		}
	}

	public void tex_grabbing(float x1, float y1, float x2, float y2) {
		if(sw != null){
			sw.textureGrabbing(x1, y1, x2, y2);
		}
		draw();
	}

	public void tex_stopgrabbing() {
		if(sw != null){
			sw.textureGrabbingStop();
		}
	}

	public void tex_startscaling(float x1, float y1, float x2, float y2) {
		if(sw != null){
			sw.textureScalingStart(x1, y1, x2, y2);
		}
	}

	public void tex_scaling(float x1, float y1, float x2, float y2) {
		if(sw != null){
			sw.textureScaling(x1, y1, x2, y2);
		}
		draw();
	}

	public void tex_stopscaling() {
		if(sw != null){
			sw.textureScalingStop();
		}
	}

	public void tex_startrotating(float x1, float y1, float x2, float y2) {
		if(sw != null){
			sw.textureRotatingStart(x1, y1, x2, y2);
		}
	}

	public void tex_rotating(float x1, float y1, float x2, float y2) {
		if(sw != null){
			sw.textureRotating(x1, y1, x2, y2);
		}
		draw();
	}

	public void tex_stoprotating() {
		if(sw != null){
			sw.textureRotatingStop();
		}
	}

	/****************************************************************
	 * 					IO / Un-Do
	 ****************************************************************/

	public void undo(){
		if(sw != null){
			sw.undo();
		}
		draw();
	}

	public void redo(){
		if(sw != null){
			sw.redo();
		}
		draw();
	}

	public void verbose(int i){
		if(sw != null){
			sw.verbose(i);
		}
	}

	public void read(String path) {
		if(path.indexOf("/") >= 0){
			post("read obj from: " + path);
			sw = new Sketcher();
			sw.load(path);
			outlet(infoOutlet, new Atom[]{Atom.newAtom("read"),Atom.newAtom(path)});
		}else{
			errorPost("while reading obj", "invalid path: " + path);
		}
		draw();
	}

	public void saveas(String s) {
		if(s.indexOf("/") >= 0){
			String path = s.substring(s.indexOf("/"));
			post("saving obj to: " + path);
			sw.saveAs(path);
			Atom[] out = new Atom[2];
			outlet(infoOutlet, new Atom[]{Atom.newAtom("saveas"),Atom.newAtom(s)});
		}else{
			errorPost("while saving obj", "invalid path: " + s);
		}
	}
	
	public void save() {
		if(sw != null){
			post("saving obj");
			sw.save();
			outlet(infoOutlet, Atom.newAtom("save"));
		}else{
			errorPost("no obj to save", "");
		}
	}

	private void errorPost(String title, String message){
		post("MeshWarp Error: " + title + " : " + message);
	}

	/*
	public void list(Atom[] args) {
		Atom a;
		for(int i = 0; i < args.length; i++)
		{
			a = args[i]; 
			if(a.isFloat())
				System.out.print(" "+a.getFloat());
			else if(a.isInt()) 
				System.out.print(" "+a.getInt());
			else if(a.isString())
				System.out.print(" "+a.getString());
		}
		System.out.println();
	}
	*/
	
}

