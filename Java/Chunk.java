import java.util.ArrayList;

public class Chunk {
	private ArrayList<Triangle> triangles = new ArrayList<Triangle>();
	private Point coord;

	public Chunk(Point coord) {
		this.coord = coord;
	}

	public void addTriangle(Triangle tri) {
		triangles.add(tri);
	}

	public ArrayList<Triangle> getTriangles() {
		return triangles;
	}

	public Boolean isOpaque() {
		return false;
	}

	public Point getCoord() {
		return coord;
	}
}
