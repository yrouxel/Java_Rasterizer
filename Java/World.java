import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class World {
	private int biggerChunkSize;
	private ArrayList<Double> chunkSizes = new ArrayList<Double>();
	private int chunkLevels = 0;

	private TreeMap<Point, Object> chunks = new TreeMap<Point, Object>();

	public World(Double baseChunkSize, int biggerChunkSize) {
		chunkSizes.add(0, baseChunkSize);
		this.biggerChunkSize = biggerChunkSize;
	}

	public void addObjectToWorld(Object3D obj) {
		addObject(obj);
		countChunks(chunks, chunkLevels);
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

		if (chunkLevel > 0) {
			countChunks(listChunks, chunkLevel - 1);
		}
	}

	//** generates the pyramidal structure so that there is <8 chunks at the top */
	public void addChunksToChunks() {
		while (chunks.size() > 8) {
			chunkLevels++;
			chunkSizes.add(chunkLevels, biggerChunkSize * chunkSizes.get(chunkLevels - 1));

			TreeMap<Point, Object> biggerChunks = new TreeMap<Point, Object>();
			for (Map.Entry<Point, Object> entry : chunks.entrySet()) {
				addObjectToChunk(chunkLevels, chunkLevels, biggerChunks, entry.getKey(), entry.getValue());
			}
			chunks = biggerChunks;
		}
	}

	/** add an object to the given chunk level, creates its own container if necessary */
	public void addObjectToChunk(int currentChunkLevel, int destinationChunkLevel, TreeMap<Point, Object> subChunks, Point key, Object obj) {
		Double chunkSize = chunkSizes.get(currentChunkLevel);
		Point chunkPoint = computeChunkPoint(key, chunkSize);

		Chunk smallerChunk = (Chunk)subChunks.get(chunkPoint);

		if (destinationChunkLevel == currentChunkLevel) {
			if (smallerChunk != null) {
				smallerChunk.getSmallerChunks().put(key, obj);
			} else {
				smallerChunk = new Chunk(chunkPoint, chunkSize, currentChunkLevel);
				smallerChunk.getSmallerChunks().put(key, obj);
				subChunks.put(chunkPoint, smallerChunk);
			}
		} else {
			if (smallerChunk != null) {
				addObjectToChunk(currentChunkLevel - 1, destinationChunkLevel, smallerChunk.getSmallerChunks(), key, obj);
			} else {
				smallerChunk = new Chunk(chunkPoint, chunkSize, currentChunkLevel);
				subChunks.put(chunkPoint, smallerChunk);
				addObjectToChunk(currentChunkLevel - 1, destinationChunkLevel, smallerChunk.getSmallerChunks(), key, obj);
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

	public void addObjectWithoutChunks(Object3D obj) {
		int triProcessed = 0;
		Chunk chunk = new Chunk(new Point(-100, -100, -100), 10000.0, 0);
		chunks.put(new Point(-100, -100, -100), chunk);

		for (Triangle tri : obj.getTriangles()) {
			chunk.getSmallerChunks().put(tri.getCenterOfGravity(), tri);
			triProcessed++;
			if (triProcessed%100000 == 0) {
				System.out.println("PROCESSED " + triProcessed + " TRIANGLES");
			}
		}
	}

	public void addObject(Object3D obj) {
		int triProcessed = 0;
		ArrayList<Triangle> trianglesToUse = obj.getTriangles();
		// ArrayList<Triangle> trianglesToUse = obj.computeEdgesReduction(obj.generateEdges(obj.getTriangles()), 0.9);
		for (Triangle tri : trianglesToUse) {
			addObjectToChunk(chunkLevels, 0, chunks, tri.getCenterOfGravity(), tri);

			triProcessed++;
			if (triProcessed%100000 == 0) {
				// addChunksToChunks();
				System.out.println("PROCESSED " + triProcessed + " TRIANGLES");
			}
		}

		addChunksToChunks();
		countChunks(chunks, chunkLevels);
		chunks = removeUnnecessaryChunks(chunks);
		System.out.println("\nREMOVING UNNECESSARY CHUNKS \n");
	}

	public ArrayList<Triangle> generateWorkableTriangles(Chunk chunk) {
		ArrayList<Triangle> workableTriangles = new ArrayList<Triangle>();

		for (Map.Entry<Point, Object> entry : chunk.getSmallerChunks().entrySet()) {
			Triangle tri = (Triangle)entry.getValue();

			if (isTriangleEntirelyInChunk(chunk.getCoord(), tri)) {
				workableTriangles.add(tri);
			}
		}

		return workableTriangles;
	}

	public boolean isTriangleEntirelyInChunk(Point coord, Triangle tri) {
		for (Point point : tri.getPoints()) {
			Point p = computeChunkPoint(point, chunkSizes.get(0));
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

	public ArrayList<Double> getChunkSizes() {
		return chunkSizes;
	}

	public TreeMap<Point, Object> getChunks() {
		return chunks;
	}

	public int getChunkLevel() {
		return chunkLevels;
	}
}