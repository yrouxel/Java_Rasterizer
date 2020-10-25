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
		double gx = 0, gy = 0, gz = 0;
		for (int i = 0; i < 3; i++) {
			gx += points[i].getX();
			gy += points[i].getY();
			gz += points[i].getZ();
		}
		Point g =  new Point(gx, gy, gz);
		g.multiply(1.0/3.0);
		return g;
	}

	@Override
	public String toString() {
		return "Triangle [points=" + Arrays.toString(points) + "]";
	}
}
