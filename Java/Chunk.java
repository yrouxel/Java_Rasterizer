import java.util.Map;
import java.util.TreeMap;

/** base brick of the world, each chunk contains sub elements */
public class Chunk{
	private Point coord;
	private int chunkLevel;
	private TreeMap<Point, Object> smallerChunks = new TreeMap<Point, Object>();

	private Point centerOfGravity;
	private Vector inverseNormal;
	private double coneAngle;
	private double totalArea;

	public Chunk() {}

	public Chunk(Point coord, int chunkLevel) {
		this.coord = coord;
		this.chunkLevel = chunkLevel;
	}

	/** computes an inverseNormale and a center of gravity (ponderated average)
	 * if normal == 0, the surface is closed
	 */
	public void computeVisibilityCone() {
		centerOfGravity = new Point();
		inverseNormal = new Vector();
		totalArea = 0;

		if (chunkLevel == 1) {
			for (Map.Entry<Point, Object> entry : smallerChunks.entrySet()) {
				Triangle tri = (Triangle)entry.getValue();

				Vector normale = tri.getNormal();
				double area = normale.getNorm() / 2;
				totalArea += area;

				Point center = tri.getCenterOfGravity();
				center.multiply(area);
				centerOfGravity.add(center);
				inverseNormal.add(normale);
			}
			centerOfGravity.multiply(1.0 / totalArea);
			inverseNormal.multiply(-1);
		} else {
			for (Map.Entry<Point, Object> entry : smallerChunks.entrySet()) {
				Chunk chunk = (Chunk)entry.getValue();

				totalArea += chunk.getTotalArea();

				Point center = new Point(chunk.getCenterOfGravity());
				center.multiply(chunk.getTotalArea());
				centerOfGravity.add(center);

				inverseNormal.add(chunk.getInverseNormal());
			}
			centerOfGravity.multiply(1.0 / totalArea);
		}
	}

	//---GETTERS---
	
	public Point getCenterOfGravity() {
		return centerOfGravity;
	}

	public Vector getInverseNormal() {
		return inverseNormal;
	}

	public double getTotalArea() {
		return totalArea;
	}

	public Point getCoord() {
		return coord;
	}

	public int getChunkLevel() {
		return chunkLevel;
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
		Point pt = new Point(1, 1, 1);
		pt.multiply(chunkSize/2);
		pt.add(coord);

		return pt;
	}

	public TreeMap<Point, Object> getSmallerChunks() {
		return smallerChunks;
	}

	//---SETTERS---

	public void setSmallerChunks(TreeMap<Point, Object> smallerChunks) {
		this.smallerChunks = smallerChunks;
	}
}
