import java.util.TreeMap;

public class Chunk{
	private Point coord;
	private TreeMap<Point, Object> smallerChunks = new TreeMap<Point, Object>();

	public Chunk() {}

	public Chunk(Point coord) {
		this.coord = coord;
	}

	public Point getCoord() {
		return coord;
	}

	/** takes the size of the chunk, returns all 8 corners */
	public Point[] getPoints(double chunkSize) {
		Point[] points = new Point[8];

		for (int i = 0; i < 8; i++) {
			points[i] = new Point(coord);
		}

		points[1].add(new Point(chunkSize, 0, 0));
		points[2].add(new Point(0, 0, chunkSize));
		points[3].add(new Point(chunkSize, 0, chunkSize));
		points[4].add(new Point(0, chunkSize, 0));
		points[5].add(new Point(chunkSize, chunkSize, 0));
		points[6].add(new Point(0, chunkSize, chunkSize));
		points[7].add(new Point(chunkSize, chunkSize, chunkSize));

		return points;
	}

	/** takes the size of the chunk, returns its middle point */
	public Point getCenter(double chunkSize) {
		Point center = new Point(coord);
		Point pt = new Point(1, 1, 1);
		pt.multiply(chunkSize/2);
		center.add(pt);

		return center;
	}

	public TreeMap<Point, Object> getSmallerChunks() {
		return smallerChunks;
	}
}
