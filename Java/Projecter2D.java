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

	private BufferedImage imageBuffer;// buffer d’affichage
	private BufferedImage depthBuffer; // buffer d’affichage
	
	private boolean drawingImage = true;
	private boolean displayingDebug = false;
	private boolean displayingChunks = false;

	//use for doom object
	// private Point cameraP = new Point(-63, 202, 10);
	private Point cameraP = new Point(0, 0, 10);
	private Point lightingP = new Point(0, 0, 10);

	private double cameraTheta = 0;
	private double cameraPhi = 0;

	private double cosTheta;
	private double sinTheta;
	private double cosPhi;
	private double sinPhi;

	private double alphaMax = Math.PI / 2;
	private double focalDistance;

	private World world;

	private Robot robot;
	private Boolean mouseLocked = false;

	private long startTime;
	private long sortTime;
	private long drawDepthBufferTime;
	private long drawChunksTime;


	TreeMap<Double, Triangle> depthTrianglesByChunk = new TreeMap<Double, Triangle>(Collections.reverseOrder());
	TreeMap<Double, Chunk> depthBufferChunks = new TreeMap<Double, Chunk>(Collections.reverseOrder());
	TreeMap<Double, Chunk> depthChunks = new TreeMap<Double, Chunk>(Collections.reverseOrder());


	public Projecter2D(World world) {
		super("3D ENGINE");
		this.world = world;
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

		imageBuffer = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);

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

		imageBuffer = new BufferedImage((int)dim.getWidth(), (int)dim.getHeight(), BufferedImage.TYPE_INT_RGB);
		depthBuffer = new BufferedImage((int)dim.getWidth(), (int)dim.getHeight(), BufferedImage.TYPE_INT_RGB);
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

	public void drawWithoutDepthBuffer(Graphics g) {
		for (Map.Entry<Double, Chunk> entry : depthChunks.entrySet()) {
			sortTrianglesInChunk(entry.getValue());
			drawTriangles(g);
		}
	}

	public void drawDepthBuffer(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, (int)dim.getWidth(), (int)dim.getHeight());

		depthBufferChunks.clear();

		for (Map.Entry<Double, Chunk> entry : depthChunks.descendingMap().entrySet()) {
			if (isChunkHidden(entry.getValue(), world.getChunkSize())) {
				depthBufferChunks.put(entry.getKey(), entry.getValue());
				drawChunkInDepthBuffer(g, entry.getValue());
			}
		}
	}

	public Boolean arePointsVisible(Point[] points) {
		for (Point pt : points) {
			Point ptNewBase = pt.getPointNewBaseOptimized(cameraP, cosTheta, sinTheta, cosPhi, sinPhi);
			if (Math.abs(ptNewBase.get2DYTransformation(0, focalDistance)) < centerY && Math.abs(ptNewBase.get2DXTransformation(0, focalDistance)) < centerX) {
				return true;
			}
		}
		return false;
	}

	public Boolean isChunkHidden(Chunk chunk, double chunkSize) {
		int isVisible = 0;
		for (Point pt : chunk.getPoints(chunkSize)) {
			isVisible += isColorBlack(pt);
		}
		if (isVisible == -8 || isVisible == 8) {
			return false;
		} else {
			return true;
		}
	}

	public int isColorBlack(Point pt) {
		pt = pt.getPointNewBaseOptimized(cameraP, cosTheta, sinTheta, cosPhi, sinPhi);
		int x = pt.get2DXTransformation(centerX, focalDistance);
		int y = pt.get2DYTransformation(centerY, focalDistance);
		if (x >= 0 && x < dim.getWidth() && y >= 0 && y < dim.getHeight()) {
			if (new Color(depthBuffer.getRGB(x, y)).equals(Color.BLACK)) {
				return 0;
			} else {
				return 1;
			}
		} else {
			return -1;
		}
	}

	public void drawChunkInDepthBuffer(Graphics g, Chunk chunk) {
		g.setColor(Color.WHITE);

		for (Map.Entry<Point, Object> entry : chunk.getSmallerChunks().entrySet()) {
			Triangle tri = (Triangle)entry.getValue();
			Point center = tri.getCenterOfGravity();
			if (tri.getNormal().getScalarProduct(new Vector(center, cameraP)) < 0) {
				drawTriangle(g, tri);
			}
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
			drawTriangleWithShade(g, entry.getValue());
		}
	}

	public void drawChunks(Graphics g) {
		for (Map.Entry<Double, Chunk> entry : depthBufferChunks.entrySet()) {
			sortTrianglesInChunk(entry.getValue());
			drawTriangles(g);
		}

		if (displayingChunks) {
			g.setColor(Color.WHITE);
			for (Map.Entry<Double, Chunk> entry : depthBufferChunks.entrySet()) {
				drawBoundaries(g, entry.getValue().getPoints(world.getChunkSize()));
			}
		}
	}

	public void drawTriangle(Graphics g, Triangle tri) {
		int[] xs = new int[3];
		int[] ys = new int[3];

		Point ptNewBase;
		for (int i = 0; i < 3; i++) {
			ptNewBase = tri.getPoints()[i].getPointNewBaseOptimized(cameraP, cosTheta, sinTheta, cosPhi, sinPhi);
			xs[i] = ptNewBase.get2DXTransformation(centerX, focalDistance);
			ys[i] = ptNewBase.get2DYTransformation(centerY, focalDistance);
		}
		g.fillPolygon(xs, ys, 3);
	}

	public void drawTriangleWithShade(Graphics g, Triangle tri) {
		Vector normal = tri.getNormal();
		Vector lightToTriangle = new Vector(tri.getCenterOfGravity(), lightingP);
		normal.normalize();
		lightToTriangle.normalize();

		double shade = Math.max(-2.0 * normal.getScalarProduct(lightToTriangle), 0);
		g.setColor(new Color(Math.min(255, (int)(shade*tri.getColor().getRed())), Math.min(255, (int)(shade*tri.getColor().getGreen())), Math.min(255, (int)(shade*tri.getColor().getBlue()))));

		drawTriangle(g, tri);
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

	public void drawBoundaries(Graphics g, Point[] points) {
		drawLine(g, points[0], points[1]);
		drawLine(g, points[2], points[3]);
		drawLine(g, points[4], points[5]);
		drawLine(g, points[6], points[7]);

		drawLine(g, points[0], points[2]);
		drawLine(g, points[1], points[3]);
		drawLine(g, points[4], points[6]);
		drawLine(g, points[5], points[7]);

		drawLine(g, points[0], points[4]);
		drawLine(g, points[1], points[5]);
		drawLine(g, points[2], points[6]);
		drawLine(g, points[3], points[7]);
	}

	public void paint(Graphics g) {
		Prepaint(imageBuffer.getGraphics());
		if (drawingImage) {
			g.drawImage(imageBuffer, 0, 0, null);
		} else {
			g.drawImage(depthBuffer, 0, 0, null);
		}
	}
	
	public void Prepaint(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, (int)dim.getWidth(), (int)dim.getHeight());

		cosTheta = Math.cos(cameraTheta);
		sinTheta = Math.sin(cameraTheta);
		cosPhi = Math.cos(cameraPhi);
		sinPhi = Math.sin(cameraPhi);

		startTime = System.currentTimeMillis();

		depthChunks.clear();
		sortChunksRecursively(world.getChunks(), world.getChunkLevel());

		sortTime = System.currentTimeMillis() - startTime;
		// drawWithoutDepthBuffer(g);
		drawDepthBuffer(depthBuffer.getGraphics());
		drawDepthBufferTime = System.currentTimeMillis() - startTime - sortTime;
		drawChunks(g);
		drawChunksTime = System.currentTimeMillis() - startTime - sortTime - drawDepthBufferTime;

		if (displayingDebug) {
			if (drawingImage) {
				displayDebug(g);
			} else {
				displayDebug(depthBuffer.getGraphics());
			}
		}
	}

	public void displayDebug(Graphics g) {
		g.setColor(Color.WHITE);
		g.drawString("(" + (int)cameraP.getX() + ", " + (int)cameraP.getY() + ", " + (int)cameraP.getZ() + ")", 20, 20);

		Point movement = new Point(0, 100, 0);
		movement.rotate(-cameraTheta, -cameraPhi);
		g.drawString("(" + ((double)(int)movement.getX()/100) + ", " + ((double)(int)movement.getY()/100) + ", " + ((double)(int)movement.getZ()/100) + ")", 20, 40);

		g.drawString("SORT TIME : " + sortTime + " ms", 20, 60);
		g.drawString("DRAW DEPTH BUFFER TIME : " + drawDepthBufferTime + " ms", 20, 80);
		g.drawString("DRAW CHUNKS TIME : " + drawChunksTime + " ms", 20, 100);
		g.drawString("REFRESH TIME : " + (System.currentTimeMillis() - startTime) + " ms", 20, 120);

		g.drawString("TOTAL CHUNKS : " + world.getChunks().size() + " chunks\n", 20, 160);
		g.drawString("CHUNK SORTING : " + depthChunks.size() + " chunks\n", 20, 180);
		g.drawString("DISPLAYING : " + depthBufferChunks.size() + " chunks\n", 20, 200);
	}

	public void sortChunksRecursively(TreeMap<Point, Object> bigChunks, int chunkLevel) {
		for (Map.Entry<Point, Object> entry : bigChunks.entrySet()) {
			Chunk chunk = (Chunk)entry.getValue();		

			if (arePointsVisible(chunk.getPoints(world.getChunkSize() * Math.pow(world.getBiggerChunkSize(), chunkLevel-1)))) {
				if (chunkLevel == 1) {
					depthChunks.put(new Vector(chunk.getCenter(world.getChunkSize() * Math.pow(world.getBiggerChunkSize(), chunkLevel-1)), cameraP).getNorm(), chunk);
				} else {
					sortChunksRecursively(chunk.getSmallerChunks(), chunkLevel - 1);
				}
			}
		}
	}

	public void sortTrianglesInChunk(Chunk chunk) {
		depthTrianglesByChunk.clear();

		for (Map.Entry<Point, Object> entry : chunk.getSmallerChunks().entrySet()) {
			Triangle tri = (Triangle)entry.getValue();

			Vector cameraToTriangle = new Vector(tri.getCenterOfGravity(), cameraP);

			// object must be facing towards the camera and have at least one point in the fov
			if (tri.getNormal().getScalarProduct(cameraToTriangle) < 0 && arePointsVisible(tri.getPoints())) {
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
			} else if (key == 't') {
				move *= 2;
			} else if (key == 'g') {
				move /= 2;
			} else if (key == 'u') {
				mouseLocked = !mouseLocked;
			} else if (key == 'i') {
				drawingImage = !drawingImage;
			} else if (key == 'o') {
				displayingChunks = !displayingChunks;
			} else if (key == 'p') {
				displayingDebug = !displayingDebug;
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
