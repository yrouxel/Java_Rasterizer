public class Edge implements Comparable<Edge> {
	private Point a, b;

	public Edge(Point a, Point b) {
		this.a = a;
		this.b = b;
	}

	public double getLength() {
		return new Vector(a, b).getNorm();
	}

	public Point getPointA() {
		return a;
	}

	public Point getPointB() {
		return b;
	}

	public Point getMiddlePoint() {
		Point p = new Point();
		p.add(a);
		p.add(b);
		p.multiply(1.0/2.0);
		return p;
	}

	@Override
	public int compareTo(Edge v) {
		if (getLength() < v.getLength()) {
			return -1;
		} else if (getLength() > v.getLength()) {
			return 1;
		} else if ((a.equals(v.a) && b.equals(v.b)) || (b.equals(v.a) && a.equals(v.b))) {
			return 0;
		} else {
			return getMiddlePoint().compareTo(v.getMiddlePoint());
		}
	}
}
