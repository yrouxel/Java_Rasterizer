public class Vector {
	private double x, y, z;

	public Vector() {}

	public Vector(double x, double y, double z) {
		setVector(x, y, z);
	}

	public Vector(Point p1,  Point p2) {
		setVector(p1, p2);
	}

	public void setVector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void setVector(Point p1,  Point p2) {
		x = p1.getX() - p2.getX();
		y = p1.getY() - p2.getY();
		z = p1.getZ() - p2.getZ();
	}

	public void normalize() {
		multiply(getNorm());
	}

	public void multiply(double d) {
		x /= d;
		y /= d;
		z /= d;
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

    public void rotate(double cosTheta, double sinTheta, double cosPhi, double sinPhi) {
        double xBefore = x;

        //theta rotation
        x = xBefore * cosTheta + y * sinTheta;
        double yBefore = y*cosTheta - xBefore*sinTheta;

        //phi rotation
        y = yBefore*cosPhi + z*sinPhi;
		z = z*cosPhi - yBefore*sinPhi;
    }
	
	public double getNorm() {
        return Math.sqrt(x*x + y*y + z*z);
    }

	public double getSquareNorm() {
		return x*x + y*y + z*z;
	}

	public Vector getCrossProduct(Vector b) {
		b.setVector(y*b.z - z*b.y, z*b.x - x*b.z, x*b.y - y*b.x);
		return b;
	}

	public double getScalarProduct(Vector v) {
		return (x * v.x + y * v.y + z * v.z);
	}

	@Override
	public String toString() {
		return "Vector [x=" + x + ", y=" + y + ", z=" + z + "]";
	}
}
