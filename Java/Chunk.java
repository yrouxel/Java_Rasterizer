import java.util.TreeMap;

/** base brick of the world, each chunk contains sub elements */
public class Chunk {
	private Point coord;
	private Double chunkSize;
	private int chunkLevel;
	private TreeMap<Point, Object> smallerChunks = new TreeMap<Point, Object>();

	public Chunk() {}

	public Chunk(Point coord, Double chunkSize, int chunkLevel) {
		this.coord = coord;
		this.chunkSize = chunkSize;
		this.chunkLevel = chunkLevel;
	}

	//---GETTERS---

	public Point getCoord() {
		return coord;
	}

	public int getChunkLevel() {
		return chunkLevel;
	}

	/** takes the size of the chunk, returns all 8 corners */
	public Point[] getPoints() {
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
	public void computeCenter(Point center) {
		center.setPoint(1, 1, 1);
		center.multiply(chunkSize/2);
		center.add(coord);
	}

	public TreeMap<Point, Object> getSmallerChunks() {
		return smallerChunks;
	}

	public double getChunkSize() {
		return chunkSize;
	}

	//---SETTERS---

	public void setSmallerChunks(TreeMap<Point, Object> smallerChunks) {
		this.smallerChunks = smallerChunks;
	}
}
