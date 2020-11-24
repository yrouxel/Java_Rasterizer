import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class Object3D {
	private ArrayList<Triangle> triangles;

	public Object3D(ArrayList<Triangle> triangles) {
		this.triangles = triangles;
	}

	public Object3D() {
		triangles = new ArrayList<Triangle>();
	}

	public Boolean validObject() {
		for (Triangle tri : triangles) {
			int commonVertices = 0;
			for (Triangle tri2 : triangles) {
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

	public void getObjectFromFile(String path) {
		try {
			//default buffer size = 8192
			BufferedReader br = new BufferedReader(new FileReader(path), 100000); 
			ArrayList<Point> points = new ArrayList<Point>();
			String line;
			while ((line = br.readLine()) != null) {
				String[] elements = line.split(" ");
				if (elements[0].equals("v")) {
					points.add(new Point(Double.parseDouble(elements[1]), Double.parseDouble(elements[2]), Double.parseDouble(elements[3])));
				} else if (elements[0].equals("f")) {
					if (elements.length == 4) {
						triangles.add(new Triangle(points.get(Integer.valueOf(elements[1].split("/")[0])-1), points.get(Integer.valueOf(elements[2].split("/")[0])-1), points.get(Integer.valueOf(elements[3].split("/")[0])-1)));
					} else if (elements.length == 5) {
						triangles.add(new Triangle(points.get(Integer.valueOf(elements[1].split("/")[0])-1), points.get(Integer.valueOf(elements[2].split("/")[0])-1), points.get(Integer.valueOf(elements[3].split("/")[0])-1)));
						triangles.add(new Triangle(points.get(Integer.valueOf(elements[1].split("/")[0])-1), points.get(Integer.valueOf(elements[3].split("/")[0])-1), points.get(Integer.valueOf(elements[4].split("/")[0])-1)));
					}	
				}
			}
			br.close();

		} catch (Exception e) {
			System.out.println("INVALID OBJECT");
			e.printStackTrace();
		}

	}

	public ArrayList<Triangle> getTriangles() {
		return triangles;
	}
}
