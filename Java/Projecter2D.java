import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.awt.*;

public class Projecter2D extends JFrame {
	private Dimension dim;
	private double centerX;
	private double centerY;

	private BufferedImage monBuf; // buffer d’affichage
	private Point cameraP = new Point(0, 0, 10);
	private Point lightingP = new Point(0, 0, 10);

	private double cameraTheta = 0;
	private double cameraPhi = 0;

	private double cosTheta;
	private double sinTheta;
	private double cosPhi;
	private double sinPhi;

	private int[] xs = new int[3];
	private int[] ys = new int[3];

	private Point a;
	private Point b;
	private Point c;

	private double alphaMax = Math.PI / 2;
	private double focalDistance;

	private int space = 20;
	private Grid grid = new Grid(space);

	private World world;
	private Object3D obj;

	private Robot robot;
	private Boolean mouseLocked = false;

	private long startTime;

	TreeMap<Double, Triangle> depthTrianglesByChunk = new TreeMap<Double, Triangle>(Collections.reverseOrder());
	TreeMap<Double, Chunk> depthChunks = new TreeMap<Double, Chunk>(Collections.reverseOrder());


	public Projecter2D(World world, Object3D obj) {
		super("3D ENGINE");
		this.world = world;
		this.obj = obj;
		dim = Toolkit.getDefaultToolkit().getScreenSize();

		this.setExtendedState(JFrame.MAXIMIZED_BOTH); //full screen
		this.setUndecorated(true);//enleve la bande du haut

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
		mainPanel.setBackground(Color.green);
		this.setContentPane(mainPanel);

		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		// Create a new blank cursor.
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new java.awt.Point(0, 0), "blank cursor");
		// Set the blank cursor to the JFrame.
		mainPanel.setCursor(blankCursor);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		try {
			robot = new Robot();
		} catch (Exception e) {
			e.printStackTrace();
		}

		addKeyListener(new MoveKeyListener());
		addMouseMotionListener(new CameraMouseListener());

		setVisible(true);

