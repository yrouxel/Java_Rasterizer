#ifndef DEF_PERSONNAGE
#define DEF_PERSONNAGE
 
class PhysicsObject {
    public:

    PhysicsObject();
    PhysicsObject(int x, int y, int z);

	~PhysicsObject();
 
    private:
 
    int x, y, z;
};

#endif