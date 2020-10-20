#include "PhysicObject.h"

using namespace std;

class PhysicObject {
    public:
 
    PhysicObject::PhysicObject(int x, int y, int z):
		x (x), y (y), z (z)	{}
 
    private:
 
    int x, y, z;
};