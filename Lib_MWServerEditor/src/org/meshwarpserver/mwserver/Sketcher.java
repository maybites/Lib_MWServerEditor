package org.meshwarpserver.mwserver;

import com.cycling74.max.*;
import java.util.ArrayList;

import org.meshwarpserver.objloader.*;
import org.meshwarpserver.util.*;

public class Sketcher implements SketcherCommands {

	OBJModel model;

	public UnDoFileManager fileManager;

	public static int HANDLER_NONE = 0;
	public static int HANDLER_ROTATING = 1;
	public static int HANDLER_GRABBING = 2;
	public static int HANDLER_SCALING = 3;

	private static int HANDLING_NOAXIS = 0;
	private static int HANDLING_ALLAXIS = 1;
	private static int HANDLING_XAXIS = 2;
	private static int HANDLING_YAXIS = 3;
	private static int HANDLING_ZAXIS = 4;

	private SketchCanvas glCommandCanvas;
	private float[] bkgFaceColor = { 1, 1, 1, .3f };
	private float[] bkgLineColor = { 1, 1, 1, 1 };
	private float[] faceMainColor = { 1, 1, 1, 1 };
	private float[] faceEditColor = { 1, 1, 1, .5f };
	private float[] lineColor = { 1, 0, 0, 1 };
	private float[] pointColor = { 0, 1, 0, 1 };
	private float[] selectedPointColor = { 0, 0, 1, 1 };
	private float pointSize = 5.f;

	private float[] axisColorX = { 1, 1, 0, 1 };
	private float[] axisColorY = { 0, 1, 1, 1 };
	private float[] axisColorZ = { 1, 0, 1, 1 };

	private float _scale3DHandlerX = 1.f;
	private float _scale3DHandlerY = 1.f;
	private float _scale3DHandlerZ = 1.f;

	private float _scale2DHandlerX = 1.f;
	private float _scale2DHandlerY = 1.f;
	private float _scale2DHandlerZ = 0.f;

	private PVector center3DHandler, center2DHandler;

	private OBJModel referenceModel;

	private int handling3DType = HANDLING_NOAXIS;
	private int handler3DMode = HANDLER_GRABBING;

	private int handling2DType = HANDLING_NOAXIS;
	private int handler2DMode = HANDLER_GRABBING;

	private boolean maniplutationFlag = false;

	private float textureShiftX = -0.5f; // there is another mirrored set in the Face-Object!!
	private float textureShiftY = -0.5f;

	public Sketcher() {
		model = new OBJModel(this);
		fileManager = new UnDoFileManager(10);
		model.debug.enabled = true;
	}

	/****************************************************************
	 * FILES / UN-DO
	 ****************************************************************/

	public void undo() {
		model = fileManager.unDo(model);
		center3DHandler = model.getSelected3DAverageCenter();
		center2DHandler = model.getSelected2DAverageCenter();
	}

	public void redo() {
		model = fileManager.reDo(model);
		center3DHandler = model.getSelected3DAverageCenter();
		center2DHandler = model.getSelected2DAverageCenter();
	}

	public void load(String filename) {
		fileManager.load(model, filename);
	}

	public void saveAs(String filename) {
		fileManager.saveAs(model, filename);
	}

	public void save() {
		fileManager.save(model);
	}

	/****************************************************************
	 * 3D Transformations
	 ****************************************************************/

	public void modelRotatingStart(int useX, int useY, int useZ, float x1,
			float y1, float z1, float x2, float y2, float z2) {
		handling3DType = get3DHandlingType(useX, useY, useZ, x2, y2, z2);
		referenceModel = model.clone();
	}

	public void modelRotating(int useX, int useY, int useZ, float x1, float y1,
			float z1, float x2, float y2, float z2) {
		PVector origVertex, transVertex;
		if (handling3DType != HANDLING_NOAXIS) {
			for (int i = 0; i < referenceModel.getSelectedModelVerticeCount(); i++) {
				origVertex = model.getSelectedModelVertice(i);
				transVertex = referenceModel.getSelectedModelVertice(i);
				PVector dist = PVector.sub(transVertex, center3DHandler);
				if (useZ == 0
						&& (handling3DType == HANDLING_XAXIS || handling3DType == HANDLING_YAXIS)) {
					dist.rotZ((x1 - x2) + (y1 - y2));
				}
				if (useY == 0
						&& (handling3DType == HANDLING_XAXIS || handling3DType == HANDLING_ZAXIS)) {
					dist.rotY((x1 - x2) + (z1 - z2));
				}
				if (useX == 0
						&& (handling3DType == HANDLING_ZAXIS || handling3DType == HANDLING_YAXIS)) {
					dist.rotX((z1 - z2) + (y1 - y2));
				}
				dist.add(center3DHandler);
				origVertex.set(dist);
			}
			maniplutationFlag = true;
		}
		center3DHandler = model.getSelected3DAverageCenter();
		model.updateSegments();
	}

	public void modelRotatingStop() {
		if (maniplutationFlag)
			fileManager.newDo(model);
		maniplutationFlag = false;
	}

	public void modelScalingStart(int useX, int useY, int useZ, float x1,
			float y1, float z1, float x2, float y2, float z2) {
		handling3DType = get3DHandlingType(useX, useY, useZ, x2, y2, z2);
		referenceModel = model.clone();
	}

