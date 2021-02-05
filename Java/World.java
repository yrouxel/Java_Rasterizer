import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class World {
	private static double chunkSize = 10;
	private static int biggerChunkSize = 2;
	private int chunkLevels = 1;

	private TreeMap<Point, Surface> chunks = new TreeMap<Point, Surface>();

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

	public int countTriangles(int total, TreeMap<Point, Surface> chunk) {
		for (Map.Entry<Point, Surface> entry : chunk.entrySet()) {
			Chunk subChunk = (Chunk)entry.getValue();
			if (subChunk.getChunkLevel() == 1) {
				total += subChunk.getSmallerChunks().size();
			} else {
				total = countTriangles(total, ((Chunk)entry.getValue()).getSmallerChunks());
			}
		}
		return total;
	}

	public void checkType(TreeMap<Point, Surface> chunk, int chunkLevel) {
		int chunksCount = 0;
		int triangleCount = 0;

		for (Map.Entry<Point, Surface> entry : chunk.entrySet()) {
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
		for (Map.Entry<Point, Surface> entry : chunk.entrySet()) {
			if (entry.getValue() instanceof Chunk) {
				checkType(((Chunk)entry.getValue()).getSmallerChunks(), chunkLevel - 1);
			}
		}
	}

	public void countChunks(TreeMap<Point, Surface> chunks, int chunkLevel) {
		int total = 0;
		TreeMap<Point, Surface> listChunks = new TreeMap<Point, Surface>();
		for (Map.Entry<Point, Surface> entry : chunks.entrySet()) {
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

	//** generates the pyramidal structure so that there is <8 chunks at the top */
	public void addChunksToChunks() {
		while (chunks.size() > 8) {
			chunkLevels++;
			TreeMap<Point, Surface> biggerChunks = new TreeMap<Point, Surface>();
			for (Map.Entry<Point, Surface> entry : chunks.entrySet()) {
				addObjectToChunk(chunkLevels, chunkLevels, entry.getKey(), biggerChunks, entry.getKey(), entry.getValue());
			}
			chunks = biggerChunks;
		}
	}

	/** add an object to the given chunk level, creates its own container if necessary */
	public void addObjectToChunk(int currentChunkLevel, int destinationChunkLevel, Point pt, TreeMap<Point, Surface> subChunks, Point key, Surface obj) {
		Point chunkPoint = computeChunkPoint(pt, chunkSize * Math.pow(biggerChunkSize, currentChunkLevel-1));

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
	public TreeMap<Point, Surface> removeUnnecessaryChunks(TreeMap<Point, Surface> chunks) {
		TreeMap<Point, Surface> updatedChunks = new TreeMap<Point, Surface>();
		
		for (Map.Entry<Point, Surface> entry : chunks.entrySet()) {
			Chunk chunk = (Chunk)entry.getValue();
			TreeMap<Point, Surface> subChunks = chunk.getSmallerChunks();

			if (chunk.getChunkLevel() > 2) {
				subChunks = removeUnnecessaryChunks(subChunks);
				chunk.setSmallerChunks(subChunks);
			}

			if (subChunks.size() == 1) {
				Map.Entry<Point, Surface> subEntry = subChunks.firstEntry();

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

		ArrayList<Triangle> trianglesToUse = obj.getTriangles();
		// ArrayList<Triangle> trianglesToUse = obj.computeEdgesReduction(obj.generateEdges(obj.getTriangles()), 0.99);
		for (Triangle tri : trianglesToUse) {
			chunkPoint1 = computeChunkPoint(tri.getPoints()[0], chunkSize);
			chunkPoint2 = computeChunkPoint(tri.getPoints()[1], chunkSize);
			chunkPoint3 = computeChunkPoint(tri.getPoints()[2], chunkSize);

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

	public ArrayList<Triangle> generateWorkableTriangles(Chunk chunk) {
		ArrayList<Triangle> workableTriangles = new ArrayList<Triangle>();

		for (Map.Entry<Point, Surface> entry : chunk.getSmallerChunks().entrySet()) {
			Triangle tri = (Triangle)entry.getValue();

			if (isTriangleEntirelyInChunk(chunk.getCoord(), tri)) {
				workableTriangles.add(tri);
			}
		}

		return workableTriangles;
	}

	public boolean isTriangleEntirelyInChunk(Point coord, Triangle tri) {
		for (Point point : tri.getPoints()) {
			Point p = computeChunkPoint(point, chunkSize);
			if (!p.equals(coord)) {
				return false;
			}
		}
		return true;
	}


	/** returns the coordinates of the chunk a point should be contained by */
	public Point computeChunkPoint(Point pt, double chunkSize) {
		double x = (int)Math.floor(pt.getX()/chunkSize);
		double y = (int)Math.floor(pt.getY()/chunkSize);
		double z = (int)Math.floor(pt.getZ()/chunkSize);

		Point chunkPoint = new Point(x, y, z);
		chunkPoint.multiply(chunkSize);

		return chunkPoint;
	}

	//---GETTERS---

	public int getBiggerChunkSize() {
		return biggerChunkSize;
	}

	public double getChunkSize() {
		return chunkSize;
	}

	public TreeMap<Point, Surface> getChunks() {
		return chunks;
	}

	public int getChunkLevel() {
		return chunkLevels;
	}
}