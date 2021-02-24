public class Point implements Comparable<Point> {
    private double x, y, z;

    public Point() {}
 
    public Point(double x, double y, double z) { 
		setPoint(x, y, z); 
    }
    
    public Point(Point clone) {
        setPoint(clone);
    }

    public Point(Point clone, Vector vec, double scalar) {
        setPoint(clone, vec, scalar);
    }

    public void setPoint(Point clone) {
        this.x = clone.x;
        this.y = clone.y;
        this.z = clone.z;
    }

    public void setPoint(double x, double y, double z) { 
		this.x = x;
		this.y = y;
		this.z = z; 
    }

    public void setPoint(Point clone, Vector vec, double scalar) {
        this.x = clone.x + vec.getX() * scalar;
        this.y = clone.y + vec.getY() * scalar;
        this.z = clone.z + vec.getZ() * scalar;
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

    public Point getAverage(Point b) {
        Point p = new Point(x + b.x, y + b.y, z + b.z);
        p.multiply(1.0/2.0);
        return p;
    }

    public double getNorm() {
        return Math.sqrt(x*x + y*y + z*z);
    }

    public Point getPointNewBase(Point camera, double theta, double phi) {
        Point pt = new Point(x, y, z);
        pt.substract(camera);
        pt.rotate(theta, phi);
        return pt;
    }

    //same function with less function calls
    public Point getPointNewBaseOptimized(Point camera, double cosTheta, double sinTheta, double cosPhi, double sinPhi) {
        double x2 = x - camera.getX();
        double y2 = y - camera.getY();
        double z2 = z - camera.getZ();

        double xBefore = x2;

        //theta rotation
        x2 = xBefore*cosTheta + y2*sinTheta;
        double yBefore = y2*cosTheta - xBefore*sinTheta;

        //phi rotation
        y2 = yBefore*cosPhi + z2*sinPhi;
        z2 = z2*cosPhi - yBefore*sinPhi;

        return new Point(x2, y2, z2);
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

    @Override
    public String toString() {
        return "(x=" + x + ", y=" + y + ", z=" + z + ")";
    }

    public Boolean equals(Point b) {
        return x == b.getX() && y == b.getY() && z == b.getZ();
    }

    @Override
    public int compareTo(Point pt) {
        if (pt.getX() > x) {
            return -1;
        } else if (pt.getX() < x) {
            return 1;
        } else {
            if (pt.getY() > y) {
                return -1;
            } else if (pt.getY() < y) {
                return 1;
            } else {
                if (pt.getZ() > z) {
                    return -1;
                } else if (pt.getZ() < z) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }
}