	public void modelScaling(int useX, int useY, int useZ, float x1, float y1,
			float z1, float x2, float y2, float z2) {
		PVector origVertex, transVertex;
		if (handling3DType != HANDLING_NOAXIS) {
			for (int i = 0; i < referenceModel.getSelectedModelVerticeCount(); i++) {
				origVertex = model.getSelectedModelVertice(i);
				transVertex = referenceModel.getSelectedModelVertice(i);
				PVector dist = PVector.sub(transVertex, center3DHandler);
				if (handling3DType == HANDLING_XAXIS) {
					dist.x = dist.x * (1 + (x1 - x2));
				}else if (handling3DType == HANDLING_YAXIS) {
					dist.y = dist.y * (1 + (y1 - y2));
				}else if (handling3DType == HANDLING_ZAXIS) {
					dist.z = dist.z * (1 + (z1 - z2));
				}else if (handling3DType == HANDLING_ALLAXIS) {
					dist.mult(1 + (x1 - x2));
				}
				dist.add(center3DHandler);
				origVertex.set(dist);
			}
			maniplutationFlag = true;
		}
		center3DHandler = model.getSelected3DAverageCenter();
		model.updateSegments();
	}

	public void modelScalingStop() {
		if (maniplutationFlag)
			fileManager.newDo(model);
		maniplutationFlag = false;
	}

	public void modelGrabbingStart(int useX, int useY, int useZ, float x1,
			float y1, float z1, float x2, float y2, float z2) {
		handling3DType = get3DHandlingType(useX, useY, useZ, x2, y2, z2);
		referenceModel = model.clone();
	}

	public void modelGrabbing(int useX, int useY, int useZ, float x1, float y1,
			float z1, float x2, float y2, float z2) {
		PVector origVertex, transVertex;
		if (handling3DType != HANDLING_NOAXIS) {
			for (int i = 0; i < referenceModel.getSelectedModelVerticeCount(); i++) {
				origVertex = model.getSelectedModelVertice(i);
				transVertex = referenceModel.getSelectedModelVertice(i);
				origVertex.set(transVertex);
				if (handling3DType == HANDLING_XAXIS) {
					origVertex.add(new PVector(x1 - x2, 0.f, 0.f));
				} else if (handling3DType == HANDLING_YAXIS) {
					origVertex.add(new PVector(0.f, y1 - y2, 0.f));
				} else if (handling3DType == HANDLING_ZAXIS) {
					origVertex.add(new PVector(0.f, 0.f, z1 - z2));
				} else if (handling3DType == HANDLING_ALLAXIS) {
					origVertex.add(new PVector(x1 - x2, y1 - y2, z1 - z2));
				}
			}
			maniplutationFlag = true;
		}
		center3DHandler = model.getSelected3DAverageCenter();
		model.updateSegments();
	}

	public void modelGrabbingStop() {
		if (maniplutationFlag)
			fileManager.newDo(model);
		maniplutationFlag = false;
	}

	public void modelKeyGrabbing(float xDiff, float yDiff, float zDiff) {
		referenceModel = model.clone();
		PVector origVertex, transVertex;
		for (int i = 0; i < referenceModel.getSelectedModelVerticeCount(); i++) {
			origVertex = model.getSelectedModelVertice(i);
			transVertex = referenceModel.getSelectedModelVertice(i);
			origVertex.set(transVertex);
			origVertex.add(new PVector(xDiff, yDiff, zDiff));
		}
		center3DHandler = model.getSelected3DAverageCenter();
		model.updateSegments();
		fileManager.newDo(model);
		maniplutationFlag = false;
	}
	
	public void modelTranslationPosition(float xDiff, float yDiff, float zDiff) {
		referenceModel = model.clone();
		PVector origVertex, transVertex;
		for (int i = 0; i < referenceModel.getModelVerticesCount(); i++) {
			origVertex = model.getModelVertice(i);
			transVertex = referenceModel.getModelVertice(i);
			origVertex.set(transVertex);
			origVertex.add(new PVector(xDiff, yDiff, zDiff));
		}
		center3DHandler = model.getSelected3DAverageCenter();
		model.updateSegments();
		fileManager.newDo(model);
		maniplutationFlag = false;
	}

	public void modelTranslationRotation(float xDiff, float yDiff, float zDiff) {
		referenceModel = model.clone();
		PVector origVertex, transVertex;
		for (int i = 0; i < referenceModel.getModelVerticesCount(); i++) {
			origVertex = model.getModelVertice(i);
			transVertex = referenceModel.getModelVertice(i);
			origVertex.set(transVertex);
			origVertex.rotX(xDiff);
			origVertex.rotY(yDiff);
			origVertex.rotZ(zDiff);
		}
		center3DHandler = model.getSelected3DAverageCenter();
		model.updateSegments();
		fileManager.newDo(model);
		maniplutationFlag = false;
	}

	public void modelTranslationScale(float xDiff, float yDiff, float zDiff) {
		referenceModel = model.clone();
		PVector origVertex, transVertex;
		for (int i = 0; i < referenceModel.getModelVerticesCount(); i++) {
			origVertex = model.getModelVertice(i);
			transVertex = referenceModel.getModelVertice(i);
			origVertex.set(transVertex);
			origVertex.mult(new PVector(xDiff, yDiff, zDiff));
		}
		center3DHandler = model.getSelected3DAverageCenter();
		model.updateSegments();
		fileManager.newDo(model);
		maniplutationFlag = false;
	}

