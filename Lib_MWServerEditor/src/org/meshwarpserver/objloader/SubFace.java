package org.meshwarpserver.objloader;

import java.util.ArrayList;

import org.meshwarpserver.util.GLCommands;
import org.meshwarpserver.util.PVector;

import com.cycling74.max.Atom;


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
