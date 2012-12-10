package org.meshwarpserver.objloader;

import java.util.ArrayList;

import org.meshwarpserver.util.PVector;

import com.cycling74.max.Atom;

public class FaceSubDivision {

	private ArrayList<SubFace> _faces;
	private Face _myFace;
	
	private int _subDivision;

	public FaceSubDivision(Face myFace, int subDivision) {
		_myFace = myFace;
		_faces = new ArrayList<SubFace>();
		_subDivision = subDivision - 1;
	}

	public void refreshSketchCommands() {
		//System.out.println("create subfaces...");
		if (_myFace.getVertexCount() == 4) {
			PVector[] vertice = new PVector[9];
			vertice[0] = _myFace.getVertice(0);
			vertice[2] = _myFace.getVertice(1);
			vertice[8] = _myFace.getVertice(2);
			vertice[6] = _myFace.getVertice(3);
			vertice[1] = PVector.add(vertice[0], vertice[2]);
			vertice[1].div(2f);
			vertice[3] = PVector.add(vertice[0], vertice[6]);
			vertice[3].div(2f);
			vertice[5] = PVector.add(vertice[2], vertice[8]);
			vertice[5].div(2f);
			vertice[7] = PVector.add(vertice[8], vertice[6]);
			vertice[7].div(2f);
			vertice[4] = PVector.add(vertice[1], vertice[7]);
			vertice[4].div(2f);

			PVector[] normals = new PVector[9];
			if (_myFace.getNormalCount() > 0) {
				normals[0] = _myFace.getNormal(0);
				normals[2] = _myFace.getNormal(1);
				normals[8] = _myFace.getNormal(2);
				normals[6] = _myFace.getNormal(3);
				normals[1] = PVector.add(normals[0], normals[2]);
				normals[1].div(2f);
				normals[3] = PVector.add(normals[0], normals[6]);
				normals[3].div(2f);
				normals[5] = PVector.add(normals[2], normals[8]);
				normals[5].div(2f);
				normals[7] = PVector.add(normals[8], normals[6]);
				normals[7].div(2f);
				normals[4] = PVector.add(normals[1], normals[7]);
				normals[4].div(2f);
			}

			PVector[] uvs = new PVector[9];
			if (_myFace.getUVCount() > 0) {
				uvs[0] = _myFace.getUvs(0);
				uvs[2] = _myFace.getUvs(1);
				uvs[8] = _myFace.getUvs(2);
				uvs[6] = _myFace.getUvs(3);
				uvs[1] = PVector.add(uvs[0], uvs[2]);
				uvs[1].div(2f);
				uvs[3] = PVector.add(uvs[0], uvs[6]);
				uvs[3].div(2f);
				uvs[5] = PVector.add(uvs[2], uvs[8]);
				uvs[5].div(2f);
				uvs[7] = PVector.add(uvs[8], uvs[6]);
				uvs[7].div(2f);
				uvs[4] = PVector.add(uvs[1], uvs[7]);
				uvs[4].div(2f);
			}

			_faces.clear();

			/*
			 * add first subface:
			 */
			SubFace face = new SubFace(_myFace);
			face.vertices.add(vertice[0]);
			face.vertices.add(vertice[1]);
			face.vertices.add(vertice[4]);
			face.vertices.add(vertice[3]);

			if (_myFace.getUVCount() > 0) {
				face.uvs.add(uvs[0]);
				face.uvs.add(uvs[1]);
				face.uvs.add(uvs[4]);
				face.uvs.add(uvs[3]);
			}

			if (_myFace.getNormalCount() > 0) {
				face.normals.add(normals[0]);
				face.normals.add(normals[1]);
				face.normals.add(normals[4]);
				face.normals.add(normals[3]);
			}

			_faces.add(face);

			/*
			 * add second subface:
			 */
			face = new SubFace(_myFace);
			face.vertices.add(vertice[1]);
			face.vertices.add(vertice[2]);
			face.vertices.add(vertice[5]);
			face.vertices.add(vertice[4]);

			if (_myFace.getUVCount() > 0) {
				face.uvs.add(uvs[1]);
				face.uvs.add(uvs[2]);
				face.uvs.add(uvs[5]);
				face.uvs.add(uvs[4]);
			}

			if (_myFace.getNormalCount() > 0) {
				face.normals.add(normals[1]);
				face.normals.add(normals[2]);
				face.normals.add(normals[5]);
				face.normals.add(normals[4]);
			}

			_faces.add(face);

			/*
			 * add third subface:
			 */
			face = new SubFace(_myFace);
			face.vertices.add(vertice[3]);
			face.vertices.add(vertice[4]);
			face.vertices.add(vertice[7]);
			face.vertices.add(vertice[6]);

			if (_myFace.getUVCount() > 0) {
				face.uvs.add(uvs[3]);
				face.uvs.add(uvs[4]);
				face.uvs.add(uvs[7]);
				face.uvs.add(uvs[6]);
			}

			if (_myFace.getNormalCount() > 0) {
				face.normals.add(normals[3]);
				face.normals.add(normals[4]);
				face.normals.add(normals[7]);
				face.normals.add(normals[6]);
			}

			_faces.add(face);

			/*
			 * add fourth subface:
			 */
			face = new SubFace(_myFace);
			face.vertices.add(vertice[4]);
			face.vertices.add(vertice[5]);
			face.vertices.add(vertice[8]);
			face.vertices.add(vertice[7]);

			if (_myFace.getUVCount() > 0) {
				face.uvs.add(uvs[4]);
				face.uvs.add(uvs[5]);
				face.uvs.add(uvs[8]);
				face.uvs.add(uvs[7]);
			}

			if (_myFace.getNormalCount() > 0) {
				face.normals.add(normals[4]);
				face.normals.add(normals[5]);
				face.normals.add(normals[8]);
				face.normals.add(normals[7]);
			}

			_faces.add(face);

			/*
			 * refresh the faceCommands
			 */

			for (int i = 0; i < _faces.size(); i++) {
				_faces.get(i).makeSubFaces(_subDivision);
				_faces.get(i).refreshSketchCommands();
			}

		}
		// threee vertexes
		if (_myFace.getVertexCount() == 3) {
			PVector[] vertice = new PVector[6];
			vertice[0] = _myFace.getVertice(0);
			vertice[1] = _myFace.getVertice(1);
			vertice[2] = _myFace.getVertice(2);
			vertice[3] = PVector.add(vertice[0], vertice[1]);
			vertice[3].div(2f);
			vertice[4] = PVector.add(vertice[1], vertice[2]);
			vertice[4].div(2f);
			vertice[5] = PVector.add(vertice[2], vertice[0]);
			vertice[5].div(2f);

			PVector[] normals = new PVector[9];
			if (_myFace.getNormalCount() > 0) {
				normals[0] = _myFace.getNormal(0);
				normals[1] = _myFace.getNormal(1);
				normals[2] = _myFace.getNormal(2);
				normals[3] = PVector.add(normals[0], normals[1]);
				normals[3].div(2f);
				normals[4] = PVector.add(normals[1], normals[2]);
				normals[4].div(2f);
				normals[5] = PVector.add(normals[2], normals[0]);
				normals[5].div(2f);
			}

			PVector[] uvs = new PVector[9];
			if (_myFace.getUVCount() > 0) {
				uvs[0] = _myFace.getUvs(0);
				uvs[1] = _myFace.getUvs(1);
				uvs[2] = _myFace.getUvs(2);
				uvs[3] = PVector.add(uvs[0], uvs[1]);
				uvs[3].div(2f);
				uvs[4] = PVector.add(uvs[1], uvs[2]);
				uvs[4].div(2f);
				uvs[5] = PVector.add(uvs[2], uvs[0]);
				uvs[5].div(2f);
			}

			_faces.clear();

			/*
			 * add first subface:
			 */
			SubFace face = new SubFace(_myFace);
			face.vertices.add(vertice[0]);
			face.vertices.add(vertice[3]);
			face.vertices.add(vertice[5]);

			if (_myFace.getUVCount() > 0) {
				face.uvs.add(uvs[0]);
				face.uvs.add(uvs[3]);
				face.uvs.add(uvs[5]);
			}

			if (_myFace.getNormalCount() > 0) {
				face.normals.add(normals[0]);
				face.normals.add(normals[3]);
				face.normals.add(normals[5]);
			}

			_faces.add(face);

			/*
			 * add second subface:
			 */
			face = new SubFace(_myFace);
			face.vertices.add(vertice[1]);
			face.vertices.add(vertice[4]);
			face.vertices.add(vertice[3]);

			if (_myFace.getUVCount() > 0) {
				face.uvs.add(uvs[1]);
				face.uvs.add(uvs[4]);
				face.uvs.add(uvs[3]);
			}

			if (_myFace.getNormalCount() > 0) {
				face.normals.add(normals[1]);
				face.normals.add(normals[4]);
				face.normals.add(normals[3]);
			}

			_faces.add(face);

			/*
			 * add third subface:
			 */
			face = new SubFace(_myFace);
			face.vertices.add(vertice[2]);
			face.vertices.add(vertice[5]);
			face.vertices.add(vertice[4]);

			if (_myFace.getUVCount() > 0) {
				face.uvs.add(uvs[2]);
				face.uvs.add(uvs[5]);
				face.uvs.add(uvs[4]);
			}

			if (_myFace.getNormalCount() > 0) {
				face.normals.add(normals[2]);
				face.normals.add(normals[5]);
				face.normals.add(normals[4]);
			}

			_faces.add(face);

			/*
			 * add fourth subface:
			 */
			face = new SubFace(_myFace);
			face.vertices.add(vertice[3]);
			face.vertices.add(vertice[4]);
			face.vertices.add(vertice[5]);

			if (_myFace.getUVCount() > 0) {
				face.uvs.add(uvs[3]);
				face.uvs.add(uvs[4]);
				face.uvs.add(uvs[5]);
			}

			if (_myFace.getNormalCount() > 0) {
				face.normals.add(normals[3]);
				face.normals.add(normals[4]);
				face.normals.add(normals[5]);
			}

			_faces.add(face);

			/*
			 * refresh the faceCommands
			 */

			for (int i = 0; i < _faces.size(); i++) {
				_faces.get(i).makeSubFaces(_subDivision);
				_faces.get(i).refreshSketchCommands();
			}

		}
	}

}
