package org.meshwarpserver.mwserver;

import com.cycling74.max.*;

public class SketchCanvas {
	
	public int outletNumber;
	private GLCanvas _canvas;
	
	public SketchCanvas(GLCanvas canvas, int outletnumber){
		outletNumber = outletnumber;
		_canvas = canvas;
	}
		
	public void drawGlCommand(Atom[] command){
		_canvas.drawGlCommand(command, outletNumber);
	}

}
