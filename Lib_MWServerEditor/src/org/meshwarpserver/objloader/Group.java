package org.meshwarpserver.objloader;

/*
 * Alias .obj loader for processing
 * programmed by Tatsuya SAITO / UCLA Design | Media Arts 
 * Created on 2005/04/17
 *
 * 
 *  
 */

import java.util.Vector;

/**
 * @author tatsuyas
 * 
 */
public class Group {
	public String groupName;
	public Vector segments;

	public Group(String groupName) {
		segments = new Vector();
		this.groupName = groupName;
	}

	public String getName() {
		return groupName;
	}
}
