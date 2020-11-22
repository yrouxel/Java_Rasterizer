import java.util.Map;
import java.util.TreeMap;

public class World {
	private static double chunkSize = 10;
	private static int biggerChunkSize = 4;
	private TreeMap<Point, Chunk> chunks = new TreeMap<Point, Chunk>();

	/*public void addObject(Object3D obj) {
		int xBigChunk; 
		int yBigChunk;
		int zBigChunk;

		int xChunk;
		int yChunk;
		int zChunk;

		boolean found;

		for (Triangle tri : obj.getFaces()) {
			for (Point pt : tri.getPoints()) {
				found = false;

				xBigChunk = (int)(pt.getX()/(chunkSize*biggerChunkSize));
				yBigChunk = (int)(pt.getY()/(chunkSize*biggerChunkSize));
				zBigChunk = (int)(pt.getZ()/(chunkSize*biggerChunkSize));

				xChunk = (int)((pt.getX()%biggerChunkSize)/chunkSize);
				yChunk = (int)((pt.getY()%biggerChunkSize)/chunkSize);
				zChunk = (int)((pt.getZ()%biggerChunkSize)/chunkSize);

				for (BiggerChunk bigChunk : biggerChunks) {
					if (bigChunk.getX() == xBigChunk && bigChunk.getY() == yBigChunk && bigChunk.getZ() == zBigChunk) {
						found = true;
						if (bigChunk.getChunks()[xChunk][yChunk][zChunk] != null) {
							bigChunk.getChunks()[xChunk][yChunk][zChunk].addTriangle(tri);
						} else {
							Chunk chunk = new Chunk();
							chunk.addTriangle(tri);
							bigChunk.getChunks()[xChunk][yChunk][zChunk] = chunk;
						}
					}
					break;
				}

				if (!found) {
					BiggerChunk bigChunk = new BiggerChunk(biggerChunkSize, xBigChunk, yBigChunk, zBigChunk);
					Chunk chunk = new Chunk();
					chunk.addTriangle(tri);
					bigChunk.getChunks()[xChunk][yChunk][zChunk] = chunk;
					biggerChunks.add(bigChunk);
				}
			}
		}
	}*/

	public void addObject(Object3D obj) {
		Point chunkPoint1;
		Point chunkPoint2;
		Point chunkPoint3;

		int triProcessed = 0;
		int triKProcessed = 0;

		for (Triangle tri : obj.getFaces()) {
			chunkPoint1 = getChunkPoint(tri.getPoints()[0]);
			chunkPoint2 = getChunkPoint(tri.getPoints()[1]);
			chunkPoint3 = getChunkPoint(tri.getPoints()[2]);

			if (chunkPoint1.equals(chunkPoint2)) {
				if (chunkPoint1.equals(chunkPoint3)) {
					addTriangleToChunk(chunkPoint1, tri);
				} else {
					addTriangleToChunk(chunkPoint1, tri);
					addTriangleToChunk(chunkPoint3, tri);
				}
			} else if (chunkPoint1.equals(chunkPoint3) || chunkPoint2.equals(chunkPoint3)) {
				addTriangleToChunk(chunkPoint1, tri);
				addTriangleToChunk(chunkPoint2, tri);
			} else {
				addTriangleToChunk(chunkPoint1, tri);
				addTriangleToChunk(chunkPoint2, tri);
				addTriangleToChunk(chunkPoint3, tri);
			}

			triProcessed++;
			if (triProcessed == 100000) {
				triProcessed = 0;
				triKProcessed++;
				System.out.println("PROCESSED " + (triKProcessed * 100000) + " TRIANGLES");
			}
		}
	}

	public Point getChunkPoint(Point pt) {
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

		return new Point(x, y, z);
	}

	public int getBiggerChunkSize() {
		return biggerChunkSize;
	}

	public double getChunkSize() {
		return chunkSize;
	}

	public TreeMap<Point, Chunk> getChunks() {
		return chunks;
	}

	public void addTriangleToChunk(Point chunkPoint, Triangle tri) {
		chunkPoint.multiply(chunkSize);
		Chunk chunk;

		chunk = chunks.get(chunkPoint);
		if (chunk != null) {
			chunk.addTriangle(tri);
		} else {
			chunk = new Chunk(chunkPoint);
			chunk.addTriangle(tri);
			chunks.put(chunkPoint, chunk);
		}
	}

	@Override
	public String toString() {
		long totalTriangles = 0;
		for (Map.Entry<Point, Chunk> entry : chunks.entrySet()) {
			totalTriangles += entry.getValue().getTriangles().size();
		}
		return "TOTAL CHUNKS = " + chunks.size() + "\nTOTAL TRIANGLES = " + totalTriangles;
	}
}
