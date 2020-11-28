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

	TreeMap<Double, Chunk> chunksToDraw = new TreeMap<Double, Chunk>(Collections.reverseOrder());

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

		Timer t = new Timer(20, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
		});
		t.start();

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

	public void sortChunksAndDrawDepthBuffer(Graphics gDepthBuffer, Graphics gImage, TreeMap<Point, Object> chunks, int chunkLevel, double chunkSize) {
		gDepthBuffer.setColor(Color.BLACK);
		gDepthBuffer.fillRect(0, 0, (int)dim.getWidth(), (int)dim.getHeight());
		gDepthBuffer.setColor(Color.WHITE);

		gImage.setColor(Color.BLACK);
		gImage.fillRect(0, 0, (int)dim.getWidth(), (int)dim.getHeight());

		cosTheta = Math.cos(cameraTheta);
		sinTheta = Math.sin(cameraTheta);
		cosPhi = Math.cos(cameraPhi);
		sinPhi = Math.sin(cameraPhi);

		chunksToDraw.clear();
		drawDepthBufferAndSortRecursively(gDepthBuffer, gImage, chunks, chunkLevel, chunkSize);
		drawChunks(gImage);
	}

	public void drawDepthBufferAndSortRecursively(Graphics gDepthBuffer, Graphics gImage, TreeMap<Point, Object> chunks, int chunkLevel, double chunkSize) {
		TreeMap<Double, Chunk> sortedChunks = sortChunks(chunks, chunkSize);

		for (Map.Entry<Double, Chunk> entry : sortedChunks.entrySet()) {
			Chunk chunk = entry.getValue();
			if (chunkLevel == 1) {
				for (Map.Entry<Double, Triangle> triangle : sortTrianglesInChunk(chunk).entrySet()) {
					drawTriangle(gDepthBuffer, triangle.getValue());
				}
				chunksToDraw.put(new Vector(chunk.getCenter(chunkSize), cameraP).getNorm(), chunk);
			} else {
				drawDepthBufferAndSortRecursively(gDepthBuffer, gImage, chunk.getSmallerChunks(), chunkLevel - 1, chunkSize / 2);
			} 
		}
	}

	public TreeMap<Double, Chunk> sortChunks(TreeMap<Point, Object> chunks, double chunkSize) {
		TreeMap<Double, Chunk> depthChunks = new TreeMap<Double, Chunk>();
		for (Map.Entry<Point, Object> entry : chunks.entrySet()) {
			Chunk chunk = (Chunk)entry.getValue();
			if (isChunkVisible(chunk, chunkSize)) {
				depthChunks.put(new Vector(chunk.getCenter(chunkSize), cameraP).getNorm(), chunk);
			}
		}
		return depthChunks;
	}

	public TreeMap<Double, Triangle> sortTrianglesInChunk(Chunk chunk) {
		TreeMap<Double, Triangle> triangles = new TreeMap<Double, Triangle>();

		for (Map.Entry<Point, Object> entry : chunk.getSmallerChunks().entrySet()) {
			Triangle tri = (Triangle)entry.getValue();
			Vector cameraToTriangle = new Vector(tri.getCenterOfGravity(), cameraP);

			// object must be facing towards the camera
			if (tri.getNormal().getScalarProduct(cameraToTriangle) < 0) {
				triangles.put(cameraToTriangle.getNorm(), tri);
			}
		}
		return triangles;
	}

	/** checks every pixel in a box containing the 2D view of the chunk, returns true if there's at least one black pixel, true otherwise */
	public Boolean isChunkVisible(Chunk chunk, double chunkSize) {
		int xMin = (int)dim.getWidth() + 1;
		int xMax = -1;
		int yMin = (int)dim.getHeight() + 1;
		int yMax = -1;

		for (Point pt : chunk.getPoints(chunkSize)) {
			pt = pt.getPointNewBaseOptimized(cameraP, cosTheta, sinTheta, cosPhi, sinPhi);
			int x = pt.get2DXTransformation(centerX, focalDistance);
			int y = pt.get2DYTransformation(centerY, focalDistance);

			xMin = Math.min(xMin, x);
			xMax = Math.max(xMax, x);
			yMin = Math.min(yMin, y);
			yMax = Math.max(yMax, y);
		}

		if (xMax < 0 || xMin > dim.getWidth() || yMax < 0 || yMin > dim.getHeight()) {
			return false;
		}

		xMin = Math.max(0, xMin);
		xMax = Math.min((int)dim.getWidth(), xMax);
		yMin = Math.max(0, yMin);
		yMax = Math.min((int)dim.getHeight(), yMax);

		for (int i = xMin; i < xMax; i++) {
			for (int j = yMin; j < yMax; j++) {
				if (new Color(depthBuffer.getRGB(i, j)).equals(Color.BLACK)) {
					return true;
				}
			}
		}
		return false;
	}

	public void drawChunks(Graphics g) {
		for (Map.Entry<Double, Chunk> entry : chunksToDraw.entrySet()) {
			for (Map.Entry<Double, Triangle> triangle : sortTrianglesInChunk(entry.getValue()).entrySet()) {
				drawTriangleWithShade(g, triangle.getValue());
			}
		}

		if (displayingChunks) {
			g.setColor(Color.WHITE);
			for (Map.Entry<Double, Chunk> entry : chunksToDraw.entrySet()) {
				drawBoundaries(g, entry.getValue().getPoints(world.getChunkSize()));
			}
		}
	}

	public void drawTriangle(Graphics g, Triangle tri) {
		int[] xs = new int[3];
		int[] ys = new int[3];

		for (int i = 0; i < 3; i++) {
			Point ptNewBase = tri.getPoints()[i].getPointNewBaseOptimized(cameraP, cosTheta, sinTheta, cosPhi, sinPhi);
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
		startTime = System.currentTimeMillis();
		sortChunksAndDrawDepthBuffer(depthBuffer.getGraphics(), imageBuffer.getGraphics(), world.getChunks(), world.getChunkLevel(), world.getChunkSize() * Math.pow(world.getBiggerChunkSize(), world.getChunkLevel()-1));

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
		g.drawString("REFRESH TIME : " + (System.currentTimeMillis() - startTime) + " ms", 20, 60);
		g.drawString("DISPLAYING : " + chunksToDraw.size() + " chunks\n", 20, 80);
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
