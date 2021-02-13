public class Main {
	public static void main(String[] args) {
		World world = new World(20.0, 4);
		Object3D obj = new Object3D();

		// obj.getObjectFromFile("Objects/teapot/teapot.obj");
		// obj.getObjectFromFile("Objects/WatchTower/wooden_watch_tower.obj");

		// CHUNK SIZE = 10/20
		// obj.getObjectFromFile("Objects/doom_combat_scene/doom_combat_scene.obj");
		
		// CHUNK SIZE = 20
		obj.getObjectFromFile("Objects/motorbike/bike.obj");
		// obj.getObjectFromFile("Objects/alien_wall/alien_wall.obj");
		// obj.getObjectFromFile("Objects/borderlands_cosplay/borderlands_cosplay.obj");

		System.out.println("ADDING OBJECT");
		world.addObjectToWorld(obj);

		new Projecter2D(world);
	}
}
