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

		double x = coord.getX();
		double y = coord.getY();	
		double z = coord.getZ();

		double x1 = x + chunkSize;
		double y1 = z + chunkSize;	
		double z1 = x + chunkSize;

		points[0] = new Point(x, y, z);
		points[2] = new Point(x, y, z1);
		points[4] = new Point(x, y1, z);
		points[6] = new Point(x, y1, z1);

		points[1] = new Point(x1, y, z);
		points[3] = new Point(x1, y, z1);
		points[5] = new Point(x1, y1, z);
		points[7] = new Point(x1, y1, z1);

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