	private int get3DHandlingType(int useX, int useY, int useZ, float x,
			float y, float z) {
		float xHandlingCenter = 0.f;
		float yHandlingCenter = 0.f;
		float zHandlingCenter = 0.f;
		if (useX == 1) {
			xHandlingCenter = center3DHandler.x + _scale3DHandlerX * .5f;
			yHandlingCenter = center3DHandler.y;
			zHandlingCenter = 0;
			if (liesIn3DHandlingSpace(xHandlingCenter, yHandlingCenter,
					zHandlingCenter, x, y, z))
				return HANDLING_XAXIS;
			zHandlingCenter = center3DHandler.z;
			yHandlingCenter = 0.f;
			if (liesIn3DHandlingSpace(xHandlingCenter, yHandlingCenter,
					zHandlingCenter, x, y, z))
				return HANDLING_XAXIS;
		}
		if (useY == 1) {
			xHandlingCenter = 0.f;
			yHandlingCenter = center3DHandler.y + _scale3DHandlerY * .5f;
			zHandlingCenter = center3DHandler.z;
			if (liesIn3DHandlingSpace(xHandlingCenter, yHandlingCenter,
					zHandlingCenter, x, y, z))
				return HANDLING_YAXIS;
			xHandlingCenter = center3DHandler.x;
			zHandlingCenter = 0.f;
			if (liesIn3DHandlingSpace(xHandlingCenter, yHandlingCenter,
					zHandlingCenter, x, y, z))
				return HANDLING_YAXIS;
		}
		if (useZ == 1) {
			xHandlingCenter = 0.f;
			yHandlingCenter = center3DHandler.y;
			zHandlingCenter = center3DHandler.z + _scale3DHandlerZ * .5f;
			if (liesIn3DHandlingSpace(xHandlingCenter, yHandlingCenter,
					zHandlingCenter, x, y, z))
				return HANDLING_ZAXIS;
			xHandlingCenter = center3DHandler.x;
			yHandlingCenter = 0.f;
			if (liesIn3DHandlingSpace(xHandlingCenter, yHandlingCenter,
					zHandlingCenter, x, y, z))
				return HANDLING_ZAXIS;
		}
		xHandlingCenter = 0.f;
		yHandlingCenter = 0.f;
		zHandlingCenter = 0.f;
		if (useX == 1 && useY == 1) {
			xHandlingCenter = center3DHandler.x;
			yHandlingCenter = center3DHandler.y;
		} else if (useX == 1 && useZ == 1) {
			xHandlingCenter = center3DHandler.x;
			zHandlingCenter = center3DHandler.z;
		} else if (useZ == 1 && useY == 1) {
			zHandlingCenter = center3DHandler.z;
			yHandlingCenter = center3DHandler.y;
		}
		if (liesIn3DHandlingSpace(xHandlingCenter, yHandlingCenter,
				zHandlingCenter, x, y, z))
			return HANDLING_ALLAXIS;
		return HANDLING_NOAXIS;
	}

	private boolean liesIn3DHandlingSpace(float centerX, float centerY,
			float centerZ, float x, float y, float z) {
		if (centerZ + _scale3DHandlerZ * .1f > z
				&& centerZ - _scale3DHandlerZ * .1f < z
				&& centerY + _scale3DHandlerY * .1f > y
				&& centerY - _scale3DHandlerY * .1f < y
				&& centerX + _scale3DHandlerX * .1f > x
				&& centerX - _scale3DHandlerX * .1f < x)
			return true;
		return false;
	}

	/****************************************************************
	 * 2D Texture Transformations
	 ****************************************************************/

	public void textureRotatingStart(float x1, float y1, float x2, float y2) {
		handling2DType = get2DHandlingType(x2, y2);
		referenceModel = model.clone();
	}

	public void textureRotating(float x1, float y1, float x2, float y2) {
		PVector origVertex, transVertex;
		for (int i = 0; i < referenceModel.getSelectedTextureVerticeCount(); i++) {
			origVertex = model.getSelectedTextureVertice(i);
			transVertex = referenceModel.getSelectedTextureVertice(i);
			PVector dist = PVector.sub(transVertex, center2DHandler);
			dist.rotZ((x1 - x2) + (y1 - y2));
			dist.add(center2DHandler);
			origVertex.set(dist);
		}
		maniplutationFlag = true;
		update2DHandler();
		model.updateSegments();
	}

	public void textureRotatingStop() {
		if (maniplutationFlag)
			fileManager.newDo(model);
		maniplutationFlag = false;
	}

	public void textureScalingStart(float x1, float y1, float x2, float y2) {
		handling2DType = get2DHandlingType(x2, y2);
		referenceModel = model.clone();
	}

	public void textureScaling(float x1, float y1, float x2, float y2) {
		PVector origVertex, transVertex;
		if (handling2DType == HANDLING_XAXIS) {
			for (int i = 0; i < referenceModel.getSelectedTextureVerticeCount(); i++) {
				origVertex = model.getSelectedTextureVertice(i);
				transVertex = referenceModel.getSelectedTextureVertice(i);
				PVector dist = PVector.sub(transVertex, center2DHandler);
				dist.x = dist.x * (1 + (x1 - x2));
				dist.add(center2DHandler);
				origVertex.set(dist);
			}
			maniplutationFlag = true;
		}
		if (handling2DType == HANDLING_YAXIS) {
			for (int i = 0; i < referenceModel.getSelectedTextureVerticeCount(); i++) {
				origVertex = model.getSelectedTextureVertice(i);
				transVertex = referenceModel.getSelectedTextureVertice(i);
				PVector dist = PVector.sub(transVertex, center2DHandler);
				dist.y = dist.y * (1 + (y1 - y2));
				dist.add(center2DHandler);
				origVertex.set(dist);
			}
			maniplutationFlag = true;
		}
		if (handling2DType == HANDLING_ALLAXIS) {
			for (int i = 0; i < referenceModel.getSelectedTextureVerticeCount(); i++) {
				origVertex = model.getSelectedTextureVertice(i);
				transVertex = referenceModel.getSelectedTextureVertice(i);
				PVector dist = PVector.sub(transVertex, center2DHandler);
				dist.mult(1 + (x1 - x2));
				dist.add(center2DHandler);
				origVertex.set(dist);
			}
			maniplutationFlag = true;
		}
		update2DHandler();
		model.updateSegments();
	}

	public void textureScalingStop() {
		if (maniplutationFlag)
			fileManager.newDo(model);
		maniplutationFlag = false;
	}

	public void textureGrabbingStart(float x1, float y1, float x2, float y2) {
		handling2DType = get2DHandlingType(x2, y2);
		referenceModel = model.clone();
	}

