package org.meshwarpserver.objloader;

import java.util.ArrayList;

import com.cycling74.max.Atom;

import ch.maybites.mxj.math.PVector;
import ch.maybites.mxj.opengl.GLCommands;

public class SubFace extends Face {
	
	public SubFace(Face parent){
		this._sketchFaceCommands = parent._sketchFaceCommands;
		this.sketchLineCommands = parent.sketchLineCommands;
		this.sketchTexLineCommands = parent.sketchTexLineCommands;
	}

	public void refreshSketchCommands(){		
		if(_mySubFaces == null){
			createFaceCommands();
			createLineCommands();
			createTexLineCommands();
		} else {
			_mySubFaces.refreshSketchCommands();
		}
	}

}
