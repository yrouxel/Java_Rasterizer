import java.util.Arrays;
import java.awt.*;

public class Triangle extends Surface implements Comparable<Triangle>{
	private Point points[] = new Point[3];
	private TexturePoint texturePoints[];
	String texturePath;

	private Color color;

	public Triangle(Point a, Point b, Point c, Color color) {
		this.color = color;
		points[0] = a;
		points[1] = b;
		points[2] = c;
	}

	public Triangle(Point a, Point b, Point c) {
		color = Color.GRAY;
		points[0] = a;
		points[1] = b;
		points[2] = c;
	}

	public void addTextures(TexturePoint a, TexturePoint b, TexturePoint c, String texturePath) {
		this.texturePath = texturePath;
		texturePoints = new TexturePoint[3];
		texturePoints[0] = a;
		texturePoints[1] = b;
		texturePoints[2] = c;
	}

	public boolean contains(Point p) {
		for (Point pt : points) {
			if (pt.equals(p)) {
				return true;
			}
		}
		return false;
	}

	public Boolean shareEdge(Triangle triangle) {
		int commonPoints = 0;
		for (int i = 0; i < points.length; i++) {
			for (int j = 0; j < triangle.getPoints().length; j++) {
				if (points[i].equals(triangle.getPoints()[j])) {
					commonPoints++;
				}
			}
		}
		return commonPoints == 2;
	}

	public void replacePoint(Point a, Point b) {
		for (int i = 0; i < 3; i++) {
			if (points[i].equals(a)) {
				points[i].replace(b);
				break;
			}
		}
	}

	//---GETTERS---

	public Vector getNormal() {
		return new Vector(points[1], points[0]).getCrossProduct(new Vector(points[2], points[1]));
	}

	public double getTotalSurface() {
		return getNormal().getNorm() / 2;
	}

	public Point getCenterOfGravity() {
		Point g = new Point(0, 0, 0);
		g.add(points[0]);
		g.add(points[1]);
		g.add(points[2]);
		g.multiply(1.0/3.0);
		return g;
	}

	public Point[] getPoints() {
		return points;
	}

	public TexturePoint[] getTexturePoints() {
		return texturePoints;
	}

	public Color getColor() {
		return color;
	}

	//---END GETTERS---

	@Override
	public String toString() {
		return "Triangle [points=" + Arrays.toString(points) + "]";
	}

	@Override
	public int compareTo(Triangle tri) {
		return points[0].compareTo(tri.getPoints()[0]);
	}
}
