import java.util.ArrayList;

public class Grid {
	private int bottomBound = -40;
	private int height = 80;

	private int frontBound = -40;
	private int depth = 80;

	private int leftBound = -40;
	private int width = 80;

	private ArrayList<Point> points = new ArrayList<Point>();

	public Grid(int space) {
		for (int i = 0; i < height / space; i++) {
			for (int j = 0; j < depth / space; j++) {
				for (int k = 0; k < width / space; k++) {
					points.add(new Point(leftBound + k*space, frontBound + j*space, bottomBound + i*space));
				}
			}
		}
	}

	public ArrayList<Point> getPoints() {
		return points;
	}
}
