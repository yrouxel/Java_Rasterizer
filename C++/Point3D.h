#ifndef DEF_POINT3D
#define DEF_POINT3D

#include "Vector3D.h"
 
class Point3D {
    public:

    Point3D();
    Point3D(float x, float y, float z);

	~Point3D();

    float getX() const;
    float getY() const;
    float getZ() const;
    float getX2D() const;
    float getY2D() const;
    void get2dProjections(Vector3D cameraVec, Point3D cameraPoint, float alphaMax);

    private:
 
    float x, y, z;
    float x2d, y2d;
};

#endif