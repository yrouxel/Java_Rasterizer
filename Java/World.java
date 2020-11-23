import java.util.Map;
import java.util.TreeMap;

public class World {
	private static double chunkSize = 10;
	private static int biggerChunkSize = 2;
	private int chunkLevels = 1;

	private TreeMap<Point, Object> bigChunks = new TreeMap<Point, Object>();

	public void addObjectToWorld(Object3D obj) {
		addObject(obj);
		System.out.println("OBJECT ADDED");
		// checkChunks();
		addChunksToChunks();
		// System.out.println("CHUNKS MODIFIED");
		// checkType(bigChunks, chunkLevels);
		// countChunkLevels(bigChunks, chunkLevels);
	}

	public void checkType(TreeMap<Point, Object> chunk, int chunkLevel) {
		int bigChunksCount = 0;
		int triangleCount = 0;

		for (Map.Entry<Point, Object> entry : chunk.entrySet()) {
			if (entry.getValue() instanceof Chunk) {
				bigChunksCount++;
			} else {
				triangleCount++;
			}
		}
		if (bigChunksCount != 0 && triangleCount != 0) {
			System.out.println("TYPE PROBLEM");
		}
		if (triangleCount != 0 && chunkLevel != 0) {
			System.out.println("TRIANGLE COUNT : " + triangleCount);
			System.out.println("CHUNK LEVEL : " + chunkLevel);
		}
		for (Map.Entry<Point, Object> entry : chunk.entrySet()) {
			if (entry.getValue() instanceof Chunk) {
				checkType(((Chunk)entry.getValue()).getSmallerChunks(), chunkLevel - 1);
			}
		}
	}

	public void countChunkLevels(TreeMap<Point, Object> bigChunks, int currentChunkLevel) {
		for (Map.Entry<Point, Object> entry : bigChunks.entrySet()) {
			if (entry.getValue() instanceof Chunk) {
				countChunkLevels(((Chunk)entry.getValue()).getSmallerChunks(), currentChunkLevel - 1);
			} else if (currentChunkLevel < 0) {
				System.out.println("WARNING CHUNK LEVEL = " + currentChunkLevel);
			}
		}
	}

	public void addChunksToChunks() {
		System.out.println("CHUNK LEVEL " + chunkLevels + " CONTAINING : " + bigChunks.size() + " ELEMENTS");
		if (bigChunks.size() > 8) {
			chunkLevels++;
			TreeMap<Point, Object> biggerChunks = new TreeMap<Point, Object>();
			for (Map.Entry<Point, Object> entry : bigChunks.entrySet()) {
				addObjectToChunk(chunkLevels, chunkLevels, entry.getKey(), biggerChunks, entry.getValue());
			}
			bigChunks = biggerChunks;
			addChunksToChunks();
		}
	}

	public void addObjectToChunk(int currentChunkLevel, int destinationChunkLevel, Point pt, TreeMap<Point, Object> subChunks, Object obj) {
		Point chunkPoint = getChunkPoint(pt, chunkSize * Math.pow(biggerChunkSize, currentChunkLevel-1));

		Chunk smallerChunk = (Chunk)subChunks.get(chunkPoint);

		if (destinationChunkLevel == currentChunkLevel) {
			if (smallerChunk != null) {
				smallerChunk.getSmallerChunks().put(pt, obj);
			} else {
				smallerChunk = new Chunk(chunkPoint);
				smallerChunk.getSmallerChunks().put(pt, obj);
				subChunks.put(chunkPoint, smallerChunk);
			}
		} else {
			if (smallerChunk != null) {
				addObjectToChunk(currentChunkLevel - 1, destinationChunkLevel, pt, smallerChunk.getSmallerChunks(), obj);
			} else {
				smallerChunk = new Chunk(chunkPoint);
				subChunks.put(chunkPoint, smallerChunk);
				addObjectToChunk(currentChunkLevel - 1, destinationChunkLevel, pt, smallerChunk.getSmallerChunks(), obj);
			}
		}
	}

	public void addObject(Object3D obj) {
		Point chunkPoint1;
		Point chunkPoint2;
		Point chunkPoint3;

		int triProcessed = 0;
		int triKProcessed = 0;

		for (Triangle tri : obj.getFaces()) {
			chunkPoint1 = getChunkPoint(tri.getPoints()[0], chunkSize);
			chunkPoint2 = getChunkPoint(tri.getPoints()[1], chunkSize);
			chunkPoint3 = getChunkPoint(tri.getPoints()[2], chunkSize);

			if (chunkPoint1.equals(chunkPoint2)) {
				if (chunkPoint1.equals(chunkPoint3)) {
					addObjectToChunk(chunkLevels, 1, tri.getPoints()[0], bigChunks, tri);
				} else {
					addObjectToChunk(chunkLevels, 1, tri.getPoints()[0], bigChunks, tri);
					addObjectToChunk(chunkLevels, 1, tri.getPoints()[2], bigChunks, tri);
				}
			} else if (chunkPoint1.equals(chunkPoint3) || chunkPoint2.equals(chunkPoint3)) {
				addObjectToChunk(chunkLevels, 1, tri.getPoints()[0], bigChunks, tri);
				addObjectToChunk(chunkLevels, 1, tri.getPoints()[1], bigChunks, tri);
			} else {
				addObjectToChunk(chunkLevels, 1, tri.getPoints()[0], bigChunks, tri);
				addObjectToChunk(chunkLevels, 1, tri.getPoints()[1], bigChunks, tri);
				addObjectToChunk(chunkLevels, 1, tri.getPoints()[2], bigChunks, tri);
			}

			triProcessed++;
			if (triProcessed == 100000) {
				// addChunksToChunks();
				triProcessed = 0;
				triKProcessed++;
				System.out.println("PROCESSED " + (triKProcessed * 100000) + " TRIANGLES");
			}
		}
	}

	public Point getChunkPoint(Point pt, double chunkSize) {
		int x, y, z;
		x = (int)(pt.getX()/chunkSize);
		y = (int)(pt.getY()/chunkSize);
		z = (int)(pt.getZ()/chunkSize);

		if (pt.getX() < 0) {
			x--;
		}
		if (pt.getY() < 0) {
			y--;
		}
		if (pt.getZ() < 0) {
			z--;
		}

		Point chunkPoint = new Point(x, y, z);
		chunkPoint.multiply(chunkSize);

		return chunkPoint;
	}

	public int getBiggerChunkSize() {
		return biggerChunkSize;
	}

	public double getChunkSize() {
		return chunkSize;
	}

	public TreeMap<Point, Object> getBigChunks() {
		return bigChunks;
	}

	public int getChunkLevel() {
		return chunkLevels;
	}
}