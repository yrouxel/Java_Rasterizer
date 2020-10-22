public class Point {
	private double x, y, z;
    private double x2d, y2d;
 
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

    public void setX(double a) {
        x += a;
    }

    public void setY(double a) {
        y += a;
    }

    public void setZ(double a) {
        z += a;
    }

    public double getX2D() {
        return x2d;
    }

    public double getY2D()  {
        return y2d;
    }

    public void get2dProjections(Vector cameraVec, Point cameraPoint, double alphaMax) {
        Vector pointVec = new Vector(this, cameraPoint);
        //System.out.println("pointVec " + pointVec);
        pointVec.normalize();
        //System.out.println("normalized pointVec " + pointVec);
        //System.out.println("cameraVec " + cameraVec + "\n");
        Vector difVec = pointVec.substract(cameraVec);
        x2d = difVec.projectOnAlpha(alphaMax);
        y2d = difVec.projectOnBeta(alphaMax);
    }

    public Boolean equals(Point b) {
        return x == b.getX() && y == b.getY() && z == b.getZ();
    }

    @Override
    public String toString() {
        return "Point [x=" + x + ", x2d=" + x2d + ", y=" + y + ", y2d=" + y2d + ", z=" + z + "]";
    }
}