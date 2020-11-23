import java.util.TreeMap;

public class Chunk{
	private Point coord;
	private TreeMap<Point, Object> smallerChunks = new TreeMap<Point, Object>();

	public Chunk(Point coord) {
		this.coord = coord;
	}

	public Point getCoord() {
		return coord;
	}

	public TreeMap<Point, Object> getSmallerChunks() {
		return smallerChunks;
	}
}
