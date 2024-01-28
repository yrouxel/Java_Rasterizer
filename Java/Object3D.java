import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Color;

public class Object3D {
	private ArrayList<Triangle> triangles;
	private BufferedImage texture;

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

	public void getObjectFromFile(final File path) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(path), 100000); 

			ArrayList<Point> points = new ArrayList<Point>();
			ArrayList<TexturePoint> texturesPoints = new ArrayList<TexturePoint>();

			String texturePath = path.getAbsolutePath();
			texturePath = texturePath.split(".obj")[0];
			texturePath += ".png";
			texture = ImageIO.read(new File(texturePath));

			double width = texture.getWidth();
			double height = texture.getHeight();

			String line;
			while ((line = br.readLine()) != null) {
				String[] elements = line.split(" ");
				// if (elements[0].equals("#") && elements.length >= 3 && elements[2].equals("Edges")) {
				// 	Edges = Integer.parseInt(elements[1]);
				// } else 
				if (elements[0].equals("v")) {
					points.add(new Point(-Double.parseDouble(elements[1]), Double.parseDouble(elements[3]), Double.parseDouble(elements[2])));
				} else if (elements[0].equals("vt")) {
					texturesPoints.add(new TexturePoint(Double.parseDouble(elements[1]) * width, Double.parseDouble(elements[2]) * height));
				} else if (elements[0].equals("f")) {
					if (elements.length == 4) {
						Triangle tri = new Triangle(points.get(Integer.valueOf(elements[1].split("/")[0])-1), points.get(Integer.valueOf(elements[2].split("/")[0])-1), points.get(Integer.valueOf(elements[3].split("/")[0])-1));
						tri.addTextures(texturesPoints.get(Integer.valueOf(elements[1].split("/")[1])-1), texturesPoints.get(Integer.valueOf(elements[2].split("/")[1])-1), texturesPoints.get(Integer.valueOf(elements[3].split("/")[1])-1), texture);
						// tri.setColor(computeTriangleColor(tri));
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

	/*
	public Color computeTriangleColor(Triangle tri) {
		int xMin = Integer.MAX_VALUE;
		int xMax = Integer.MIN_VALUE;
		int yMin = Integer.MAX_VALUE;
		int yMax = Integer.MIN_VALUE;

		TexturePoint[] tp = tri.getTexturePoints();
		for (int i = 0; i < 3; i++) {
			xMin = (int)Math.min(xMin, tp[i].getX());
			xMax = (int)Math.max(xMax, tp[i].getX());
			yMin = (int)Math.min(yMin, tp[i].getY());
			yMax = (int)Math.max(yMax, tp[i].getY());
		}

		xMin = (int)Math.max(xMin, 0);
		xMax = (int)Math.min(xMax, texture.getWidth()-1);
		yMin = (int)Math.max(yMin, 0);
		yMax = (int)Math.min(yMax, texture.getHeight()-1);

		int totalR = 0;
		int totalG = 0;
		int totalB = 0;
		int totalPixels = 0;

		for (int y = yMin; y < yMax; y++) {
			boolean firstPixelFound = false;
			for (int x = xMin; x < xMax; x++) {
				if (isPointInTriangle(x, y, tp[0].getX(), tp[0].getY(), tp[1].getX(), tp[1].getY(), tp[2].getX(), tp[2].getY())) {
					if (!firstPixelFound) {
						firstPixelFound = true;
					}
					totalPixels++;
					Color color = new Color(texture.getRGB(x, y));
					totalR += color.getRed();
					totalG += color.getGreen();
					totalB += color.getBlue();
				} else if (firstPixelFound) {
					break;
				}
			}
		}

		if (totalPixels != 0) {
			totalR /= totalPixels;
			totalG /= totalPixels;
			totalB /= totalPixels;

			return new Color(totalR, totalG, totalB);
		} else {
			return Color.GRAY;
		}
	}*/

	/** returns the sign of the cross product */
	public boolean edgeFunction(double x1, double y1, double x2, double y2, double x3, double y3) {
		return (x1 - x3) * (y2 - y3) >= (x2 - x3) * (y1 - y3);
	}

	/** checks if a given point is a triangle by a methode of cross products */
	public boolean isPointInTriangle(double xPt, double yPt, int x1, int y1, int x2, int y2, int x3, int y3){
		return (edgeFunction(xPt, yPt, x1, y1, x2, y2) && edgeFunction(xPt, yPt, x2, y2, x3, y3) && edgeFunction(xPt, yPt, x3, y3, x1, y1));
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
