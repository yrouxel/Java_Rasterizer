import java.util.ArrayList;

public class Object3D {
	private ArrayList<Triangle> faces;

	public Object3D(ArrayList<Triangle> faces) {
		this.faces = faces;
	}

	public Boolean validObject() {
		for (Triangle tri : faces) {
			int commonVertices = 0;
			for (Triangle tri2 : faces) {
				if (tri.shareVertice(tri2)) {
					commonVertices++;
				}
			}
			if (commonVertices != 3) {
				return false;
			}
		}
		return true;
	}

	public ArrayList<Triangle> getFaces() {
		return faces;
	}
}
