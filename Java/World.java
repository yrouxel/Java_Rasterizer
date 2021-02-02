import java.util.Map;
import java.util.TreeMap;

public class World {
	private static double chunkSize = 10;
	private static int biggerChunkSize = 2;
	private int chunkLevels = 1;

	private TreeMap<Point, Object> chunks = new TreeMap<Point, Object>();

	public void addObjectToWorld(Object3D obj) {
		addObject(obj);
		addChunksToChunks();
		countChunks(chunks, chunkLevels);
		chunks = removeUnnecessaryChunks(chunks);
		System.out.println("\nREMOVING UNNECESSARY CHUNKS \n");

		countChunks(chunks, chunkLevels);
		System.out.println("TRIANGLE COUNT : " + countTriangles(0, chunks));
		// checkType(chunks, chunkLevels);
	}

	public int countTriangles(int total, TreeMap<Point, Object> chunk) {
		for (Map.Entry<Point, Object> entry : chunk.entrySet()) {
			Chunk subChunk = (Chunk)entry.getValue();
			if (subChunk.getChunkLevel() == 1) {
				total += subChunk.getSmallerChunks().size();
			} else {
				total = countTriangles(total, ((Chunk)entry.getValue()).getSmallerChunks());
			}
		}
		return total;
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

	public void countChunks(TreeMap<Point, Object> chunks, int chunkLevel) {
		int total = 0;
		TreeMap<Point, Object> listChunks = new TreeMap<Point, Object>();
		for (Map.Entry<Point, Object> entry : chunks.entrySet()) {
			Chunk chunk = (Chunk)entry.getValue();
			if (chunk.getChunkLevel() == chunkLevel) {
				total++;
				listChunks.putAll(chunk.getSmallerChunks());
			} else {
				listChunks.put(entry.getKey(), chunk);
			}
		}

		System.out.println("CHUNK LEVEL " + chunkLevel + " CONTAINS : " + total + " ELEMENTS");

		if (chunkLevel > 1) {
			countChunks(listChunks, chunkLevel - 1);
		}
	}

	public void addChunksToChunks() {
		while (chunks.size() > 8) {
			chunkLevels++;
			TreeMap<Point, Object> biggerChunks = new TreeMap<Point, Object>();
			for (Map.Entry<Point, Object> entry : chunks.entrySet()) {
				addObjectToChunk(chunkLevels, chunkLevels, entry.getKey(), biggerChunks, entry.getKey(), entry.getValue());
			}
			chunks = biggerChunks;
		}
	}

	public void addObjectToChunk(int currentChunkLevel, int destinationChunkLevel, Point pt, TreeMap<Point, Object> subChunks, Point key, Object obj) {
		Point chunkPoint = getChunkPoint(pt, chunkSize * Math.pow(biggerChunkSize, currentChunkLevel-1));

		Chunk smallerChunk = (Chunk)subChunks.get(chunkPoint);

		if (destinationChunkLevel == currentChunkLevel) {
			if (smallerChunk != null) {
				smallerChunk.getSmallerChunks().put(key, obj);
			} else {
				smallerChunk = new Chunk(chunkPoint, currentChunkLevel);
				smallerChunk.getSmallerChunks().put(key, obj);
				subChunks.put(chunkPoint, smallerChunk);
			}
		} else {
			if (smallerChunk != null) {
				addObjectToChunk(currentChunkLevel - 1, destinationChunkLevel, pt, smallerChunk.getSmallerChunks(), key, obj);
			} else {
				smallerChunk = new Chunk(chunkPoint, currentChunkLevel);
				subChunks.put(chunkPoint, smallerChunk);
				addObjectToChunk(currentChunkLevel - 1, destinationChunkLevel, pt, smallerChunk.getSmallerChunks(), key, obj);
			}
		}
	}

	/** if a chunk contains only one subChunk, the subChunk becomes the top chunk */
	public TreeMap<Point, Object> removeUnnecessaryChunks(TreeMap<Point, Object> chunks) {
		TreeMap<Point, Object> updatedChunks = new TreeMap<Point, Object>();
		
		for (Map.Entry<Point, Object> entry : chunks.entrySet()) {
			Chunk chunk = (Chunk)entry.getValue();
			TreeMap<Point, Object> subChunks = chunk.getSmallerChunks();

			if (chunk.getChunkLevel() > 1) {
				subChunks = removeUnnecessaryChunks(subChunks);
				chunk.setSmallerChunks(subChunks);
			}

			if (subChunks.size() == 1) {
				Map.Entry<Point, Object> subEntry = subChunks.firstEntry();

				updatedChunks.put(subEntry.getKey(), subEntry.getValue());
			} else if (subChunks.size() > 1) {
				updatedChunks.put(entry.getKey(), entry.getValue());
			}
		}
		return updatedChunks;
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
					addObjectToChunk(chunkLevels, 1, tri.getPoints()[0], chunks, tri.getCenterOfGravity(), tri);
				} else {
					addObjectToChunk(chunkLevels, 1, tri.getPoints()[0], chunks, tri.getCenterOfGravity(), tri);
					addObjectToChunk(chunkLevels, 1, tri.getPoints()[2], chunks, tri.getCenterOfGravity(), tri);
				}
			} else if (chunkPoint1.equals(chunkPoint3) || chunkPoint2.equals(chunkPoint3)) {
				addObjectToChunk(chunkLevels, 1, tri.getPoints()[0], chunks, tri.getCenterOfGravity(), tri);
				addObjectToChunk(chunkLevels, 1, tri.getPoints()[1], chunks, tri.getCenterOfGravity(), tri);
			} else {
				addObjectToChunk(chunkLevels, 1, tri.getPoints()[0], chunks, tri.getCenterOfGravity(), tri);
				addObjectToChunk(chunkLevels, 1, tri.getPoints()[1], chunks, tri.getCenterOfGravity(), tri);
				addObjectToChunk(chunkLevels, 1, tri.getPoints()[2], chunks, tri.getCenterOfGravity(), tri);
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

	// public void addObject2(Object3D obj) {
	// 	int triProcessed = 0;
	// 	int triKProcessed = 0;

	// 	System.out.println("TRIANGLE COUNT : " + obj.getTriangles().size());
	// 	for (Triangle tri : obj.getTriangles()) {
	// 		for (Point pt : tri.getPoints()) {
	// 			addObjectToChunk(chunkLevels, 1, pt, chunks, tri.getCenterOfGravity(), tri);;

	// 			triProcessed++;
	// 			if (triProcessed == 100000) {
	// 				// addChunksToChunks();
	// 				triProcessed = 0;
	// 				triKProcessed++;
	// 				System.out.println("PROCESSED " + (triKProcessed * 100000) + " TRIANGLES");
	// 			}
	// 		}
	// 	}
	// }

	public Point getChunkPoint(Point pt, double chunkSize) {
		double x = (int)Math.floor(pt.getX()/chunkSize);
		double y = (int)Math.floor(pt.getY()/chunkSize);
		double z = (int)Math.floor(pt.getZ()/chunkSize);

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