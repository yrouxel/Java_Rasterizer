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

	@Override
	public String toString() {
		return "Triangle [points=" + Arrays.toString(points) + "]";
	}
}