	public void textureGrabbing(float x1, float y1, float x2, float y2) {
		PVector origVertex, transVertex;
		if (handling2DType == HANDLING_XAXIS) {
			for (int i = 0; i < referenceModel.getSelectedTextureVerticeCount(); i++) {
				origVertex = model.getSelectedTextureVertice(i);
				transVertex = referenceModel.getSelectedTextureVertice(i);
				origVertex.set(transVertex);
				origVertex.add(new PVector(x1 - x2, 0.f, 0.f));
			}
			maniplutationFlag = true;
		}
		if (handling2DType == HANDLING_YAXIS) {
			for (int i = 0; i < referenceModel.getSelectedTextureVerticeCount(); i++) {
				origVertex = model.getSelectedTextureVertice(i);
				transVertex = referenceModel.getSelectedTextureVertice(i);
				origVertex.set(transVertex);
				origVertex.add(new PVector(0.f, y1 - y2, 0.f));
			}
			maniplutationFlag = true;
		}
		if (handling2DType == HANDLING_ALLAXIS) {
			for (int i = 0; i < referenceModel.getSelectedTextureVerticeCount(); i++) {
				origVertex = model.getSelectedTextureVertice(i);
				transVertex = referenceModel.getSelectedTextureVertice(i);
				origVertex.set(transVertex);
				origVertex.add(new PVector(x1 - x2, y1 - y2, 0.f));
			}
			maniplutationFlag = true;
		}
		update2DHandler();
		model.updateSegments();
	}

	public void textureGrabbingStop() {
		if (maniplutationFlag)
			fileManager.newDo(model);
		maniplutationFlag = false;
	}

	private int get2DHandlingType(float x, float y) {
		float xHandlingCenter = 0.f;
		float yHandlingCenter = 0.f;
		xHandlingCenter = center2DHandler.x + _scale2DHandlerX * .5f;
		yHandlingCenter = center2DHandler.y;
		if (liesIn2DHandlingSpace(xHandlingCenter, yHandlingCenter, x-textureShiftX, y-textureShiftY))
			return HANDLING_XAXIS;
		yHandlingCenter = center2DHandler.y + _scale2DHandlerY * .5f;
		xHandlingCenter = center2DHandler.x;
		if (liesIn2DHandlingSpace(xHandlingCenter, yHandlingCenter, x-textureShiftX, y-textureShiftY))
			return HANDLING_YAXIS;
		yHandlingCenter = center2DHandler.y;
		xHandlingCenter = center2DHandler.x;
		if (liesIn2DHandlingSpace(xHandlingCenter, yHandlingCenter, x-textureShiftX, y-textureShiftY))
			return HANDLING_ALLAXIS;
		return HANDLING_NOAXIS;
	}

	private boolean liesIn2DHandlingSpace(float centerX, float centerY,
			float x, float y) {
		if (centerY + _scale2DHandlerY * .1f > y
				&& centerY - _scale2DHandlerY * .1f < y
				&& centerX + _scale2DHandlerX * .1f > x
				&& centerX - _scale2DHandlerX * .1f < x)
			return true;
		return false;
	}

	/****************************************************************
	 * Selections
	 ****************************************************************/

	public void selectAll() {
		if (model.getSelectedModelVerticeCount() == model
				.getModelVerticesCount()) {
			model.unselectAllVertices();
			center3DHandler = model.getSelected3DAverageCenter();
			update2DHandler();
			fileManager.newDo(model);
		} else {
			model.selectAllVertices();
			center3DHandler = model.getSelected3DAverageCenter();
			update2DHandler();
			fileManager.newDo(model);
		}
	}

	public void selectModel(int useX, int useY, int useZ, float x1, float y1,
			float z1, float x2, float y2, float z2) {
		if (model.selectModelVertices(useX, useY, useZ, x1, y1, z1, x2, y2, z2) > 0)
			fileManager.newDo(model);
		center3DHandler = model.getSelected3DAverageCenter();
		update2DHandler();
	}

	public void unSelectModel(int useX, int useY, int useZ, float x1, float y1,
			float z1, float x2, float y2, float z2) {
		if (model.unSelectModelVertices(useX, useY, useZ, x1, y1, z1, x2, y2,
				z2) > 0)
			fileManager.newDo(model);
		center3DHandler = model.getSelected3DAverageCenter();
		center2DHandler = model.getSelected2DAverageCenter();
	}

	public void selectTexture(float x1, float y1, float x2, float y2) {
		if (model.selectTextureVertices(x1-textureShiftX, y1-textureShiftY, x2-textureShiftX, y2-textureShiftY) > 0)
			fileManager.newDo(model);
		center3DHandler = model.getSelected3DAverageCenter();
		update2DHandler();
	}

	public void unSelectTexture(float x1, float y1, float x2, float y2) {
		if (model.unSelectTextureVertices(x1-textureShiftX, y1-textureShiftY, x2-textureShiftX, y2-textureShiftY) > 0)
			fileManager.newDo(model);
		center3DHandler = model.getSelected3DAverageCenter();
	}
	
	private void update2DHandler(){
		center2DHandler = model.getSelected2DAverageCenter();
	}


	/****************************************************************
	 * 3D Drawing
	 ****************************************************************/

	public void draw3D(SketchCanvas canvas) {
		glCommandCanvas = canvas;
		command(GLCommands.sketch_reset());
		draw3DModel(faceMainColor);
	}

	public void draw3D(SketchCanvas canvas, boolean showFaces,
			boolean showLines, boolean showPoints) {
		glCommandCanvas = canvas;
		command(GLCommands.sketch_reset());
		if (showFaces) {
			draw3DModel(faceMainColor);
		}
		if (showLines) {
			draw3DLines();
		}
		if (showPoints) {
			draw3DPoints();
			draw3DSelectedPoints();
		}
	}

