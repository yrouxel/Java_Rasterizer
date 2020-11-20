public class Point {
    private double x, y, z;
    // private int x2D, y2D;
 
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
    
    public int get2DXTransformation(double offset, double focalDistance) {
        return (int)(offset + x * focalDistance / y);
    }

    public int get2DYTransformation(double offset, double focalDistance) {
        return (int)(offset - z * focalDistance / y);
    }

    /*public void computeScreenCoordinates(Point camera, double theta, double phi, double offsetX, double offsetY, double focalDistance) {
        double x2 = x - camera.getX();
        double y2 = y - camera.getY();
        double z2 = z - camera.getZ();

        double xBefore = x2;

        //theta rotation
        x2 = xBefore*Math.cos(theta) + y2*Math.sin(theta);
        double yBefore = y2*Math.cos(theta) - xBefore*Math.sin(theta);

        //phi rotation
        y2 = yBefore*Math.cos(phi) + z2*Math.sin(phi);
        z2 = z2*Math.cos(phi) - yBefore*Math.sin(phi);

        x2D = (int)(offsetX + x2 * focalDistance / y2);
        y2D = (int)(offsetY - z2 * focalDistance / y2);
    }

    public void eraseScreenCoordinates() {
        x2D = -1;
        y2D = -1;

    }

    public int getX2D() {
        return x2D;
    }

    public int getY2D() {
        return y2D;
    }*/

    @Override
    public String toString() {
        return "Point [x=" + x + ", y=" + y + ", z=" + z + "]";
    }
}