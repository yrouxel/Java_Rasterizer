import java.util.Map;
import java.util.TreeMap;

public class World {
	private static double chunkSize = 10;
	private static int biggerChunkSize = 2;
	private int chunkLevels = 1;

	private TreeMap<Point, Object> chunks = new TreeMap<Point, Object>();

	public void addObjectToWorld(Object3D obj) {
		addObject2(obj);
		addChunksToChunks();
		// System.out.println("TRIANGLE COUNT : " + countTriangles(0, chunkLevels, chunks));
		// checkType(chunks, chunkLevels);
		// countChunkLevels(chunks, chunkLevels);
	}

	public int countTriangles(int total, int currentChunkLevel, TreeMap<Point, Object> chunk) {
		if (currentChunkLevel == 0) {
			return total + chunk.entrySet().size();
		} else {
			for (Map.Entry<Point, Object> entry : chunk.entrySet()) {
				total = countTriangles(total, currentChunkLevel - 1, ((Chunk)entry.getValue()).getSmallerChunks());
			}
			return total;
		}
	}

	public void checkType(TreeMap<Point, Object> chunk, int chunkLevel) {
		int chunksCount = 0;
		int triangleCount = 0;

		for (Map.Entry<Point, Object> entry : chunk.entrySet()) {
			if (entry.getValue() instanceof Chunk) {
				chunksCount++;
			} else {
				triangleCount++;
			}
		}
		if (chunksCount != 0 && triangleCount != 0) {
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

	public void countChunkLevels(TreeMap<Point, Object> chunks, int currentChunkLevel) {
		for (Map.Entry<Point, Object> entry : chunks.entrySet()) {
			if (entry.getValue() instanceof Chunk) {
				countChunkLevels(((Chunk)entry.getValue()).getSmallerChunks(), currentChunkLevel - 1);
			} else if (currentChunkLevel < 0) {
				System.out.println("WARNING CHUNK LEVEL = " + currentChunkLevel);
			}
		}
	}

	public void addChunksToChunks() {
		System.out.println("CHUNK LEVEL " + chunkLevels + " CONTAINS : " + chunks.size() + " ELEMENTS");
		if (chunks.size() > 8) {
			chunkLevels++;
			TreeMap<Point, Object> biggerChunks = new TreeMap<Point, Object>();
			for (Map.Entry<Point, Object> entry : chunks.entrySet()) {
				addObjectToChunk(chunkLevels, chunkLevels, entry.getKey(), biggerChunks, entry.getValue());
			}
			chunks = biggerChunks;
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

		System.out.println("TRIANGLE COUNT : " + obj.getTriangles().size());
		for (Triangle tri : obj.getTriangles()) {
			chunkPoint1 = getChunkPoint(tri.getPoints()[0], chunkSize);
			chunkPoint2 = getChunkPoint(tri.getPoints()[1], chunkSize);
			chunkPoint3 = getChunkPoint(tri.getPoints()[2], chunkSize);

			if (chunkPoint1.equals(chunkPoint2)) {
				if (chunkPoint1.equals(chunkPoint3)) {
					addObjectToChunk(chunkLevels, 1, tri.getPoints()[0], chunks, tri);
				} else {
					addObjectToChunk(chunkLevels, 1, tri.getPoints()[0], chunks, tri);
					addObjectToChunk(chunkLevels, 1, tri.getPoints()[2], chunks, tri);
				}
			} else if (chunkPoint1.equals(chunkPoint3) || chunkPoint2.equals(chunkPoint3)) {
				addObjectToChunk(chunkLevels, 1, tri.getPoints()[0], chunks, tri);
				addObjectToChunk(chunkLevels, 1, tri.getPoints()[1], chunks, tri);
			} else {
				addObjectToChunk(chunkLevels, 1, tri.getPoints()[0], chunks, tri);
				addObjectToChunk(chunkLevels, 1, tri.getPoints()[1], chunks, tri);
				addObjectToChunk(chunkLevels, 1, tri.getPoints()[2], chunks, tri);
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

	public void addObject2(Object3D obj) {
		int triProcessed = 0;
		int triKProcessed = 0;

		System.out.println("TRIANGLE COUNT : " + obj.getTriangles().size());
		for (Triangle tri : obj.getTriangles()) {
			addObjectToChunk(chunkLevels, 1, tri.getCenterOfGravity(), chunks, tri);;

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

	public TreeMap<Point, Object> getChunks() {
		return chunks;
	}

	public int getChunkLevel() {
		return chunkLevels;
	}
}