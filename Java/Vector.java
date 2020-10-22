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

	public Vector applyThetaRotation(double theta) {
		return new Vector(x*Math.cos(theta) + y*Math.sin(theta), y*Math.cos(theta) - x*Math.sin(theta), z);
	}

	public Vector applyPhiRotation(double phi) {
		return new Vector(x, y*Math.cos(phi) + z*Math.sin(phi), z*Math.cos(phi) - y*Math.sin(phi));
	}

	@Override
	public String toString() {
		return "Vector [x=" + x + ", y=" + y + ", z=" + z + "]";
	}
}
