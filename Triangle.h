#ifndef DEF_TRIANGLE
#define DEF_TRIANGLE

#include "Point3D.h"

class Triangle {
    public:
 
	Triangle();
    Triangle(Point3D a, Point3D b, Point3D c);

	~Triangle();
 
    private:

    Point3D points[3];
};

#endif