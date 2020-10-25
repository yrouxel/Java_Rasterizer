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

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public void add(Vector v) {
		x += v.x;
		y += v.y;
		z += v.z;
	}

	public void rotate(double theta, double phi) {
        applyThetaRotation(theta);
        applyPhiRotation(phi);
    }

	public void applyThetaRotation(double theta) {
        x = x*Math.cos(theta) + y*Math.sin(theta);
        y = y*Math.cos(theta) - x*Math.sin(theta);
	}

	public void applyPhiRotation(double phi) {
        y = y*Math.cos(phi) + z*Math.sin(phi);
        z = z*Math.cos(phi) - y*Math.sin(phi);
    }

	public Vector getNormal(Vector b) {
		return new Vector(y*b.z - z*b.y, z*b.x - x*b.z, x*b.y - y*b.x);
	}

	public double getScalarProduct(Vector v) {
		return (x * v.x + y * v.y + z * v.z);
	}

	@Override
	public String toString() {
		return "Vector [x=" + x + ", y=" + y + ", z=" + z + "]";
	}
}
