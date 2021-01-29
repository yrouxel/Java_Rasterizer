import java.util.Arrays;
import java.awt.*;

public class Triangle {
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

	public Point[] getPoints() {
		return points;
	}

	public TexturePoint[] getTexturePoints() {
		return texturePoints;
	}

	public Color getColor() {
		return color;
	}

	public Boolean shareVertice(Triangle triangle) {
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

	public Vector getNormal() {
		return new Vector(points[1], points[0]).getNormal(new Vector(points[2], points[1]));
	}

	public Point getCenterOfGravity() {
		Point g = new Point(0, 0, 0);
		g.add(points[0]);
		g.add(points[1]);
		g.add(points[2]);
		g.multiply(1.0/3.0);
		return g;
	}

	public Point getCenterOfGravityOptimized() {
		return new Point((points[0].getX() + points[1].getX() + points[2].getX())/3.0, (points[0].getY() + points[1].getY() + points[2].getY())/3.0, (points[0].getZ() + points[1].getZ() + points[2].getZ())/3.0);
	}

	@Override
	public String toString() {
		return "Triangle [points=" + Arrays.toString(points) + "]";
	}
}
