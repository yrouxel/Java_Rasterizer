import java.util.TreeMap;

public class BiggerChunk extends Chunk{
	private TreeMap<Point, Chunk> chunks = new TreeMap<Point, Chunk>();

	public void addTriangleToChunk(Point chunkPoint, Triangle tri) {
		Chunk chunk;

		chunk = chunks.get(chunkPoint);
		if (chunk != null) {
			chunk.addTriangle(tri);
		} else {
			chunk = new Chunk();
			chunk.addTriangle(tri);
			chunks.put(chunkPoint, chunk);
		}
	}
}
