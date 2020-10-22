#include "Point3D.h"
#include "Vector3D.h"

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

    float Point3D::getX2D() const {
        return x2d;
    }

    float Point3D::getY2D() const {
        return y2d;
    }

    void Point3D::get2dProjections(Vector3D cameraVec, Point3D cameraPoint, float alphaMax) {
        Vector3D pointVec(*this, cameraPoint);
        Vector3D difVec = pointVec.substract(cameraVec);
        x2d = difVec.projectOnAlpha(alphaMax);
        y2d = difVec.projectOnBeta(alphaMax);
    }
 
    private:
 
    float x, y, z;
    float x2d, y2d;
};