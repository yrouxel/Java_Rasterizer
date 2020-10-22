#include "Triangle.h"
#include "Point3D.h"

using namespace std;

class Triangle {
    public:
 
    Triangle::Triangle(Point3D a, Point3D b, Point3D c) {
		points[0] = a;
		points[1] = b;
		points[2] = c;
	}
 
    private:

    Point3D points[3];
};