		monBuf = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);

		Timer t = new Timer(20, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
		});
		t.start();

		startTime = System.currentTimeMillis();

		focalDistance = dim.getHeight() / (2 * Math.tan(alphaMax / 2.0));
		centerX = dim.getWidth()/2;
		centerY = dim.getHeight()/2;
	}

	public void drawLine(Graphics g, Point a, Point b) {
		a = a.getPointNewBaseOptimized(cameraP, cosTheta, sinTheta, cosPhi, sinPhi);		
		b = b.getPointNewBaseOptimized(cameraP, cosTheta, sinTheta, cosPhi, sinPhi);

		if (a.getY() > 0 && b.getY() > 0) {

			int x1 = a.get2DXTransformation(centerX, focalDistance);
			int x2 = b.get2DXTransformation(centerX, focalDistance);

			int y1 = a.get2DYTransformation(centerY, focalDistance);
			int y2 = b.get2DYTransformation(centerY, focalDistance);

			g.drawLine(x1, y1, x2, y2);
			// g.drawString((int)a.getX() + ", " + (int)a.getY() + ", " + (int)a.getZ(), x1-10, y1-10);
			// g.drawString((int)a.getX() + ", " + (int)a.getY() + ", " + (int)a.getZ(), x2-10, y2-10);
		}
	}

	public void drawTriangles(Graphics g) {
		// for (Map.Entry<Double, Triangle> entry : depthTriangles.entrySet()) {
		// 	Point[] points = entry.getValue().getPoints();
		// 	points[0].eraseScreenCoordinates();
		// 	points[1].eraseScreenCoordinates();
		// 	points[2].eraseScreenCoordinates();
		// }

		for (Map.Entry<Double, Triangle> entry : depthTrianglesByChunk.entrySet()) {
			// drawTriangleWithMemory(g, entry.getValue());
			drawTriangle(g, entry.getValue());
		}
	}

	public void drawChunks(Graphics g) {
		for (Map.Entry<Double, Chunk> entry : depthChunks.entrySet()) {
			depthTrianglesByChunk.clear();
			sortTrianglesInChunk(entry.getValue(), depthTrianglesByChunk);
			drawTriangles(g);
		}

		// g.setColor(Color.WHITE);
		// for (Map.Entry<Point, Chunk> entry : world.getChunks().entrySet()) {
		// 	drawBoundaries(g, entry.getKey(), world.getChunkSize());
		// }
	}


	public void drawTriangle(Graphics g, Triangle tri) {
		Vector normal = tri.getNormal();
		Vector lightToTriangle = new Vector(tri.getCenterOfGravity(), lightingP);
		normal.normalize();
		lightToTriangle.normalize();
		double shade = Math.max(-2.0 * normal.getScalarProduct(lightToTriangle), 0);

		a = tri.getPoints()[0].getPointNewBaseOptimized(cameraP, cosTheta, sinTheta, cosPhi, sinPhi);
		b = tri.getPoints()[1].getPointNewBaseOptimized(cameraP, cosTheta, sinTheta, cosPhi, sinPhi);
		c = tri.getPoints()[2].getPointNewBaseOptimized(cameraP, cosTheta, sinTheta, cosPhi, sinPhi);

		xs[0] = a.get2DXTransformation(centerX, focalDistance);
		xs[1] = b.get2DXTransformation(centerX, focalDistance);
		xs[2] = c.get2DXTransformation(centerX, focalDistance);

		ys[0] = a.get2DYTransformation(centerY, focalDistance);
		ys[1] = b.get2DYTransformation(centerY, focalDistance);
		ys[2] = c.get2DYTransformation(centerY, focalDistance);

		g.setColor(new Color(Math.min(255, (int)(shade*tri.getColor().getRed())), Math.min(255, (int)(shade*tri.getColor().getGreen())), Math.min(255, (int)(shade*tri.getColor().getBlue()))));
		g.fillPolygon(xs, ys, 3);
	} 

	/*public void drawTriangleWithMemory(Graphics g, Triangle tri) {
		Vector normal = tri.getNormal();
		Vector lightToTriangle = new Vector(tri.getCenterOfGravity(), lightingP);
		normal.normalize();
		lightToTriangle.normalize();
		double shade = Math.max(-2.0 * normal.getScalarProduct(lightToTriangle), 0);

		if (tri.getPoints()[0].getX2D() == -1 && tri.getPoints()[0].getY2D() == -1) {
			tri.getPoints()[0].computeScreenCoordinates(cameraP, cameraTheta, cameraPhi, centerX, centerY, focalDistance);
		}
		if (tri.getPoints()[1].getX2D() == -1 && tri.getPoints()[1].getY2D() == -1) {
			tri.getPoints()[1].computeScreenCoordinates(cameraP, cameraTheta, cameraPhi, centerX, centerY, focalDistance);
		}
		if (tri.getPoints()[2].getX2D() == -1 && tri.getPoints()[2].getY2D() == -1) {
			tri.getPoints()[2].computeScreenCoordinates(cameraP, cameraTheta, cameraPhi, centerX, centerY, focalDistance);
		}

		xs[0] = tri.getPoints()[0].getX2D();
		xs[1] = tri.getPoints()[1].getX2D();
		xs[2] = tri.getPoints()[2].getX2D();

		ys[0] = tri.getPoints()[0].getY2D();
		ys[1] = tri.getPoints()[1].getY2D();
		ys[2] = tri.getPoints()[2].getY2D();

		g.setColor(new Color(Math.min(255, (int)(shade*tri.getColor().getRed())), Math.min(255, (int)(shade*tri.getColor().getGreen())), Math.min(255, (int)(shade*tri.getColor().getBlue()))));
		g.fillPolygon(xs, ys, 3);
	}*/

	public void drawBoundaries(Graphics g, Point p, double chunkSize) {
		Point frontRight = new Point(p);
		Point frontTopLeft = new Point(p);
		Point frontTopRight = new Point(p);
		Point backLeft = new Point(p);
		Point backRight = new Point(p);
		Point backTopLeft = new Point(p);
		Point backTopRight = new Point(p);

		frontRight.add(new Point(chunkSize, 0, 0));
		frontTopLeft.add(new Point(0, 0, chunkSize));
		frontTopRight.add(new Point(chunkSize, 0, chunkSize));
		backLeft.add(new Point(0, chunkSize, 0));
		backRight.add(new Point(chunkSize, chunkSize, 0));
		backTopLeft.add(new Point(0, chunkSize, chunkSize));
		backTopRight.add(new Point(chunkSize, chunkSize, chunkSize));

		drawLine(g, p, frontRight);
		drawLine(g, p, frontTopLeft);
		drawLine(g, frontTopLeft, frontTopRight);
		drawLine(g, frontTopRight, frontRight);
		drawLine(g, backLeft, backRight);
		drawLine(g, backLeft, backTopLeft);
		drawLine(g, backTopLeft, backTopRight);
		drawLine(g, backTopRight, backRight);
		drawLine(g, p, backLeft);
		drawLine(g, frontTopLeft, backTopLeft);
		drawLine(g, frontTopRight, backTopRight);
		drawLine(g, frontRight, backRight);
	}

	public void drawGrid(Graphics g) {
		g.setColor(Color.WHITE);
		for (Point point : grid.getPoints()) {
			Point topPoint   = new Point(point.getX(), point.getY(), point.getZ() + space);
			Point backPoint  = new Point(point.getX(), point.getY() + space, point.getZ());
			Point rightPoint = new Point(point.getX() + space, point.getY(), point.getZ());

			drawLine(g, point, topPoint);
			drawLine(g, point, backPoint);
			drawLine(g, point, rightPoint);
		}
	}

	public void paint(Graphics g) {
		Prepaint(monBuf.getGraphics());
		g.drawImage(monBuf, 0, 0, null);
	}
	
	public void Prepaint(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, (int)dim.getWidth(), (int)dim.getHeight());

		sortChunks();
		drawChunks(g);

		displayComments(g);
		startTime = System.currentTimeMillis();
	}

	public void displayComments(Graphics g) {
		g.setColor(Color.WHITE);
		g.drawString("(" + (int)cameraP.getX() + ", " + (int)cameraP.getY() + ", " + (int)cameraP.getZ() + ")", 20, 20);

		Point movement = new Point(0, 100, 0);
		movement.rotate(-cameraTheta, -cameraPhi);
		g.drawString("(" + ((double)(int)movement.getX()/100) + ", " + ((double)(int)movement.getY()/100) + ", " + ((double)(int)movement.getZ()/100) + ")", 20, 40);

		g.drawString("REFRESH TIME : " + (System.currentTimeMillis() - startTime) + " ms", 20, 60);
		g.drawString("DISPLAYING : " + depthChunks.size() + " chunks\n", 20, 80);
	}

	public void sortChunks() {
		cosTheta = Math.cos(cameraTheta);
		sinTheta = Math.sin(cameraTheta);
		cosPhi = Math.cos(cameraPhi);
		sinPhi = Math.sin(cameraPhi);

		depthChunks.clear();

		for (Map.Entry<Point, Chunk> entry : world.getChunks().entrySet()) {
			Point center = new Point(entry.getKey());
			Point pt = new Point(1, 1, 1);
			pt.multiply(world.getChunkSize()/2);
			center.add(pt);
			Vector cameraToChunk = new Vector(center, cameraP);
			center = center.getPointNewBaseOptimized(cameraP, cosTheta, sinTheta, cosPhi, sinPhi);
			if (Math.abs(center.get2DYTransformation(0, focalDistance)) < dim.getHeight() && Math.abs(center.get2DXTransformation(0, focalDistance)) < dim.getWidth()) {
				depthChunks.put(cameraToChunk.getNorm(), entry.getValue());
			}
		}
	}

	public void sortTrianglesInChunk(Chunk chunk, TreeMap<Double, Triangle> depthTriangleInChunk) {
		for (Triangle tri : chunk.getTriangles()) {
			Point center = tri.getCenterOfGravity();
			Vector cameraToTriangle = new Vector(center, cameraP);
			center = center.getPointNewBaseOptimized(cameraP, cosTheta, sinTheta, cosPhi, sinPhi);
			// triangles must be facing towards the camera and in the fov
			//triangles facing us should be the last condition in a real world, but here i'm displaying only one object
			// second and third conditions are responsible for rare artifacts
			if (tri.getNormal().getScalarProduct(cameraToTriangle) < 0 && Math.abs(center.get2DYTransformation(0, focalDistance)) < dim.getHeight() && Math.abs(center.get2DXTransformation(0, focalDistance)) < dim.getWidth()) {
				depthTriangleInChunk.put(cameraToTriangle.getNorm(), tri);
			}
		}
	}

	public void sortTrianglesWorld() {
		cosTheta = Math.cos(cameraTheta);
		sinTheta = Math.sin(cameraTheta);
		cosPhi = Math.cos(cameraPhi);
		sinPhi = Math.sin(cameraPhi);

		depthTrianglesByChunk.clear();

		for (Map.Entry<Point, Chunk> entry : world.getChunks().entrySet()) {
			for (Triangle tri : entry.getValue().getTriangles()) {
				Point center = tri.getCenterOfGravity();
				Vector cameraToTriangle = new Vector(center, cameraP);
				center = center.getPointNewBaseOptimized(cameraP, cosTheta, sinTheta, cosPhi, sinPhi);
				// triangles must be facing towards the camera and in the fov
				//triangles facing us should be the last condition in a real world, but here i'm displaying only one object
				// second and third conditions are responsible for rare artifacts
				if (tri.getNormal().getScalarProduct(cameraToTriangle) < 0 && Math.abs(center.get2DYTransformation(0, focalDistance)) < dim.getHeight() && Math.abs(center.get2DXTransformation(0, focalDistance)) < dim.getWidth()) {
					depthTrianglesByChunk.put(cameraToTriangle.getNorm(), tri);
				}
			}
		}
	}

	public void sortTrianglesObject() {
		cosTheta = Math.cos(cameraTheta);
		sinTheta = Math.sin(cameraTheta);
		cosPhi = Math.cos(cameraPhi);
		sinPhi = Math.sin(cameraPhi);

		depthTrianglesByChunk.clear();

		for (Triangle tri : obj.getFaces()) {
			Point center = tri.getCenterOfGravity();
			Vector cameraToTriangle = new Vector(center, cameraP);
			center = center.getPointNewBaseOptimized(cameraP, cosTheta, sinTheta, cosPhi, sinPhi);
			// triangles must be facing towards the camera and in the fov
			//triangles facing us should be the last condition in a real world, but here i'm displaying only one object
			if (Math.abs(center.get2DYTransformation(0, focalDistance)) < dim.getHeight() && Math.abs(center.get2DXTransformation(0, focalDistance)) < dim.getWidth()) {
				depthTrianglesByChunk.put(cameraToTriangle.getNorm(), tri);
			}
		}
	}

	public class MoveKeyListener implements KeyListener {
		private double move = 2;

		public void keyPressed(KeyEvent e) {
			char key = e.getKeyChar();
			Point movement = new Point(0, 0, 0);

			if (key == 'z') {
				movement.addY(move);
			} else if (key == 's') {
				movement.addY(-move);
			} else if (key == 'q') {
				movement.addX(-move);
			} else if (key == 'd') {
				movement.addX(move);
			} else if (key == 'r') {
				movement.addZ(move);
			} else if (key == 'f') {
				movement.addZ(-move);
			} else if (key == 'w') {
				mouseLocked = !mouseLocked;
			} else if (key == 't') {
				move *= 2;
			} else if (key == 'g') {
				move /= 2;
			}

			movement.rotate(-cameraTheta, -cameraPhi);
			cameraP.add(movement);
		}
		public void keyReleased(KeyEvent e) {
		}
		public void keyTyped(KeyEvent e) {
		}
	}

	public class CameraMouseListener implements MouseMotionListener {
		private int mouseX = (int)(dim.getWidth() / 2.0);
		private int mouseY = (int)(dim.getHeight() / 2.0);

		public void mouseDragged(MouseEvent e) {
		}
		public void mouseMoved(MouseEvent e) {
			if (!mouseLocked) {
				cameraTheta -= (e.getXOnScreen() - mouseX) * 2.0 * Math.PI/dim.getWidth();
				cameraPhi   -= (e.getYOnScreen() - mouseY) * 2.0 * Math.PI/dim.getHeight();
				robot.mouseMove(mouseX, mouseY);
			}
		}
	}
}
