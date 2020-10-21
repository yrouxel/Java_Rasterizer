#include <cmath>

#include "Vector3D.h"
#include "Point3D.h"

using namespace std;

class Vector3D {
    public:
 
    Vector3D::Vector3D(float x, float y, float z):
		  x (x), y (y), z (z) {}

	Vector3D::Vector3D(Point3D p1, Point3D p2):
		x (p1.getX() - p2.getX()), y (p1.getY() - p1.getY()), z (p1.getZ() - p1.getZ()) {}

	void Vector3D::normalize() {
		float norm (sqrt(x*x + y*y + z*z));
		x = x/norm;
		y = y/norm;
		z = z/norm;
	}

	Vector3D Vector3D::substract(Vector3D &vec) const {
		return Vector3D(x - vec.x, y - vec.y, z - vec.z);
	}

	float Vector3D::projectOnAlpha(float alphaMax) const {
		return - sin(M_PI * atan(y / x) / alphaMax);
	}

	float Vector3D::projectOnBeta(float alphaMax) const {
		return sin(M_PI * asin(z) / alphaMax);
	}
 
    private:
 
    float x, y, z;
};