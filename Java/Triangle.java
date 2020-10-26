import java.util.Arrays;

public class Triangle {
	private Point points[] = new Point[3];

	public Triangle(Point a, Point b, Point c) {
		points[0] = a;
		points[1] = b;
		points[2] = c;
	}

	public Point[] getPoints() {
		return points;
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

	@Override
	public String toString() {
		return "Triangle [points=" + Arrays.toString(points) + "]";
	}
}
