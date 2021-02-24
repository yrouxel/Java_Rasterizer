import java.util.Map;
import java.util.TreeMap;
import java.awt.image.BufferedImage;

/** base brick of the world, each chunk contains sub elements */
public class Chunk extends Surface implements Comparable<Chunk> {
	private BufferedImage texture;

	private Point coord;
	private Double chunkSize;
	private int chunkLevel;
	private TreeMap<Point, Surface> smallerChunks = new TreeMap<Point, Surface>();

	private boolean alwaysVisible = false;
	private Point centerOfGravity;
	private Point coneTop;
	private Vector normal;
	private double coneAngle;
	private double totalSurface;
	private double distanceToPoint;

	public Chunk() {}

	public Chunk(Point coord, Double chunkSize, int chunkLevel) {
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

		for (Surface surface : smallerChunks.values()) {
			if (chunkLevel > 0) {
				((Chunk)surface).computeVisibilityCone();
			}
			totalSurface += surface.getTotalSurface();
			centerOfGravity.add(surface.getCenterOfGravity());
			normal.add(surface.getNormal());
		}

		Surface maxSurface = getSortedSurfacesByScalarProduct();
		if (maxSurface != null) {
			double d = new Vector(maxSurface.getCenterOfGravity(), centerOfGravity).getScalarProduct(maxSurface.getNormal()) / normal.getScalarProduct(maxSurface.getNormal());
			coneTop = new Point(centerOfGravity, normal, d);

			Vector closestLine = normal.getCrossProduct(maxSurface.getNormal()).getCrossProduct(maxSurface.getNormal());
			coneAngle = normal.getScalarProduct(closestLine) / (normal.getNorm() * closestLine.getNorm());
		} else {
			alwaysVisible = true;
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

	public Point getConeTop() {
		return coneTop;
	}

	public double getConeAngle() {
		return coneAngle;
	}

	public boolean isAlwaysVisible() {
		return true;
	}

	public double getDistanceToPoint() {
		return distanceToPoint;
	}

	//---SETTERS---

	public void setTexture(BufferedImage texture) {
		this.texture = texture;
	}

	public void setSmallerChunks(TreeMap<Point, Surface> smallerChunks) {
		this.smallerChunks = smallerChunks;
	}

	 public void setDistanceToPoint(double distanceToPoint) {
		this.distanceToPoint = distanceToPoint;
	}

	@Override
	public int compareTo(Chunk chunk) {
		if (distanceToPoint == chunk.getDistanceToPoint()) {
			return coord.compareTo(chunk.getCoord());
		} else if (distanceToPoint < chunk.getDistanceToPoint()) {
			return 1;
		} else {
			return -1;
		}
	}
}
