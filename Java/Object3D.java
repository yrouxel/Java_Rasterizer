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
			ArrayList<TexturePoint> texturesPoints = new ArrayList<TexturePoint>();

			String texturePath = path;
			texturePath.split(".obj");
			texturePath += ".png";

			String line;
			while ((line = br.readLine()) != null) {
				String[] elements = line.split(" ");
				if (elements[0].equals("v")) {
					points.add(new Point(Double.parseDouble(elements[1]), Double.parseDouble(elements[2]), Double.parseDouble(elements[3])));
				} else if (elements[0].equals("vt")) {
					texturesPoints.add(new TexturePoint(Double.parseDouble(elements[1]), Double.parseDouble(elements[2])));
				} else if (elements[0].equals("f")) {
					if (elements.length == 4) {
						Triangle tri = new Triangle(points.get(Integer.valueOf(elements[1].split("/")[0])-1), points.get(Integer.valueOf(elements[2].split("/")[0])-1), points.get(Integer.valueOf(elements[3].split("/")[0])-1));
						// tri.addTextures(texturesPoints.get(Integer.valueOf(elements[1].split("/")[1])-1), texturesPoints.get(Integer.valueOf(elements[3].split("/")[1])-1), texturesPoints.get(Integer.valueOf(elements[2].split("/")[1])-1), texturePath);
						triangles.add(tri);
					} else if (elements.length == 5) {
						triangles.add(new Triangle(points.get(Integer.valueOf(elements[1].split("/")[0])-1), points.get(Integer.valueOf(elements[3].split("/")[0])-1), points.get(Integer.valueOf(elements[2].split("/")[0])-1)));
						triangles.add(new Triangle(points.get(Integer.valueOf(elements[1].split("/")[0])-1), points.get(Integer.valueOf(elements[4].split("/")[0])-1), points.get(Integer.valueOf(elements[3].split("/")[0])-1)));
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
