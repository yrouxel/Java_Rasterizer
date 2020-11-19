public class Point {
	private double x, y, z;
 
    public Point(double x, double y, double z) { 
		this.x = x;
		this.y = y;
		this.z = z; 
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

    public void addX(double a) {
        x += a;
    }

    public void addY(double a) {
        y += a;
    }

    public void addZ(double a) {
        z += a;
    }

    public Boolean equals(Point b) {
        return x == b.getX() && y == b.getY() && z == b.getZ();
    }

    public Point getPointNewBase(Point camera, double theta, double phi) {
        Point pt = new Point(x, y, z);
        pt.substract(camera);
        pt.rotate(theta, phi);
        return pt;
    }

    public void substract(Point pt) {
        x -= pt.x;
        y -= pt.y;
        z -= pt.z;
    }

    public void add(Point p) {
		x += p.x;
		y += p.y;
		z += p.z;
    }

    public void multiply(double a) {
        x *= a;
        y *= a;
        z *= a;
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
    
    public int get2DXTransformation(double offset, double focalDistance) {
        return (int)(offset + x * focalDistance / y);
    }

    public int get2DYTransformation(double offset, double focalDistance) {
        return (int)(offset - z * focalDistance / y);
    }

    @Override
    public String toString() {
        return "Point [x=" + x + ", y=" + y + ", z=" + z + "]";
    }
}