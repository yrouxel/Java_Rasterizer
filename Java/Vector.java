public class Vector {
	private double x, y, z;

	public Vector() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

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

    public void rotate(double theta, double phi) {
        double xBefore = x;

        //theta rotation
        x = xBefore*Math.cos(theta) + y*Math.sin(theta);
        double yBefore = y*Math.cos(theta) - xBefore*Math.sin(theta);

        //phi rotation
        y = yBefore*Math.cos(phi) + z*Math.sin(phi);
        z = z*Math.cos(phi) - yBefore*Math.sin(phi);
    }
	
	public double getNorm() {
        return Math.sqrt(x*x + y*y + z*z);
    }

	public Vector getCrossProduct(Vector b) {
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
