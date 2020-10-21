#ifndef DEF_POINT3D
#define DEF_POINT3D
 
class Point3D {
    public:

    Point3D();
    Point3D(float x, float y, float z);

	~Point3D();

    float getX() const;
    float getY() const;
    float getZ() const;
 
    private:
 
    float x, y, z;
};

#endif