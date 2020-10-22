#ifndef DEF_VECTOR3D
#define DEF_VECTOR3D

#include <cmath>
 
class Vector3D {
    public:

    Vector3D();
    Vector3D(float x, float y, float z);
	Vector3D(Point3D p1, Point3D p2);
	
	void normalize();
	Vector3D substract(Vector3D &vec) const;
	float Vector3D::projectOnAlpha(float alphaMax) const;
	float Vector3D::projectOnBeta(float alphaMax) const;

	~Vector3D();
 
    private:
 
    float x, y, z;
};

#endif