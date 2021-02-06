import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

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
			int commonEdges = 0;
			for (Triangle tri2 : triangles) {
				if (tri.shareEdge(tri2)) {
					commonEdges++;
				}
			}
			if (commonEdges != 3) {
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
				// if (elements[0].equals("#") && elements.length >= 3 && elements[2].equals("Edges")) {
				// 	Edges = Integer.parseInt(elements[1]);
				// } else 
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

			System.out.println("OBJECT UPLOADED CONTAINS : " + triangles.size() + " TRIANGLES");
		} catch (Exception e) {
			System.out.println("INVALID OBJECT");
			e.printStackTrace();
		}

	}

	public TreeMap<Edge, ArrayList<Triangle>> generateEdges(ArrayList<Triangle> workableTriangles) {
		TreeMap<Edge, ArrayList<Triangle>> edges = new TreeMap<Edge, ArrayList<Triangle>> ();

		for (Triangle tri : workableTriangles) {
			Point[] p = tri.getPoints();
			for (int i = 0; i < 3; i++) {
				Edge vert = new Edge(p[i], p[(i+1)%3]);

				ArrayList<Triangle> list = edges.get(vert);
				if (list == null) {
					list = new ArrayList<Triangle>();
					edges.put(vert, list);
				}
				list.add(tri);
			}
		}
		return edges;
	}

	/** need to change Edges impacted by other Edges being removed */
	public ArrayList<Triangle> computeEdgesReduction(TreeMap<Edge, ArrayList<Triangle>> edges, double ratioReduction) {
		ArrayList<Triangle> triangle = new ArrayList<Triangle>();

		for (Triangle tri : triangles) {
			triangle.add(tri);
		}

		System.out.println("TRIANGLE COUNT : " + triangle.size());
		System.out.println("EDGES SIZE : " + edges.size());
		int max = (int)((double)edges.size() * (1.0 - ratioReduction));
		System.out.println("EDGES TO BE REMOVED : " + max);

		for (int i = 0; i < max; i++) {
			Map.Entry<Edge, ArrayList<Triangle>> entry = edges.firstEntry();
			Edge vert = entry.getKey();
			Point med = vert.getMiddlePoint();

			for (Triangle tri : entry.getValue()) {
				if (tri.contains(vert.getPointA()) && tri.contains(vert.getPointB())) {
					triangle.remove(tri);
				} else {
					tri.replacePoint(vert.getPointA(), med);
					tri.replacePoint(vert.getPointB(), med);
				}
			}

			edges.remove(vert);

			if (i%10000 == 0) {
				System.out.println("REMOVED " + i + " EDGES");
			}
		}

		System.out.println("TRIANGLE COUNT AFTER REDUCTION : " + triangle.size() + "\n");
		return triangle;
	}

	public ArrayList<Triangle> getTriangles() {
		return triangles;
	}
}
