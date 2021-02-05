import java.util.Map;
import java.util.TreeMap;

/** base brick of the world, each chunk contains sub elements */
public class Chunk extends Surface {
	private Point coord;
	private Double chunkSize;
	private int chunkLevel;
	private TreeMap<Point, Surface> smallerChunks = new TreeMap<Point, Surface>();

	private Point centerOfGravity;
	private Point coneTop;
	private Vector normal;
	private double coneAngle;
	private double totalSurface;

	public Chunk() {}

	public Chunk(Point coord, double chunkSize, int chunkLevel) {
		this.coord = coord;
		this.chunkSize = chunkSize;
		this.chunkLevel = chunkLevel;
	}

	/** computes an normale and a center of gravity (ponderated average)
	 * if normal == 0, the surface is closed
	 */
	public void computeVisibilityCone() {
		centerOfGravity = new Point();
		normal = new Vector();
		totalSurface = 0;

		for (Map.Entry<Point, Surface> entry : smallerChunks.entrySet()) {
			totalSurface += entry.getValue().getTotalSurface();
			centerOfGravity.add(entry.getValue().getCenterOfGravity());
			normal.add(entry.getValue().getNormal());
		}

		Surface maxSurface = getSortedSurfacesByScalarProduct();
		if (maxSurface != null) {
			
		}
	}

	public Surface getSortedSurfacesByScalarProduct() {
		if (normal.getNorm() == 0) {
			return null;
		}

		Surface maxSurface = null;
		double minScalarProduct = 0;

		for (Map.Entry<Point, Surface> entry : smallerChunks.entrySet()) {
			double scalarProduct = entry.getValue().getNormal().getScalarProduct(normal);
			if (scalarProduct <= 0) {
				return null;
			}
			if (minScalarProduct < -scalarProduct) {
				minScalarProduct = -scalarProduct;
				maxSurface = entry.getValue();
			}
		}
		return maxSurface;
	}

	//---GETTERS---
	
	public Point getCenterOfGravity() {
		return centerOfGravity;
	}

	public Vector getNormal() {
		return normal;
	}

	public double getTotalSurface() {
		return totalSurface;
	}

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
	public Point getCenter() {
		Point pt = new Point(1, 1, 1);
		pt.multiply(chunkSize/2);
		pt.add(coord);

		return pt;
	}

	public TreeMap<Point, Surface> getSmallerChunks() {
		return smallerChunks;
	}

	public double getChunkSize() {
		return chunkSize;
	}

	//---SETTERS---

	public void setSmallerChunks(TreeMap<Point, Surface> smallerChunks) {
		this.smallerChunks = smallerChunks;
	}
}
