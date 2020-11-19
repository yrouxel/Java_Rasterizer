import java.util.ArrayList;

public class Main {
	public static void main(String[] args) {
		Point a = new Point(10, 10, 10);
		Point b = new Point(10, 10, 20);
		Point c = new Point(20, 10, 10);
		Point d = new Point(20, 10, 20);

		Point e = new Point(10, 20, 10);
		Point f = new Point(10, 20, 20);
		Point g = new Point(20, 20, 10);
		Point h = new Point(20, 20, 20);

		Triangle t1 = new Triangle(a, c, b);
		Triangle t2 = new Triangle(b, c, d);

		Triangle t3 = new Triangle(e, f, g);
		Triangle t4 = new Triangle(g, f, h);

		Triangle t5 = new Triangle(c, a, e);
		Triangle t6 = new Triangle(e, g, c);

		Triangle t7 = new Triangle(b, d, f);
		Triangle t8 = new Triangle(f, d, h);

		Triangle t9 = new Triangle(a, b, e);
		Triangle t10 = new Triangle(e, b, f);

		Triangle t11 = new Triangle(d, c, g);
		Triangle t12 = new Triangle(d, g, h);

		ArrayList<Triangle> list = new ArrayList<Triangle>();
		list.add(t1);
		list.add(t2);
		list.add(t3);
		list.add(t4);
		list.add(t5);
		list.add(t6);
		list.add(t7);
		list.add(t8);
		list.add(t9);
		list.add(t10);
		list.add(t11);
		list.add(t12);

		// Object3D obj = new Object3D(list);

		Object3D obj = new Object3D();
		obj.getObjectFromFile("Objects/WatchTower/wooden_watch_tower.obj");

		new Projecter2D(obj);

		// Point test = new Point(0, 1, 0);
		// System.out.println(test);
		// test.applyThetaRotation(Math.PI/2);
		// System.out.println(test);
	}
}
