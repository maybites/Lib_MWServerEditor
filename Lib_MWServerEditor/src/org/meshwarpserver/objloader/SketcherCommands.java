package org.meshwarpserver.objloader;

import java.util.ArrayList;
import com.cycling74.max.Atom;

public interface SketcherCommands {

	public void command(ArrayList<Atom[]> list);

	public void command(Atom[] command);

}
