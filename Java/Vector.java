public class Vector {
	private double x, y, z;

	public Vector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector(Point p1,  Point p2) {
		x = p1.getX() - p2.getX();
		y = p1.getY() - p2.getY();
		z = p1.getZ() - p2.getZ();
	}		

	public void normalize() {
		double norm = Math.sqrt(x*x + y*y + z*z);
		x = x/norm;
		y = y/norm;
		z = z/norm;
	}

	public Vector substract(Vector vec) {
		return new Vector(x - vec.x, y - vec.y, z - vec.z);
	}

	public double projectOnAlpha(double alphaMax) {
		return - Math.sin(Math.PI * Math.atan(y / x) / alphaMax);
	}

	public double projectOnBeta(double alphaMax) {
		return Math.sin(Math.PI * Math.asin(z) / alphaMax);
	}

	@Override
	public String toString() {
		return "Vector [x=" + x + ", y=" + y + ", z=" + z + "]";
	}
}