	public void draw3DHandler(SketchCanvas canvas) {
		glCommandCanvas = canvas;
		command(GLCommands.sketch_reset());
		if (handler3DMode == HANDLER_GRABBING) {
			draw3DGrabbingHandler();
		}
		if (handler3DMode == HANDLER_ROTATING) {
			draw3DRotatingHandler();
		}
		if (handler3DMode == HANDLER_SCALING) {
			draw3DScalingHandler();
		}
	}

	private void draw3DGrabbingHandler() {
		if (model.getSelectedModelVerticeCount() > 0) {
			command(GLCommands.sketch_glcolor(axisColorX));
			command(GLCommands.sketch_moveto(center3DHandler.x,
					center3DHandler.y, center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .5f, center3DHandler.y,
					center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .5f, center3DHandler.y
					- _scale3DHandlerY * .05f, center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .6f, center3DHandler.y,
					center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .5f, center3DHandler.y
					+ _scale3DHandlerY * .05f, center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .5f, center3DHandler.y,
					center3DHandler.z));

			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .5f, center3DHandler.y,
					center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .5f, center3DHandler.y,
					center3DHandler.z - _scale3DHandlerZ * .05f));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .6f, center3DHandler.y,
					center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .5f, center3DHandler.y,
					center3DHandler.z + _scale3DHandlerZ * .05f));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .5f, center3DHandler.y,
					center3DHandler.z));

			command(GLCommands.sketch_glcolor(axisColorY));
			command(GLCommands.sketch_moveto(center3DHandler.x,
					center3DHandler.y, center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .5f,
					center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .5f,
					center3DHandler.z - _scale3DHandlerZ * .05f));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .6f,
					center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .5f,
					center3DHandler.z + _scale3DHandlerZ * .05f));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .5f,
					center3DHandler.z));

			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .5f,
					center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					- _scale3DHandlerX * .05f, center3DHandler.y
					+ _scale3DHandlerY * .5f, center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .6f,
					center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .05f, center3DHandler.y
					+ _scale3DHandlerY * .5f, center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .5f,
					center3DHandler.z));

			command(GLCommands.sketch_glcolor(axisColorZ));
			command(GLCommands.sketch_moveto(center3DHandler.x,
					center3DHandler.y, center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y, center3DHandler.z + _scale3DHandlerZ
							* .5f));
			command(GLCommands.sketch_lineto(center3DHandler.x
					- _scale3DHandlerX * .05f, center3DHandler.y,
					center3DHandler.z + _scale3DHandlerZ * .5f));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y, center3DHandler.z + _scale3DHandlerZ
							* .6f));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .05f, center3DHandler.y,
					center3DHandler.z + _scale3DHandlerZ * .5f));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y, center3DHandler.z + _scale3DHandlerZ
							* .5f));

			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y, center3DHandler.z + _scale3DHandlerZ
							* .5f));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .05f,
					center3DHandler.z + _scale3DHandlerZ * .5f));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y, center3DHandler.z + _scale3DHandlerZ
							* .6f));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y - _scale3DHandlerY * .05f,
					center3DHandler.z + _scale3DHandlerZ * .5f));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y, center3DHandler.z + _scale3DHandlerZ
							* .5f));
		}
	}

	private void draw3DScalingHandler() {
		if (model.getSelectedModelVerticeCount() > 0) {
			command(GLCommands.sketch_glcolor(axisColorX));
			command(GLCommands.sketch_moveto(center3DHandler.x,
					center3DHandler.y, center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .5f, center3DHandler.y,
					center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .5f, center3DHandler.y
					- _scale3DHandlerY * .05f, center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .6f, center3DHandler.y
					- _scale3DHandlerY * .05f, center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .6f, center3DHandler.y
					+ _scale3DHandlerY * .05f, center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .5f, center3DHandler.y
					+ _scale3DHandlerY * .05f, center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .5f, center3DHandler.y,
					center3DHandler.z));

			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .5f, center3DHandler.y,
					center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .5f, center3DHandler.y,
					center3DHandler.z - _scale3DHandlerZ * .05f));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .6f, center3DHandler.y,
					center3DHandler.z - _scale3DHandlerZ * .05f));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .6f, center3DHandler.y,
					center3DHandler.z + _scale3DHandlerZ * .05f));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .5f, center3DHandler.y,
					center3DHandler.z + _scale3DHandlerZ * .05f));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .5f, center3DHandler.y,
					center3DHandler.z));

			command(GLCommands.sketch_glcolor(axisColorY));
			command(GLCommands.sketch_moveto(center3DHandler.x,
					center3DHandler.y, center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .5f,
					center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .5f,
					center3DHandler.z - _scale3DHandlerZ * .05f));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .6f,
					center3DHandler.z - _scale3DHandlerZ * .05f));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .6f,
					center3DHandler.z + _scale3DHandlerZ * .05f));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .5f,
					center3DHandler.z + _scale3DHandlerZ * .05f));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .5f,
					center3DHandler.z));

			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .5f,
					center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					- _scale3DHandlerX * .05f, center3DHandler.y
					+ _scale3DHandlerY * .5f, center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					- _scale3DHandlerX * .05f, center3DHandler.y
					+ _scale3DHandlerY * .6f, center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .05f, center3DHandler.y
					+ _scale3DHandlerY * .6f, center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .05f, center3DHandler.y
					+ _scale3DHandlerY * .5f, center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .5f,
					center3DHandler.z));

			command(GLCommands.sketch_glcolor(axisColorZ));
			command(GLCommands.sketch_moveto(center3DHandler.x,
					center3DHandler.y, center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y, center3DHandler.z + _scale3DHandlerZ
							* .5f));
			command(GLCommands.sketch_lineto(center3DHandler.x
					- _scale3DHandlerX * .05f, center3DHandler.y,
					center3DHandler.z + _scale3DHandlerZ * .5f));
			command(GLCommands.sketch_lineto(center3DHandler.x
					- _scale3DHandlerX * .05f, center3DHandler.y,
					center3DHandler.z + _scale3DHandlerZ * .6f));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .05f, center3DHandler.y,
					center3DHandler.z + _scale3DHandlerZ * .6f));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .05f, center3DHandler.y,
					center3DHandler.z + _scale3DHandlerZ * .5f));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y, center3DHandler.z + _scale3DHandlerZ
							* .5f));

			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y, center3DHandler.z + _scale3DHandlerZ
							* .5f));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .05f,
					center3DHandler.z + _scale3DHandlerZ * .5f));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .05f,
					center3DHandler.z + _scale3DHandlerZ * .6f));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y - _scale3DHandlerY * .05f,
					center3DHandler.z + _scale3DHandlerZ * .6f));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y - _scale3DHandlerY * .05f,
					center3DHandler.z + _scale3DHandlerZ * .5f));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y, center3DHandler.z + _scale3DHandlerZ
							* .5f));
		}
	}

	private void draw3DRotatingHandler() {
		if (model.getSelectedModelVerticeCount() > 0) {
			command(GLCommands.sketch_glcolor(axisColorX));
			command(GLCommands.sketch_moveto(center3DHandler.x,
					center3DHandler.y, center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .5f, center3DHandler.y,
					center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .6f, center3DHandler.y,
					center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .6f, center3DHandler.y
					+ _scale3DHandlerY * .1f, center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .5f, center3DHandler.y,
					center3DHandler.z));

			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .5f, center3DHandler.y,
					center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .6f, center3DHandler.y,
					center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .6f, center3DHandler.y,
					center3DHandler.z + _scale3DHandlerZ * .1f));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .5f, center3DHandler.y,
					center3DHandler.z));

			command(GLCommands.sketch_glcolor(axisColorY));
			command(GLCommands.sketch_moveto(center3DHandler.x,
					center3DHandler.y, center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .5f,
					center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .6f,
					center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .6f,
					center3DHandler.z + _scale3DHandlerZ * .1f));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .5f,
					center3DHandler.z));

			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .5f,
					center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .6f,
					center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .1f, center3DHandler.y
					+ _scale3DHandlerY * .6f, center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .5f,
					center3DHandler.z));

			command(GLCommands.sketch_glcolor(axisColorZ));
			command(GLCommands.sketch_moveto(center3DHandler.x,
					center3DHandler.y, center3DHandler.z));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y, center3DHandler.z + _scale3DHandlerZ
							* .5f));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y, center3DHandler.z + _scale3DHandlerZ
							* .6f));
			command(GLCommands.sketch_lineto(center3DHandler.x
					+ _scale3DHandlerX * .1f, center3DHandler.y,
					center3DHandler.z + _scale3DHandlerZ * .6f));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y, center3DHandler.z + _scale3DHandlerZ
							* .5f));

			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y, center3DHandler.z + _scale3DHandlerZ
							* .5f));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y + _scale3DHandlerY * .1f,
					center3DHandler.z + _scale3DHandlerZ * .6f));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y, center3DHandler.z + _scale3DHandlerZ
							* .6f));
			command(GLCommands.sketch_lineto(center3DHandler.x,
					center3DHandler.y, center3DHandler.z + _scale3DHandlerZ
							* .5f));
		}
	}

	private void draw3DModel(float[] _faceEditColor) {
		command(GLCommands.sketch_glcolor(_faceEditColor));
		model.drawModelFaces();
	}

	private void draw3DLines() {
		command(GLCommands.sketch_glcolor(lineColor));
		model.drawModelLines();
	}

	private void draw3DPoints() {
		command(GLCommands.sketch_glcolor(pointColor));
		command(GLCommands.sketch_pointSize(pointSize));
		model.drawModelPoints();
	}

	private void draw3DSelectedPoints() {
		command(GLCommands.sketch_glcolor(selectedPointColor));
		command(GLCommands.sketch_pointSize(pointSize));
		model.drawSelectedPoints();
	}

	/****************************************************************
	 * 2D Drawing
	 ****************************************************************/

	public void draw2D(SketchCanvas canvas, boolean showFaces,
			boolean showLines, boolean showPoints) {
		glCommandCanvas = canvas;
		command(GLCommands.sketch_reset());
		if (showFaces) {
			draw2DModel();
			draw2DBackground();
		}
		if (showLines) {
			draw2DLines();
		}
		if (showPoints) {
			draw2DPoints();
			draw2DSelectedPoints();
		}
	}

	public void draw2DHandler(SketchCanvas canvas) {
		update2DHandler();
		glCommandCanvas = canvas;
		command(GLCommands.sketch_reset());
		if (handler2DMode == HANDLER_GRABBING) {
			draw2DGrabbingHandler();
		}
		if (handler2DMode == HANDLER_ROTATING) {
			draw2DRotatingHandler();
		}
		if (handler2DMode == HANDLER_SCALING) {
			draw2DScalingHandler();
		}
	}

	private void draw2DGrabbingHandler() {
		PVector shifted2DHandler = new PVector(textureShiftX, textureShiftY, 0.f);
		shifted2DHandler.add(center2DHandler);
		
		if (model.getSelectedModelVerticeCount() > 0) {
			command(GLCommands.sketch_glcolor(axisColorX));
			command(GLCommands.sketch_moveto(shifted2DHandler.x,
					shifted2DHandler.y, shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .5f, shifted2DHandler.y,
					shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .5f, shifted2DHandler.y
					- _scale2DHandlerY * .05f, shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .6f, shifted2DHandler.y,
					shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .5f, shifted2DHandler.y
					+ _scale2DHandlerY * .05f, shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .5f, shifted2DHandler.y,
					shifted2DHandler.z));

			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .5f, shifted2DHandler.y,
					shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .5f, shifted2DHandler.y,
					shifted2DHandler.z - _scale2DHandlerZ * .05f));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .6f, shifted2DHandler.y,
					shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .5f, shifted2DHandler.y,
					shifted2DHandler.z + _scale2DHandlerZ * .05f));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .5f, shifted2DHandler.y,
					shifted2DHandler.z));

			command(GLCommands.sketch_glcolor(axisColorY));
			command(GLCommands.sketch_moveto(shifted2DHandler.x,
					shifted2DHandler.y, shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x,
					shifted2DHandler.y + _scale2DHandlerY * .5f,
					shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x,
					shifted2DHandler.y + _scale2DHandlerY * .5f,
					shifted2DHandler.z - _scale2DHandlerZ * .05f));
			command(GLCommands.sketch_lineto(shifted2DHandler.x,
					shifted2DHandler.y + _scale2DHandlerY * .6f,
					shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x,
					shifted2DHandler.y + _scale2DHandlerY * .5f,
					shifted2DHandler.z + _scale2DHandlerZ * .05f));
			command(GLCommands.sketch_lineto(shifted2DHandler.x,
					shifted2DHandler.y + _scale2DHandlerY * .5f,
					shifted2DHandler.z));

			command(GLCommands.sketch_lineto(shifted2DHandler.x,
					shifted2DHandler.y + _scale2DHandlerY * .5f,
					shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					- _scale2DHandlerX * .05f, shifted2DHandler.y
					+ _scale2DHandlerY * .5f, shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x,
					shifted2DHandler.y + _scale2DHandlerY * .6f,
					shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .05f, shifted2DHandler.y
					+ _scale2DHandlerY * .5f, shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x,
					shifted2DHandler.y + _scale2DHandlerY * .5f,
					shifted2DHandler.z));
		}
	}

	private void draw2DScalingHandler() {
		PVector shifted2DHandler = new PVector(textureShiftX, textureShiftY, 0.f);
		shifted2DHandler.add(center2DHandler);

		if (model.getSelectedModelVerticeCount() > 0) {
			command(GLCommands.sketch_glcolor(axisColorX));
			command(GLCommands.sketch_moveto(shifted2DHandler.x,
					shifted2DHandler.y, shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .5f, shifted2DHandler.y,
					shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .5f, shifted2DHandler.y
					- _scale2DHandlerY * .05f, shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .6f, shifted2DHandler.y
					- _scale2DHandlerY * .05f, shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .6f, shifted2DHandler.y
					+ _scale2DHandlerY * .05f, shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .5f, shifted2DHandler.y
					+ _scale2DHandlerY * .05f, shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .5f, shifted2DHandler.y,
					shifted2DHandler.z));

			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .5f, shifted2DHandler.y,
					shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .5f, shifted2DHandler.y,
					shifted2DHandler.z - _scale2DHandlerZ * .05f));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .6f, shifted2DHandler.y,
					shifted2DHandler.z - _scale2DHandlerZ * .05f));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .6f, shifted2DHandler.y,
					shifted2DHandler.z + _scale2DHandlerZ * .05f));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .5f, shifted2DHandler.y,
					shifted2DHandler.z + _scale2DHandlerZ * .05f));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .5f, shifted2DHandler.y,
					shifted2DHandler.z));

			command(GLCommands.sketch_glcolor(axisColorY));
			command(GLCommands.sketch_moveto(shifted2DHandler.x,
					shifted2DHandler.y, shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x,
					shifted2DHandler.y + _scale2DHandlerY * .5f,
					shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x,
					shifted2DHandler.y + _scale2DHandlerY * .5f,
					shifted2DHandler.z - _scale2DHandlerZ * .05f));
			command(GLCommands.sketch_lineto(shifted2DHandler.x,
					shifted2DHandler.y + _scale2DHandlerY * .6f,
					shifted2DHandler.z - _scale2DHandlerZ * .05f));
			command(GLCommands.sketch_lineto(shifted2DHandler.x,
					shifted2DHandler.y + _scale2DHandlerY * .6f,
					shifted2DHandler.z + _scale2DHandlerZ * .05f));
			command(GLCommands.sketch_lineto(shifted2DHandler.x,
					shifted2DHandler.y + _scale2DHandlerY * .5f,
					shifted2DHandler.z + _scale2DHandlerZ * .05f));
			command(GLCommands.sketch_lineto(shifted2DHandler.x,
					shifted2DHandler.y + _scale2DHandlerY * .5f,
					shifted2DHandler.z));

			command(GLCommands.sketch_lineto(shifted2DHandler.x,
					shifted2DHandler.y + _scale2DHandlerY * .5f,
					shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					- _scale2DHandlerX * .05f, shifted2DHandler.y
					+ _scale2DHandlerY * .5f, shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					- _scale2DHandlerX * .05f, shifted2DHandler.y
					+ _scale2DHandlerY * .6f, shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .05f, shifted2DHandler.y
					+ _scale2DHandlerY * .6f, shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .05f, shifted2DHandler.y
					+ _scale2DHandlerY * .5f, shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x,
					shifted2DHandler.y + _scale2DHandlerY * .5f,
					shifted2DHandler.z));
		}
	}

	private void draw2DRotatingHandler() {
		PVector shifted2DHandler = new PVector(textureShiftX, textureShiftY, 0.f);
		shifted2DHandler.add(center2DHandler);

		if (model.getSelectedModelVerticeCount() > 0) {
			command(GLCommands.sketch_glcolor(axisColorX));
			command(GLCommands.sketch_moveto(shifted2DHandler.x,
					shifted2DHandler.y, shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .5f, shifted2DHandler.y,
					shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .6f, shifted2DHandler.y,
					shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .6f, shifted2DHandler.y
					+ _scale2DHandlerY * .1f, shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .5f, shifted2DHandler.y,
					shifted2DHandler.z));

			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .5f, shifted2DHandler.y,
					shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .6f, shifted2DHandler.y,
					shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .6f, shifted2DHandler.y,
					shifted2DHandler.z + _scale2DHandlerZ * .1f));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .5f, shifted2DHandler.y,
					shifted2DHandler.z));

			command(GLCommands.sketch_glcolor(axisColorY));
			command(GLCommands.sketch_moveto(shifted2DHandler.x,
					shifted2DHandler.y, shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x,
					shifted2DHandler.y + _scale2DHandlerY * .5f,
					shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x,
					shifted2DHandler.y + _scale2DHandlerY * .6f,
					shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x,
					shifted2DHandler.y + _scale2DHandlerY * .6f,
					shifted2DHandler.z + _scale2DHandlerZ * .1f));
			command(GLCommands.sketch_lineto(shifted2DHandler.x,
					shifted2DHandler.y + _scale2DHandlerY * .5f,
					shifted2DHandler.z));

			command(GLCommands.sketch_lineto(shifted2DHandler.x,
					shifted2DHandler.y + _scale2DHandlerY * .5f,
					shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x,
					shifted2DHandler.y + _scale2DHandlerY * .6f,
					shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x
					+ _scale2DHandlerX * .1f, shifted2DHandler.y
					+ _scale2DHandlerY * .6f, shifted2DHandler.z));
			command(GLCommands.sketch_lineto(shifted2DHandler.x,
					shifted2DHandler.y + _scale2DHandlerY * .5f,
					shifted2DHandler.z));
		}
	}

	private void draw2DModel() {
		command(GLCommands.sketch_glcolor(faceEditColor));
		model.drawTexFaces();
	}

	private void draw2DBackground() {
		/**
		command(GLCommands.sketch_glcolor(bkgLineColor));
		command(GLCommands.sketch_moveto(0, 0, 0));
		command(GLCommands.sketch_lineto(1, 0, 0));
		command(GLCommands.sketch_lineto(1, 1, 0));
		command(GLCommands.sketch_lineto(0, 1, 0));
		command(GLCommands.sketch_lineto(0, 0, 0));
		**/

		command(GLCommands.sketch_glcolor(bkgFaceColor));
		command(GLCommands.sketch_beginShape());
		command(GLCommands.sketch_texture(0, 0));
		command(GLCommands.sketch_vertex(0+textureShiftX, 0+textureShiftY, 0));
		command(GLCommands.sketch_texture(1, 0));
		command(GLCommands.sketch_vertex(1+textureShiftX, 0+textureShiftY, 0));
		command(GLCommands.sketch_texture(1, 1));
		command(GLCommands.sketch_vertex(1+textureShiftX, 1+textureShiftY, 0));
		command(GLCommands.sketch_texture(0, 1));
		command(GLCommands.sketch_vertex(0+textureShiftX, 1+textureShiftY, 0));
		command(GLCommands.sketch_endShape());
	}

	private void draw2DLines() {
		command(GLCommands.sketch_glcolor(lineColor));
		model.drawTexLines();
	}

	private void draw2DPoints() {
		command(GLCommands.sketch_glcolor(pointColor));
		command(GLCommands.sketch_pointSize(pointSize));
		model.drawTexturePoints(textureShiftX, textureShiftY);
	}

	private void draw2DSelectedPoints() {
		command(GLCommands.sketch_glcolor(selectedPointColor));
		command(GLCommands.sketch_pointSize(pointSize));
		model.drawSelectedTexturePoints(textureShiftX, textureShiftY);
	}

	/****************************************************************
	 * Setter Methods
	 ****************************************************************/

	public void setFaceSubdivision(int sub) {
		model.setFaceSubDivision(sub);
		model.updateSegments();
	}

	public void set3DHandlerGrabbing() {
		handler3DMode = HANDLER_GRABBING;
	}

	public void set3DHandlerRotating() {
		handler3DMode = HANDLER_ROTATING;
	}

	public void set3DHandlerScaling() {
		handler3DMode = HANDLER_SCALING;
	}

	public void set2DHandlerGrabbing() {
		handler2DMode = HANDLER_GRABBING;
	}

	public void set2DHandlerRotating() {
		handler2DMode = HANDLER_ROTATING;
	}

	public void set2DHandlerScaling() {
		handler2DMode = HANDLER_SCALING;
	}

	public void setFaceEditColor(float[] color) {
		faceEditColor = color;
	}

	public void setFaceMainColor(float[] color) {
		faceMainColor = color;
	}

	public void setLineColor(float[] color) {
		lineColor = color;
	}

	public void setPointColor(float[] color) {
		pointColor = color;
	}

	public void setSelectedPointColor(float[] color) {
		selectedPointColor = color;
	}

	public void scale3DHandler(float x, float y, float z) {
		_scale3DHandlerX = 1.f / x;
		_scale3DHandlerY = 1.f / y;
		_scale3DHandlerZ = 1.f / z;
	}

	public void scale2DHandler(float x, float y, float z) {
		_scale2DHandlerX = 1.f / x;
		_scale2DHandlerY = 1.f / y;
	}

	public void verbose(int i) {
		if (i == 0) {
			model.debug.enabled = false;
		} else {
			model.debug.enabled = true;
		}
	}

	/****************************************************************
	 * GL Command Methods
	 ****************************************************************/

	public void command(ArrayList<Atom[]> list) {
		for (int i = 0; i < list.size(); i++) {
			glCommandCanvas.drawGlCommand(list.get(i));
		}
	}

	public void command(Atom[] command) {
		glCommandCanvas.drawGlCommand(command);
	}
}
