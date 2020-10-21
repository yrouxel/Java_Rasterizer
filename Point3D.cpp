#include "Point3D.h"

using namespace std;

class Point3D {
    public:
 
    Point3D::Point3D(float x, float y, float z):
		  x (x), y (y), z (z)	{}

    float Point3D::getX() const {
        return x;
    }

    float Point3D::getY() const {
        return y;
    }

    float Point3D::getZ() const {
        return z;
    }
 
    private:
 
    float x, y, z;
};